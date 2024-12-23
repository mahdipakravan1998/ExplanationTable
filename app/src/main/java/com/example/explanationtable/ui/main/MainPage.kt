package com.example.explanationtable.ui.main

import android.app.Activity
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.popup.PopupOptions
import com.example.explanationtable.ui.popup.SettingsPopup
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

    Background(isHomePage = true, isDarkTheme = isDarkTheme) { // Pass isDarkTheme here
        Scaffold(
            topBar = {
                AppTopBar(
                    isHomePage = true,
                    isDarkTheme = isDarkTheme, // Pass the theme state here
                    onSettingsClick = { showSettingsDialog = true }, // Show settings popup
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

                // Difficulty Selection
                if (showDifficultyDialog) {
                    AlertDialog(
                        onDismissRequest = { showDifficultyDialog = false },
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(Alignment.TopEnd)
                            ) {
                                IconButton(
                                    onClick = { showDifficultyDialog = false },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = stringResource(id = R.string.close),
                                        tint = MaterialTheme.colorScheme.onSurface
                                    )
                                }
                            }
                        },
                        text = {
                            PopupOptions(
                                onOptionSelected = { option ->
                                    selectedOption = option
                                    showDifficultyDialog = false
                                    navController.navigate("${Routes.STAGES_LIST}/$option")
                                }
                            )
                        },
                        confirmButton = {},
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }

                // Settings Dialog
                if (showSettingsDialog) {
                    Dialog(onDismissRequest = { showSettingsDialog = false }) {
                        Surface(
                            shape = MaterialTheme.shapes.medium,
                            color = MaterialTheme.colorScheme.background
                        ) {
                            SettingsPopup(
                                onDismiss = { showSettingsDialog = false },
                                currentTheme = isDarkTheme,
                                onToggleTheme = { viewModel.toggleTheme() },
                                isMuted = isMuted,
                                onToggleMute = { viewModel.toggleMute() },
                                onExit = {
                                    // Exit the app by finishing the activity
                                    activity?.finishAndRemoveTask()
                                }
                            )
                        }
                    }
                }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
fun MainPagePreview() {
    // For previews, we can still wrap in a theme to see the UI.
    ExplanationTableTheme(darkTheme = false) {
        val navController = rememberNavController()
        val viewModel: MainViewModel = viewModel()
        MainPage(navController = navController, viewModel = viewModel)
    }
}
