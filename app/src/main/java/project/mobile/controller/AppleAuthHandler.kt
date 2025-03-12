package project.mobile.controller

import android.content.Context
import android.content.Intent
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.OAuthProvider
import kotlinx.coroutines.tasks.await

class AppleAuthHandler(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()

    suspend fun signInWithApple(): Boolean {
        return try {
            // Configurar el proveedor de Apple para OAuth
            val provider = OAuthProvider.newBuilder("apple.com")
                .setScopes(listOf("email", "name"))
                .build()

            // Iniciar el flujo de autenticación con Apple
            val result = auth.startActivityForSignInWithProvider(context as android.app.Activity, provider).await()

            // Verificar si el usuario se autenticó correctamente
            result.user != null
        } catch (e: Exception) {
            // Log del error para depuración
            android.util.Log.e("AppleAuthHandler", "Apple Sign-In failed: ${e.message}", e)
            false
        }
    }

    fun signOut() {
        auth.signOut()
    }
}