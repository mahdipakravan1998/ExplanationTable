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

@Immutable
data class CustomColors(
    val success: Color,
    val accent: Color,
    val highlight: Color,
    val overlay: Color,
    // Add more custom colors if needed
)

val LocalCustomColors = staticCompositionLocalOf<CustomColors> {
    error("No CustomColors provided")
}

// Define your color schemes using the new palettes
private val DarkColors = darkColorScheme(
    primary = PrimaryDark, // From Core Brand Colors
    onPrimary = OnPrimaryDark,
    primaryContainer = PrimaryContainerDark,

    secondary = SecondaryDark, // From Secondary Palette
    onSecondary = OnSecondaryDark,
    secondaryContainer = SecondaryContainerDark,

    tertiary = TertiaryDark, // From Duo's Palette or others
    onTertiary = OnTertiaryDark,
    tertiaryContainer = TertiaryContainerDark,

    background = BackgroundDark,
    surface = SurfaceDark,
    onBackground = OnBackgroundDark,
    onSurface = OnSurfaceDark,
    error = ErrorDark,
    onError = OnErrorDark
)

private val LightColors = lightColorScheme(
    primary = PrimaryLight, // From Core Brand Colors
    onPrimary = OnPrimaryLight,
    primaryContainer = PrimaryContainerLight,

    secondary = SecondaryLight, // From Secondary Palette
    onSecondary = OnSecondaryLight,
    secondaryContainer = SecondaryContainerLight,

    tertiary = TertiaryLight, // From Duo's Palette or others
    onTertiary = OnTertiaryLight,
    tertiaryContainer = TertiaryContainerLight,

    background = BackgroundLight,
    surface = SurfaceLight,
    onBackground = OnBackgroundLight,
    onSurface = OnSurfaceLight,
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
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        darkTheme -> DarkColors
        else -> LightColors
    }

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
            CompositionLocalProvider(LocalCustomColors provides customColors) {
                content()
            }
        }
    )
}
