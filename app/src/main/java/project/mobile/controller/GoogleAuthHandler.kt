package project.mobile.controller

import android.content.Context
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.Identity
import com.google.android.gms.auth.api.identity.SignInClient

class GoogleAuthHandler(private val context: Context) {
    private val signInClient: SignInClient = Identity.getSignInClient(context)

    fun startGoogleSignIn(launcher: ActivityResultLauncher<IntentSenderRequest>) {
        val signInRequest = BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    .setServerClientId("YOUR_WEB_CLIENT_ID_FROM_FIREBASE") // Reemplaza con tu Client ID de Firebase
                    .setFilterByAuthorizedAccounts(false) // Permite elegir cualquier cuenta
                    .build()
            )
            .build()

        signInClient.beginSignIn(signInRequest)
            .addOnSuccessListener { result ->
                val intentSenderRequest = IntentSenderRequest.Builder(result.pendingIntent.intentSender).build()
                launcher.launch(intentSenderRequest)
            }
            .addOnFailureListener { e ->
                e.printStackTrace()
            }
    }

    fun signOut() {
        signInClient.signOut()
    }
}