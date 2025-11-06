package com.example.explanationtable.ui.main.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.toDifficultyFromLabel
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog

/**
 * MainPage sets up the home screen structure (top bar, background, body content, and dialogs).
 *
 * Behavior preserved. Internals improved:
 * - Use typed route builder (Routes.stagesList) instead of stringly-typed construction.
 * - Map dialog option safely to Difficulty via shared model helpers.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    // ---- State (UI-only; saveable for rotation/process death resilience)
    var showDifficultyDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }
    var selectedOption by rememberSaveable { mutableStateOf<String?>(null) }

    // ---- Environment
    val context = LocalContext.current
    // Memoize Activity cast to avoid doing it on every recomposition.
    val activity = remember(context) { context as? Activity }

    // ---- System back: preserve exact behavior (always show exit confirmation)
    BackHandler { showExitConfirmation = true }

    // ---- Background + Scaffold chrome
    Background(isHomePage = true, isDarkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                AppTopBar(
                    isHomePage = true,
                    isDarkTheme = isDarkTheme,
                    onSettingsClick = { showSettingsDialog = true },
                    // Read from MaterialTheme each recomposition to respect dynamic theme changes.
                    iconTint = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = Color.Transparent,
            // Insets are consumed app-wide; keep zero here.
            contentWindowInsets = WindowInsets(0)
        ) { paddingValues ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                // Main actions: start game / list stages (difficulty)
                MainContent(
                    isDarkTheme = isDarkTheme,
                    onListClicked = { showDifficultyDialog = true },
                    onStartGameClicked = {
                        // TODO: Implement navigation to StartGame screen (preserved as-is).
                    }
                )
            }

            // ---- Dialogs (shown as overlays; order preserved)
            DifficultyDialog(
                showDialog = showDifficultyDialog,
                onDismiss = { showDifficultyDialog = false },
                onOptionSelected = { option ->
                    selectedOption = option
                    showDifficultyDialog = false
                    // Convert option -> Difficulty robustly, then use typed route builder.
                    val difficulty: Difficulty = option.toDifficultyFromLabel()
                    navController.navigate(Routes.stagesList(difficulty))
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
}
