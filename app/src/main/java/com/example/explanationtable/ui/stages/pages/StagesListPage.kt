package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.content.StagesListContent

/**
 * The parent composable that sets up the stage list screen.
 *
 * @param navController The NavController used for navigation.
 * @param difficulty The current difficulty level.
 * @param gems The number of gems to display.
 * @param onSettingsClick Callback for when the settings icon is clicked.
 * @param isDarkTheme Whether the dark theme is enabled.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    gems: Int = 1000,
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
                gems = gems,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                iconTint = MaterialTheme.colorScheme.onSurface
            )

            // Main content for the stage list
            StagesListContent(
                difficulty = difficulty,
                onStageClick = { stageNumber ->
                    // Navigate to the gameplay page with the given stageNumber AND difficulty
                    navController.navigate("${Routes.GAMEPLAY}/$stageNumber/${difficulty.name}")
                }
            )

            // SettingsDialog
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkTheme = isDarkTheme,
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
