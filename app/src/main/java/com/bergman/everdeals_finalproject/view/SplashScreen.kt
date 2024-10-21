package com.bergman.everdeals_finalproject.view

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.bergman.everdeals_finalproject.R
import com.bergman.everdeals_finalproject.ui.theme.EverdealsFinalProjectTheme

class SplashScreen : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Configuramos el Splash Screen para que se muestre durante 3 segundos
        Handler(Looper.getMainLooper()).postDelayed({
            // Después de 3 segundos, redirigir al Login
            val intent = Intent(this@SplashScreen, Login::class.java)
            startActivity(intent)
            finish()  // Cerrar el Splash Screen para que no regrese con el botón "atrás"
        }, 3000)  // 1000 milisegundos = 1 segundo

        // Mostrar el contenido del Splash Screen
        setContent {
            EverdealsFinalProjectTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(Color(0xFF001875)),  // Color azul de fondo
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.loginlogo),
                        contentDescription = "Logo de Splash Screen",
                        modifier = Modifier.fillMaxSize(0.9f),  // Ajusta el tamaño de la imagen
                        contentScale = ContentScale.Fit
                    )
                }
            }
        }
    }
}
