package project.mobile.view.navigations

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import kotlinx.coroutines.delay
import project.mobile.controller.AppleAuthHandler
import project.mobile.controller.AuthViewModel
import project.mobile.controller.CommentViewModel
import project.mobile.controller.DealRepository
import project.mobile.controller.GoogleAuthHandler
import project.mobile.controller.ProductViewModel
import project.mobile.controller.UserRepository
import project.mobile.models.AuthState
import project.mobile.models.UserInfo
import project.mobile.tools.SessionStorage
import project.mobile.ui.theme.EverDealsTheme
import project.mobile.view.screens.CategoriesScreen
import project.mobile.view.screens.ForgotPasswordScreen
import project.mobile.view.screens.HomeScreen
import project.mobile.view.screens.LoginScreen
import project.mobile.view.screens.RegisterScreen
import project.mobile.view.screens.ShareScreen
import project.mobile.view.screens.SplashScreenContent
import project.mobile.view.screens.UserInfoScreen
import project.mobile.view.screens.ViewCategoryScreen
import project.mobile.view.screens.ViewProductScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Home : Screen("home")
    object ViewProduct : Screen("viewProduct/{productId}") {
        fun createRoute(productId: String) = "viewProduct/$productId"
    }
    object ViewCategory : Screen("viewCategory/{categoryName}") {
        fun createRoute(categoryName: String) = "viewCategory/$categoryName"
    }
    object Categories : Screen("categories")
    object UserInfo : Screen("userInfo")
    object Share : Screen("share")
    object Profile : Screen("profile")
    object Deals : Screen("deals")
    object Shopping : Screen("shopping")
    object Community : Screen("community")
}

class AuthViewModelFactory(private val activity: ComponentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AuthViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return AuthViewModel(UserRepository(), SessionStorage(activity)) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

class ProductViewModelFactory(private val activity: ComponentActivity) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ProductViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return ProductViewModel(activity.application, DealRepository()) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun EverDealsNavigation(
    navController: NavHostController = rememberNavController(),
    activity: ComponentActivity
) {
    val authViewModel: AuthViewModel = viewModel(factory = AuthViewModelFactory(activity))
    val productViewModel: ProductViewModel = viewModel(factory = ProductViewModelFactory(activity))
    val googleAuthHandler = GoogleAuthHandler(activity)
    val appleAuthHandler = AppleAuthHandler(activity)
    val authState by authViewModel.authState.collectAsState()
    val scope = rememberCoroutineScope()

    EverDealsTheme {
        Scaffold(
            topBar = {
                if (authState is AuthState.Success) {
                    TopNavigationBar(
                        userState = authState,
                        authViewModel = authViewModel,
                        scope = scope,
                        activity = activity,
                        dealViewModel = productViewModel,
                        navController = navController
                    )
                }
            },
            bottomBar = {
                if (authState is AuthState.Success) {
                    BottomNavigationBar(navController)
                }
            }
        ) { paddingValues ->
            NavHost(
                navController = navController,
                startDestination = Screen.Splash.route,
                modifier = Modifier.padding(paddingValues)
            ) {
                composable(Screen.Splash.route) {
                    SplashScreenContent()
                    LaunchedEffect(Unit) {
                        delay(2000)
                        if (authState is AuthState.Success) {
                            navController.navigate(Screen.Home.route) {
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
                        authViewModel = authViewModel,
                        googleAuthHandler = googleAuthHandler,
                        appleAuthHandler = appleAuthHandler,
                        onLoginSuccess = {
                            navController.navigate(Screen.Home.route) {
                                popUpTo(Screen.Login.route) { inclusive = true }
                            }
                        },
                        onNavigateToRegister = { navController.navigate(Screen.Register.route) },
                        onNavigateToForgotPassword = { navController.navigate(Screen.ForgotPassword.route) }
                    )
                }
                composable(Screen.Register.route) {
                    RegisterScreen(
                        authViewModel = authViewModel,
                        googleAuthHandler = googleAuthHandler,
                        appleAuthHandler = appleAuthHandler,
                        onNavigateToLogin = { navController.navigate(Screen.Login.route) }
                    )
                }
                composable(Screen.ForgotPassword.route) {
                    ForgotPasswordScreen(
                        onBackClick = { navController.navigate(Screen.Login.route) }
                    )
                }
                composable(Screen.Home.route) {
                    HomeScreen(
                        authViewModel = authViewModel,
                        dealViewModel = productViewModel,
                        activity = activity,
                        navController = navController
                    )
                }
                composable(Screen.Share.route) {
                    ShareScreen(
                        authViewModel = authViewModel,
                        productViewModel = productViewModel,
                        onNavigateBack = { navController.popBackStack() }
                    )
                }
                composable(Screen.UserInfo.route) {
                    UserInfoScreen(
                        navController = navController,
                        userInfo = UserInfo(),
                        onBackClick = { navController.popBackStack() }
                    )
                }
                composable(Screen.ViewProduct.route) { backStackEntry ->
                    val productId = backStackEntry.arguments?.getString("productId") ?: ""
                    val product = productViewModel.products.collectAsState().value.find { it.documentId == productId } // Cambiado de 'id' a 'documentId'
                    val commentViewModel: CommentViewModel = viewModel()
                    if (product != null) {
                        ViewProductScreen(productViewModel, product, commentViewModel)
                    } else {
                        Text(
                            "Product not found",
                            color = MaterialTheme.colorScheme.error,
                            modifier = Modifier.padding(16.dp)
                        )
                    }
                }
                composable(Screen.Categories.route) {
                    CategoriesScreen(
                        navController = navController,
                        dealViewModel = productViewModel
                    )
                }
                composable(
                    Screen.ViewCategory.route,
                    arguments = listOf(navArgument("categoryName") { type = NavType.StringType })
                ) { backStackEntry ->
                    val categoryName = backStackEntry.arguments?.getString("categoryName") ?: ""
                    ViewCategoryScreen(
                        navController = navController,
                        categoryName = categoryName,
                        dealViewModel = productViewModel
                    )
                }
                composable(Screen.Profile.route) {
                    Text(
                        "Profile Screen",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                composable(Screen.Deals.route) {
                    Text(
                        "Deals Screen",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                composable(Screen.Shopping.route) {
                    Text(
                        "Shopping Screen",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
                composable(Screen.Community.route) {
                    Text(
                        "Community Screen",
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(16.dp)
                    )
                }
            }
        }
    }
}