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
 */
@OptIn(ExperimentalAnimationApi::class)
@Composable
fun GameplayPage(
    navController: NavHostController,
    isDarkTheme: Boolean,
    stageNumber: Int,
    difficulty: Difficulty
) {
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    val viewModel: MainViewModel = viewModel()
    val diamonds by viewModel.diamonds.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity
    val pageTitle = "${stringResource(id = R.string.stage)} ${stageNumber.toPersianDigits()}"
    val animationDuration = 300

    var isGameOver by remember { mutableStateOf(false) }
    var isPrizeBoxVisible by remember { mutableStateOf(false) }

    // Separate state variables for the two types of accuracy-related values.
    var optimalMoves by remember { mutableStateOf(0) }
    var userAccuracy by remember { mutableStateOf(0) }
    var playerMoves by remember { mutableStateOf(0) }
    var elapsedTime by remember { mutableStateOf(0L) }

    LaunchedEffect(stageNumber, difficulty) {
        isGameOver = false
        isPrizeBoxVisible = false
        optimalMoves = 0
        userAccuracy = 0
        playerMoves = 0
        elapsedTime = 0L
    }

    LaunchedEffect(isGameOver) {
        if (isGameOver) {
            delay(animationDuration.toLong())
            isPrizeBoxVisible = true
        }
    }

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier.fillMaxSize(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = pageTitle,
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    onHelpClick = { /* TODO: Implement help action */ }
                )
                Spacer(modifier = Modifier.height(72.dp))
                AnimatedContent(
                    targetState = isGameOver,
                    transitionSpec = {
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
                    },
                    label = ""
                ) { targetGameOver ->
                    if (!targetGameOver) {
                        GameTable(
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            stageNumber = stageNumber,
                            onGameComplete = { optimal, accuracy, playerMoveCount, timeElapsed ->
                                isGameOver = true
                                optimalMoves = optimal
                                userAccuracy = accuracy
                                playerMoves = playerMoveCount
                                elapsedTime = timeElapsed
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
                        // In your GameplayPage (or wherever the navigation occurs):
                        navController.navigate(
                            "game_rewards/$optimalMoves/$userAccuracy/$playerMoves/$elapsedTime/${difficulty.name}/$stageNumber"
                        )
                    }
                )
            }
            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                isDarkTheme = isDarkTheme,
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted = isMuted,
                onToggleMute = { viewModel.toggleMute() },
                onExit = {
                    activity?.finishAndRemoveTask()
                }
            )
        }
    }
}
