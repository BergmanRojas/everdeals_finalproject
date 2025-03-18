package project.mobile.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
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
    secondary = OrangeFF6200,
    tertiary = OrangeFF6200,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

private val LightColorScheme = lightColorScheme(
    primary = OrangeFF6200,
    secondary = OrangeFF6200,
    tertiary = OrangeFF6200,
    background = Color(0xFF1A1A1A),
    surface = Color(0xFF2A2A2A),
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White,
)

@Composable
fun EverDealsTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.primary.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}