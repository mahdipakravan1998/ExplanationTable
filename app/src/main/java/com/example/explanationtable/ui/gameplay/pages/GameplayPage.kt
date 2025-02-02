package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.utils.toPersianDigits

/**
 * Composable function representing the gameplay screen for a specific stage.
 *
 * @param isDarkTheme Boolean indicating whether the dark theme is enabled.
 * @param stageNumber The stage number to display in the top bar title.
 * @param difficulty The difficulty level for the current stage.
 * @param gems The number of gems to display in the top bar (default is 1000).
 */
@Composable
fun GameplayPage(
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty,
    gems: Int = 1000
) {
    // Retrieve the main view model instance to manage UI state.
    val viewModel: MainViewModel = viewModel()

    // Observe the mute state from the view model.
    val isMuted by viewModel.isMuted.collectAsState()

    // State variable controlling the visibility of the settings dialog.
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Get the current context and safely cast it to an Activity for exit operations.
    val context = LocalContext.current
    val activity = context as? Activity

    // Build the page title by combining a localized "stage" string with the stage number
    // converted to Persian digits.
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"

    // Apply the custom background for the page.
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        // Arrange components vertically.
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Top bar displaying the title, gem count, and action buttons.
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = pageTitle,
                gems = gems,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                onHelpClick = { /* Help action not implemented yet */ }
            )

            // Spacer for vertical spacing between the top bar and game table.
            Spacer(modifier = Modifier.height(72.dp))

            // Main gameplay table that adapts to the current stage and difficulty.
            GameTable(
                isDarkTheme = isDarkTheme,
                difficulty = difficulty,
                stageNumber = stageNumber
            )

            // Settings dialog allowing theme toggling, mute toggling, and exit functionality.
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    // Exit the application by finishing the current task.
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
