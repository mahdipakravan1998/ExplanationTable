package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
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
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.components.BackAnchor
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
import com.example.explanationtable.ui.system.AppScreenScaffold
import com.example.explanationtable.ui.system.BottomBarAppearance
import com.example.explanationtable.ui.system.SystemBarsDefaults

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    val animationDuration = 300
    val pageTitle = "${stringResource(R.string.stage)} ${stageNumber.toPersianDigits()}"

    BackHandler {
        navController.navigate(Routes.stagesList(difficulty)) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    val mainViewModel: MainViewModel = viewModel()
    val diamondCount by mainViewModel.diamonds.collectAsState()

    val gameplayViewModel: GameplayViewModel = viewModel()
    val result by gameplayViewModel.result.collectAsState()
    val originalTable by gameplayViewModel.originalTable.collectAsState()
    val currentTable by gameplayViewModel.currentTable.collectAsState()

    val stageProgressViewModel: StageProgressViewModel = viewModel()

    var isSettingsDialogVisible by remember { mutableStateOf(false) }
    var isHintDialogVisible by remember { mutableStateOf(false) }

    // Android context and activity for dialog callbacks
    val context = LocalContext.current
    val activity = context as? Activity

    LaunchedEffect(result.over) {
        if (result.over) stageProgressViewModel.markStageCompleted(difficulty, stageNumber)
    }
    LaunchedEffect(stageNumber, difficulty) { gameplayViewModel.resetGame() }

    val showPrize = result.showPrize

    // We do NOT paint the nav-bar area separately. The PrizeBox will underlap the nav bar
    // so the bar shows the PrizeBox background naturally, synchronized with the animation.
    val bottomAppearance = BottomBarAppearance.None

    // PrizeBox will underlap the nav bar, so keep its *content* above the nav bar via padding.
    val navBottomPadding = WindowInsets.navigationBars
        .asPaddingValues()
        .calculateBottomPadding()

    val prizeBoxBgColor = SystemBarsDefaults.prizeOverlayColor(isDarkTheme)

    AppScreenScaffold(
        isHomePage = false,
        isDarkTheme = isDarkTheme,
        topBar = {
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = pageTitle,
                gems = diamondCount,
                difficulty = difficulty,
                onSettingsClick = { isSettingsDialogVisible = true },
                onHelpClick = if (!result.over) ({ isHintDialogVisible = true }) else null
            )
        },
        contentTopSpacing = 72.dp,
        bottomBarAppearance = bottomAppearance,
        // Floating center must NOT respect nav insets so it can draw behind the nav bar.
        floatingCenterRespectNavInsets = false,
        // No extra gap; the content padding we add below keeps text/buttons visible.
        floatingBottomPadding = 0.dp,
        floatingStartRespectNavInsets = true,
        floatingEndRespectNavInsets = true,
        floatingStart = {
            if (!result.over) {
                BackAnchor(
                    isDarkTheme = isDarkTheme,
                    onClick = {
                        navController.navigate(Routes.MAIN) {
                            popUpTo(Routes.MAIN) { inclusive = true }
                        }
                    }
                )
            }
        },
        floatingCenter = {
            AnimatedVisibility(
                visible = showPrize,
                enter = slideInVertically(
                    // Slide up from just below the bottom edge.
                    initialOffsetY = { it },
                    animationSpec = tween(animationDuration)
                ),
                exit = fadeOut(animationSpec = tween(animationDuration / 2))
            ) {
                // Stack two things:
                // 1) A thin background "filler" that occupies ONLY the nav-bar height, so
                //    the nav area shows the PrizeBox color as the box slides in.
                // 2) The PrizeBox itself, padded up so its CONTENT stays above the nav bar.
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentHeight(align = Alignment.Bottom) // keep tight to its content
                ) {
                    // (1) Background behind the nav bar area
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .fillMaxWidth()
                            .windowInsetsBottomHeight(WindowInsets.navigationBars)
                            .background(prizeBoxBgColor)
                    )

                    // (2) PrizeBox content just above the nav bar
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = navBottomPadding)
                    ) {
                        PrizeBox(
                            isDarkTheme = isDarkTheme,
                            onPrizeButtonClick = {
                                val route = Routes.gameRewards(
                                    optimalMoves = result.optimalMoves,
                                    userAccuracy = result.accuracy,
                                    playerMoves = result.playerMoves,
                                    elapsedTime = result.elapsedMs,
                                    difficulty = difficulty,
                                    stageNumber = stageNumber
                                )
                                navController.navigate(route)
                            }
                        )
                    }
                }
            }
        }
    ) {
        AnimatedContent(
            targetState = result.over,
            transitionSpec = {
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
                StageReviewTable(
                    difficulty = difficulty,
                    stageNumber = stageNumber,
                    isDarkTheme = isDarkTheme
                )
            }
        }

        SettingsDialog(
            showDialog = isSettingsDialogVisible,
            onDismiss  = { isSettingsDialogVisible = false },
            onExit     = { activity?.finishAndRemoveTask() }
        )

        HintDialogHandler(
            showDialog         = isHintDialogVisible,
            isDarkTheme        = isDarkTheme,
            difficulty         = difficulty,
            originalTableState = originalTable,
            currentTableState  = currentTable,
            onDismiss          = { isHintDialogVisible = false },
            onCellsRevealed    = { correctPositions ->
                gameplayViewModel.handleCellsRevealed(correctPositions)
            }
        )
    }
}
