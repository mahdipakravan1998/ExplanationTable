package com.example.explanationtable.ui.stages.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import com.example.explanationtable.ui.stages.components.DifficultyStepButton

/**
 * A composable function that displays a vertically scrollable list of stage buttons.
 * Each button is horizontally offset according to a dynamic pattern.
 *
 * @param difficulty The current difficulty level, used to determine the number of stages.
 * @param onStageClick Callback invoked with the stage number when a stage button is clicked.
 */
@Composable
fun StagesListContent(
    difficulty: Difficulty,
    onStageClick: (Int) -> Unit
) {
    // Determine the total number of stages based on the provided difficulty.
    // If the difficulty key is not found in the map, default to 9 stages.
    val totalSteps = difficultyStepCountMap[difficulty] ?: 9

    // Define a symmetric base pattern for horizontal offsets.
    // This pattern creates a visually appealing staggered layout.
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
                // Display the stage button with its corresponding difficulty and stage number.
                DifficultyStepButton(
                    difficulty = difficulty,
                    stepNumber = stageNumber,
                    onClick = { onStageClick(stageNumber) }
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
        // For indices within the base pattern, use the predefined offset.
        if (index < basePattern.size) {
            basePattern[index]
        } else {
            // For additional stages, cycle through the base pattern (skipping the first element).
            val cycleIndex = (index - 1) % (basePattern.size - 1) + 1
            basePattern[cycleIndex]
        }
    }
}
