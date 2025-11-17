package com.example.explanationtable.ui.main.pages

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.util.Log
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
private const val TAG = "MainPage"

/**
 * Home screen of the app.
 *
 * - Hosts the main content and global dialogs (difficulty selector, settings, exit confirmation).
 * - Collects navigation events from [MainViewModel] in a lifecycle-aware way (STARTED).
 * - Handles back-press with sane priorities:
 *   1) Close an open dialog (difficulty/settings/exit) if any is shown.
 *   2) Otherwise, prompt for exit confirmation.
 *
 * Preflight for the stages list now happens on [StagesListPage], behind a placeholder.
 * This page only:
 *  - Opens the Difficulty dialog,
 *  - Plays the dialog exit animation (bounded by [DIALOG_EXIT_NAV_DELAY_MS]),
 *  - Navigates to the stages list route.
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

    // Difficulty selection + navigation state.
    var selectedDifficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var isNavigatingToStages by rememberSaveable { mutableStateOf(false) }

    fun resetDialogState() {
        Log.d(
            TAG,
            "resetDialogState(): selectedDifficulty=$selectedDifficulty, " +
                    "isNavigatingToStages=$isNavigatingToStages"
        )
        selectedDifficulty = null
        isNavigatingToStages = false
    }

    val context = LocalContext.current
    val activity: Activity? = context.findActivity()

    // Lifecycle-aware collection ensures we don't navigate while screen is not visible.
    val lifecycleOwner = LocalLifecycleOwner.current

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.startGameRoutes.collectLatest { route ->
                Log.d(TAG, "startGameRoutes emission: route=$route")
                navController.navigate(route)
            }
        }
    }

    // Drive dialog-exit + navigation after a difficulty is selected.
    LaunchedEffect(selectedDifficulty, isNavigatingToStages) {
        val difficulty = selectedDifficulty
        if (difficulty != null && isNavigatingToStages) {
            val lifecycle = lifecycleOwner.lifecycle

            Log.d(
                TAG,
                "Dialog navigation start: difficulty=$difficulty, " +
                        "lifecycleState=${lifecycle.currentState}"
            )

            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.w(TAG, "Lifecycle not STARTED at nav time → aborting stages navigation")
                resetDialogState()
                return@LaunchedEffect
            }

            // Start dialog exit animation immediately.
            if (showDifficultyDialog) {
                showDifficultyDialog = false
            }

            // Let the dialog exit animation play.
            delay(DIALOG_EXIT_NAV_DELAY_MS)

            // Re-check lifecycle + state after the delay.
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.w(TAG, "Lifecycle not STARTED after delay → aborting stages navigation")
                resetDialogState()
                return@LaunchedEffect
            }

            if (!isNavigatingToStages || selectedDifficulty != difficulty) {
                Log.w(
                    TAG,
                    "Navigation state changed during dialog exit delay; " +
                            "selectedDifficulty=$selectedDifficulty, " +
                            "expected=$difficulty, isNavigatingToStages=$isNavigatingToStages"
                )
                return@LaunchedEffect
            }

            val route = Routes.stagesList(difficulty)
            Log.d(TAG, "Navigating from DifficultyDialog to route=$route")
            navController.navigate(route) {
                // Avoid duplicate stages destinations when re-entering from the dialog.
                launchSingleTop = true
            }

            resetDialogState()
        }
    }

    // Back handling order:
    // - If a dialog is open, close it.
    // - If no dialog is open, show exit confirmation.
    if (showDifficultyDialog) {
        BackHandler {
            Log.d(TAG, "BackHandler in DifficultyDialog")
            if (!isNavigatingToStages) {
                resetDialogState()
                showDifficultyDialog = false
            }
        }
    } else if (showSettingsDialog) {
        BackHandler {
            Log.d(TAG, "BackHandler in SettingsDialog")
            showSettingsDialog = false
        }
    } else if (showExitConfirmation) {
        BackHandler {
            Log.d(TAG, "BackHandler in ExitConfirmation")
            showExitConfirmation = false
        }
    } else {
        BackHandler {
            Log.d(TAG, "BackHandler at home → show exit confirmation")
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
                onListClicked = {
                    Log.d(TAG, "MainContent.onListClicked → open DifficultyDialog")
                    resetDialogState()
                    showDifficultyDialog = true
                },
                onStartGameClicked = { viewModel.onStartGameClick() }
            )
        }

        DifficultyDialog(
            isDarkTheme = isDarkTheme,
            showDialog = showDifficultyDialog,
            isInteractionLocked = isNavigatingToStages,
            onDismiss = {
                Log.d(
                    TAG,
                    "DifficultyDialog.onDismiss invoked; isNavigatingToStages=$isNavigatingToStages"
                )
                if (!isNavigatingToStages) {
                    resetDialogState()
                    showDifficultyDialog = false
                }
            },
            onOptionSelected = { option ->
                if (isNavigatingToStages) {
                    Log.d(
                        TAG,
                        "DifficultyDialog.onOptionSelected ignored; navigation already in progress"
                    )
                    return@DifficultyDialog
                }

                val difficulty: Difficulty = option.toDifficultyFromLabel()
                Log.d(
                    TAG,
                    "DifficultyDialog.onOptionSelected: option=$option, difficulty=$difficulty"
                )
                selectedDifficulty = difficulty
                isNavigatingToStages = true
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
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
