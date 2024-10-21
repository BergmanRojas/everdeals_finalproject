package com.bergman.everdeals_finalproject.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    primary = Blue001875,
    secondary = RedFF0000,
    tertiary = YellowFFE100,
    background = Dark161C2A,
    surface = Dark161C2A,
    onPrimary = WhiteFFFFFF,
    onSecondary = WhiteFFFFFF,
    onTertiary = WhiteFFFFFF,
    onBackground = WhiteFFFFFF,
    onSurface = WhiteFFFFFF
)

private val LightColorScheme = lightColorScheme(
    primary = Blue001875,
    secondary = RedFF0000,
    tertiary = YellowFFE100,
    background = WhiteFFFFFF,
    surface = WhiteFFFFFF,
    onPrimary = WhiteFFFFFF,
    onSecondary = WhiteFFFFFF,
    onTertiary = WhiteFFFFFF,
    onBackground = Dark161C2A,
    onSurface = Dark161C2A
)

@Composable
fun EverdealsFinalProjectTheme(
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
