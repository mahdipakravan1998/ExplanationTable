package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.navigation.AppNavHost
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.system.AppImmersiveSystemBars
import com.example.explanationtable.ui.theme.ExplanationTableTheme

/**
 * App entry point hosting the Compose hierarchy.
 *
 * Responsibilities:
 * - Applies system UI policy (immersive mode).
 * - Provides the app theme based on [MainViewModel.isDarkTheme].
 * - Forces Left-To-Right layout direction at the root to preserve current visuals.
 *
 * Notes:
 * - Behavior and visuals are preserved as-is.
 * - We explicitly set an initial value for theme collection to guard against
 *   future changes in the upstream flow type.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            // App-wide: hide status & nav bars, consume insets everywhere.
            // This composable is assumed to install side-effects safely (e.g., System UI controller).
            AppImmersiveSystemBars()

            val mainViewModel: MainViewModel = viewModel()

            // Explicit initial avoids surprises if isDarkTheme stops being a StateFlow<Boolean>.
            // If it remains a StateFlow, this is still a no-op behaviorally.
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState(initial = false)

            ExplanationTableTheme(darkTheme = isDarkTheme) {
                // Enforce LTR to preserve current UI output, regardless of device language.
                // (See §6 for i18n/a11y considerations.)
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AppNavHost(isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}
