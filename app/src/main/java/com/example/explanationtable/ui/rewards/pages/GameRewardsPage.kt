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
 * The screen overlays a rewards table on a themed background and positions
 * three navigation buttons at the bottom of the screen:
 * - A primary button to navigate to the next page.
 * - A secondary button to replay the game.
 * - A secondary button to navigate back.
 *
 * @param isDarkTheme Determines whether the dark theme should be applied.
 * @param minMoves The minimum moves for the stage.
 * @param playerMoves The number of moves the player took.
 * @param elapsedTime The time elapsed during the game in milliseconds.
 */
@Composable
fun GameResultScreen(
    isDarkTheme: Boolean,
    minMoves: Int,
    playerMoves: Int,
    elapsedTime: Long,
    navController: NavController,
    difficulty: Difficulty,
    stageNumber: Int
) {
    // Handle the back button press
    BackHandler {
        // Navigate to the stages list page with difficulty passed
        navController.navigate("stages_list/${difficulty.name}") {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Apply the background with theme settings; not a home page.
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        // Main container that fills the entire screen.
        Box(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 32.dp)
                .fillMaxSize()
        ) {
            // Display the rewards table occupying the full available space.
            RewardsTable(
                isDarkTheme = isDarkTheme,
                minMoves = minMoves,
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                modifier = Modifier.fillMaxSize()
            )
            // Overlay container for aligning the navigation buttons at the bottom center.
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.BottomCenter
            ) {
                // Arrange the buttons vertically with proper spacing and padding.
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // Primary button to navigate to the next page.
                    PrimaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            // Navigate to the next stage or another screen
                            // Assuming you want to go to the next stage
                            val nextStage = stageNumber + 1  // Increment the current stage number to get the next one
                            navController.navigate("gameplay/$nextStage/${difficulty.name}") {
                                // Optionally clear the back stack so that the user can't go back to the result page
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }
                            }
                        },
                        text = "مرحله بعدی",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(18.dp))

                    // Secondary button to replay the game.
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            // Navigate back to the gameplay page and restart the game
                            navController.navigate("gameplay/${stageNumber}/${difficulty.name}") {
                                popUpTo(Routes.GAME_REWARDS_WITH_ARGS) { inclusive = true }  // Pop the current reward screen off the back stack
                            }
                        },
                        text = "دوباره بازی کن",
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    // Secondary button to navigate back.
                    SecondaryButton(
                        isDarkTheme = isDarkTheme,
                        onClick = {
                            // Navigate to the stages list page with difficulty passed
                            navController.navigate("stages_list/${difficulty.name}") {
                                popUpTo(Routes.MAIN) { inclusive = true } // Clear back stack if needed
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
