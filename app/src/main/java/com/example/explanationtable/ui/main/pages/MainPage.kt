package com.example.explanationtable.ui.main.pages

import android.app.Activity
import android.annotation.SuppressLint
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.R

/**
 * MainPage composable sets up the primary UI structure including the top bar,
 * main content area, and associated dialogs for difficulty selection and settings.
 *
 * @param navController The navigation controller for handling navigation actions.
 * @param viewModel The ViewModel managing UI-related data and state.
 * @param isDarkTheme Flag indicating whether the dark theme is enabled.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    // Observe the mute state from the ViewModel
    val isMuted by viewModel.isMuted.collectAsState()

    // Local state variables to control dialog visibility and store selected option
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // Safely obtain the Activity context to perform activity-specific actions (e.g., exiting)
    val context = LocalContext.current
    val activity = context as? Activity

    // BackHandler for handling the back button press to show the exit confirmation
    BackHandler {
        showExitConfirmation = true
    }

    // Wrap the entire UI in a custom background component styled for the home page.
    Background(isHomePage = true, isDarkTheme = isDarkTheme) {
        // Scaffold provides a basic layout structure including a top bar and content area.
        Scaffold(
            topBar = {
                // AppTopBar displays the top bar with a settings icon.
                AppTopBar(
                    isHomePage = true,
                    isDarkTheme = isDarkTheme,
                    onSettingsClick = { showSettingsDialog = true },
                    iconTint = MaterialTheme.colorScheme.onSurface
                )
            },
            containerColor = Color.Transparent,
            content = { paddingValues ->
                // Column arranges the main content vertically, applying the Scaffold's padding.
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // MainContent provides the primary interactive elements.
                    MainContent(
                        onListClicked = { showDifficultyDialog = true },
                        onStartGameClicked = {
                            // TODO: Implement navigation to StartGame screen
                        }
                    )
                }

                // Display the DifficultyDialog when the corresponding flag is true.
                DifficultyDialog(
                    showDialog = showDifficultyDialog,
                    onDismiss = { showDifficultyDialog = false },
                    onOptionSelected = { option ->
                        // Update the selected option, dismiss the dialog, and navigate accordingly.
                        selectedOption = option
                        showDifficultyDialog = false
                        navController.navigate("${Routes.STAGES_LIST}/$option")
                    }
                )

                // Display the SettingsDialog when the corresponding flag is true.
                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    isDarkTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    onExit = {
                        // Exit the app safely by finishing the activity and removing it from the task list.
                        activity?.finishAndRemoveTask()
                    }
                )

                // Exit Confirmation Dialog
                ConfirmationDialog(
                    showDialog = showExitConfirmation,
                    titleResId = R.string.confirm_exit_title,
                    messageResId = R.string.confirm_exit_message,
                    onConfirm = {
                        showExitConfirmation = false
                        activity?.finishAndRemoveTask()
                    },
                    onDismiss = {
                        showExitConfirmation = false
                    }
                )
            }
        )
    }
}
