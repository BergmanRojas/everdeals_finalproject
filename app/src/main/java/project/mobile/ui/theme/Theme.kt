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

// Colores específicos para el gradiente y diseño
val EverdealsYellow = Color(0xFFFFD700) // Amarillo para el gradiente
val EverdealsRed = Color(0xFFFF4444)    // Rojo para el gradiente y detalles
val CircleBackground = Color(0xFF333333).copy(alpha = 0.7f) // Gris oscuro para el círculo
val BottomNavBackground = Color.White
val BottomNavSelected = Color(0xFF40C4FF)   // Azul para íconos seleccionados
val BottomNavIndicator = Color(0xFFE1F5FE)   // Fondo de ítem seleccionado
val BottomNavBorder = Color.LightGray
val BottomNavDivider = Color.DarkGray
val EverdealsBackground = Color(0xFF1A1A1A) // Fondo heredado
val EverdealsSurface = Color(0xFF2A2A2A)    // Superficie heredada

private val DarkColorScheme = darkColorScheme(
    primary = OrangeFF6200,
    secondary = Blue001875,
    background = Dark161C2A,
    surface = Color(0xFF2A2E38),
    surfaceVariant = Color(0xFF3A3F4A),
    onPrimary = WhiteFFFFFF,
    onSecondary = WhiteFFFFFF,
    onBackground = WhiteFFFFFF, // Blanco en modo oscuro
    onSurface = WhiteFFFFFF,    // Blanco en modo oscuro
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
    darkTheme: Boolean = isSystemInDarkTheme(),
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
            window.statusBarColor = colorScheme.background.toArgb()
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}