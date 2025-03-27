package project.mobile.navigation

import android.app.Application
import android.util.Log
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import project.mobile.view.ChatScreen
import project.mobile.view.ForumScreen
import project.mobile.view.LoginScreen
import project.mobile.view.MainScreenContent
import project.mobile.view.MessagesScreen
import project.mobile.view.ProductDetailScreen
import project.mobile.view.ProfileScreen
import project.mobile.view.RegisterScreen
import project.mobile.view.SettingsScreen
import project.mobile.view.screens.AffiliateScreen
import project.mobile.view.screens.ForgotPasswordScreen
import project.mobile.view.screens.SplashScreenContent

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
    val currentScreen = currentRoute?.destination?.route ?: Screen.Splash.route

    val excludedRoutes = listOf(Screen.Splash.route, Screen.Login.route, Screen.Register.route, Screen.ForgotPassword.route)

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
                    onAddProductClick = { navController.navigate(Screen.AddProduct.route) },
                    onProfileClick = {
                        scope.launch {
                            val userId = authManager.getCurrentUser()?.id ?: return@launch
                            navController.navigate(Screen.Profile.createRoute(userId, true))
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
            startDestination = Screen.Splash.route,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(Screen.Splash.route) {
                SplashScreenContent()
                LaunchedEffect(Unit) {
                    delay(2000L)
                    isLoggedIn = authManager.checkSession()
                    if (isLoggedIn) {
                        currentUser = authManager.getCurrentUser()
                        navController.navigate(Screen.Main.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    } else {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Splash.route) { inclusive = true }
                        }
                    }
                }
            }

            composable(Screen.Login.route) {
                LoginScreen(
                    onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                    onLoginSuccess = {
                        scope.launch {
                            isLoggedIn = true
                            currentUser = authManager.getCurrentUser()
                            navController.navigate(Screen.Main.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        }
                    },
                    onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) },
                    authManager = authManager
                )
            }

            composable(Screen.Register.route) {
                RegisterScreen(
                    onNavigateToLogin = {
                        navController.navigate(Screen.Login.route) {
                            popUpTo(Screen.Register.route) { inclusive = true }
                        }
                    },
                    authRepository = authRepository,
                    googleAuthHandler = googleAuthHandler,
                    appleAuthHandler = appleAuthHandler
                )
            }

            composable(Screen.Main.route) {
                val productViewModel: ProductViewModel = viewModel(
                    factory = ProductViewModel.Factory(
                        context.applicationContext as Application,
                        productRepository,
                        amazonScraper,
                        authRepository
                    )
                )
                MainScreenContent(
                    onAddProductClick = { navController.navigate(Screen.AddProduct.route) },
                    onProfileClick = { // Corrección aquí
                        scope.launch {
                            val userId = authManager.getCurrentUser()?.id ?: return@launch
                            navController.navigate(Screen.Profile.createRoute(userId, true))
                        }
                    },
                    productViewModel = productViewModel,
                    navController = navController,
                    authManager = authManager
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

            composable(Screen.AddProduct.route) {
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
                    onUserClick = { navController.navigate(Screen.Profile.createRoute(productId, false)) },
                    navController = navController
                )
            }

            composable(
                route = Screen.Profile.route,
                arguments = listOf(
                    navArgument("userId") { type = NavType.StringType },
                    navArgument("isOwnProfile") { type = NavType.BoolType }
                )
            ) { backStackEntry ->
                val userId = backStackEntry.arguments?.getString("userId") ?: ""
                val isOwnProfile = backStackEntry.arguments?.getBoolean("isOwnProfile") ?: true
                val currentUserId = currentUser?.id
                Log.d("AppNavigation", "Profile route: userId=$userId, isOwnProfile=$isOwnProfile, currentUserId=$currentUserId")
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
                    onNavigateToSettings = { navController.navigate(Screen.Settings.route) },
                    onSignOut = {
                        scope.launch {
                            authManager.signOut()
                            isLoggedIn = false
                            currentUser = null
                            navController.navigate(Screen.Login.route) {
                                popUpTo(0) { inclusive = true }
                            }
                        }
                    },
                    onNavigateBack = { navController.popBackStack() },
                    onShareClick = { /* Implementar lógica de compartir */ },
                    onAddProductClick = { navController.navigate(Screen.AddProduct.route) },
                    onProfileClick = { navController.navigate(Screen.Profile.createRoute(userId, isOwnProfile = false)) },
                    isOwnProfile = isOwnProfile,
                    navController = navController,
                    userId = userId
                )
            }

            composable(Screen.Settings.route) {
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
                            navController.navigate(Screen.Login.route) {
                                popUpTo(Screen.Main.route) { inclusive = true }
                            }
                        }
                    },
                    navController = navController
                )
            }

            composable(Screen.ForgotPassword.route) {
                ForgotPasswordScreen(
                    onBackClick = { navController.popBackStack() }
                )
            }

            composable(Screen.Forum.route) {
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

            composable(Screen.Messages.route) {
                MessagesScreen(
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController,
                    authManager = authManager
                )
            }

            composable(
                route = Screen.Chat.route,
                arguments = listOf(
                    navArgument("targetUserId") { type = NavType.StringType },
                    navArgument("targetUserName") { type = NavType.StringType }
                )
            ) { backStackEntry ->
                val targetUserId = backStackEntry.arguments?.getString("targetUserId") ?: ""
                val targetUserName = backStackEntry.arguments?.getString("targetUserName") ?: ""
                val profileViewModel: ProfileViewModel = viewModel(
                    factory = object : ViewModelProvider.Factory {
                        @Suppress("UNCHECKED_CAST")
                        override fun <T : ViewModel> create(modelClass: Class<T>): T {
                            return ProfileViewModel(authManager) as T
                        }
                    }
                )
                ChatScreen(
                    targetUserId = targetUserId,
                    targetUserName = targetUserName,
                    viewModel = profileViewModel,
                    authManager = authManager,
                    onNavigateBack = { navController.popBackStack() },
                    navController = navController
                )
            }
        }
    }
}