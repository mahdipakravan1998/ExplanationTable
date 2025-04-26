package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
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
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel

/**
 * Composable that sets up and renders the stage list screen.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    // App-wide state (for gems, mute) stays in MainViewModel
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsState()

    // New StageViewModel for our stage count
    val stageViewModel: StageViewModel = viewModel()

    // Local UI state
    var showSettingsDialog by remember { mutableStateOf(false) }

    // For exit
    val context = LocalContext.current
    val activity = context as? Activity

    // Handle Android back
    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Kick off loading the count whenever difficulty changes
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Top bar with gems and difficulty
            AppTopBar(
                isHomePage      = false,
                isDarkTheme     = isDarkTheme,
                title           = stringResource(id = R.string.stages_list),
                gems            = diamonds,
                difficulty      = difficulty,
                onSettingsClick = { showSettingsDialog = true },
                iconTint        = MaterialTheme.colorScheme.onSurface
            )

            // The actual list of stages delegates to its own Composable
            StagesListContent(
                navController = navController,
                difficulty    = difficulty
            )

            // Settings dialog (theme, mute, exit)
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss  = { showSettingsDialog = false },
                onExit     = { activity?.finishAndRemoveTask() }
            )
        }
    }
}
