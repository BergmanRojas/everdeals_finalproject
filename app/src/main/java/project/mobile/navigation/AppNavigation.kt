package project.mobile.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
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

@Composable
fun AppNavigation() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    val userPreferences = UserPreferences(context)
    val authRepository = AuthRepository(userPreferences)
    val productRepository = ProductRepository(authRepository)
    val amazonScraper = AmazonScraper()
    val authManager = AuthManager(authRepository, context)

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    // Fetch user data when the app starts
    LaunchedEffect(Unit) {
        isLoggedIn = authManager.checkSession()
        if (isLoggedIn) {
            currentUser = authManager.getCurrentUser()
            navController.navigate("main") {
                popUpTo("login") { inclusive = true }
            }
        }
    }

    NavHost(navController = navController, startDestination = if (isLoggedIn) "main" else "login") {
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
                authManager = authManager
            )
        }

        composable("main") {
            val productViewModel: ProductViewModel = viewModel(
                factory = ProductViewModel.Factory(
                    (context.applicationContext as Application),
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
                    (context.applicationContext as Application),
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
                // Refresh user data when entering settings screen
                currentUser = authManager.getCurrentUser()
            }

            SettingsScreen(
                onNavigateBack = { navController.popBackStack() },
                currentUser = currentUser
            )
        }
    }
}

