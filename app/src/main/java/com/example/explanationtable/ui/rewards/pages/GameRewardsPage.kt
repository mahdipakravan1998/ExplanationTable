package com.example.explanationtable.ui.rewards.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.rewards.components.RewardsTable
import com.example.explanationtable.ui.components.buttons.PrimaryButton
import com.example.explanationtable.ui.components.buttons.SecondaryButton
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.R
import com.example.explanationtable.ui.rewards.viewmodel.RewardsViewModel

/**
 * Displays the game result screen with a rewards table and navigation buttons.
 *
 * @param isDarkTheme Determines whether the dark theme should be applied.
 * @param optimalMoves The optimal moves calculated by A*.
 * @param userAccuracy The fallback user accuracy value.
 * @param playerMoves The number of moves the player made.
 * @param elapsedTime The elapsed time of the game (in milliseconds).
 * @param navController The navigation controller to manage screen transitions.
 * @param difficulty The difficulty level of the current game.
 * @param stageNumber The current stage number.
 * @param viewModel RewardsViewModel used by the table to award diamonds.
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
    // Handle the back navigation when the user presses the back button.
    BackHandler {
        navController.navigate(Routes.stagesList(difficulty)) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Main background for the result screen
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxSize()
        ) {
            // Display the rewards table with the results
            RewardsTable(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,
                userAccuracy = userAccuracy,
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize(),
                viewModel = viewModel
            )

            // Bottom section with navigation buttons
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Next stage button
                    PrimaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            val nextStage = stageNumber + 1
                            navController.navigate(Routes.gameplay(nextStage, difficulty)) {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = stringResource(id = R.string.next_stage_button),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Spacer between buttons
                    Spacer(modifier = Modifier.height(18.dp))

                    // Replay button
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate(Routes.gameplay(stageNumber, difficulty)) {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = stringResource(id = R.string.replay_button),
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Spacer between buttons
                    Spacer(modifier = Modifier.height(16.dp))

                    // Return to stages list button
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate(Routes.stagesList(difficulty)) {
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
