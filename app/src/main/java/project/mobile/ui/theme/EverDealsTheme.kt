package project.mobile.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

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
    darkTheme: Boolean = false, // Cambiado a false para usar tema claro por defecto
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}