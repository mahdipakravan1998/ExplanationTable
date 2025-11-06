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
import androidx.compose.runtime.LaunchedEffect
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
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog
import kotlinx.coroutines.flow.collectLatest

/**
 * MainPage: Home screen with chrome, actions, and dialogs.
 *
 * Behavior preserved. Improvements:
 * - Removed unused state to cut recompositions.
 * - Clearer one-shot navigation collection tied to ViewModel lifecycle.
 * - Explicit docs for future routing standardization.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    // UI dialog flags (process-death safe via rememberSaveable)
    var showDifficultyDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }

    // Cache Activity cast once; avoids repeating on every recomposition
    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    // Collect one-shot routes from VM and navigate. Keyed by VM for single collector.
    LaunchedEffect(viewModel) {
        viewModel.startGameRoutes.collectLatest { route ->
            // NOTE: This string route intentionally preserves legacy casing ("GAMEPLAY/...").
            navController.navigate(route)
        }
    }

    // Back press → exit confirmation (preserved behavior)
    BackHandler { showExitConfirmation = true }

    Background(isHomePage = true, isDarkTheme = isDarkTheme) {
        Scaffold(
            topBar = {
                AppTopBar(
                    isHomePage = true,
                    isDarkTheme = isDarkTheme,
                    onSettingsClick = { showSettingsDialog = true },
                    iconTint = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = Color.Transparent,
            contentWindowInsets = WindowInsets(0)
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

            // Difficulty selection → typed route using builder (existing behavior)
            DifficultyDialog(
                showDialog = showDifficultyDialog,
                onDismiss = { showDifficultyDialog = false },
                onOptionSelected = { option ->
                    showDifficultyDialog = false
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
