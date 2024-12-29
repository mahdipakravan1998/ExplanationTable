// File: StagesListPage.kt
package com.example.explanationtable.ui.stages

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
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
import com.example.explanationtable.ui.main.MainViewModel
import com.example.explanationtable.ui.settings.SettingsDialog

/**
 * The parent composable that sets up the stage list screen.
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
    // Obtain the Activity context
    val activity = LocalContext.current as? Activity

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
                    // Exit the app
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
