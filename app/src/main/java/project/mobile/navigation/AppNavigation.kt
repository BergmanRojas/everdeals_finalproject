package project.mobile.navigation

import android.app.Application
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import project.mobile.controller.AppleAuthHandler
import project.mobile.controller.AuthManager
import project.mobile.controller.ForumViewModel
import project.mobile.controller.GoogleAuthHandler
import project.mobile.controller.ProductViewModel
import project.mobile.controller.ProfileViewModel
import project.mobile.model.AuthRepository
import project.mobile.model.ProductRepository
import project.mobile.model.User
import project.mobile.model.UserPreferences
import project.mobile.util.AmazonScraper
import project.mobile.view.AddProductScreen
import project.mobile.view.ForumScreen
import project.mobile.view.LoginScreen
import project.mobile.view.MainScreenContent
import project.mobile.view.ProductDetailScreen
import project.mobile.view.ProfileScreen
import project.mobile.view.RegisterScreen
import project.mobile.view.SettingsScreen
import project.mobile.view.screens.ForgotPasswordScreen
import project.mobile.view.screens.SplashScreenContent
import project.mobile.view.screens.AffiliateScreen

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
    val googleAuthHandler = GoogleAuthHandler(context)
    val appleAuthHandler = AppleAuthHandler(context)

    var isLoggedIn by remember { mutableStateOf(false) }
    var currentUser by remember { mutableStateOf<User?>(null) }

    val currentRoute by navController.currentBackStackEntryAsState()
    val currentScreen = currentRoute?.destination?.route ?: "splash"

    val excludedRoutes = listOf("splash", "login", "register", "forgot_password")

    var selectedItem by remember { mutableStateOf("Menu") }

    LaunchedEffect(Unit) {
        isLoggedIn = authManager.checkSession()
        currentUser = authManager.getCurrentUser()
    }

    LaunchedEffect(isSystemInDarkTheme()) {
        isLoggedIn = authManager.checkSession()
        currentUser = authManager.getCurrentUser()
    }

    Scaffold(
        bottomBar = {
            if (isLoggedIn && currentScreen !in excludedRoutes) {
                BottomNavigationBar(
                    navController = navController,
                    onAddProductClick = { navController.navigate("add_product") },
                    onProfileClick = {
                        scope.launch {
                            navController.navigate(Screen.Profile.route)
                        }
                    },
                    selectedItem = selectedItem,
                    onItemSelected = { selectedItem = it }
                )
            }
        },
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "splash",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("splash") {
                SplashScreenContent()
                LaunchedEffect(Unit) {
                    delay(2000L)
                    isLoggedIn = authManager.checkSession()
                    if (isLoggedIn) {
                        currentUser = authManager.getCurrentUser()
                        navController.navigate("main") {
                            popUpTo("splash") { inclusive = true }
                        }
                    } else {
                        navController.navigate("login") {
                            popUpTo("splash") { inclusive = true }
                        }
                    }
                }
            }

            composable("login") {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate("register") },
                    onLoginSuccess = {
                        scope.launch {
                            isLoggedIn = true
                            currentUser = authManager.getCurrentUser()
                            navController.navigate("main") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    },
                    onNavigateToForgotPassword = { navController.navigate("forgot_password") },
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
                    authRepository = authRepository,
                    googleAuthHandler = googleAuthHandler,
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
                    onProfileClick = {
                        scope.launch {
                            navController.navigate(Screen.Profile.route)
                        }
                    },
                    productViewModel = productViewModel,
                    navController = navController
                )
            }

            composable(Screen.Affiliate.route) {
                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(
                        context.applicationContext as Application,
                        productRepository,
                        amazonScraper,
                        authRepository
                    )
                )
                AffiliateScreen(productViewModel = productViewModel)
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

            composable(
                route = Screen.ProductDetail.route,
                arguments = listOf(
                    navArgument("productId") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val productId = backStackEntry.arguments?.getString("productId") ?: ""
                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(
                        context.applicationContext as Application,
                        productRepository,
                        amazonScraper,
                        authRepository
                    )
                )
                ProductDetailScreen(
                    productId = productId,
                    onNavigateBack = { navController.popBackStack() },
                    productViewModel = productViewModel,
                    authManager = authManager,
                    onUserClick = {
                        navController.navigate(Screen.Profile.route)
                    }
                )
            }

            composable(Screen.Profile.route) {
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(authManager) as T
                        }
                    }
                )
                ProfileScreen(
                    viewModel = profileViewModel,
                    authManager = authManager,
                    onNavigateBack = { navController.popBackStack() },
                    onNavigateToSettings = { navController.navigate("settings") },
                    onSignOut = {
                        scope.launch {
                            authManager.signOut()
                            isLoggedIn = false
                            currentUser = null
                            navController.navigate("login") {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onShareClick = { /* Implementar lógica de compartir */ },
                    onAddProductClick = { navController.navigate("add_product") },
                    onProfileClick = {
                        scope.launch {
                            navController.navigate(Screen.Profile.route)
                        }
                    },
                    isOwnProfile = true,
                    navController = navController
                )
            }

            composable("settings") {
                LaunchedEffect(Unit) {
                    currentUser = authManager.getCurrentUser()
                }
                SettingsScreen(
                    onNavigateBack = { navController.popBackStack() },
                    currentUser = currentUser,
                    onSignOut = {
                        scope.launch {
                            authManager.signOut()
                            isLoggedIn = false
                            currentUser = null
                            navController.navigate("login") {
                                popUpTo("main") { inclusive = true }
                            }
                        }
                    },
                    navController = navController
                )
            }

            composable("forgot_password") {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable("forum") {
                val forumViewModel: ForumViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ForumViewModel(authManager) as T
                        }
                    }
                )
                ForumScreen(
                    viewModel = forumViewModel,
                    navController = navController
                )
            }
        }
    }
}