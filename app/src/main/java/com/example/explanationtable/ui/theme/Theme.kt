package com.example.explanationtable.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext

private val DarkColorScheme = darkColorScheme(
    // Easy
    primary          = EasyOptionBackgroundDark,
    onPrimary        = EasyOptionTextDark,
    primaryContainer = EasyOptionShadowDark,

    // Medium
    secondary          = MediumOptionBackgroundDark,
    onSecondary        = MediumOptionTextDark,
    secondaryContainer = MediumOptionShadowDark,

    // Hard
    tertiary          = HardOptionBackgroundDark,
    onTertiary        = HardOptionTextDark,
    tertiaryContainer = HardOptionShadowDark,

    // You can define background/surface if desired:
    background = Color(0xFF000000),
    surface    = Color(0xFF1C1B1F),

    onBackground = Color(0xFFD3D3D3),
    onSurface    = Color(0xFFD3D3D3),
)

private val LightColorScheme = lightColorScheme(
    // Easy
    primary          = EasyOptionBackgroundLight,
    onPrimary        = EasyOptionTextLight,
    primaryContainer = EasyOptionShadowLight,

    // Medium
    secondary          = MediumOptionBackgroundLight,
    onSecondary        = MediumOptionTextLight,
    secondaryContainer = MediumOptionShadowLight,

    // Hard
    tertiary          = HardOptionBackgroundLight,
    onTertiary        = HardOptionTextLight,
    tertiaryContainer = HardOptionShadowLight,

    background = Color(0xFFFFFFFF),
    surface    = Color(0xFFFFFFFF),
    onBackground = Color(0xFF2F4F4F),
    onSurface    = Color(0xFF2F4F4F),
)

@Composable
fun ExplanationTableTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // If you want to override with dynamic wallpaper-based colors
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

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        content = content
    )
}
