package project.mobile.navigation

sealed class Screen(val route: String) {
    object Home : Screen("home")
    object MyAlerts : Screen("my_alerts")
    object Splash : Screen("splash")
    object Main : Screen("main")
    object ProductDetail : Screen("product_detail/{productId}") {
        fun createRoute(productId: String) = "product_detail/$productId"
    }
    object AddProduct : Screen("add_product")
    object Profile : Screen("profile/{userId}/{isOwnProfile}") {
        fun createRoute(userId: String, isOwnProfile: Boolean) = "profile/$userId/$isOwnProfile"
    }
    object Settings : Screen("settings")
    object Login : Screen("login")
    object Register : Screen("register")
    object ForgotPassword : Screen("forgot_password")
    object Affiliate : Screen("affiliate")
    object Forum : Screen("forum") // Agregado
    object Messages : Screen("messages") // Agregado
    object Chat : Screen("chat/{targetUserId}/{targetUserName}") {
        fun createRoute(targetUserId: String, targetUserName: String) = "chat/$targetUserId/$targetUserName"
    }
    object NewChat : Screen("new_chat")// Agregado
}