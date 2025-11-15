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
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
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
import com.example.explanationtable.ui.stages.preflight.StagesListPreflight
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

    // When a difficulty option is chosen and preflight completes, we navigate via this route.
    var pendingDifficultyRoute by rememberSaveable { mutableStateOf<String?>(null) }

    // Preflight + loading orchestration state.
    var preflightDifficulty by rememberSaveable { mutableStateOf<Difficulty?>(null) }
    var isPreflightActive by rememberSaveable { mutableStateOf(false) }
    var isPreflightReady by rememberSaveable { mutableStateOf(false) }
    var loadingDifficultyValue by rememberSaveable { mutableStateOf<String?>(null) }

    // One-shot guard so dialog-driven navigation into the stages list is centralized and single-fire.
    var hasNavigatedFromDialog by rememberSaveable { mutableStateOf(false) }

    // Helper to reset all preflight-related state when the host lifecycle goes below STARTED
    // or when starting a brand-new dialog → preflight flow.
    fun resetPreflightState() {
        Log.d(
            TAG,
            "resetPreflightState(): " +
                    "isPreflightActive=$isPreflightActive, " +
                    "isPreflightReady=$isPreflightReady, " +
                    "preflightDifficulty=$preflightDifficulty, " +
                    "pendingDifficultyRoute=$pendingDifficultyRoute"
        )
        isPreflightActive = false
        isPreflightReady = false
        preflightDifficulty = null
        pendingDifficultyRoute = null
        loadingDifficultyValue = null
        hasNavigatedFromDialog = false
    }

    val context = LocalContext.current
    // Resolve Activity safely through potential ContextWrappers (e.g., Material3, Hilt, etc.).
    val activity: Activity? = context.findActivity()

    // Lifecycle-aware collection ensures we don't navigate while screen is not visible.
    val lifecycleOwner = androidx.lifecycle.compose.LocalLifecycleOwner.current

    // Reset any in-flight preflight run when the lifecycle leaves STARTED.
    DisposableEffect(lifecycleOwner) {
        val lifecycle = lifecycleOwner.lifecycle
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_STOP || event == Lifecycle.Event.ON_DESTROY) {
                Log.d(TAG, "Lifecycle event=$event → resetting preflight state")
                resetPreflightState()
            }
        }
        lifecycle.addObserver(observer)
        onDispose {
            lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(viewModel, lifecycleOwner) {
        lifecycleOwner.lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
            viewModel.startGameRoutes.collectLatest { route ->
                Log.d(TAG, "startGameRoutes emission: route=$route, isPreflightActive=$isPreflightActive")
                // While a preflight run is active, ignore other navigation intents to avoid collisions.
                if (isPreflightActive) {
                    Log.d(TAG, "Ignoring startGame route due to active preflight")
                    return@collectLatest
                }

                // Route emitted by the primary CTA.
                navController.navigate(route)
            }
        }
    }

    // Authoritative navigation effect for the difficulty-dialog → stages-list flow.
    LaunchedEffect(
        pendingDifficultyRoute,
        showDifficultyDialog,
        isPreflightReady,
        hasNavigatedFromDialog,
        lifecycleOwner
    ) {
        val route = pendingDifficultyRoute
        if (route != null && !showDifficultyDialog && isPreflightReady && !hasNavigatedFromDialog) {
            val lifecycle = lifecycleOwner.lifecycle

            Log.d(
                TAG,
                "Dialog navigation check: route=$route, " +
                        "showDifficultyDialog=$showDifficultyDialog, " +
                        "isPreflightReady=$isPreflightReady, " +
                        "hasNavigatedFromDialog=$hasNavigatedFromDialog, " +
                        "lifecycleState=${lifecycle.currentState}"
            )

            // Ensure we only navigate while the host is at least STARTED.
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.w(TAG, "Lifecycle not STARTED at nav time → resetting preflight")
                resetPreflightState()
                return@LaunchedEffect
            }

            delay(DIALOG_EXIT_NAV_DELAY_MS)

            // Re-check lifecycle + preflight state after the delay.
            if (!lifecycle.currentState.isAtLeast(Lifecycle.State.STARTED)) {
                Log.w(TAG, "Lifecycle not STARTED after delay → resetting preflight")
                resetPreflightState()
                return@LaunchedEffect
            }
            if (pendingDifficultyRoute != route || !isPreflightReady || !isPreflightActive) {
                Log.w(
                    TAG,
                    "Preflight state changed during dialog exit delay; " +
                            "pendingDifficultyRoute=$pendingDifficultyRoute, " +
                            "isPreflightReady=$isPreflightReady, " +
                            "isPreflightActive=$isPreflightActive"
                )
                return@LaunchedEffect
            }

            Log.d(TAG, "Navigating from dialog to route=$route")
            navController.navigate(route) {
                // Avoid duplicate stages destinations when re-entering from the dialog.
                launchSingleTop = true
            }

            hasNavigatedFromDialog = true

            // Clean up preflight + loading state for the next run.
            pendingDifficultyRoute = null
            preflightDifficulty = null
            loadingDifficultyValue = null
            isPreflightActive = false
            isPreflightReady = false
        }
    }

    // Back handling order:
    // - If a dialog is open, close it (unless locked by preflight).
    // - If no dialog is open, show exit confirmation.
    if (showDifficultyDialog) {
        BackHandler {
            Log.d(TAG, "BackHandler in DifficultyDialog; isPreflightActive=$isPreflightActive")
            if (!isPreflightActive) {
                resetPreflightState()
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
        // Off-screen preflight runner, mounted only while active.
        val activeDifficulty = preflightDifficulty
        if (isPreflightActive && activeDifficulty != null) {
            Log.d(
                TAG,
                "Mounting StagesListPreflight: difficulty=$activeDifficulty, isDarkTheme=$isDarkTheme"
            )
            StagesListPreflight(
                difficulty = activeDifficulty,
                isDarkTheme = isDarkTheme,
                onPrepared = {
                    Log.d(
                        TAG,
                        "onPrepared from StagesListPreflight: difficulty=$activeDifficulty, " +
                                "isPreflightActive=$isPreflightActive"
                    )
                    // Preflight signals readiness for the stages list.
                    isPreflightReady = true
                    pendingDifficultyRoute = Routes.stagesList(activeDifficulty)

                    // Reset one-shot guard for this new navigation run.
                    hasNavigatedFromDialog = false

                    // Begin dialog dismissal; navigation is gated until the exit animation completes.
                    showDifficultyDialog = false
                }
            )
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainContent(
                isDarkTheme = isDarkTheme,
                onListClicked = {
                    Log.d(TAG, "MainContent.onListClicked → open DifficultyDialog")
                    // Each entry into the difficulty dialog starts from a clean preflight state
                    resetPreflightState()
                    showDifficultyDialog = true
                },
                onStartGameClicked = { viewModel.onStartGameClick() }
            )
        }

        DifficultyDialog(
            isDarkTheme = isDarkTheme,
            showDialog = showDifficultyDialog,
            loadingDifficultyValue = loadingDifficultyValue,
            isInteractionLocked = isPreflightActive,
            onDismiss = {
                Log.d(
                    TAG,
                    "DifficultyDialog.onDismiss invoked; isPreflightActive=$isPreflightActive"
                )
                if (!isPreflightActive) {
                    // User cancelled before or after selection; ensure no loading state persists.
                    resetPreflightState()
                    showDifficultyDialog = false
                }
            },
            onOptionSelected = { option ->
                // If a preflight run is already in progress, ignore further taps.
                if (isPreflightActive) {
                    Log.d(TAG, "DifficultyDialog.onOptionSelected ignored; preflight already active")
                    return@DifficultyDialog
                }

                val difficulty: Difficulty = option.toDifficultyFromLabel()
                Log.d(
                    TAG,
                    "DifficultyDialog.onOptionSelected: option=$option, difficulty=$difficulty"
                )
                preflightDifficulty = difficulty
                loadingDifficultyValue = option
                isPreflightActive = true
                isPreflightReady = false
                hasNavigatedFromDialog = false
                // Dialog remains visible; navigation is triggered after preflight + exit animation.
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
