package com.example.explanationtable.ui.rewards.pages

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.rewards.components.RewardsTable
import com.example.explanationtable.ui.components.PrimaryButton
import com.example.explanationtable.ui.components.SecondaryButton
import androidx.compose.material3.Text
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Routes

/**
 * Displays the game result screen with a rewards table and navigation buttons.
 *
 * @param isDarkTheme Determines whether the dark theme should be applied.
 * @param optimalMoves The optimal moves calculated by A*.
 * @param userAccuracy The fallback user accuracy value.
 * @param playerMoves The number of moves the player made.
 * @param elapsedTime The elapsed time of the game (in milliseconds).
 */
@Composable
fun GameResultScreen(
    isDarkTheme: Boolean,
    optimalMoves: Int,  // Optimal moves computed by A*
    userAccuracy: Int,  // Fallback accuracy if optimal moves weren't calculated
    playerMoves: Int,
    elapsedTime: Long,
    navController: NavController,
    difficulty: Difficulty,
    stageNumber: Int
) {
    BackHandler {
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxSize()
        ) {
            RewardsTable(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,  // Pass optimalMoves
                userAccuracy = userAccuracy,  // Pass userAccuracy
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize()
            )
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    PrimaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            val nextStage = stageNumber + 1
                            navController.navigate("gameplay/$nextStage/${difficulty.name}") {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = "مرحله بعدی",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(18.dp))
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate("gameplay/${stageNumber}/${difficulty.name}") {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = "دوباره بازی کن",
                        modifier = Modifier.fillMaxWidth()
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            navController.navigate("stages_list/${difficulty.name}") {
                                popUpTo(Routes.MAIN) { inclusive = true }
                            }
                        },
                        text = "بازگشت",
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}
