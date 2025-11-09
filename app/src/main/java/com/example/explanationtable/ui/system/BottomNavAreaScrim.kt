package com.example.explanationtable.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

private const val DARK_END_ALPHA = 0.18f
private const val LIGHT_END_ALPHA = 0.12f

// Precomputed to avoid repeating Color.copy(0f) across theme flips.
private val TransparentBlack = Color.Black.copy(alpha = 0f)

/**
 * Draws a vertical gradient scrim that exactly covers the system navigation bar height.
 *
 * The gradient fades from transparent black (top) to a subtle black at the bottom.
 * End alpha is slightly stronger in dark theme for comparable perceived depth.
 *
 * Performance:
 * - The gradient [Brush] is memoized via [remember] to avoid per-frame allocations.
 * - Only recomputes when [isDarkTheme] changes.
 *
 * Visual output and API are intentionally identical to the original implementation.
 *
 * @param isDarkTheme Whether the current theme is dark. Controls the end alpha intensity.
 * @param modifier Optional [Modifier] for width/placement adjustments. The height is derived
 * from [WindowInsets.navigationBars] so that it always matches the bottom system bar area.
 */
@Composable
fun BottomNavAreaScrim(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    // Memoized gradient; recalculated only if the theme flips.
    val brush = rememberBottomNavScrimBrush(isDarkTheme)

    Box(
        modifier = modifier
            .fillMaxWidth() // Expand to the full width of the window
            .windowInsetsBottomHeight(WindowInsets.navigationBars) // Match nav-bar height precisely
            .background(brush) // Paint the gradient behind the area
    )
}

/**
 * Produces a memoized vertical gradient [Brush] for the bottom nav scrim.
 * The end alpha is selected based on [isDarkTheme].
 */
@Composable
private fun rememberBottomNavScrimBrush(isDarkTheme: Boolean): Brush {
    val endAlpha = if (isDarkTheme) DARK_END_ALPHA else LIGHT_END_ALPHA
    return remember(isDarkTheme) {
        Brush.verticalGradient(
            colors = listOf(
                TransparentBlack,
                Color.Black.copy(alpha = endAlpha)
            )
        )
    }
}
