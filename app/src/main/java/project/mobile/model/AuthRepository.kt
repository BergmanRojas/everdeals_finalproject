package project.mobile.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.FirebaseFirestoreException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.tasks.await

class AuthRepository(private val userPreferences: UserPreferences) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    private val _authState = MutableStateFlow<AuthState>(AuthState.Idle)
    val authState: StateFlow<AuthState> = _authState

    suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        _authState.value = AuthState.Loading
        return try {
            Log.d("AuthRepository", "Starting sign up process")

            val usernameSnapshot = firestore.collection("users")
                .whereEqualTo("username", username)
                .get()
                .await()
            if (usernameSnapshot.documents.isNotEmpty()) {
                _authState.value = AuthState.Error("This username is already taken")
                return Result.failure(Exception("Username already taken"))
            }

            val emailCheck = auth.fetchSignInMethodsForEmail(email).await()
            if (emailCheck.signInMethods?.isNotEmpty() == true) {
                _authState.value = AuthState.Error("This email is already registered")
                return Result.failure(Exception("Email already registered"))
            }

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

            Log.d("AuthRepository", "User created in Auth with ID: $userId")

            val user = User(
                id = userId,
                email = email,
                username = username,
                name = username,
                photoUrl = null
            )

            saveUserToFirestore(user)

            val token = authResult.user?.getIdToken(false)?.await()?.token
            token?.let {
                userPreferences.setSessionToken(it)
                userPreferences.setLoggedIn(true)
            }

            _authState.value = AuthState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed: ${e.message}", e)
            _authState.value = AuthState.Error("Registration failed: ${e.message}")
            Result.failure(Exception("Registration failed: ${e.message}", e))
        }
    }

    private suspend fun saveUserToFirestore(user: User) {
        try {
            Log.d("AuthRepository", "Saving user to Firestore: ${user.id}")
            val userMap = mapOf(
                "id" to user.id,
                "email" to user.email,
                "username" to user.username,
                "name" to user.name,
                "photoUrl" to user.photoUrl,
                "followers" to user.followers,
                "following" to user.following
            )
            firestore.collection("users").document(user.id).set(userMap).await()
            Log.d("AuthRepository", "User saved to Firestore successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error saving user to Firestore: ${e.message}", e)
            throw e
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            _authState.value = AuthState.Loading
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val token = firebaseUser.getIdToken(false).await().token
                userPreferences.setSessionToken(token)
                userPreferences.setLoggedIn(true)
            }
            _authState.value = AuthState.Success
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign in failed: ${e.message}", e)
            _authState.value = AuthState.Error(e.message ?: "Sign in failed")
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        userPreferences.clearSession()
        _authState.value = AuthState.Idle
        Log.d("AuthRepository", "User signed out")
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser
            if (firebaseUser == null) {
                Log.w("AuthRepository", "No authenticated user found")
                return null
            }

            Log.d("AuthRepository", "Fetching user data for ID: ${firebaseUser.uid}")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                val userData = userDoc.data
                if (userData != null) {
                    val user = User(
                        id = userData["id"] as? String ?: firebaseUser.uid,
                        email = userData["email"] as? String ?: firebaseUser.email ?: "",
                        username = userData["username"] as? String ?: "",
                        name = userData["name"] as? String ?: "",
                        photoUrl = userData["photoUrl"] as? String,
                        followers = userData["followers"] as? List<String> ?: emptyList(),
                        following = userData["following"] as? List<String> ?: emptyList()
                    )
                    Log.d("AuthRepository", "User data retrieved from Firestore: $user")
                    user
                } else {
                    Log.e("AuthRepository", "User document exists but data is null")
                    createFallbackUser(firebaseUser)
                }
            } else {
                Log.w("AuthRepository", "User document does not exist in Firestore, creating fallback user")
                createFallbackUser(firebaseUser)
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting current user: ${e.message}", e)
            if (e is FirebaseFirestoreException && e.code == FirebaseFirestoreException.Code.PERMISSION_DENIED) {
                val firebaseUser = auth.currentUser
                if (firebaseUser != null) {
                    Log.w("AuthRepository", "Permission denied, using fallback user")
                    return createFallbackUser(firebaseUser)
                }
            }
            null
        }
    }

    private fun createFallbackUser(firebaseUser: com.google.firebase.auth.FirebaseUser): User {
        val fallbackUser = User(
            id = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            username = firebaseUser.displayName ?: "User_${firebaseUser.uid.take(8)}",
            name = firebaseUser.displayName ?: "",
            photoUrl = firebaseUser.photoUrl?.toString()
        )
        Log.w("AuthRepository", "Created fallback user: $fallbackUser")
        return fallbackUser
    }

    suspend fun refreshSession(): Boolean {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            try {
                val token = currentUser.getIdToken(true).await().token // Forzar renovación del token
                userPreferences.setSessionToken(token)
                Log.d("AuthRepository", "Session refreshed successfully")
                true
            } catch (e: Exception) {
                Log.e("AuthRepository", "Error refreshing session: ${e.message}", e)
                false
            }
        } else {
            Log.w("AuthRepository", "No current user to refresh session")
            false
        }
    }
}