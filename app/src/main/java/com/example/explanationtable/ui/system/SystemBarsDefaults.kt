package com.example.explanationtable.ui.system

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.DarkGreenBackground
import com.example.explanationtable.ui.theme.SeaSponge

/**
 * Single source of truth for system-bar related “chrome” sizing and colors.
 *
 * Public APIs are stable and side-effect free. The object is annotated as [Immutable]
 * so Compose can treat it as a stable node and avoid unnecessary invalidations.
 */
@Immutable
object SystemBarsDefaults {

    /** Visible height of the app's top bar area. */
    val TopBarHeight: Dp = 72.dp

    /**
     * Prize overlay color used to paint the system bottom area during review.
     *
     * This Composable is intentionally marked [ReadOnlyComposable] to indicate it has no
     * side effects and does not read Composition locals; it simply delegates to a pure
     * function so callers can keep using it from Compose code while tests and non-Compose
     * code can use the pure version directly.
     *
     * @param isDarkTheme Whether the app is currently using a dark theme.
     * @return The overlay [Color] appropriate for the current theme.
     */
    @Composable
    @ReadOnlyComposable
    fun prizeOverlayColor(isDarkTheme: Boolean): Color =
        computePrizeOverlayColor(isDarkTheme)

    /**
     * Pure (non-Composable) computation of the prize overlay color.
     *
     * Kept `internal` to preserve the public surface while enabling straightforward
     * unit testing without Compose runtime. No Composition locals are read here.
     */
    internal fun computePrizeOverlayColor(isDarkTheme: Boolean): Color {
        // Branch is trivial and predictable; no allocations beyond returning a constant Color.
        return if (isDarkTheme) DarkGreenBackground else SeaSponge
    }
}
