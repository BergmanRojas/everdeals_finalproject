    package project.mobile

    import android.os.Bundle
    import androidx.activity.ComponentActivity
    import androidx.activity.compose.setContent
    import project.mobile.ui.theme.EverDealsTheme
    import project.mobile.navigation.AppNavigation

    class MainActivity : ComponentActivity() {
        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContent {
                EverDealsTheme {
                    AppNavigation()
                }
            }
        }
    }

