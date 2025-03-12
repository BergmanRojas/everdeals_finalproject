package project.mobile.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
<<<<<<< HEAD
import project.mobile.controller.AppleAuthHandler
import project.mobile.controller.AuthManager
import project.mobile.controller.GoogleAuthHandler
import project.mobile.controller.ProductViewModel
import project.mobile.model.AuthRepository
import project.mobile.model.ProductRepository
import project.mobile.model.User
import project.mobile.model.UserPreferences
import project.mobile.util.AmazonScraper
import project.mobile.view.*
import project.mobile.view.screens.ForgotPasswordScreen
import android.app.Application
import kotlinx.coroutines.launch
=======
import project.mobile.controller.ProductViewModel
import project.mobile.model.AuthRepository
import project.mobile.model.ProductRepository
import project.mobile.model.UserPreferences
import project.mobile.view.*
import project.mobile.util.AmazonScraper
import android.app.Application
import project.mobile.controller.AuthManager
import kotlinx.coroutines.launch
import project.mobile.model.User
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

<<<<<<< HEAD
    // Dependencias
=======
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
    val userPreferences = UserPreferences(context)
    val authRepository = AuthRepository(userPreferences)
    val productRepository = ProductRepository(authRepository)
    val amazonScraper = AmazonScraper()
    val authManager = AuthManager(authRepository, context)
<<<<<<< HEAD
    val googleAuthHandler = GoogleAuthHandler(context) // Instancia para Google
    val appleAuthHandler = AppleAuthHandler(context) // Instancia para Apple
=======
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }

<<<<<<< HEAD
    // Verificar sesión al iniciar la app
=======
    // Fetch user data when the app starts
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
    LaunchedEffect(Unit) {
        isLoggedIn = authManager.checkSession()
        if (isLoggedIn) {
            currentUser = authManager.getCurrentUser()
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

<<<<<<< HEAD
    NavHost(
        navController = navController,
        startDestination = if (isLoggedIn) "main" else "login"
    ) {
=======
    NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "login") {
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = {
                    scope.launch {
                        currentUser = authManager.getCurrentUser()
                        navController.navigate("main") {
                            popUpTo("login") { inclusive = true }
                        }
                    }
                },
<<<<<<< HEAD
                onNavigateToForgotPassword = { navController.navigate("forgot_password") }, // Añadido
=======
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
                authManager = authManager
            )
        }

        composable("register") {
            RegisterScreen(
                onNavigateToLogin = {
                    navController.navigate("login") {
                        popUpTo("register") { inclusive = true }
                    }
                },
<<<<<<< HEAD
                authManager = authManager,
                googleAuthHandler = googleAuthHandler, // Añadido
                appleAuthHandler = appleAuthHandler    // Añadido
=======
                authManager = authManager
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
            )
        }

        composable("main") {
            val productViewModel: ProductViewModel = viewModel(
                factory = ProductViewModel.Factory(
<<<<<<< HEAD
                    context.applicationContext as Application,
=======
                    (context.applicationContext as Application),
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
                    productRepository,
                    amazonScraper,
                    authRepository
                )
            )
            MainScreenContent(
                onAddProductClick = { navController.navigate("add_product") },
                onProfileClick = { navController.navigate("profile") },
                productViewModel = productViewModel,
                navController = navController
            )
        }

        composable("add_product") {
            val productViewModel: ProductViewModel = viewModel(
                factory = ProductViewModel.Factory(
<<<<<<< HEAD
                    context.applicationContext as Application,
=======
                    (context.applicationContext as Application),
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
                    productRepository,
                    amazonScraper,
                    authRepository
                )
            )
            AddProductScreen(
                onNavigateBack = { navController.popBackStack() },
                onProductAdded = { navController.popBackStack() },
                productViewModel = productViewModel,
                authManager = authManager
            )
        }

        composable("profile") {
            ProfileScreen(
                onNavigateToSettings = { navController.navigate("settings") },
                onSignOut = {
                    scope.launch {
                        authManager.signOut()
                        currentUser = null
                        navController.navigate("login") {
                            popUpTo("main") { inclusive = true }
                        }
                    }
                }
            )
        }

        composable("settings") {
            LaunchedEffect(Unit) {
<<<<<<< HEAD
                currentUser = authManager.getCurrentUser()
            }
=======
                // Refresh user data when entering settings screen
                currentUser = authManager.getCurrentUser()
            }

>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                currentUser = currentUser
            )
        }
<<<<<<< HEAD

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
=======
>>>>>>> b42bef80a5310ce8443e2568a67b50c0ffc95e28
    }
}

