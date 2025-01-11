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
 * A composable that represents the gameplay screen for a given stage.
 *
 * @param stageNumber The stage number to display in the top bar title.
 * @param difficulty The difficulty level of this stage.
 * @param isDarkTheme Whether the dark theme is enabled.
 * @param diamonds The number of diamonds to display in the top bar.
 */
@Composable
fun GameplayPage(
    stageNumber: Int,
    difficulty: Difficulty,
    isDarkTheme: Boolean,
    diamonds: Int = 999
) {
    val viewModel: MainViewModel = viewModel()
    val isMuted by viewModel.isMuted.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Convert stageNumber to Persian digits when building the string
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(
            modifier = Modifier.fillMaxSize(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = pageTitle,
                diamonds = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                onHelpClick = { /* no implementation yet */ }
            )

            Spacer(modifier = Modifier.height(72.dp))

            // Pass both difficulty and stageNumber to the GameTable
            GameTable(
                difficulty = difficulty,
                stageNumber = stageNumber  // <-- now passes the stage number
            )

            // Any additional gameplay UI can be added here

            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                currentTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
