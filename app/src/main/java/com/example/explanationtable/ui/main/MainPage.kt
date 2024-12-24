package com.example.explanationtable.ui.main

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
import com.example.explanationtable.ui.stages.DifficultyDialog
import com.example.explanationtable.ui.settings.SettingsDialog
import com.example.explanationtable.ui.theme.ExplanationTableTheme

@SuppressLint("StateFlowValueCalledInComposition")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    var showDifficultyDialog by remember { mutableStateOf(false) }
    var showSettingsDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    // Obtain the Activity context
    val activity = LocalContext.current as? Activity

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
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // Show difficulty popup on button click
                    MainContent(onListClicked = { showDifficultyDialog = true })
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
                        // Exit the app
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
