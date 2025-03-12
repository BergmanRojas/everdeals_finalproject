package project.mobile.view

<<<<<<< HEAD
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
=======
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
<<<<<<< HEAD
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.launch
import project.mobile.R
import project.mobile.controller.AppleAuthHandler
import project.mobile.controller.AuthManager
import project.mobile.controller.GoogleAuthHandler
=======
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import project.mobile.R
import project.mobile.controller.AuthManager
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
import project.mobile.model.AuthState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterScreen(
    onNavigateToLogin: () -> Unit,
<<<<<<< HEAD
    authManager: AuthManager,
    googleAuthHandler: GoogleAuthHandler,
    appleAuthHandler: AppleAuthHandler
) {
    var username by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var usernameError by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf("") }
    var passwordError by remember { mutableStateOf("") }
    var confirmPasswordError by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    val googleSignInLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        scope.launch {
            val success = googleAuthHandler.handleSignInResult(result.data)
            if (success) {
                snackbarHostState.showSnackbar("Registered with Google successfully!")
                onNavigateToLogin()
            } else {
                snackbarHostState.showSnackbar("Google Sign-Up failed")
            }
        }
    }
=======
    authManager: AuthManager
) {
    var email by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    val scope = rememberCoroutineScope()
    val authState by authManager.authState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Success -> {
                snackbarHostState.showSnackbar("Registration successful!")
                onNavigateToLogin()
            }
            is AuthState.Error -> {
<<<<<<< HEAD
                val errorMessage = (authState as AuthState.Error).message
                when {
                    errorMessage.contains("email address is already in use") -> {
                        emailError = "The email address is already in use by another account"
                    }
                    errorMessage.contains("password") -> {
                        passwordError = errorMessage
                    }
                    else -> {
                        snackbarHostState.showSnackbar(errorMessage)
                    }
                }
=======
                snackbarHostState.showSnackbar(
                    "Registration failed: ${(authState as AuthState.Error).message}"
                )
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
            }
            else -> {}
        }
    }

    Scaffold(
<<<<<<< HEAD
        snackbarHost = {
            SnackbarHost(hostState = snackbarHostState) { data ->
                Snackbar(
                    modifier = Modifier.padding(16.dp),
                    content = { Text(data.visuals.message, fontSize = 16.sp) },
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(paddingValues)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp, start = 4.dp),
                    horizontalArrangement = Arrangement.Start
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.back_icon),
                        contentDescription = "Back",
                        modifier = Modifier
                            .size(32.dp)
                            .clickable { onNavigateToLogin() }
                    )
                }

                Spacer(modifier = Modifier.height(26.dp))

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "Welcome",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 32.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                    Text(
                        text = "benefits are waiting for you!",
                        color = MaterialTheme.colorScheme.secondary,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                TextField(
                    value = username,
                    onValueChange = { username = it; usernameError = "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    label = { Text("Username", color = MaterialTheme.colorScheme.onSurface) },
                    colors = TextFieldDefaults.colors(
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledTextColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface,
                        disabledContainerColor = MaterialTheme.colorScheme.surface,
                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (usernameError.isNotEmpty()) {
                    Text(
                        text = usernameError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = email,
                    onValueChange = { email = it; emailError = "" },
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
                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (emailError.isNotEmpty()) {
                    Text(
                        text = emailError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = password,
                    onValueChange = { password = it; passwordError = "" },
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
                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (passwordError.isNotEmpty()) {
                    Text(
                        text = passwordError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                TextField(
                    value = confirmPassword,
                    onValueChange = { confirmPassword = it; confirmPasswordError = "" },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                    label = { Text("Confirm Password", color = MaterialTheme.colorScheme.onSurface) },
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
                        focusedIndicatorColor = MaterialTheme.colorScheme.secondary,
                        unfocusedIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant,
                        disabledIndicatorColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    shape = RoundedCornerShape(12.dp)
                )
                if (confirmPasswordError.isNotEmpty()) {
                    Text(
                        text = confirmPasswordError,
                        color = MaterialTheme.colorScheme.error,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(top = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        confirmPasswordError = if (password != confirmPassword) "Passwords do not match" else ""
                        if (username.isEmpty()) {
                            usernameError = "Username cannot be empty"
                        }
                        if (email.isEmpty()) {
                            emailError = "Email cannot be empty"
                        }
                        if (password.isEmpty()) {
                            passwordError = "Password cannot be empty"
                        }
                        if (confirmPassword.isEmpty()) {
                            confirmPasswordError = "Confirm Password cannot be empty"
                        }
                        if (usernameError.isEmpty() && emailError.isEmpty() && passwordError.isEmpty() && confirmPasswordError.isEmpty()) {
                            scope.launch {
                                authManager.signUp(email, password, username)
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(55.dp)
                        .padding(horizontal = 24.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (usernameError.isNotEmpty() || emailError.isNotEmpty() || passwordError.isNotEmpty() || confirmPasswordError.isNotEmpty()) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            MaterialTheme.colorScheme.secondary
                        }
                    ),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Text("Register", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSecondary)
                }

                Spacer(modifier = Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Divider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        text = "or register with",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontSize = 14.sp,
                        modifier = Modifier.padding(horizontal = 8.dp)
                    )
                    Divider(
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        thickness = 1.dp,
                        modifier = Modifier.weight(1f)
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp),
                    horizontalArrangement = Arrangement.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_google),
                        contentDescription = "Google Icon",
                        modifier = Modifier
                            .size(40.dp)
                            .clickable { googleSignInLauncher.launch(googleAuthHandler.getSignInIntent()) }
                    )
                    Spacer(modifier = Modifier.width(32.dp))
                    Image(
                        painter = painterResource(id = R.drawable.ic_apple),
                        contentDescription = "Apple Icon",
                        modifier = Modifier
                            .size(48.dp)
                            .clickable {
                                scope.launch {
                                    val success = appleAuthHandler.signInWithApple()
                                    if (success) {
                                        snackbarHostState.showSnackbar("Registered with Apple successfully!")
                                        onNavigateToLogin()
                                    } else {
                                        snackbarHostState.showSnackbar("Apple Sign-Up failed")
                                    }
                                }
                            }
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                if (authState is AuthState.Loading) {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(bottom = 8.dp),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
=======
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.everdeals),
                contentDescription = "EverDeals Logo",
                modifier = Modifier
                    .size(120.dp)
                    .padding(bottom = 16.dp),
                contentScale = ContentScale.Fit
            )

            OutlinedTextField(
                value = email,
                onValueChange = { email = it },
                label = { Text("Email") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Username") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = password,
                onValueChange = { password = it },
                label = { Text("Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = confirmPassword,
                onValueChange = { confirmPassword = it },
                label = { Text("Confirm Password") },
                visualTransformation = PasswordVisualTransformation(),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Button(
                onClick = {
                    if (email.isNotEmpty() && username.isNotEmpty() &&
                        password.isNotEmpty() && confirmPassword.isNotEmpty()
                    ) {
                        if (password == confirmPassword) {
                            scope.launch {
                                authManager.signUp(email, password, username)
                            }
                        } else {
                            scope.launch {
                                snackbarHostState.showSnackbar("Passwords do not match")
                            }
                        }
                    } else {
                        scope.launch {
                            snackbarHostState.showSnackbar("Please fill all fields")
                        }
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Register")
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(onClick = onNavigateToLogin) {
                Text("Already have an account? Login")
            }

            when (authState) {
                is AuthState.Loading -> {
                    CircularProgressIndicator(
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }
                else -> {}
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
            }
        }
    }
}
<<<<<<< HEAD
=======

>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
