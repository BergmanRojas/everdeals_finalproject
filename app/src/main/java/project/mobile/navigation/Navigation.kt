package project.mobile.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import project.mobile.controller.ProductViewModel
import project.mobile.controller.AuthViewModel
import project.mobile.view.ProductDetailScreen
import project.mobile.view.MainScreenContent
import project.mobile.view.SplashScreen

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
}

@Composable
fun Navigation(
    navController: NavHostController,
    productViewModel: ProductViewModel,
    authViewModel: AuthViewModel
) {
    NavHost(
        navController = navController,
        startDestination = Screen.Splash.route
    ) {
        composable(Screen.Splash.route) {
            SplashScreen(navController = navController)
        }

        composable(Screen.Main.route) {
            MainScreenContent(
                onAddProductClick = { /* TODO */ },
                onProfileClick = { /* TODO */ },
                productViewModel = productViewModel,
                navController = navController
            )
        }

        composable(
            route = Screen.ProductDetail.route,
            arguments = listOf(
                navArgument("productId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val productId = backStackEntry.arguments?.getString("productId") ?: return@composable
            val product = productViewModel.getProductById(productId)
            val comments by productViewModel.getCommentsForProduct(productId).collectAsState()

            if (product != null) {
                ProductDetailScreen(
                    product = product,
                    comments = comments,
                    onAddComment = { text -> 
                        productViewModel.addComment(productId, text)
                    },
                    onReplyToComment = { commentId, text ->
                        productViewModel.addReply(productId, commentId, text)
                    },
                    onBack = { navController.popBackStack() },
                    onShare = {
                        // TODO: Implement share functionality
                    },
                    onLikeDislike = { id, isLike ->
                        productViewModel.toggleLikeDislike(id, isLike)
                    },
                    viewModel = productViewModel
                )
            }
        }
    }
} 