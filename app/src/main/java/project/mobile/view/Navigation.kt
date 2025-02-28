package project.mobile.view

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import project.mobile.controller.*
import project.mobile.models.AuthState

sealed class Screen(val route: String) {
    object Login : Screen("login")
    object Register : Screen("register")
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
}

@Composable
fun EverDealsNavigation(navController: NavHostController, activity: ComponentActivity) {
    val userSessionManager: UserSessionManager = viewModel()
    val isLoggedIn by userSessionManager.authState.collectAsState()

    Scaffold(
        bottomBar = { BottomNavigationBar(navController, currentRoute = navController.currentDestination?.route ?: "home") }
    ) { paddingValues ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn is AuthState.Success) "home" else "login",
            modifier = Modifier.padding(paddingValues)
        ) {
            composable("login") {
                val googleAuthHandler = GoogleAuthHandler(activity)
                LoginScreen(
                    sessionManager = userSessionManager,
                    googleAuthHandler = googleAuthHandler,
                    onLoginSuccess = { navController.navigate("home") { popUpTo("login") { inclusive = true } } },
                    onNavigateToRegister = { navController.navigate("register") }
                )
            }

            composable("register") {
                RegisterScreen(
                    sessionManager = userSessionManager,
                    onNavigateToLogin = { navController.navigate("login") }
                )
            }

            composable("home") {
                val dealViewModel: DealViewModel = viewModel()
                HomeScreen(dealViewModel = dealViewModel, navController = navController)
            }

            composable("share") {
                ShareScreen()
            }

            composable("profile") {
                UserInfoScreen()
            }

            composable("deals") {
                DealsScreen()
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewEverDealsNavigation() {
    val fakeNavController = rememberNavController()
    EverDealsNavigation(navController = fakeNavController, activity = androidx.compose.ui.platform.LocalContext.current as ComponentActivity)
}

