package com.example.explanationtable.ui.main.components

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Stable spacing defaults and height-to-lift mapping used by [MainContent].
 *
 * IMPORTANT: Values mirror the original implementation exactly to preserve layout.
 */
object MainContentDefaults {
    /** Horizontal side padding (stable gutter). */
    val SidePadding: Dp = 24.dp

    /** Vertical space between the two buttons. */
    val BetweenButtons: Dp = 16.dp

    /** Stable bottom padding; NOT driven by transient system bar insets. */
    val BaseBottomPadding: Dp = 16.dp

    /**
     * Maps screen height (in dp) to a small "lift" so buttons aren't on the very edge.
     * Thresholds and outputs are identical to the original code.
     */
    fun liftForHeight(screenHeightDp: Int): Dp = when {
        screenHeightDp <= 640 -> 48.dp
        screenHeightDp <= 760 -> 56.dp
        screenHeightDp <= 880 -> 64.dp
        else -> 72.dp
    }
}
