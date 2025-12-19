package com.example.pushuptracker.ui.theme

import android.app.Activity
import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// Sunny Beach Day - Light Theme
private val LightColorScheme = lightColorScheme(
    primary = CharcoalBlue,      // Main interactive color (Top bars, buttons)
    onPrimary = White,           // Text on primary
    secondary = Verdigris,       // Secondary actions, highlights, chart lines
    onSecondary = White,         // Text on secondary
    tertiary = BurntPeach,       // Accent for FABs, important calls to action
    onTertiary = White,          // Text on tertiary
    background = PapayaWhip,     // App background
    onBackground = CharcoalBlue, // Main text color
    surface = PapayaWhip,        // Card backgrounds
    onSurface = CharcoalBlue,    // Text on cards
    error = BurntPeach,          // Error/Warning color
    onError = White
)

// Sunny Beach Day - Dark Theme (A variation for contrast)
private val DarkColorScheme = darkColorScheme(
    primary = SandyBrown,        // A lighter, warm primary for dark mode
    onPrimary = CharcoalBlue,    // High contrast text on primary
    secondary = Verdigris,       // Keep this vibrant for accents
    onSecondary = White,
    tertiary = BurntPeach,       // The most vibrant accent remains
    onTertiary = White,
    background = CharcoalBlue,   // Deep blue background
    onBackground = PapayaWhip,   // Light, warm text on the dark background
    surface = CharcoalBlue,      // Surfaces are same as background
    onSurface = PapayaWhip,      // Text on surfaces
    error = BurntPeach,
    onError = White
)

@Composable
fun PushupTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Dynamic color is available on Android 12+
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
            window.statusBarColor = Color.Transparent.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
