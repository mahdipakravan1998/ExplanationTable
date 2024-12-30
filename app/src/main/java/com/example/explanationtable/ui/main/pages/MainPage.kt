package com.example.explanationtable.ui.main.pages

import android.app.Activity
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.theme.ExplanationTableTheme

/**
 * The main page composable that sets up the primary UI structure, including the top bar,
 * main content, and various dialogs.
 *
 * @param navController The navigation controller for handling navigation actions.
 * @param viewModel The ViewModel managing UI-related data and state.
 */
@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel()
) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // Obtain the Activity context safely
    val context = LocalContext.current
    val activity = context as? Activity

    // Main background
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
            content = { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues) // Apply padding from Scaffold
                ) {
                    MainContent(
                        onListClicked = { showDifficultyDialog = true },
                        onStartGameClicked = { /* TODO: Implement navigation to StartGame screen */ }
                    )
                }

                // (1) DifficultyDialog
                DifficultyDialog(
                    showDialog = showDifficultyDialog,
                    onDismiss = { showDifficultyDialog = false },
                    onOptionSelected = { option ->
                        selectedOption = option
                        showDifficultyDialog = false
                        navController.navigate("${Routes.STAGES_LIST}/$option")
                    }
                )

                // (2) SettingsDialog
                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    currentTheme = isDarkTheme,
                    onToggleTheme = { viewModel.toggleTheme() },
                    isMuted = isMuted,
                    onToggleMute = { viewModel.toggleMute() },
                    onExit = {
                        // Exit the app safely
                        activity?.finishAndRemoveTask()
                    }
                )
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    ExplanationTableTheme(darkTheme = false) {
        val navController = rememberNavController()
        val viewModel: MainViewModel = viewModel()
        MainPage(navController = navController, viewModel = viewModel)
    }
}
