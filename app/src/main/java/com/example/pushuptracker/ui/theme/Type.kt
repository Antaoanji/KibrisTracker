package com.example.pushuptracker.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.googlefonts.Font
import androidx.compose.ui.text.googlefonts.GoogleFont
import androidx.compose.ui.unit.sp
import com.example.pushuptracker.R

// Initialize the font provider
private val fontProvider = GoogleFont.Provider(
    providerAuthority = "com.google.android.gms.fonts",
    providerPackage = "com.google.android.gms",
    certificates = 0
)

// Define the Inter font family
private val interFontFamily = FontFamily(
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Normal),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Medium),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.SemiBold),
    Font(googleFont = GoogleFont("Inter"), fontProvider = fontProvider, weight = FontWeight.Bold)
)

// Define the new Typography
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 57.sp
    ),
    displayMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 45.sp
    ),
    displaySmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    headlineLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 32.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 28.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp
    ),
    titleLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 22.sp
    ),
    titleMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp
    ),
    titleSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    bodyLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    bodySmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp
    ),
    labelLarge = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 14.sp
    ),
    labelMedium = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 12.sp
    ),
    labelSmall = TextStyle(
        fontFamily = interFontFamily,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp
    )
)
