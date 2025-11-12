package com.example.explanationtable.ui.main.pages

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.repeatOnLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.toDifficultyFromLabel
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog
import com.example.explanationtable.ui.system.AppScreenScaffold
import com.example.explanationtable.ui.system.BottomBarAppearance
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest

private const val DIALOG_EXIT_NAV_DELAY_MS: Long = 120L

/**
 * Home screen of the app.
 *
 * - Hosts the main content and global dialogs (difficulty selector, settings, exit confirmation).
 * - Collects navigation events from [MainViewModel] in a lifecycle-aware way (STARTED).
 * - Handles back-press with sane priorities:
 *   1) Close an open dialog (difficulty/settings/exit) if any is shown.
 *   2) Otherwise, prompt for exit confirmation.
 *
 * @param navController The [NavController] used to navigate from this page.
 * @param viewModel The [MainViewModel] that exposes navigation intents.
 * @param isDarkTheme Whether the app is in dark theme; forwarded to child composables.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    // Dialog visibility is saveable across config/process recreation.
    var showDifficultyDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }

    // When a difficulty option is chosen, we dismiss the dialog and stage a route here.
    var pendingDifficultyRoute by rememberSaveable { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    // Resolve Activity safely through potential ContextWrappers (e.g., Material3, Hilt, etc.).
    val activity: Activity? = context.findActivity()

    // Lifecycle-aware collection ensures we don't navigate while screen is not visible.
    val lifecycleOwner = LocalLifecycleOwner.current
    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.startGameRoutes.collectLatest { route ->
                // Route emitted by the primary CTA.
                navController.navigate(route)
            }
        }
    }

    // If a difficulty was chosen, wait for the dialog to finish its exit animation,
    // then navigate. This avoids clashing animations.
    LaunchedEffect(pendingDifficultyRoute, showDifficultyDialog) {
        val route = pendingDifficultyRoute
        if (route != null && !showDifficultyDialog) {
            delay(DIALOG_EXIT_NAV_DELAY_MS)
            navController.navigate(route)
            pendingDifficultyRoute = null
        }
    }

    // Back handling order:
    // - If a dialog is open, close it.
    // - If no dialog is open, show exit confirmation.
    if (showDifficultyDialog) {
        BackHandler {
            showDifficultyDialog = false
        }
    } else if (showSettingsDialog) {
        BackHandler {
            showSettingsDialog = false
        }
    } else if (showExitConfirmation) {
        BackHandler {
            showExitConfirmation = false
        }
    } else {
        BackHandler {
            showExitConfirmation = true
        }
    }

    AppScreenScaffold(
        isHomePage = true,
        isDarkTheme = isDarkTheme,
        topBar = {
            AppTopBar(
                isHomePage = true,
                isDarkTheme = isDarkTheme,
                onSettingsClick = { showSettingsDialog = true }
            )
        },
        // Image should extend behind nav bar.
        bottomBarAppearance = BottomBarAppearance.None
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainContent(
                isDarkTheme = isDarkTheme,
                onListClicked = { showDifficultyDialog = true },
                onStartGameClicked = { viewModel.onStartGameClick() }
            )
        }

        DifficultyDialog(
            isDarkTheme = isDarkTheme,
            showDialog = showDifficultyDialog,
            onDismiss = { showDifficultyDialog = false },
            onOptionSelected = { option ->
                // Close dialog first; navigate after a tiny motion buffer in LaunchedEffect.
                showDifficultyDialog = false
                val difficulty: Difficulty = option.toDifficultyFromLabel()
                pendingDifficultyRoute = Routes.stagesList(difficulty)
            }
        )

        SettingsDialog(
            showDialog = showSettingsDialog,
            onDismiss = { showSettingsDialog = false },
            onExit = { activity?.finishAndRemoveTask() }
        )

        ConfirmationDialog(
            showDialog = showExitConfirmation,
            titleResId = R.string.confirm_exit_title,
            messageResId = R.string.confirm_exit_message,
            onConfirm = {
                showExitConfirmation = false
                activity?.finishAndRemoveTask()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}

/**
 * Resolves the nearest [Activity] from a [Context], unwrapping [ContextWrapper]s if needed.
 *
 * Safe alternative to casting `LocalContext.current as? Activity`, which can fail when
 * the context is wrapped by libraries or theming layers.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
