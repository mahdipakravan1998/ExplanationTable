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

/**
 * Displays the post-game results and actions (Next stage, Replay, Return).
 *
 * This composable is intentionally **UI-only** and does not perform IO.
 * All business logic / data loading is delegated to [RewardsTable] and its [RewardsViewModel].
 *
 * Behavior preserved:
 * - Back press navigates to the stages list for the current [difficulty] and clears back stack up to MAIN.
 * - "Next" navigates to the computed next stage (if available), popping current rewards from back stack.
 * - "Replay" restarts the same stage, popping current rewards from back stack.
 * - "Return" goes to the stage list and clears back stack up to MAIN.
 */
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
    // Handle Android back to return to the stage list for the current difficulty.
    BackHandler {
        navController.navigate(Routes.stagesList(difficulty)) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Build a fixed snapshot of stage counts from the static map once per composition.
    // (No need to rebuild on recomposition; the map is effectively constant.)
    val counts: StageCounts = remember {
        StageCounts.fromMap(difficultyStepCountMap, defaultCount = 9)
    }

    // Resolve the next target when inputs that can affect it change.
    val nextTarget: NextTarget? = resolveNextTarget(difficulty, stageNumber, counts)

    // Cheap, deterministic derived routes -> no need to remember.
    val nextRoute: String? = nextTarget?.let { Routes.gameplay(it.stage, it.difficulty) }
    val replayRoute: String = Routes.gameplay(stageNumber, difficulty)
    val listRoute: String = Routes.stagesList(difficulty)

    // Stabilize onClick lambdas to avoid referential changes across recompositions.
    val onNextStageClick: () -> Unit = remember(nextRoute, navController) {
        {
            nextRoute?.let { route ->
                navController.navigate(route) {
                    // Remove the current rewards screen from back stack
                    popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                }
            }
        }
    }
    val onReplayClick: () -> Unit = remember(replayRoute, navController) {
        {
            navController.navigate(replayRoute) {
                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
            }
        }
    }
    val onReturnClick: () -> Unit = remember(listRoute, navController) {
        {
            navController.navigate(listRoute) {
                // Clear back stack to MAIN, consistent with BackHandler
                popUpTo(Routes.MAIN) { inclusive = true }
            }
        }
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
            // Delegates observable UI state to RewardsTable (unchanged behavior)
            RewardsTable(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,
                userAccuracy = userAccuracy,
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )

            // Bottom actions; insets keep buttons above the system navigation bar.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .windowInsetsPadding(WindowInsets.navigationBars),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    if (nextRoute != null) {
                        PrimaryButton(
                            isDarkTheme = isDarkTheme,
                            onClick = onNextStageClick,
                            text = stringResource(id = R.string.next_stage_button),
                            modifier = Modifier.fillMaxWidth()
                        )
                        Spacer(modifier = Modifier.height(18.dp))
                    }

                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = onReplayClick,
                        text = stringResource(id = R.string.replay_button),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = onReturnClick,
                        text = stringResource(id = R.string.return_button),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
