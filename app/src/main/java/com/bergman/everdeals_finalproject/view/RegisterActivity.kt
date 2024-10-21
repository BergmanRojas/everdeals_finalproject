package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.bergman.everdeals_finalproject.R
import com.bergman.everdeals_finalproject.ui.theme.EverdealsFinalProjectTheme

val Blue001875 = Color(0xFF001875) // Color de fondo azul
val OrangeFF6200 = Color(0xFFFF6200) // Color naranja para botones
val White = Color(0xFFFFFFFF) // Color blanco

class RegisterActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EverdealsFinalProjectTheme {
                RegisterScreen()
            }
        }
    }

    @Composable
    fun RegisterScreen() {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue001875),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                // Título
                Text(
                    text = "Welcome benefits are waiting for you!",
                    color = White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(top = 24.dp, bottom = 16.dp)
                )

                // Botón para Iniciar sesión con Apple
                ButtonWithIcon(
                    iconResId = R.drawable.ic_apple, // Agregar el icono de Apple en la carpeta drawable
                    text = "Sign in with Apple"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para Iniciar sesión con Facebook
                ButtonWithIcon(
                    iconResId = R.drawable.ic_facebook, // Agregar el icono de Facebook en drawable
                    text = "Sign in with Facebook"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para Iniciar sesión con Google
                ButtonWithIcon(
                    iconResId = R.drawable.ic_google, // Agregar el icono de Google en drawable
                    text = "Sign in with Google"
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Botón para Iniciar sesión con correo electrónico
                ButtonWithIcon(
                    iconResId = R.drawable.ic_email, // Agregar el icono de correo electrónico en drawable
                    text = "Sign in with email"
                )

                Spacer(modifier = Modifier.height(24.dp))

                // Imagen final, si es necesario
                Image(
                    painter = painterResource(R.drawable.ic_welcome_image), // Aquí va la imagen que adjuntaste en drawable
                    contentDescription = "Welcome image",
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 16.dp)
                )
            }
        }
    }

    @Composable
    fun ButtonWithIcon(iconResId: Int, text: String) {
        Button(
            onClick = { /* Aquí va la lógica de autenticación */ },
            modifier = Modifier
                .fillMaxWidth()
                .height(55.dp),
            colors = ButtonDefaults.buttonColors(containerColor = White),
            shape = RoundedCornerShape(50)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Image(
                    painter = painterResource(id = iconResId),
                    contentDescription = null,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = text,
                    color = Color.Black,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewRegisterScreen() {
    EverdealsFinalProjectTheme {
        RegisterActivityScreen(logoSize = 350)
    }
}
