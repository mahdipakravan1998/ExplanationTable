// "StagesListPage.kt":
package com.example.explanationtable.ui.stages

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.components.AppTopBar
import com.example.explanationtable.ui.popup.SettingsPopup
import com.example.explanationtable.ui.main.MainViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun StagesListPage(
    difficulty: Difficulty = Difficulty.EASY,
    diamonds: Int = 100,
    onSettingsClick: () -> Unit = {},
    isDarkTheme: Boolean // New parameter
) {
    val viewModel: MainViewModel = viewModel()
    val isMuted by viewModel.isMuted.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }

    // Obtain the Activity context
    val activity = LocalContext.current as? Activity

    // Use the same background composable, but isHomePage = false
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme, // Pass the theme state here
                title = stringResource(id = R.string.stages_list),
                diamonds = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                iconTint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "List of Game Stages for $difficulty difficulty",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )

            // Settings Dialog
            if (showSettingsDialog) {
                Dialog(onDismissRequest = { showSettingsDialog = false }) {
                    Surface(
                        shape = MaterialTheme.shapes.medium,
                        color = MaterialTheme.colorScheme.background
                    ) {
                        SettingsPopup(
                            onDismiss = { showSettingsDialog = false },
                            currentTheme = isDarkTheme, // Pass isDarkTheme here
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
    }
}
