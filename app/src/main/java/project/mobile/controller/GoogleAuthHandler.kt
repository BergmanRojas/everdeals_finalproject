package project.mobile.controller

import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await
import project.mobile.R

class GoogleAuthHandler(private val context: Context) {
    private val auth = FirebaseAuth.getInstance()
    private val googleSignInClient: GoogleSignInClient

    init {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(context.getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(context, gso)
    }

    // Obtiene el Intent para iniciar el flujo de autenticación con Google
    fun getSignInIntent(): Intent {
        return googleSignInClient.signInIntent
    }

    // Maneja el resultado del intento de inicio de sesión con Google
    suspend fun handleSignInResult(data: Intent?): Boolean {
        return try {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data)
            val account = task.getResult(ApiException::class.java)
            account?.let { firebaseAuthWithGoogle(it) } ?: false
        } catch (e: ApiException) {
            Log.e("GoogleAuthHandler", "Google Sign-In failed: ${e.statusCode}", e)
            false
        }
    }

    // Autentica con Firebase usando las credenciales de Google
    private suspend fun firebaseAuthWithGoogle(account: GoogleSignInAccount): Boolean {
        return try {
            val credential = GoogleAuthProvider.getCredential(account.idToken, null)
            val authResult = auth.signInWithCredential(credential).await()
            authResult.user != null
        } catch (e: Exception) {
            Log.e("GoogleAuthHandler", "Firebase authentication failed", e)
            false
        }
    }

    // Cierra la sesión de Google y Firebase
    fun signOut() {
        auth.signOut()
        googleSignInClient.signOut()
    }
}