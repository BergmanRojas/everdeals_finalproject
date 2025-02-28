package project.mobile.view

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.delay
import androidx.compose.runtime.*
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import project.mobile.R

// Color de fondo
val Blue001875 = Color(0xFF001875)

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Verificar inicialización de Firebase
        try {
            FirebaseApp.initializeApp(this)
            Log.d("FirebaseInit", "✅ Firebase inicializado correctamente.")

            // Probar conexión con Firestore
            val db = FirebaseFirestore.getInstance()
            db.collection("test_connection").document("test")
                .set(hashMapOf("status" to "connected"))
                .addOnSuccessListener {
                    Log.d("FirebaseTest", "✅ Conexión a Firestore exitosa en SplashScreen.")
                }
                .addOnFailureListener { e ->
                    Log.e("FirebaseTest", "❌ Error de conexión a Firestore en SplashScreen", e)
                }

            // Verificar autenticación
            val auth = FirebaseAuth.getInstance()
            val currentUser = auth.currentUser
            if (currentUser != null) {
                Log.d("FirebaseTest", "✅ Usuario autenticado: ${currentUser.email}")
            } else {
                Log.d("FirebaseTest", "⚠️ No hay usuario autenticado en SplashScreen.")
            }

        } catch (e: Exception) {
            Log.e("FirebaseInit", "❌ Error al inicializar Firebase", e)
        }

        // Pantalla de carga con logo de fondo
        setContent {
            SplashScreenContent {
                val intent = Intent(this@SplashScreen, Login::class.java)
                startActivity(intent)
                finish()
            }
        }
    }
}

@Composable
fun SplashScreenContent(onFinish: () -> Unit) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Blue001875) // Fondo azul
    ) {
        Image(
            painter = painterResource(id = R.drawable.loginlogo),
            contentDescription = "Logo EverDeals",
            modifier = Modifier.fillMaxSize(), // La imagen cubre toda la pantalla
            contentScale = ContentScale.Crop // Ajusta la imagen para llenar la pantalla
        )
    }

    LaunchedEffect(Unit) {
        delay(2000) // Espera 2 segundos
        onFinish()
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewSplashScreen() {
    SplashScreenContent {}
}