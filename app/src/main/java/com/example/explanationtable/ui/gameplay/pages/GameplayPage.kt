package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
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
 * @param isDarkTheme Indicates if dark theme is enabled.
 * @param stageNumber The stage number to display.
 * @param difficulty  The difficulty level for the current stage.
 * @param gems        The number of gems to display in the top bar (default is 1000).
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    // Handle the back button press
    BackHandler {
        // Navigate to the stages list page with difficulty passed
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Retrieve the main view model for managing UI state.
    val viewModel: MainViewModel = viewModel()

    val diamonds by viewModel.diamonds.collectAsState()

    // Observe the mute state from the view model.
    val isMuted by viewModel.isMuted.collectAsState()

    // State controlling the visibility of the settings dialog.
    var showSettingsDialog by remember { mutableStateOf(false) }

    // Get the current context and cast it to an Activity for exit operations.
    val context = LocalContext.current
    val activity = context as? Activity

    // Construct the page title using a localized "stage" string and Persian digits.
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"

    // Common animation duration (in milliseconds) used across transitions.
    val animationDuration = 300

    // State indicating whether the game is over.
    var isGameOver by remember { mutableStateOf(false) }
    // State controlling the visibility of the PrizeBox.
    var isPrizeBoxVisible by remember { mutableStateOf(false) }

    // Store the minimum moves, player moves, and elapsed time values.
    var minMovesForThisScramble by remember { mutableStateOf(0) }
    var playerMoves by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }

    // Reset game state whenever the stageNumber or difficulty changes
    LaunchedEffect(stageNumber, difficulty) {
        // Reset game variables when the game restarts
        isGameOver = false
        isPrizeBoxVisible = false
        minMovesForThisScramble = 0
        playerMoves = 0
        elapsedTime = 0L
    }

    // Launch side-effect: after the game ends, delay briefly before showing the PrizeBox.
    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            delay(animationDuration.toLong())
            isPrizeBoxVisible = true
        }
    }

    // Apply a custom background for the gameplay page.
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        // Use a Box to stack UI components.
        Box(modifier = Modifier.fillMaxSize()) {
            // Main content column containing the top bar and animated game content.
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with title, gem count, and action buttons.
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    onHelpClick = { /* TODO: Implement help action */ }
                )

                // Spacer for vertical separation between the top bar and game content.
                Spacer(modifier = Modifier.height(72.dp))

                // Animated content transitions between the GameTable and StageReviewTable.
                AnimatedContent(
                    targetState = isGameOver,
                    transitionSpec = {
                        if (targetState) {
                            // When the game ends: slide in from the right, slide out to the left.
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(animationDuration)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(animationDuration)
                            )
                        } else {
                            // When the game is active: slide in from the left, slide out to the right.
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(animationDuration)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(animationDuration)
                            )
                        }.using(SizeTransform(clip = false))
                    },
                    label = ""
                ) { targetGameOver ->
                    if (!targetGameOver) {
                        // Display the game table while the game is active.
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            // Updated onGameComplete callback to match the expected signature (three parameters)
                            onGameComplete = { minMoves, playerMoveCount, timeElapsed ->
                                // Set isGameOver to true when the game is completed.
                                isGameOver = true
                                // Store the values.
                                minMovesForThisScramble = minMoves
                                playerMoves = playerMoveCount
                                elapsedTime = timeElapsed
                            }
                        )
                    } else {
                        // Display the stage review table after the game completes.
                        StageReviewTable(
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // Animated visibility for the PrizeBox, which slides in from the bottom.
            AnimatedVisibility(
                visible = isPrizeBoxVisible,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { fullHeight -> fullHeight },
                    animationSpec = tween(animationDuration)
                ),
                exit = fadeOut()
            ) {
                PrizeBox(
                    isDarkTheme = isDarkTheme,
                    onPrizeButtonClick = {
                        // Navigate to the game rewards page with the game results values.
                        navController.navigate(
                            "game_rewards/${minMovesForThisScramble}/${playerMoves}/${elapsedTime}/${difficulty.name}/$stageNumber"
                        )
                    }
                )
            }

            // Settings dialog for theme toggling, mute control, and exiting the application.
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
