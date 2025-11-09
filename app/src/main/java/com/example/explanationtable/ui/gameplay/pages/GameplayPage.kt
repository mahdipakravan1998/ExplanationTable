package com.example.explanationtable.ui.gameplay.pages

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import com.example.explanationtable.R
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.ui.components.BackAnchor
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.gameplay.components.PrizeBox
import com.example.explanationtable.ui.gameplay.table.GameTable
import com.example.explanationtable.ui.gameplay.viewmodel.GameplayViewModel
import com.example.explanationtable.ui.hint.dialog.HintDialogHandler
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.review.pages.StageReviewTable
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.system.AppScreenScaffold
import com.example.explanationtable.ui.system.BottomBarAppearance
import com.example.explanationtable.ui.system.SystemBarsDefaults
import com.example.explanationtable.utils.toPersianDigits

private const val ANIM_DURATION_MS = 300

/**
 * Gameplay screen that orchestrates:
 * - TopBar (gems, title, actions)
 * - Live gameplay vs. review switch with animated transition
 * - Prize overlay that underlaps the nav bar but keeps actions tappable above it
 * - Settings & Hint dialogs
 *
 * Visual behavior and public API remain identical.
 *
 * @param navController Navigation host controller
 * @param isDarkTheme Current theme mode
 * @param stageNumber Stage identifier (shown in title; digits localized)
 * @param difficulty Current difficulty (used for routing and review)
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    // One stable "go back to stages" action used by both system back and on-screen back.
    val navigateToStages = remember(navController, difficulty) {
        {
            navController.navigate(Routes.stagesList(difficulty)) {
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
    }
    BackHandler(onBack = navigateToStages)

    // ViewModels
    val mainViewModel: MainViewModel = viewModel()
    val gameplayViewModel: GameplayViewModel = viewModel()
    val stageProgressViewModel: StageProgressViewModel = viewModel()

    // Collect StateFlows (no .value in composition)
    val diamondCount by mainViewModel.diamonds.collectAsState()
    val result        by gameplayViewModel.result.collectAsState()
    val originalTable by gameplayViewModel.originalTable.collectAsState()
    val currentTable  by gameplayViewModel.currentTable.collectAsState()

    // Ephemeral UI state with state restoration
    var isSettingsDialogVisible by rememberSaveable { mutableStateOf(false) }
    var isHintDialogVisible by rememberSaveable { mutableStateOf(false) }

    // Safe Activity (no direct cast)
    val activity = currentActivity()

    // Side-effects tied to stable keys
    LaunchedEffect(result.over) {
        if (result.over) stageProgressViewModel.markStageCompleted(difficulty, stageNumber)
    }
    LaunchedEffect(stageNumber, difficulty) {
        gameplayViewModel.resetGame()
    }

    val pageTitle = "${stringResource(R.string.stage)} ${stageNumber.toPersianDigits()}"
    val showPrize = result.showPrize

    // System bar behavior: underlap nav bar; ensure actionable content sits above it.
    val bottomAppearance = BottomBarAppearance.None
    val navBottomPadding: Dp = rememberNavBottomPadding()
    val prizeOverlayColor: Color = SystemBarsDefaults.prizeOverlayColor(isDarkTheme)

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
        // Floating center draws behind nav bar.
        floatingCenterRespectNavInsets = false,
        floatingBottomPadding = 0.dp,
        floatingStartRespectNavInsets = true,
        floatingEndRespectNavInsets = true,
        floatingStart = {
            if (!result.over) {
                BackAnchor(
                    isDarkTheme = isDarkTheme,
                    onClick = navigateToStages
                )
            }
        },
        floatingCenter = {
            PrizeOverlay(
                visible = showPrize,
                isDarkTheme = isDarkTheme,
                navBottomPadding = navBottomPadding,
                overlayColor = prizeOverlayColor,
                onPrimaryAction = {
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
    ) {
        GameplayContentSwitch(
            isGameOver = result.over,
            isDarkTheme = isDarkTheme,
            difficulty = difficulty,
            stageNumber = stageNumber,
            onGameComplete = { optimal, accuracy, moves, time ->
                gameplayViewModel.onGameComplete(optimal, accuracy, moves, time)
            },
            onTableInitialized = { orig: LevelTable, current: MutableMap<CellPosition, List<String>> ->
                gameplayViewModel.setTableData(orig, current)
            },
            onRegisterCellsCorrectlyPlaced = { callback: (List<CellPosition>) -> Unit ->
                gameplayViewModel.registerCellsCorrectlyPlacedCallback(callback)
            }
        )

        GameplayDialogs(
            isSettingsDialogVisible = isSettingsDialogVisible,
            onDismissSettings = { isSettingsDialogVisible = false },
            onExit = { activity?.finishAndRemoveTask() },
            isHintDialogVisible = isHintDialogVisible,
            onDismissHint = { isHintDialogVisible = false },
            isDarkTheme = isDarkTheme,
            difficulty = difficulty,
            originalTableState = originalTable, // LevelTable?
            currentTableState = currentTable,   // MutableMap<CellPosition, List<String>>?
            onCellsRevealed = { positions: List<CellPosition> ->
                gameplayViewModel.handleCellsRevealed(positions)
            }
        )
    }
}

/**
 * Computes the bottom navigation-bar padding in dp using WindowInsets.
 * This avoids reliance on helper extensions that may not exist across versions.
 */
@Composable
private fun rememberNavBottomPadding(): Dp {
    val density = LocalDensity.current
    val bottomPx = WindowInsets.navigationBars.getBottom(density)
    return with(density) { bottomPx.toDp() }
}

/**
 * Safely resolves the current Activity from LocalContext without direct casts.
 */
@Composable
private fun currentActivity(): Activity? {
    val context = LocalContext.current
    return remember(context) { context.findActivity() }
}

private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Animated prize overlay that slides in from bottom and draws a background
 * only behind the nav bar area while keeping the CTA above system bars.
 */
@Composable
private fun PrizeOverlay(
    visible: Boolean,
    isDarkTheme: Boolean,
    navBottomPadding: Dp,
    overlayColor: Color,
    onPrimaryAction: () -> Unit
) {
    AnimatedVisibility(
        visible = visible,
        enter = slideInVertically(
            initialOffsetY = { it },
            animationSpec = tween(ANIM_DURATION_MS)
        ),
        exit = fadeOut(animationSpec = tween(ANIM_DURATION_MS / 2))
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .wrapContentHeight(align = Alignment.Bottom)
        ) {
            // (1) Background behind only the nav bar height
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
                    .windowInsetsBottomHeight(WindowInsets.navigationBars)
                    .background(overlayColor)
            )
            // (2) PrizeBox content just above the nav bar
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = navBottomPadding)
            ) {
                PrizeBox(
                    isDarkTheme = isDarkTheme,
                    onPrizeButtonClick = onPrimaryAction
                )
            }
        }
    }
}

/**
 * Crossfades between live game and review with horizontal slide;
 * SizeTransform.clip=false prevents clipping during motion.
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
private fun GameplayContentSwitch(
    isGameOver: Boolean,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageNumber: Int,
    onGameComplete: (optimal: Int, accuracy: Int, moves: Int, elapsedMs: Long) -> Unit,
    onTableInitialized: (original: LevelTable, current: MutableMap<CellPosition, List<String>>) -> Unit,
    onRegisterCellsCorrectlyPlaced: ((List<CellPosition>) -> Unit) -> Unit
) {
    AnimatedContent(
        targetState = isGameOver,
        transitionSpec = {
            val enter = slideInHorizontally(
                initialOffsetX = { if (targetState) it else -it },
                animationSpec = tween(ANIM_DURATION_MS)
            )
            val exit = slideOutHorizontally(
                targetOffsetX = { if (targetState) -it else it },
                animationSpec = tween(ANIM_DURATION_MS)
            )
            enter togetherWith exit using SizeTransform(clip = false)
        }
    ) { gameOver ->
        if (!gameOver) {
            GameTable(
                isDarkTheme = isDarkTheme,
                difficulty = difficulty,
                stageNumber = stageNumber,
                onGameComplete = onGameComplete,
                onTableDataInitialized = onTableInitialized,
                registerCellsCorrectlyPlacedCallback = onRegisterCellsCorrectlyPlaced
            )
        } else {
            StageReviewTable(
                difficulty = difficulty,
                stageNumber = stageNumber,
                isDarkTheme = isDarkTheme
            )
        }
    }
}

/**
 * Hosts Settings and Hint dialogs. Types align with VM state and dialog contracts.
 */
@Composable
private fun GameplayDialogs(
    isSettingsDialogVisible: Boolean,
    onDismissSettings: () -> Unit,
    onExit: () -> Unit,
    isHintDialogVisible: Boolean,
    onDismissHint: () -> Unit,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    originalTableState: LevelTable?, // matches dialog signature
    currentTableState: MutableMap<CellPosition, List<String>>?, // matches dialog signature
    onCellsRevealed: (List<CellPosition>) -> Unit
) {
    SettingsDialog(
        showDialog = isSettingsDialogVisible,
        onDismiss = onDismissSettings,
        onExit = onExit
    )

    HintDialogHandler(
        showDialog = isHintDialogVisible,
        isDarkTheme = isDarkTheme,
        difficulty = difficulty,
        originalTableState = originalTableState,
        currentTableState = currentTableState,
        onDismiss = onDismissHint,
        onCellsRevealed = onCellsRevealed
    )
}
