package com.example.explanationtable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Custom Colors class to hold additional color roles
 */
@Immutable
data class CustomColors(
    val success: Color,
    val accent: Color,
    val highlight: Color,
    val overlay: Color
)

val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

/**
 * Define Dark and Light Color Schemes with clearly different values
 * so that changing themes is visually obvious.
 */
private val DarkColors = darkColorScheme(
    // Primary (Easy)
    primary = PrimaryDark,
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,

    // Secondary (Medium)
    secondary = SecondaryDark,
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,

    // Tertiary (Hard)
    tertiary = TertiaryDark,
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,

    // Background and Surface
    background = BackgroundDark,
    surface = SurfaceDark,

    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,

    // Error Colors
    error = ErrorDark,
    onError = OnErrorDark
)

private val LightColors = lightColorScheme(
    // Primary (Easy)
    primary = PrimaryLight,
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,

    // Secondary (Medium)
    secondary = SecondaryLight,
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,

    // Tertiary (Hard)
    tertiary = TertiaryLight,
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,

    // Background and Surface
    background = BackgroundLight,
    surface = SurfaceLight,

    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,

    // Error Colors
    error = ErrorLight,
    onError = OnErrorLight
)

@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Ensure dynamicColor is disabled
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // If you want to use the system's dynamic color (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Otherwise, use our own custom color schemes
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Define your custom colors based on the theme
    val customColors = if (darkTheme) {
        CustomColors(
            success = SuccessDark,
            accent = AccentDark,
            highlight = HighlightDark,
            overlay = OverlayDark
        )
    } else {
        CustomColors(
            success = SuccessLight,
            accent = AccentLight,
            highlight = HighlightLight,
            overlay = OverlayLight
        )
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Ensure AppTypography is defined or replace with your typography
        content = {
            // Provide the custom colors to the composition
            CompositionLocalProvider(LocalCustomColors provides customColors) {
                content()
            }
        }
    )
}
