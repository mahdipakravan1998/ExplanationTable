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
 * Data class holding custom color definitions.
 * Extend this class to add any additional custom colors as needed.
 */
@Immutable
data class CustomColors(
    val success: Color,
    val accent: Color,
    val highlight: Color,
    val overlay: Color,
)

/**
 * CompositionLocal for providing [CustomColors] throughout the composable hierarchy.
 * Throws an error if no [CustomColors] is provided.
 */
val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

/**
 * Dark theme color scheme using Material3's [darkColorScheme].
 * Replace color variables (e.g. PrimaryDark) with your actual color definitions.
 */
private val DarkColors = darkColorScheme(
    primary = PrimaryDark,               // Core Brand Color
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = SecondaryDark,           // Secondary Palette
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    tertiary = TertiaryDark,             // Duo's Palette or similar
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,
    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)

/**
 * Light theme color scheme using Material3's [lightColorScheme].
 * Replace color variables (e.g. PrimaryLight) with your actual color definitions.
 */
private val LightColors = lightColorScheme(
    primary = PrimaryLight,              // Core Brand Color
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    secondary = SecondaryLight,          // Secondary Palette
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    tertiary = TertiaryLight,            // Duo's Palette or similar
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,
    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
    error = ErrorLight,
    onError = OnErrorLight
)

/**
 * Applies the ExplanationTable theme to the provided content.
 *
 * This composable sets up the color scheme (with optional dynamic coloring), typography,
 * and custom colors using a [CompositionLocalProvider].
 *
 * @param darkTheme Flag indicating whether to use the dark theme. Defaults to the system setting.
 * @param dynamicColor Flag to enable dynamic colors (supported on Android S and above).
 *                     This is disabled by default.
 * @param content The composable content to which the theme will be applied.
 */
@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Disabled by default; enable if supported and desired.
    content: @Composable () -> Unit
) {
    // Determine the appropriate color scheme based on the dynamicColor flag and dark theme setting.
    val colorScheme = when {
        // Use dynamic color if enabled and the device runs on Android S (API 31) or later.
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Define custom colors based on the current theme (dark or light).
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

    // Apply the MaterialTheme with the selected color scheme, typography, and custom colors.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Ensure AppTypography is defined or replace with your typography.
        content = {
            // Provide custom colors to the composable hierarchy.
            CompositionLocalProvider(LocalCustomColors provides customColors) {
                content()
            }
        }
    )
}
