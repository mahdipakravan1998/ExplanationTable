package com.example.explanationtable.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.Background

/**
 * Central edge-to-edge scaffold.
 *
 * Responsibilities:
 * - Paints status bar area seamlessly with [topBar].
 * - Ensures content starts exactly below the *visible* top bar height.
 * - Optionally paints/extends the system bottom (navigation) area using [bottomBarAppearance].
 * - Provides floating slots that may optionally respect navigation bar insets.
 *
 * This composable is purely presentational and does not perform IO.
 *
 * @param isHomePage Whether this is the app's home surface (affects default [bottomBarAppearance]).
 * @param isDarkTheme Whether current theme is dark (used for scrim computation).
 * @param topBar Optional top bar that visually extends into the status bar area.
 * @param topBarVisibleHeight The *visible* height of [topBar] below the status bar area.
 * @param contentTopSpacing Extra spacing between the bottom of [topBar] and the content.
 * @param bottomBarAppearance How the system bottom area should be painted/handled.
 * @param floatingBottomPadding Bottom padding for floating slots (on top of nav insets if respected).
 * @param floatingStartRespectNavInsets Whether the start-floating slot should respect nav insets.
 * @param floatingEndRespectNavInsets Whether the end-floating slot should respect nav insets.
 * @param floatingCenterRespectNavInsets Whether the center-floating slot should respect nav insets.
 * @param floatingStart Optional floating content aligned to bottom-start.
 * @param floatingEnd Optional floating content aligned to bottom-end.
 * @param floatingCenter Optional floating content aligned to bottom-center.
 * @param content Page content; receives the computed [PaddingValues] so children can align with the scaffold if needed.
 */
@Composable
fun AppScreenScaffold(
    isHomePage: Boolean,
    isDarkTheme: Boolean,
    topBar: (@Composable () -> Unit)? = null,
    topBarVisibleHeight: Dp = SystemBarsDefaults.TopBarHeight,
    contentTopSpacing: Dp = 0.dp,

    bottomBarAppearance: BottomBarAppearance =
        if (isHomePage) BottomBarAppearance.None else BottomBarAppearance.Scrim,

    floatingBottomPadding: Dp = 16.dp,
    floatingStartRespectNavInsets: Boolean = true,
    floatingEndRespectNavInsets: Boolean = true,
    floatingCenterRespectNavInsets: Boolean = true,

    floatingStart: (@Composable () -> Unit)? = null,
    floatingEnd: (@Composable () -> Unit)? = null,
    floatingCenter: (@Composable () -> Unit)? = null,

    content: @Composable (PaddingValues) -> Unit
) {
    // If there is no top bar, its visible height should be 0 to avoid offsetting content.
    val barHeight: Dp = if (topBar != null) topBarVisibleHeight else 0.dp

    // Read the current top status-bar inset. This is a state-backed value (will update on inset changes).
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    // Memoize the padding object (reduces tiny allocations across recompositions).
    val contentPadding = remember(statusTop, barHeight, contentTopSpacing) {
        PaddingValues(top = statusTop + barHeight + contentTopSpacing)
    }

    Background(isHomePage = isHomePage, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top bar paints INTO the status bar area (edge-to-edge).
            topBar?.invoke()

            // Page content starts right under the *visible* bar.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) {
                content(contentPadding)
            }

            // Paint the system bottom area (nav bar) according to the requested appearance.
            BottomBarArea(
                appearance = bottomBarAppearance,
                isDarkTheme = isDarkTheme,
                modifier = Modifier.align(Alignment.BottomCenter)
            )

            // Floating slots -----------------------------------------------------

            floatingStart?.let { startContent ->
                FloatingSlotBox(
                    respectNavInsets = floatingStartRespectNavInsets,
                    bottomPadding = floatingBottomPadding,
                    startPadding = 16.dp,
                    alignment = Alignment.BottomStart
                ) { startContent() }
            }

            floatingEnd?.let { endContent ->
                FloatingSlotBox(
                    respectNavInsets = floatingEndRespectNavInsets,
                    bottomPadding = floatingBottomPadding,
                    endPadding = 16.dp,
                    alignment = Alignment.BottomEnd
                ) { endContent() }
            }

            floatingCenter?.let { centerContent ->
                FloatingSlotBox(
                    respectNavInsets = floatingCenterRespectNavInsets,
                    bottomPadding = floatingBottomPadding,
                    alignment = Alignment.BottomCenter
                ) { centerContent() }
            }
        }
    }
}

/**
 * Encapsulates bottom-area painting to keep the main layout lean and reduce repeated branching.
 * Mirrors the original behavior exactly.
 */
@Composable
private fun BoxScope.BottomBarArea(
    appearance: BottomBarAppearance,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    when (appearance) {
        BottomBarAppearance.None -> Unit

        BottomBarAppearance.Scrim -> {
            BottomNavAreaScrim(
                isDarkTheme = isDarkTheme,
                modifier = modifier
            )
        }

        is BottomBarAppearance.Solid -> {
            // Keep identical structure: optional extra padding, then the nav-bar height filler.
            androidx.compose.foundation.layout.Column(
                modifier = modifier.fillMaxSize().then(Modifier) // keep alignment via the parent 'modifier'
            ) {
                if (appearance.extraPadding > 0.dp) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .height(appearance.extraPadding)
                            .background(appearance.color)
                    )
                }
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .windowInsetsBottomHeight(WindowInsets.navigationBars)
                        .background(appearance.color)
                )
            }
        }
    }
}

/**
 * Floating slot helper. Centralizes:
 *  - Optional nav-bar inset padding application
 *  - Standard exterior padding
 *  - Alignment within the parent Box
 *
 * Behavior exactly matches the original: same paddings and alignment.
 */
@Composable
private fun BoxScope.FloatingSlotBox(
    respectNavInsets: Boolean,
    bottomPadding: Dp,
    startPadding: Dp = 0.dp,
    endPadding: Dp = 0.dp,
    alignment: Alignment,
    content: @Composable () -> Unit
) {
    var m: Modifier = Modifier.align(alignment)
    if (respectNavInsets) {
        m = m.windowInsetsPadding(WindowInsets.navigationBars)
    }
    m = m.padding(start = startPadding, end = endPadding, bottom = bottomPadding)
    Box(modifier = m) { content() }
}
