package project.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Brand Colors
val EverdealsGreen = Color(0xFF00FFA3)
val EverdealsGreenDark = Color(0xFF00CC82)
val EverdealsPurple = Color(0xFF9D4EDD)
val EverdealsYellow = Color(0xFFFFD700)
val EverdealsRed = Color(0xFFFF4444)
val EverdealsBackground = Color(0xFF1A1A1A)
val EverdealsSurface = Color(0xFF2A2A2A)

private val DarkColorScheme = darkColorScheme(
    primary = EverdealsGreen,
    primaryContainer = EverdealsGreenDark,
    secondary = EverdealsPurple,
    tertiary = EverdealsYellow,
    background = EverdealsBackground,
    surface = EverdealsSurface,
    error = EverdealsRed,
    onPrimary = Color.Black,
    onSecondary = Color.White,
    onTertiary = Color.Black,
    onBackground = Color.White,
    onSurface = Color.White,
    onError = Color.White
)

@Composable
fun EverDealsTheme(
    darkTheme: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}

