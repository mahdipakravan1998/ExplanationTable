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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
 * main content area, and dialogs. Immersive mode is now applied **app-wide**
 * from the root, so this screen only focuses on UI.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    // Local state
    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showExitConfirmation by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    val context = LocalContext.current
    val activity = context as? Activity

    // Back exits (with confirm dialog)
    BackHandler { showExitConfirmation = true }

    // Home background + UI
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
            contentWindowInsets = WindowInsets(0), // keep zero; safe with app-wide insets consumption
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    MainContent(
                        isDarkTheme = isDarkTheme,
                        onListClicked = { showDifficultyDialog = true },
                        onStartGameClicked = {
                            // TODO: Implement navigation to StartGame screen
                        }
                    )
                }

                DifficultyDialog(
                    showDialog = showDifficultyDialog,
                    onDismiss = { showDifficultyDialog = false },
                    onOptionSelected = { option ->
                        selectedOption = option
                        showDifficultyDialog = false
                        navController.navigate("${Routes.STAGES_LIST}/$option")
                    }
                )

                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss  = { showSettingsDialog = false },
                    onExit     = { activity?.finishAndRemoveTask() }
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
        )
    }
}
