package project.mobile.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
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
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import kotlinx.coroutines.launch

@Composable
fun AppNavigation(googleSignInLauncher: ActivityResultLauncher<IntentSenderRequest>? = null) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userPreferences = UserPreferences(context)
    val authRepository = AuthRepository(userPreferences)
    val productRepository = ProductRepository(authRepository)
    val amazonScraper = AmazonScraper()
    val authManager = AuthManager(authRepository, context)
    val googleAuthHandler = GoogleAuthHandler(context)
    val appleAuthHandler = AppleAuthHandler(context)

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    LaunchedEffect(Unit) {
        isLoggedIn = authManager.checkSession()
        if (isLoggedIn) {
            currentUser = authManager.getCurrentUser()
            navController.navigate("main") { popUpTo("login") { inclusive = true } }
        }
    }

    NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "login") {
        composable("login") {
            LoginScreen(
                onNavigateToRegister = { navController.navigate("register") },
                onLoginSuccess = { scope.launch { navController.navigate("main") { popUpTo("login") { inclusive = true } } } },
                onNavigateToForgotPassword = { navController.navigate("forgot_password") },
                authManager = authManager
            )
        }
        composable("register") {
            RegisterScreen(
                onNavigateToLogin = { navController.navigate("login") { popUpTo("register") { inclusive = true } } },
                authManager = authManager,
                googleAuthHandler = googleAuthHandler.apply { if (googleSignInLauncher != null) startGoogleSignIn(googleSignInLauncher) },
                appleAuthHandler = appleAuthHandler
            )
        }

        composable("main") {
            val productViewModel: ProductViewModel = viewModel(
                factory = ProductViewModel.Factory(
                    context.applicationContext as Application,
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
                    context.applicationContext as Application,
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
                currentUser = authManager.getCurrentUser()
            }
            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                currentUser = currentUser
            )
        }

        composable("forgot_password") {
            ForgotPasswordScreen(
                onBackClick = { navController.popBackStack() }
            )
        }
    }
}

