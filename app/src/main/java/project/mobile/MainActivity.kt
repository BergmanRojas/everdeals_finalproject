package project.mobile

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.compose.rememberNavController
import project.mobile.ui.theme.EverDealsTheme
import project.mobile.view.navigations.EverDealsNavigation

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            EverDealsTheme {
                EverDealsNavigation(
                    navController = rememberNavController(),
                    activity = this // Usamos 'this' directamente, refiriéndose a MainActivity
                )
            }
        }
    }
}