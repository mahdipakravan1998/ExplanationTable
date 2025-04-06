package com.example.explanationtable.ui.stages.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import com.example.explanationtable.ui.stages.components.DifficultyStepButton

/**
 * A composable function that displays a vertically scrollable list of stage buttons.
 * Each button is horizontally offset according to a dynamic pattern.
 *
 * @param navController The NavController used for navigation.
 * @param difficulty The current difficulty level, used to determine the number of stages.
 */
@Composable
fun StagesListContent(
    navController: NavController,
    difficulty: Difficulty
) {
    // Track which stage is loading (if any). Null means no stage is loading.
    var loadingStage by remember { mutableStateOf<Int?>(null) }

    // Determine the total number of stages based on the provided difficulty.
    // If the difficulty key is not found in the map, default to 9 stages.
    val totalSteps = difficultyStepCountMap[difficulty] ?: 9

    // Define a symmetric base pattern for horizontal offsets.
    val baseOffsetPattern = listOf(
        0.dp,
        40.dp,
        80.dp,
        40.dp,
        0.dp,
        (-40).dp,
        (-80).dp,
        (-40).dp,
        0.dp
    )

    // Generate the list of horizontal offsets for each stage button.
    val stepOffsets: List<Dp> = generateStepOffsets(totalSteps, baseOffsetPattern)

    // Create a vertically scrollable column to contain the stage buttons.
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Iterate over each offset, generating a stage button with the corresponding horizontal position.
        stepOffsets.forEachIndexed { index, offset ->
            // Stage numbers are 1-indexed.
            val stageNumber = index + 1

            // Use a Box to apply horizontal offset and vertical padding to each button.
            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .padding(vertical = 24.dp)
            ) {
                // Each button is enabled only if its stage number does not match the currently loading stage.
                DifficultyStepButton(
                    difficulty = difficulty,
                    stepNumber = stageNumber,
                    enabled = loadingStage != stageNumber,
                    onClick = {
                        // Only update loadingStage if no button is already loading.
                        if (loadingStage == null) {
                            loadingStage = stageNumber
                            // Navigate using launchSingleTop to avoid multiple instances of the destination.
                            navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name.lowercase()}") {
                                launchSingleTop = true
                            }
                        }
                    }
                )
            }
        }
        // Add a spacer at the bottom of the list to provide extra padding.
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Generates a list of horizontal offsets for stage buttons based on a predefined base pattern.
 *
 * If the total number of steps exceeds the base pattern length, the pattern is cycled
 * (excluding the first element to maintain the symmetric design).
 *
 * @param totalSteps The total number of stage buttons to display.
 * @param basePattern The predefined list of horizontal offsets.
 * @return A list of horizontal offsets corresponding to each stage button.
 */
fun generateStepOffsets(totalSteps: Int, basePattern: List<Dp>): List<Dp> {
    return List(totalSteps) { index ->
        if (index < basePattern.size) {
            basePattern[index]
        } else {
            val cycleIndex = (index - 1) % (basePattern.size - 1) + 1
            basePattern[cycleIndex]
        }
    }
}
