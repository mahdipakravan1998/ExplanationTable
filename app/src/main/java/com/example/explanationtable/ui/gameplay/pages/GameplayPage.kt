package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
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
import com.example.explanationtable.ui.gameplay.components.PrizeBox
import com.example.explanationtable.ui.gameplay.review.StageReviewTable
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.delay

/**
 * Composable function representing the gameplay screen for a specific stage.
 *
 * @param isDarkTheme Boolean indicating whether the dark theme is enabled.
 * @param stageNumber The stage number to display in the top bar title.
 * @param difficulty The difficulty level for the current stage.
 * @param gems The number of gems to display in the top bar (default is 1000).
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty,
    gems: Int = 1000
) {
    // Retrieve the main view model instance to manage UI state.
    val viewModel: MainViewModel = viewModel()

    // Observe the mute state from the view model.
    val isMuted by viewModel.isMuted.collectAsState()

    // State variable controlling the visibility of the settings dialog.
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Get the current context and safely cast it to an Activity for exit operations.
    val context = LocalContext.current
    val activity = context as? Activity

    // Build the page title by combining a localized "stage" string with the stage number
    // converted to Persian digits.
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"

    // State to control game over.
    var gameOver by remember { mutableStateOf(false) }
    // State to control PrizeBox visibility.
    var showPrizeBox by remember { mutableStateOf(false) }

    // Trigger PrizeBox appearance after StageReviewTable animation completes.
    LaunchedEffect(gameOver) {
        if (gameOver) {
            delay(300) // Wait for StageReviewTable to slide in
            showPrizeBox = true
        }
    }

    // Apply the custom background for the page.
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        // Use a Box to allow stacking components.
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content column.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar displaying the title, gem count, and action buttons.
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = gems,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    onHelpClick = { /* Help action not implemented yet */ }
                )

                // Spacer for vertical spacing between the top bar and game table.
                Spacer(modifier = Modifier.height(72.dp))

                // Animated content for GameTable and StageReviewTable transition.
                AnimatedContent(
                    targetState = gameOver,
                    transitionSpec = {
                        if (targetState) {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(300)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(300)
                            )
                        }.using(
                            SizeTransform(clip = false)
                        )
                    }, label = ""
                ) { targetGameOver ->
                    if (!targetGameOver) {
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { gameOver = true }
                        )
                    } else {
                        StageReviewTable(
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // Animated PrizeBox that slides in from the bottom and sticks to the bottom.
            AnimatedVisibility(
                visible = showPrizeBox,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(300)
                ),
                exit = fadeOut()
            ) {
                PrizeBox(
                    isDarkTheme = isDarkTheme,
                    onPrizeButtonClick = { /* Handle prize receiving action here */ }
                )
            }

            // Settings dialog allowing theme toggling, mute toggling, and exit functionality.
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    // Exit the application by finishing the current task.
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
