package project.mobile.view

<<<<<<< HEAD
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
=======
import androidx.compose.foundation.layout.*
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import project.mobile.R
import project.mobile.controller.AuthManager
=======
import androidx.compose.ui.unit.dp
import project.mobile.controller.AuthManager
import kotlinx.coroutines.launch
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
import project.mobile.model.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onNavigateToRegister: () -> Unit,
    onLoginSuccess: () -> Unit,
<<<<<<< HEAD
    onNavigateToForgotPassword: () -> Unit,
=======
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
    authManager: AuthManager
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
<<<<<<< HEAD
    var passwordVisible by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current
=======
    val scope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> onLoginSuccess()
            is AuthState.Error -> {
<<<<<<< HEAD
                snackbarHostState.showSnackbar((authState as AuthState.Error).message)
=======
                snackbarHostState.showSnackbar(
                    (authState as AuthState.Error).message
                )
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
            }
            else -> {}
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
<<<<<<< HEAD
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues),
            contentAlignment = Alignment.TopCenter
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {
                Image(
                    painter = painterResource(id = R.drawable.everdeals), // Ajusta según tu recurso
                    contentDescription = "Logo EverDeals",
                    modifier = Modifier
                        .size(350.dp)
                        .padding(top = 100.dp),
                    contentScale = ContentScale.Fit
                )

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Email", color = MaterialTheme.colorScheme.onSurface) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                TextField(
                    value = password,
                    onValueChange = { password = it },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    label = { Text("Password", color = MaterialTheme.colorScheme.onSurface) },
                    trailingIcon = {
                        val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                        IconButton(onClick = { passwordVisible = !passwordVisible }) {
                            Icon(
                                imageVector = image,
                                contentDescription = "Toggle password visibility",
                                tint = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.primary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        scope.launch {
                            authManager.signIn(email, password)
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary), // OrangeFF6200
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text(
                        "Login",
                        color = MaterialTheme.colorScheme.onSecondary,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Forgot Password?",
                    color = MaterialTheme.colorScheme.onBackground,
                    fontSize = 14.sp,
                    modifier = Modifier
                        .clickable { onNavigateToForgotPassword() }
                        .padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.weight(1f))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 28.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Don't have an account?",
                        color = MaterialTheme.colorScheme.onBackground,
                        fontSize = 14.sp
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = "Create Account",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 20.sp,
                        modifier = Modifier
                            .clickable { onNavigateToRegister() }
                            .padding(top = 4.dp)
                    )
                }

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 16.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
    }
}
=======
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            TextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    scope.launch {
                        authManager.signIn(email, password)
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Login")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToRegister) {
                Text("Don't have an account? Register")
            }

            when (authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {}
            }
        }
    }
}

>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
