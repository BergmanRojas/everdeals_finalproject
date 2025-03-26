package project.mobile.navigation

sealed class Screen(val route: String) {
    object Splash : Screen("splash")
    object Main : Screen("main")
    object ProductDetail : Screen("productDetail/{productId}") {
        fun createRoute(productId: String) = "productDetail/$productId"
    }
    object AddProduct : Screen("add_product")
    object Profile : Screen("profile/{userId}/{forcePublicView}") {
        fun createRoute(userId: String, forcePublicView: Boolean = false) = "profile/$userId/$forcePublicView"
    }
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
}