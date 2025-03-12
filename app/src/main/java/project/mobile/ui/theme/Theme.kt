package project.mobile.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Colores heredados para compatibilidad (si aún los necesitas en otras partes)
val EverdealsGreen = Color(0xFF00FFA3)
val EverdealsGreenDark = Color(0xFF00CC82)
val EverdealsPurple = Color(0xFF9D4EDD)
val EverdealsYellow = Color(0xFFFFD700)
val EverdealsRed = Color(0xFFFF4444)
val EverdealsBackground = Color(0xFF1A1A1A)
val EverdealsSurface = Color(0xFF2A2A2A)

private val DarkColorScheme = darkColorScheme(
    primary = Blue001875,
    secondary = OrangeFF6200,
    background = Dark161C2A,
    surface = Color(0xFF2A2E38),
    surfaceVariant = Color(0xFF3A3F4A),
    onPrimary = WhiteFFFFFF,
    onSecondary = WhiteFFFFFF,
    onBackground = WhiteFFFFFF,
    onSurface = WhiteFFFFFF,
    onSurfaceVariant = Color(0xFFD1D5DB),
    error = RedFF0000
)

private val LightColorScheme = lightColorScheme(
    primary = Blue001875,
    secondary = OrangeFF6200,
    background = WhiteFFFFFF,
    surface = Color(0xFFF5F5F5),
    surfaceVariant = Color(0xFFE5E7EB),
    onPrimary = WhiteFFFFFF,
    onSecondary = WhiteFFFFFF,
    onBackground = Black,
    onSurface = Black,
    onSurfaceVariant = Color(0xFF4B5563),
    error = RedFF0000
)

@Composable
fun EverDealsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(), // Usar el tema del sistema por defecto
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.setDecorFitsSystemWindows(window, false)
            val insetsController = WindowCompat.getInsetsController(window, view)
            insetsController.isAppearanceLightStatusBars = !darkTheme
            window.statusBarColor = colorScheme.background.toArgb() // Usado como fallback
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}