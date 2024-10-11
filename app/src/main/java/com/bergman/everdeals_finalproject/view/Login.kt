package com.bergman.everdeals_finalproject.view

import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

class Login : ComponentActivity() {
    private var uri: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            LoginScreen()
        }
    }

    @Composable
    fun LoginScreen() {
        var emailLogin by remember { mutableStateOf("") }
        var passwordLogin by remember { mutableStateOf("") }
        var emailRegister by remember { mutableStateOf("") }
        var passwordRegister by remember { mutableStateOf("") }
        var confirmPasswordRegister by remember { mutableStateOf("") }
        var usernameRegister by remember { mutableStateOf("") }
        var isRegistering by remember { mutableStateOf(false) }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (isRegistering) {
                Text("Register", style = MaterialTheme.typography.titleLarge)

                BasicTextField(
                    value = usernameRegister,
                    onValueChange = { usernameRegister = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Username") }
                )

                BasicTextField(
                    value = emailRegister,
                    onValueChange = { emailRegister = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email") }
                )

                BasicTextField(
                    value = passwordRegister,
                    onValueChange = { passwordRegister = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Password") }
                )

                BasicTextField(
                    value = confirmPasswordRegister,
                    onValueChange = { confirmPasswordRegister = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Confirm Password") }
                )

                Button(onClick = {
                    if (passwordRegister == confirmPasswordRegister) {
                        registerUser(usernameRegister, emailRegister, passwordRegister)
                    } else {
                        Toast.makeText(this@Login, "Passwords do not match", Toast.LENGTH_SHORT).show()
                    }
                }) {
                    Text("Register")
                }

                Button(onClick = { isRegistering = false }) {
                    Text("Back to Login")
                }
            } else {
                Text("Login", style = MaterialTheme.typography.titleLarge)

                BasicTextField(
                    value = emailLogin,
                    onValueChange = { emailLogin = it },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("Email") }
                )

                BasicTextField(
                    value = passwordLogin,
                    onValueChange = { passwordLogin = it },
                    modifier = Modifier.fillMaxWidth(),
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = { Text("Password") }
                )

                Button(onClick = { loginUser(emailLogin, passwordLogin) }) {
                    Text("Login")
                }

                Button(onClick = { isRegistering = true }) {
                    Text("Create Account")
                }
            }
        }
    }

    private fun loginUser(email: String, password: String) {
        val apiService = createApiService()
        apiService.login(LoginRequest(email, password)).enqueue(object : retrofit2.Callback<LoginResponse> {
            override fun onResponse(call: retrofit2.Call<LoginResponse>, response: retrofit2.Response<LoginResponse>) {
                if (response.isSuccessful) {
                    Log.d(ContentValues.TAG, "Login successful")
                    startActivity(Intent(this@Login, MainActivity::class.java))
                } else {
                    Log.d(ContentValues.TAG, "Invalid credentials")
                    Toast.makeText(this@Login, "Invalid credentials", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: retrofit2.Call<LoginResponse>, t: Throwable) {
                Log.d(ContentValues.TAG, "Error during login: ${t.message}")
            }
        })
    }

    private fun registerUser(username: String, email: String, password: String) {
        val apiService = createApiService()
        apiService.register(RegisterRequest(username, email, password)).enqueue(object : retrofit2.Callback<RegisterResponse> {
            override fun onResponse(call: retrofit2.Call<RegisterResponse>, response: retrofit2.Response<RegisterResponse>) {
                if (response.isSuccessful) {
                    Log.d(ContentValues.TAG, "User registered successfully")
                    Toast.makeText(this@Login, "User created successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Log.d(ContentValues.TAG, "Error registering user")
                }
            }

            override fun onFailure(call: retrofit2.Call<RegisterResponse>, t: Throwable) {
                Log.d(ContentValues.TAG, "Error during registration: ${t.message}")
            }
        })
    }

    private fun createApiService(): ApiService {
        val client = OkHttpClient.Builder().build()
        val retrofit = Retrofit.Builder()
            .baseUrl("http://your-api-url/") // Cambia esto a la URL de tu API
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        return retrofit.create(ApiService::class.java)
    }

    private fun pickImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, PICK_IMAGE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null && data.data != null) {
            uri = data.data
            // Cargar la imagen seleccionada y establecerla en imgProfileBitmap
        }
    }

    @Preview(showBackground = true)
    @Composable
    fun PreviewLoginScreen() {
        LoginScreen()
    }

    companion object {
        private const val PICK_IMAGE = 0
    }
}
