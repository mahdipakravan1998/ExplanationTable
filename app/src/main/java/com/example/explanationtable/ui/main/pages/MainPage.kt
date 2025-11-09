package com.example.explanationtable.ui.main.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.toDifficultyFromLabel
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.dialogs.DifficultyDialog
import com.example.explanationtable.ui.system.AppScreenScaffold
import com.example.explanationtable.ui.system.BottomBarAppearance
import kotlinx.coroutines.flow.collectLatest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(
    navController: NavController = rememberNavController(),
    viewModel: MainViewModel = viewModel(),
    isDarkTheme: Boolean
) {
    var showDifficultyDialog by rememberSaveable { mutableStateOf(false) }
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    var showExitConfirmation by rememberSaveable { mutableStateOf(false) }

    val context = LocalContext.current
    val activity = remember(context) { context as? Activity }

    LaunchedEffect(viewModel) {
        viewModel.startGameRoutes.collectLatest { route ->
            navController.navigate(route)
        }
    }

    BackHandler { showExitConfirmation = true }

    AppScreenScaffold(
        isHomePage = true,
        isDarkTheme = isDarkTheme,
        topBar = {
            AppTopBar(
                isHomePage = true,
                isDarkTheme = isDarkTheme,
                onSettingsClick = { showSettingsDialog = true }
            )
        },
        bottomBarAppearance = BottomBarAppearance.None  // image should extend behind nav bar
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            MainContent(
                isDarkTheme = isDarkTheme,
                onListClicked = { showDifficultyDialog = true },
                onStartGameClicked = { viewModel.onStartGameClick() }
            )
        }

        DifficultyDialog(
            showDialog = showDifficultyDialog,
            onDismiss = { showDifficultyDialog = false },
            onOptionSelected = { option ->
                showDifficultyDialog = false
                val difficulty: Difficulty = option.toDifficultyFromLabel()
                navController.navigate(Routes.stagesList(difficulty))
            }
        )

        SettingsDialog(
            showDialog = showSettingsDialog,
            onDismiss = { showSettingsDialog = false },
            onExit = { activity?.finishAndRemoveTask() }
        )

        ConfirmationDialog(
            showDialog = showExitConfirmation,
            titleResId = R.string.confirm_exit_title,
            messageResId = R.string.confirm_exit_message,
            onConfirm = {
                showExitConfirmation = false
                activity?.finishAndRemoveTask()
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}
