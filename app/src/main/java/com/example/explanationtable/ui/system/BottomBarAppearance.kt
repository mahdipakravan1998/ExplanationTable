package com.example.explanationtable.ui.system

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Declarative description of **what the system navigation/bottom area should look like** for a screen.
 *
 * This contract is intentionally small and immutable to keep Compose recomposition predictable.
 * Typical usage:
 *
 * - The ViewModel exposes a `StateFlow<BottomBarAppearance>`.
 * - The UI reads that state and decides whether to draw an overlay/scrim and how much extra
 *   top padding to leave above the system nav bar cutout if needed.
 *
 * Prefer using the extension properties [hasOverlay] and [topPadding] to avoid repeating
 * `when` branches in call sites.
 */
@Immutable
sealed class BottomBarAppearance {

    /**
     * Don’t draw anything over the nav-bar area.
     * System navigation remains fully visible with no additional UI chrome.
     */
    @Immutable
    object None : BottomBarAppearance()

    /**
     * Draw a subtle gradient scrim over the nav-bar area.
     * This is purely a visual treatment (no extra padding implied).
     */
    @Immutable
    object Scrim : BottomBarAppearance()

    /**
     * Fill the nav-bar area with a solid [color].
     *
     * @param color The solid color used to cover the nav-bar area.
     * @param extraPadding Optional additional padding **above** the covered area so content
     * can rest visually on the filled bar without colliding with system navigation. `0.dp` by default.
     */
    @Immutable
    data class Solid(
        val color: Color,
        val extraPadding: Dp = 0.dp
    ) : BottomBarAppearance()
}

/**
 * Whether this appearance draws any overlay (scrim or solid) over the system nav-bar region.
 *
 * Using this avoids duplicating `when` expressions throughout the UI hierarchy and helps keep
 * recompose-triggering logic consistent.
 */
val BottomBarAppearance.hasOverlay: Boolean
    get() = when (this) {
        BottomBarAppearance.None -> false
        BottomBarAppearance.Scrim -> true
        is BottomBarAppearance.Solid -> true
    }

/**
 * The additional top padding implied by this appearance when laying out content above the nav bar.
 *
 * - `Solid` returns its [Solid.extraPadding].
 * - `None` and `Scrim` do not imply extra padding and return `0.dp`.
 */
val BottomBarAppearance.topPadding: Dp
    get() = when (this) {
        is BottomBarAppearance.Solid -> this.extraPadding
        BottomBarAppearance.None,
        BottomBarAppearance.Scrim -> 0.dp
    }
