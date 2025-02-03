package com.example.explanationtable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Immutable data class that holds custom color definitions.
 * Extend this class to include additional custom colors if needed.
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
 * An error is thrown if no [CustomColors] is provided.
 */
val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

/**
 * Pre-defined dark theme color scheme using Material3's [darkColorScheme].
 * Replace the color variables (e.g. PrimaryDark) with your actual color definitions.
 */
private val DarkColors = darkColorScheme(
    primary = PrimaryDark,               // Core Brand Color
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,
    secondary = SecondaryDark,           // Secondary Palette
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,
    tertiary = TertiaryDark,             // Additional Palette (e.g. Duo's Palette)
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
 * Pre-defined light theme color scheme using Material3's [lightColorScheme].
 * Replace the color variables (e.g. PrimaryLight) with your actual color definitions.
 */
private val LightColors = lightColorScheme(
    primary = PrimaryLight,              // Core Brand Color
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,
    secondary = SecondaryLight,          // Secondary Palette
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,
    tertiary = TertiaryLight,            // Additional Palette (e.g. Duo's Palette)
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
 * Applies the ExplanationTable theme to the provided [content].
 *
 * This composable sets up the MaterialTheme with an appropriate color scheme (optionally using dynamic colors),
 * typography, and custom colors, which are provided throughout the composable hierarchy via [LocalCustomColors].
 *
 * @param darkTheme Flag indicating whether to use the dark theme. Defaults to the system setting.
 * @param dynamicColor Flag to enable dynamic colors (supported on Android S and above). Disabled by default.
 * @param content The composable content that will be themed.
 */
@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    // Determine the appropriate color scheme:
    // Use dynamic colors if enabled and the device runs on Android S (API 31) or later.
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Use pre-defined dark/light color schemes based on the darkTheme flag.
        darkTheme -> DarkColors
        else -> LightColors
    }

    // Define custom colors based on the current theme.
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

    // Apply MaterialTheme with the selected color scheme and typography,
    // and provide the custom colors to the composable hierarchy.
    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography // Ensure AppTypography is defined elsewhere.
    ) {
        CompositionLocalProvider(LocalCustomColors provides customColors) {
            content()
        }
    }
}
