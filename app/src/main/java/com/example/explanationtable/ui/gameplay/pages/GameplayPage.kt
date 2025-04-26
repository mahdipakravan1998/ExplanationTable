package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import androidx.navigation.NavHostController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.gameplay.components.PrizeBox
import com.example.explanationtable.ui.gameplay.review.StageReviewTable
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.hint.HintDialog
import com.example.explanationtable.ui.hint.revealRandomCategory
import com.example.explanationtable.ui.hint.revealRandomCell
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.delay

/**
 * Composable function that represents the gameplay screen for a specific stage.
 *
 * @param navController Navigation controller for navigating to other screens.
 * @param isDarkTheme Indicates if the dark theme is enabled.
 * @param stageNumber The number of the current stage.
 * @param difficulty The difficulty level of the current stage.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    // Handle back press to navigate to the stage list
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // ViewModel to manage state
    val viewModel: MainViewModel = viewModel()
    val diamonds by viewModel.diamonds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    // Local states for controlling dialog visibility
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }

    // Context and activity for app-related functions
    val context = LocalContext.current
    val activity = context as? Activity

    // Page title and animation duration
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"
    val animationDuration = 300

    // State for tracking game progress and results
    var isGameOver by remember { mutableStateOf(false) }
    var isPrizeBoxVisible by remember { mutableStateOf(false) }
    var optimalMoves by remember { mutableStateOf(0) }
    var userAccuracy by remember { mutableStateOf(0) }
    var playerMoves by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }

    var originalTableState by remember { mutableStateOf<EasyLevelTable?>(null) }
    var currentTableState by remember { mutableStateOf<MutableMap<CellPosition, List<String>>?>(null) }

    // New state to hold reference to the callback for notifying cells are correct
    var notifyCorrectCellsCallback by remember { mutableStateOf<(List<CellPosition>) -> Unit>({}) }

    // Reset game-related state on stage or difficulty change
    LaunchedEffect(stageNumber, difficulty) {
        isGameOver = false
        isPrizeBoxVisible = false
        optimalMoves = 0
        userAccuracy = 0
        playerMoves = 0
        elapsedTime = 0L
    }

    // Show prize box animation after game is over
    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            delay(animationDuration.toLong())
            isPrizeBoxVisible = true
        }
    }

    // Background setup for the gameplay page
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top bar with title, settings, and help button
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    onHelpClick = if (!isGameOver) {
                        { showHintDialog = true }
                    } else {
                        null // Help button is disabled and invisible when game is over
                    }
                )
                Spacer(modifier = Modifier.height(72.dp))

                // Game content with animated transitions based on game state
                AnimatedContent(
                    targetState = isGameOver,
                    transitionSpec = {
                        // Slide in and out based on the game state
                        if (targetState) {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(animationDuration)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(animationDuration)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { fullWidth -> -fullWidth },
                                animationSpec = tween(animationDuration)
                            ) togetherWith slideOutHorizontally(
                                targetOffsetX = { fullWidth -> fullWidth },
                                animationSpec = tween(animationDuration)
                            )
                        }.using(SizeTransform(clip = false))
                    }
                ) { targetGameOver ->
                    if (!targetGameOver) {
                        // Display the game table when the game is not over
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { optimal, accuracy, playerMoveCount, timeElapsed ->
                                // Set game over state and capture results
                                isGameOver = true
                                optimalMoves = optimal
                                userAccuracy = accuracy
                                playerMoves = playerMoveCount
                                elapsedTime = timeElapsed
                            },
                            onTableDataInitialized = { origData, currentData ->
                                originalTableState = origData
                                currentTableState = currentData
                            },
                            // New parameter to receive callback for correctly placed cells notification
                            registerCellsCorrectlyPlacedCallback = { callback ->
                                notifyCorrectCellsCallback = callback
                            }
                        )
                    } else {
                        // Display stage review when the game is over
                        StageReviewTable(
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // Show prize box animation when the game ends
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
                        // Navigate to the rewards screen after the prize button is clicked
                        navController.navigate(
                            "game_rewards/$optimalMoves/$userAccuracy/$playerMoves/$elapsedTime/${difficulty.name}/$stageNumber"
                        )
                    }
                )
            }

            // Settings dialog for user preferences
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss  = { showSettingsDialog = false },
                onExit     = { activity?.finishAndRemoveTask() }
            )

            // Show hint dialog when help button is clicked
            if (showHintDialog) {
                HintDialog(
                    showDialog = showHintDialog,
                    onDismiss = { showHintDialog = false },
                    isDarkTheme = isDarkTheme,
                    difficulty = difficulty,
                    onOptionSelected = { selectedOption ->
                        if (selectedOption.displayText == context.getString(R.string.hint_single_word)) {
                            // Only call if the states have been initialized.
                            originalTableState?.let { origData ->
                                currentTableState?.let { currData ->
                                    // Pass the callback to the revealRandomCategoryHelp function
                                    revealRandomCategory(
                                        currentTableData = currData,
                                        originalTableData = origData,
                                        onCellsCorrectlyPlaced = { correctPositions ->
                                            // Notify the table about cells that are now correctly placed
                                            notifyCorrectCellsCallback(correctPositions)
                                        }
                                    )
                                }
                            }
                        } else if (selectedOption.displayText == context.getString(R.string.hint_single_letter)) {
                            // Only call if the states have been initialized.
                            originalTableState?.let { origData ->
                                currentTableState?.let { currData ->
                                    // Pass the callback to the revealRandomCellHelp function
                                    revealRandomCell(
                                        currentTableData = currData,
                                        originalTableData = origData,
                                        onCellCorrectlyPlaced = { correctPositions ->
                                            // Notify the table about the single cell that is now correctly placed
                                            notifyCorrectCellsCallback(correctPositions)
                                        }
                                    )
                                }
                            }
                        } else if (selectedOption.displayText == context.getString(R.string.hint_complete_stage)) {
                            isGameOver = true
                        }
                        showHintDialog = false
                    }
                )
            }
        }
    }
}
