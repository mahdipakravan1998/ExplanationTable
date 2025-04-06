package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.content.StagesListContent

/**
 * Composable that sets up and renders the stage list screen.
 *
 * This function displays a top app bar, a list of stages, and a settings dialog.
 * It uses a background composable and handles navigation to the gameplay screen.
 *
 * @param navController The NavController used for navigation.
 * @param difficulty The current difficulty level; defaults to [Difficulty.EASY].
 * @param gems The number of gems to display; defaults to 1000.
 * @param onSettingsClick Callback for when the settings icon is clicked (currently not used).
 * @param isDarkTheme Flag indicating whether the dark theme is enabled.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    onSettingsClick: () -> Unit = {},
    isDarkTheme: Boolean
) {
    // Retrieve the MainViewModel instance for app-wide state management.
    val viewModel: MainViewModel = viewModel()

    val diamonds by viewModel.diamonds.collectAsState()

    // Collect the current muted state from the ViewModel.
    val isMuted by viewModel.isMuted.collectAsState()

    // Local state to control the visibility of the settings dialog.
    var showSettingsDialog by remember { mutableStateOf(false) }
    // Retrieve the current context and safely cast it to Activity for exit functionality.
    val context = LocalContext.current
    val activity = context as? Activity

    // Apply the background for the screen.
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        // Use a Column layout to stack the UI elements vertically.
        Column(modifier = Modifier.fillMaxSize()) {

            // Top app bar displaying title, gem count, and difficulty.
            // On clicking the settings icon, the settings dialog is triggered.
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = stringResource(id = R.string.stages_list),
                gems = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                iconTint = MaterialTheme.colorScheme.onSurface
            )

            // Main content displaying the list of stages.
            // Navigates to the gameplay screen when a stage is clicked.
            StagesListContent(
                navController = navController,
                difficulty = difficulty
            )

            // Settings dialog allowing the user to toggle theme, mute, or exit the app.
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    // Safely finish the activity and remove the app's task from the recent apps.
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
