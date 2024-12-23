package com.example.explanationtable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

/**
 * Define Dark and Light Color Schemes with clearly different values
 * so that changing themes is visually obvious.
 */
private val DarkColorScheme = darkColorScheme(
    // Easy
    primary = EasyOptionBackgroundDark,
    onPrimary = EasyOptionTextDark,
    primaryContainer = EasyOptionShadowDark,

    // Medium
    secondary = MediumOptionBackgroundDark,
    onSecondary = MediumOptionTextDark,
    secondaryContainer = MediumOptionShadowDark,

    // Hard
    tertiary = HardOptionBackgroundDark,
    onTertiary = HardOptionTextDark,
    tertiaryContainer = HardOptionShadowDark,

    // Background and Surface
    background = NonHomeBackgroundDark,
    surface = NonHomeBackgroundDark,

    onBackground = ColorPrimaryTextDark,
    onSurface = ColorPrimaryTextDark,
)

private val LightColorScheme = lightColorScheme(
    // Easy
    primary = EasyOptionBackgroundLight,
    onPrimary = EasyOptionTextLight,
    primaryContainer = EasyOptionShadowLight,

    // Medium
    secondary = MediumOptionBackgroundLight,
    onSecondary = MediumOptionTextLight,
    secondaryContainer = MediumOptionShadowLight,

    // Hard
    tertiary = HardOptionBackgroundLight,
    onTertiary = HardOptionTextLight,
    tertiaryContainer = HardOptionShadowLight,

    // Background and Surface
    background = NonHomeBackgroundLight,
    surface = NonHomeBackgroundLight,

    onBackground = ColorPrimaryTextLight,
    onSurface = ColorPrimaryTextLight,
)

@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        // If you want to use the system's dynamic color (Android 12+)
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }
        // Otherwise, use our own custom color schemes
        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography, // Make sure AppTypography is defined or use MaterialTheme.typography
        content = content
    )
}
