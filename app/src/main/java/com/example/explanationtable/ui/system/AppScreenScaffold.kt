package com.example.explanationtable.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.Background

/**
 * Central edge-to-edge scaffold:
 *  • Paints the status-bar area seamlessly with your [topBar].
 *  • Offsets page content to start exactly below the visible bar height.
 *  • Optionally paints the **system bottom area** (nav bar) according to [bottomBarAppearance].
 *  • Provides floating slots that may (or may not) respect nav insets.
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
    val barHeight: Dp = if (topBar != null) topBarVisibleHeight else 0.dp
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val contentPadding = PaddingValues(top = statusTop + barHeight + contentTopSpacing)

    Background(isHomePage = isHomePage, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            // Top bar paints into status area itself.
            topBar?.invoke()

            // Page content starts right under the visible bar.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(contentPadding)
            ) { content(contentPadding) }

            // Paint the nav-bar area.
            when (bottomBarAppearance) {
                BottomBarAppearance.None -> Unit
                BottomBarAppearance.Scrim -> {
                    BottomNavAreaScrim(
                        isDarkTheme = isDarkTheme,
                        modifier = Modifier.align(Alignment.BottomCenter)
                    )
                }
                is BottomBarAppearance.Solid -> {
                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                    ) {
                        if (bottomBarAppearance.extraPadding > 0.dp) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(bottomBarAppearance.extraPadding)
                                    .background(bottomBarAppearance.color)
                            )
                        }
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .windowInsetsBottomHeight(WindowInsets.navigationBars)
                                .background(bottomBarAppearance.color)
                        )
                    }
                }
            }

            // Floating slots
            floatingStart?.let {
                var m = Modifier.align(Alignment.BottomStart)
                if (floatingStartRespectNavInsets) m = m.windowInsetsPadding(WindowInsets.navigationBars)
                m = m.padding(start = 16.dp, bottom = floatingBottomPadding)
                Box(modifier = m) { it() }
            }
            floatingEnd?.let {
                var m = Modifier.align(Alignment.BottomEnd)
                if (floatingEndRespectNavInsets) m = m.windowInsetsPadding(WindowInsets.navigationBars)
                m = m.padding(end = 16.dp, bottom = floatingBottomPadding)
                Box(modifier = m) { it() }
            }
            floatingCenter?.let {
                var m = Modifier.align(Alignment.BottomCenter)
                if (floatingCenterRespectNavInsets) m = m.windowInsetsPadding(WindowInsets.navigationBars)
                m = m.padding(bottom = floatingBottomPadding)
                Box(modifier = m) { it() }
            }
        }
    }
}
