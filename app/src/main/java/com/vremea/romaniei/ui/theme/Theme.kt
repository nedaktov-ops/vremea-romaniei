package com.vremea.romaniei.ui.theme

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
    primary = Blue80,
    secondary = Amber80,
    tertiary = Green80,
    error = Red80,
    background = Grey10,
    surface = Grey20
)

private val LightColorScheme = lightColorScheme(
    primary = Blue40,
    secondary = Amber40,
    tertiary = Green40,
    error = Red40,
    background = Grey99,
    surface = Grey95
)

@Composable
fun VremeaRomanieiTheme(
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

    // Note: enableEdgeToEdge() is called in MainActivity, handling status bar appearance.
    // No need for deprecated setStatusBarColor here.

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
