package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
    diamonds: Int = 100
) {
    val viewModel: MainViewModel = viewModel()
    val isMuted by viewModel.isMuted.collectAsState()

    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // 2. Convert stageNumber to Persian digits when building the string
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {

            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = pageTitle,          // e.g., "Stage ۱" if R.string.stage = "Stage"
                diamonds = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                onHelpClick = { /* no implementation yet */ }
            )

            // -- The rest of your gameplay UI goes here --

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
