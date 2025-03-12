package project.mobile.model

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class AuthRepository(private val userPreferences: UserPreferences) {
    private val auth: FirebaseAuth = FirebaseAuth.getInstance()
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()

    suspend fun signUp(email: String, password: String, username: String): Result<Unit> {
        return try {
            Log.d("AuthRepository", "Starting sign up process")

            val authResult = auth.createUserWithEmailAndPassword(email, password).await()
            val userId = authResult.user?.uid ?: throw Exception("Failed to create user")

            Log.d("AuthRepository", "User created in Auth with ID: $userId")

            val user = User(
                id = userId,
                email = email,
                username = username,
                name = username, // Using username as initial name
                photoUrl = null
            )

            saveUserToFirestore(user)

            val token = authResult.user?.getIdToken(false)?.await()?.token
            token?.let {
                userPreferences.setSessionToken(it)
                userPreferences.setLoggedIn(true)
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("AuthRepository", "Sign up failed", e)
            Result.failure(Exception("Registration failed: ${e.message}"))
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
                "photoUrl" to user.photoUrl
            )
            firestore.collection("users").document(user.id).set(userMap).await()
            Log.d("AuthRepository", "User saved to Firestore successfully")
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error saving user to Firestore", e)
            throw e
        }
    }

    suspend fun signIn(email: String, password: String): Result<Unit> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            result.user?.let { firebaseUser ->
                val token = firebaseUser.getIdToken(false).await().token
                userPreferences.setSessionToken(token)
                userPreferences.setLoggedIn(true)
            }
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun signOut() {
        auth.signOut()
        userPreferences.clearSession()
    }

    suspend fun getCurrentUser(): User? {
        return try {
            val firebaseUser = auth.currentUser ?: return null
            Log.d("AuthRepository", "Fetching user data for ID: ${firebaseUser.uid}")

            val userDoc = firestore.collection("users")
                .document(firebaseUser.uid)
                .get()
                .await()

            if (userDoc.exists()) {
                val userData = userDoc.data
                if (userData != null) {
                    User(
                        id = userData["id"] as? String ?: firebaseUser.uid,
                        email = userData["email"] as? String ?: firebaseUser.email ?: "",
                        username = userData["username"] as? String ?: "",
                        name = userData["name"] as? String ?: "",
                        photoUrl = userData["photoUrl"] as? String
                    ).also {
                        Log.d("AuthRepository", "User data retrieved: $it")
                    }
                } else {
                    Log.e("AuthRepository", "User document exists but data is null")
                    null
                }
            } else {
                Log.e("AuthRepository", "User document does not exist in Firestore")
                // Create a basic user object if Firestore document doesn't exist
                User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    username = firebaseUser.displayName ?: "",
                    name = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString()
                )
            }
        } catch (e: Exception) {
            Log.e("AuthRepository", "Error getting current user", e)
            null
        }
    }

    suspend fun refreshSession(): Boolean {
        val currentUser = auth.currentUser
        return if (currentUser != null) {
            try {
                val token = currentUser.getIdToken(false).await().token
                userPreferences.setSessionToken(token)
                true
            } catch (e: Exception) {
                false
            }
        } else {
            false
        }
    }
}

