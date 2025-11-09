package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.AppNavHost
import com.example.explanationtable.ui.system.AppEdgeToEdgeSystemBars
import com.example.explanationtable.ui.theme.ExplanationTableTheme

/**
 * Application root Activity.
 *
 * Responsibilities:
 * - Hosts the Compose UI via setContent.
 * - Collects theme state in a lifecycle-aware way (prevents stale values across lifecycle transitions).
 * - Delegates actual UI tree (theme, system bars, LTR, navigation) to [ExplanationTableRoot].
 *
 * NOTE: Visual and behavioral parity is a hard requirement. This refactor only improves readability and
 * lifecycle semantics; it does not alter edge-to-edge behavior, theming, or navigation.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = viewModel()

            // Lifecycle-aware collection with a delegated state for clarity and safety.
            // Initial value preserves the existing startup appearance.
            val isDarkTheme by mainViewModel.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = false)

            ExplanationTableRoot(isDarkTheme = isDarkTheme)
        }
    }
}

/**
 * Top-level UI tree for the app.
 *
 * Order matters:
 * 1) Theme applies first so anything reading colors/typography gets the right values.
 * 2) Edge-to-edge system bars are applied once, scoped to the composition; they must remain
 *    synchronized with theme (e.g., icon contrast).
 * 3) Use an explicit LTR layout direction to preserve current visuals (if your app later
 *    adopts RTL, change this intentionally at one place).
 * 4) Render the NavHost last to keep the root clean and testable.
 */
@Composable
private fun ExplanationTableRoot(isDarkTheme: Boolean) {
    ExplanationTableTheme(darkTheme = isDarkTheme) {
        // Transparent, visible system bars; content handles insets.
        // Keep this as a composable to remain responsive to theme changes (icon contrast).
        AppEdgeToEdgeSystemBars(isDarkTheme = isDarkTheme)

        // Preserve current LTR visuals across the app.
        CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
            AppNavHost(isDarkTheme = isDarkTheme)
        }
    }
}
