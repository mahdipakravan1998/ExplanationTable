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
import com.example.explanationtable.ui.gameplay.review.StageReviewTable
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.hint.dialog.HintDialogHandler
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.gameplay.viewmodel.GameplayViewModel
import com.example.explanationtable.utils.toPersianDigits

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    // Constants
    val animationDurationMs = 300
    val pageTitle = "${stringResource(R.string.stage)} ${stageNumber.toPersianDigits()}"

    // Back navigation
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // MainViewModel (diamonds)
    val mainVm: MainViewModel = viewModel()
    val diamonds by mainVm.diamonds.collectAsState()

    // GameplayViewModel (new)
    val gameVm: GameplayViewModel = viewModel()
    val result by gameVm.result.collectAsState()
    val originalTable by gameVm.originalTable.collectAsState()
    val currentTable by gameVm.currentTable.collectAsState()

    // UI‐only dialog flags
    var showSettingsDialog by remember { mutableStateOf(false) }
    var showHintDialog by remember { mutableStateOf(false) }

    // Android context/activity
    val context = LocalContext.current
    val activity = context as? Activity

    // Reset game whenever stage or difficulty changes
    LaunchedEffect(stageNumber, difficulty) {
        gameVm.resetGame()
    }

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
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
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { optimal, accuracy, moves, time ->
                                gameVm.onGameComplete(optimal, accuracy, moves, time)
                            },
                            onTableDataInitialized = { orig, current ->
                                gameVm.setTableData(orig, current)
                            },
                            registerCellsCorrectlyPlacedCallback = { callback ->
                                gameVm.registerCellsCorrectlyPlacedCallback(callback)
                            }
                        )
                    } else {
                        StageReviewTable(
                            stageNumber = stageNumber,
                            isDarkTheme = isDarkTheme
                        )
                    }
                }
            }

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

            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss  = { showSettingsDialog = false },
                onExit     = { activity?.finishAndRemoveTask() }
            )

            HintDialogHandler(
                showDialog = showHintDialog,
                isDarkTheme = isDarkTheme,
                difficulty = difficulty,
                originalTableState = originalTable,
                currentTableState = currentTable,
                onDismiss = { showHintDialog = false },
                onCellsRevealed = { correctPositions ->
                    gameVm.handleCellsRevealed(correctPositions)
                }
            )
        }
    }
}
