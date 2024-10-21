package com.bergman.everdeals_finalproject.view

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.bergman.everdeals_finalproject.R
import com.bergman.everdeals_finalproject.ui.theme.EverdealsFinalProjectTheme

// Define colors
val Blue001875 = Color(0xFF001875)
val OrangeFF6200 = Color(0xFFFF6200)
val White = Color(0xFFFFFFFF)
val Black = Color(0xFF000000)

class Login : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EverdealsFinalProjectTheme {
                LoginScreen()
            }
        }
    }

    @OptIn(ExperimentalMaterial3Api::class)
    @Composable
    fun LoginScreen(logoSize: Int = 120) {
        var email by remember { mutableStateOf("") }
        var password by remember { mutableStateOf("") }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Blue001875),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top  // Coloca los elementos en la parte superior
            ) {
                // Logo en la parte superior
                Image(
                    painter = painterResource(id = R.drawable.logoeverdeals),
                    contentDescription = "Logo EverDeals",
                    modifier = Modifier
                        .size(350.dp)
                        .padding(top = 100.dp),  // Reducimos el margen superior
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp)) // Espacio entre el logo y los campos de texto

                // Email Field
                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Username", color = Black) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Black,
                        containerColor = White,
                        focusedIndicatorColor = OrangeFF6200,
                        unfocusedIndicatorColor = OrangeFF6200
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                // Password Field
                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    visualTransformation = PasswordVisualTransformation(),
                    label = { Text("Password", color = Black) },
                    colors = TextFieldDefaults.textFieldColors(
                        textColor = Black,
                        containerColor = White,
                        focusedIndicatorColor = OrangeFF6200,
                        unfocusedIndicatorColor = OrangeFF6200
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))  // Espaciado antes de los botones

                // Login Button
                Button(
                    onClick = { loginUser(email, password) },
                    modifier = Modifier
                        .width(160.dp)
                        .height(55.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFF6200)
                ) {
                    Text("Login", color = White)
                }

                // Create Account Button (redirige a RegisterActivity)
                Button(
                    onClick = { navigateToRegisterScreen() },
                    modifier = Modifier
                        .width(160.dp)
                        .height(55.dp)
                        .padding(vertical = 8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = OrangeFF6200)
                ) {
                    Text("Create Account", color = White)
                }
            }
        }
    }

    // Redirige a RegisterActivity
    private fun navigateToRegisterScreen() {
        val intent = Intent(this, RegisterActivity::class.java)
        startActivity(intent)
    }

    private fun loginUser(email: String, password: String) {
        if (email == "user@example.com" && password == "password") {  // Simulación de credenciales correctas
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()  // Cierra la actividad de Login para que no pueda volver con el botón de atrás
        } else {
            Toast.makeText(this, "Invalid credentials", Toast.LENGTH_SHORT).show()
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewLoginScreen() {
        EverdealsFinalProjectTheme {
            LoginScreen(logoSize = 350)
        }
    }
}


