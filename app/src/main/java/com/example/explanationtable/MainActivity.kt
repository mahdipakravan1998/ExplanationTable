package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.AppNavHost
import com.example.explanationtable.ui.sfx.LocalUiSoundManager
import com.example.explanationtable.ui.sfx.UiSoundManager
import com.example.explanationtable.ui.system.AppEdgeToEdgeSystemBars
import com.example.explanationtable.ui.theme.ExplanationTableTheme

/**
 * Application root Activity.
 *
 * Responsibilities:
 * - Hosts the Compose UI via setContent.
 * - Collects theme state in a lifecycle-aware way (prevents stale values across lifecycle transitions).
 * - Collects global mute state and feeds it into the shared UiSoundManager.
 * - Delegates actual UI tree (theme, system bars, LTR, navigation) to [ExplanationTableRoot].
 *
 * NOTE: Visual and behavioral parity is a hard requirement. This refactor only improves readability,
 * lifecycle semantics, and adds a global UI SFX system without altering existing navigation or theming.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = viewModel()

            // Lifecycle-aware collection with delegated state for clarity and safety.
            // Initial values preserve the existing startup appearance/behavior.
            val isDarkTheme by mainViewModel.isDarkTheme
                .collectAsStateWithLifecycle(initialValue = false)

            val isMuted by mainViewModel.isMuted
                .collectAsStateWithLifecycle(initialValue = false)

            ExplanationTableRoot(
                isDarkTheme = isDarkTheme,
                isMuted = isMuted
            )
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
 * 3) Provide the global UiSoundManager via CompositionLocal so any Composable can play UI SFX.
 * 4) Use an explicit LTR layout direction to preserve current visuals.
 * 5) Render the NavHost last to keep the root clean and testable.
 */
@Composable
private fun ExplanationTableRoot(
    isDarkTheme: Boolean,
    isMuted: Boolean
) {
    ExplanationTableTheme(darkTheme = isDarkTheme) {
        // Transparent, visible system bars; content handles insets.
        // Keep this as a composable to remain responsive to theme changes (icon contrast).
        AppEdgeToEdgeSystemBars(isDarkTheme = isDarkTheme)

        val appContext = LocalContext.current.applicationContext
        val uiSoundManager = remember {
            UiSoundManager(appContext)
        }

        // Keep the sound manager's mute flag in sync with the app-wide setting.
        LaunchedEffect(isMuted) {
            uiSoundManager.updateMute(isMuted)
        }

        // Ensure SoundPool resources are released when the root composition leaves.
        DisposableEffect(Unit) {
            onDispose {
                uiSoundManager.release()
            }
        }

        // Preserve current LTR visuals across the app and provide global UI SFX.
        CompositionLocalProvider(
            LocalLayoutDirection provides LayoutDirection.Ltr,
            LocalUiSoundManager provides uiSoundManager
        ) {
            AppNavHost(isDarkTheme = isDarkTheme)
        }
    }
}
