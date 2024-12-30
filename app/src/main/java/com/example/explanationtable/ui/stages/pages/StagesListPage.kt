package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.content.StagesListContent

/**
 * The parent composable that sets up the stage list screen.
 *
 * @param difficulty The current difficulty level.
 * @param diamonds The number of diamonds to display.
 * @param onSettingsClick Callback for when the settings icon is clicked.
 * @param isDarkTheme Whether the dark theme is enabled.
 */
@Composable
fun StagesListPage(
    difficulty: Difficulty = Difficulty.EASY,
    diamonds: Int = 100,
    onSettingsClick: () -> Unit = {},
    isDarkTheme: Boolean
) {
    val viewModel: MainViewModel = viewModel()
    val isMuted by viewModel.isMuted.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Main background
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {

            // Top App Bar
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = stringResource(id = R.string.stages_list),
                diamonds = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                iconTint = MaterialTheme.colorScheme.onSurface
            )

            // Main content for the stage list
            StagesListContent(difficulty = difficulty)

            // SettingsDialog
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                currentTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    // Safely exit the app
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
