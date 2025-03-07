package project.mobile.controller

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UserRepository {
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    // Registra un nuevo usuario con email, contraseña y nombre de usuario
    suspend fun signUp(email: String, password: String, username: String): Boolean {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            result.user?.let { user ->
                val profileUpdates = UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
                user.updateProfile(profileUpdates).await()

                val userData = hashMapOf(
                    "username" to username,
                    "email" to email
                )
                db.collection("users").document(user.uid).set(userData).await()
            }
            true
        } catch (e: Exception) {
            false
        }
    }

    // Inicia sesión con email y contraseña
    suspend fun signIn(email: String, password: String): Boolean {
        return try {
            auth.signInWithEmailAndPassword(email, password).await()
            true
        } catch (e: Exception) {
            false
        }
    }

    // Cierra la sesión del usuario
    suspend fun signOut() {
        auth.signOut()
    }
}