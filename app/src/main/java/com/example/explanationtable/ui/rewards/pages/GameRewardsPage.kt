package com.example.explanationtable.ui.rewards.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.domain.rewards.NextTarget
import com.example.explanationtable.domain.rewards.StageCounts
import com.example.explanationtable.domain.rewards.resolveNextTarget
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import com.example.explanationtable.ui.components.buttons.PrimaryButton
import com.example.explanationtable.ui.components.buttons.SecondaryButton
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.rewards.components.RewardsTable
import com.example.explanationtable.ui.rewards.viewmodel.RewardsViewModel
import com.example.explanationtable.ui.system.AppScreenScaffold

@Composable
fun GameResultScreen(
    isDarkTheme: Boolean,
    optimalMoves: Int,
    userAccuracy: Int,
    playerMoves: Int,
    elapsedTime: Long,
    navController: NavController,
    difficulty: Difficulty,
    stageNumber: Int,
    viewModel: RewardsViewModel
) {
    BackHandler {
        navController.navigate(Routes.stagesList(difficulty)) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    val counts = remember {
        StageCounts.fromMap(difficultyStepCountMap, defaultCount = 9)
    }

    val nextTarget: NextTarget? = remember(difficulty, stageNumber, counts.easy, counts.medium, counts.hard) {
        resolveNextTarget(difficulty, stageNumber, counts)
    }

    val nextRoute: String? = remember(nextTarget) {
        nextTarget?.let { Routes.gameplay(it.stage, it.difficulty) }
    }
    val replayRoute: String = remember(difficulty, stageNumber) {
        Routes.gameplay(stageNumber, difficulty)
    }
    val listRoute: String = remember(difficulty) {
        Routes.stagesList(difficulty)
    }

    AppScreenScaffold(
        isHomePage = false,
        isDarkTheme = isDarkTheme,
        topBar = null
    ) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxSize()
        ) {
            RewardsTable(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,
                userAccuracy = userAccuracy,
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars), // keep buttons above nav bar
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (nextRoute != null) {
                        PrimaryButton(
                            isDarkTheme = isDarkTheme,
                            onClick = {
                                navController.navigate(nextRoute) {
                                    popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                                }
                            },
                            text = stringResource(id = R.string.next_stage_button),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate(replayRoute) {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = stringResource(id = R.string.replay_button),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate(listRoute) {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        },
                        text = stringResource(id = R.string.return_button),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
