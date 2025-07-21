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
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.gameplay.components.PrizeBox
import com.example.explanationtable.ui.review.pages.StageReviewTable
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.hint.dialog.HintDialogHandler
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.gameplay.viewmodel.GameplayViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.utils.toPersianDigits

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    // Animation timing constant
    val animationDuration = 300

    // Title string with localized "Stage" + Persian digits
    val pageTitle = "${stringResource(R.string.stage)} ${stageNumber.toPersianDigits()}"

    // Handle Android back button: navigate back to stages list
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Obtain MainViewModel for diamond count
    val mainViewModel: MainViewModel = viewModel()
    val diamondCount by mainViewModel.diamonds.collectAsState()

    // Obtain GameplayViewModel for game state
    val gameplayViewModel: GameplayViewModel = viewModel()
    val result by gameplayViewModel.result.collectAsState()
    val originalTable by gameplayViewModel.originalTable.collectAsState()
    val currentTable by gameplayViewModel.currentTable.collectAsState()

    // Obtain the ViewModel responsible for tracking stage progress
    val stageProgressViewModel: StageProgressViewModel = viewModel()

    // UI state flags for dialogs
    var isSettingsDialogVisible by remember { mutableStateOf(false) }
    var isHintDialogVisible by remember { mutableStateOf(false) }

    // Android context and activity for dialog callbacks
    val context = LocalContext.current
    val activity = context as? Activity

    // Side‐effect: when the game-over flag flips to true, mark this stage as completed
    LaunchedEffect(result.over) {
        if (result.over) {
            stageProgressViewModel.markStageCompleted(
                difficulty = difficulty,
                stage = stageNumber
            )
        }
    }

    // Whenever stage number or difficulty changes, reset the game state
    LaunchedEffect(stageNumber, difficulty) {
        gameplayViewModel.resetGame()
    }

    // Root background wrapper
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {

            // Main column: Top bar + game or review table
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top application bar with title, gems, settings & help buttons
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = diamondCount,
                    difficulty = difficulty,
                    onSettingsClick = { isSettingsDialogVisible = true },
                    onHelpClick = if (!result.over) ({ isHintDialogVisible = true }) else null
                )

                Spacer(modifier = Modifier.height(72.dp))

                // Switch between gameplay and review with sliding animation
                AnimatedContent(
                    targetState = result.over,
                    transitionSpec = {
                        // Slide in/out from left or right based on game-over state
                        val enter = slideInHorizontally(
                            initialOffsetX = { if (targetState) it else -it },
                            animationSpec = tween(animationDuration)
                        )
                        val exit  = slideOutHorizontally(
                            targetOffsetX  = { if (targetState) -it else it },
                            animationSpec  = tween(animationDuration)
                        )
                        enter togetherWith exit using SizeTransform(clip = false)
                    }
                ) { isGameOver ->
                    if (!isGameOver) {
                        // Active game table
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { optimal, accuracy, moves, time ->
                                gameplayViewModel.onGameComplete(optimal, accuracy, moves, time)
                            },
                            onTableDataInitialized = { orig, current ->
                                gameplayViewModel.setTableData(orig, current)
                            },
                            registerCellsCorrectlyPlacedCallback = { callback ->
                                gameplayViewModel.registerCellsCorrectlyPlacedCallback(callback)
                            }
                        )
                    } else {
                        // Review table shown after game over
                        StageReviewTable(
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // Prize box at bottom when available, with vertical slide/fade animation
            AnimatedVisibility(
                visible = result.showPrize,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(animationDuration)
                ),
                exit = fadeOut()
            ) {
                PrizeBox(
                    isDarkTheme = isDarkTheme,
                    onPrizeButtonClick = {
                        // Build and navigate to rewards route with parameters
                        val route = buildString {
                            append("game_rewards/")
                            append("${result.optimalMoves}/")
                            append("${result.accuracy}/")
                            append("${result.playerMoves}/")
                            append("${result.elapsedMs}/")
                            append("${difficulty.name}/")
                            append(stageNumber)
                        }
                        navController.navigate(route)
                    }
                )
            }

            // Settings dialog overlay
            SettingsDialog(
                showDialog = isSettingsDialogVisible,
                onDismiss  = { isSettingsDialogVisible = false },
                onExit     = { activity?.finishAndRemoveTask() }
            )

            // Hint dialog overlay
            HintDialogHandler(
                showDialog             = isHintDialogVisible,
                isDarkTheme            = isDarkTheme,
                difficulty             = difficulty,
                originalTableState     = originalTable,
                currentTableState      = currentTable,
                onDismiss              = { isHintDialogVisible = false },
                onCellsRevealed        = { correctPositions ->
                    gameplayViewModel.handleCellsRevealed(correctPositions)
                }
            )
        }
    }
}
