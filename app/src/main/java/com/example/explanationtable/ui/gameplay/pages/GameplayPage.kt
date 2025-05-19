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
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.gameplay.components.PrizeBox
import com.example.explanationtable.ui.gameplay.review.StageReviewTable
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.hint.dialog.HintDialogHandler
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    // -- Constants --
    val animationDurationMs = 300
    val pageTitle = "${stringResource(R.string.stage)} ${stageNumber.toPersianDigits()}"

    // -- Back navigation handler --
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // -- ViewModel and collected flows --
    val viewModel: MainViewModel = viewModel()
    val diamonds by viewModel.diamonds.collectAsState()

    // -- Dialog visibility state --
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }

    // -- References to Android context/activity --
    val context = LocalContext.current
    val activity = context as? Activity

    // -- Game progress state grouped in a data class for clarity --
    data class GameResult(
        var over: Boolean = false,
        var showPrize: Boolean = false,
        var optimalMoves: Int = 0,
        var accuracy: Int = 0,
        var playerMoves: Int = 0,
        var elapsedMs: Long = 0L
    )
    var result by remember { mutableStateOf(GameResult()) }

    // -- Table state and callback holder --
    var originalTable: EasyLevelTable? by remember { mutableStateOf(null) }
    var currentTable: MutableMap<CellPosition, List<String>>? by remember { mutableStateOf(null) }
    var onCellsCorrect: (List<CellPosition>) -> Unit by remember { mutableStateOf({}) }

    val coroutineScope = rememberCoroutineScope()

    // -- Reset state when stage or difficulty changes --
    LaunchedEffect(stageNumber, difficulty) {
        result = GameResult()
    }

    // -- Show prize box shortly after game ends --
    LaunchedEffect(result.over) {
        if (result.over) {
            delay(animationDurationMs.toLong())
            result = result.copy(showPrize = true)
        }
    }

    // -- Background wrapper for theming/layout --
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // TopAppBar with settings/help controls
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    onHelpClick = if (!result.over) ({ showHintDialog = true }) else null
                )

                Spacer(Modifier.height(72.dp))

                // Main content switches between gameplay and review
                AnimatedContent(
                    targetState = result.over,
                    transitionSpec = {
                        val enter = if (targetState) {
                            slideInHorizontally(
                                initialOffsetX = { it },
                                animationSpec = tween(animationDurationMs)
                            )
                        } else {
                            slideInHorizontally(
                                initialOffsetX = { -it },
                                animationSpec = tween(animationDurationMs)
                            )
                        }
                        val exit = if (targetState) {
                            slideOutHorizontally(
                                targetOffsetX = { -it },
                                animationSpec = tween(animationDurationMs)
                            )
                        } else {
                            slideOutHorizontally(
                                targetOffsetX = { it },
                                animationSpec = tween(animationDurationMs)
                            )
                        }
                        enter togetherWith exit using SizeTransform(clip = false)
                    }
                ) { gameOver ->
                    if (!gameOver) {
                        // Gameplay table with result callbacks
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { optimal, accuracy, moves, time ->
                                // Delay slightly before ending to allow last animation tick
                                coroutineScope.launch {
                                    delay(600)
                                    result = result.copy(
                                        over = true,
                                        optimalMoves = optimal,
                                        accuracy = accuracy,
                                        playerMoves = moves,
                                        elapsedMs = time
                                    )
                                }
                            },
                            onTableDataInitialized = { orig, current ->
                                originalTable = orig
                                currentTable = current
                            },
                            registerCellsCorrectlyPlacedCallback = { callback ->
                                onCellsCorrect = callback
                            }
                        )
                    } else {
                        // Review table shown after game completion
                        StageReviewTable(
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

            // PrizeBox slides in when eligible
            AnimatedVisibility(
                visible = result.showPrize,
                modifier = Modifier.align(Alignment.BottomCenter),
                enter = slideInVertically(
                    initialOffsetY = { it },
                    animationSpec = tween(animationDurationMs)
                ),
                exit = fadeOut()
            ) {
                PrizeBox(
                    isDarkTheme = isDarkTheme,
                    onPrizeButtonClick = {
                        navController.navigate(
                            "game_rewards/" +
                                    "${result.optimalMoves}/" +
                                    "${result.accuracy}/" +
                                    "${result.playerMoves}/" +
                                    "${result.elapsedMs}/" +
                                    "${difficulty.name}/" +
                                    "$stageNumber"
                        )
                    }
                )
            }

            // Settings dialog overlay
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss  = { showSettingsDialog = false },
                onExit     = { activity?.finishAndRemoveTask() }
            )

            // Hint dialog overlay
            HintDialogHandler(
                showDialog = showHintDialog,
                isDarkTheme = isDarkTheme,
                difficulty = difficulty,
                originalTableState = originalTable,
                currentTableState = currentTable,
                onDismiss = { showHintDialog = false },
                onCellsRevealed = { correctPositions ->
                    if (correctPositions.isEmpty()) {
                        result = result.copy(over = true)
                    } else {
                        onCellsCorrect(correctPositions)
                    }
                }
            )
        }
    }
}
