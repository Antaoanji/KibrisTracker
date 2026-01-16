package com.example.pushuptracker.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val AppDarkColorScheme = darkColorScheme(
    primary = ElectricTealPrimary,
    onPrimary = Black,
    secondary = DeepSpaceSecondaryText,
    onSecondary = White,
    tertiary = VibrantMagentaAccent,
    onTertiary = Black,
    background = DeepSpaceBackground,
    onBackground = DeepSpaceText,
    surface = DeepSpaceSurface,
    onSurface = DeepSpaceText
)

@Composable
fun PushupTrackerTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = AppDarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            @Suppress("DEPRECATION")
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
