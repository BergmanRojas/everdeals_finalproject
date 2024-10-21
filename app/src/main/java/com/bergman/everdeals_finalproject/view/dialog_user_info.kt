package com.bergman.everdeals_finalproject.view

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import coil.compose.rememberAsyncImagePainter
import com.bergman.everdeals_finalproject.R

class UserInfoDialog : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            UserInfoDialogScreen(
                userName = "Usuario de Ejemplo",
                money = 100,
                sales = 50,
                profileImageUrl = "https://example.com/profile_image.jpg"
            )
        }
    }
}

@Composable
fun UserInfoDialogScreen(userName: String, money: Int, sales: Int, profileImageUrl: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Imagen de perfil
        Image(
            painter = rememberAsyncImagePainter(profileImageUrl),
            contentDescription = "User Profile Image",
            modifier = Modifier.size(100.dp)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Nombre del usuario
        Text(text = userName, style = MaterialTheme.typography.titleLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Dinero del usuario
        Text(text = "Money: $money€", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(8.dp))

        // Ventas del usuario
        Text(text = "Sales: $sales", style = MaterialTheme.typography.bodyLarge)

        Spacer(modifier = Modifier.height(16.dp))

        // Botón para cerrar
        Button(onClick = { /* Acción para cerrar el diálogo o actividad */ }) {
            Text(text = "OK")
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewUserInfoDialogScreen() {
    UserInfoDialogScreen(
        userName = "Usuario de Ejemplo",
        money = 100,
        sales = 50,
        profileImageUrl = "https://example.com/profile_image.jpg"
    )
}
