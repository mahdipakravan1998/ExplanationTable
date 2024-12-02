package com.example.explanationtable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.graphics.Color

// Define your custom colors
private val EasyColor = Color(0xFF4CAF50)  // Green (Easy option)
private val MediumColor = Color(0xFFFFC107) // Yellow-Orange (Medium option)
private val HardColor = Color(0xFFF44336)   // Red (Hard option)
private val BackgroundColor = Color(0xFFF5F5F5) // Light background
private val OnBackgroundColor = Color(0xFF1C1B1F) // Dark text for light background
private val OnSurfaceColor = Color(0xFF1C1B1F) // Text for dark surfaces

private val DarkColorScheme = darkColorScheme(
    primary = EasyColor, // Green (easy)
    secondary = MediumColor, // Yellow-Orange (medium)
    tertiary = HardColor, // Red (hard)
    background = Color(0xFF121212), // Dark background
    surface = Color(0xFF1C1B1F), // Dark surface
    onPrimary = Color.White,
    onSecondary = Color.White,
    onTertiary = Color.White,
    onBackground = Color.White,
    onSurface = Color.White
)

private val LightColorScheme = lightColorScheme(
    primary = EasyColor, // Green (easy)
    secondary = MediumColor, // Yellow-Orange (medium)
    tertiary = HardColor, // Red (hard)
    background = BackgroundColor, // Light background
    surface = Color.White, // White surface for light theme
    onPrimary = OnBackgroundColor, // Dark text on primary
    onSecondary = OnBackgroundColor, // Dark text on secondary
    onTertiary = OnBackgroundColor, // Dark text on tertiary
    onBackground = OnSurfaceColor, // Text color for background
    onSurface = OnSurfaceColor // Text color for surface
)

@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
) {
    // Dynamic color is available on Android 12+
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
        typography = AppTypography,
        content = content
    )
}
