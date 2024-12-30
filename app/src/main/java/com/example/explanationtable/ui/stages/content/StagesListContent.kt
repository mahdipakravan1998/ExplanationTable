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
 * A composable that displays a scrollable list of step buttons with dynamic horizontal offsets.
 *
 * @param difficulty The current difficulty level.
 */
@Composable
fun StagesListContent(
    difficulty: Difficulty
) {
    val totalSteps = difficultyStepCountMap[difficulty] ?: 9

    // Define the base pattern of horizontal offsets
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

    // Generate the stepOffsets list based on totalSteps
    val stepOffsets: List<Dp> = generateStepOffsets(totalSteps, baseOffsetPattern)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Iterate over each step and apply the corresponding offset
        stepOffsets.forEachIndexed { index, offsetX ->
            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .padding(vertical = 24.dp)
            ) {
                DifficultyStepButton(
                    difficulty = difficulty,
                    stepNumber = index + 1
                )
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
    }
}

/**
 * Generates a list of horizontal offsets for step buttons based on the total number of steps
 * and a predefined base pattern.
 *
 * @param totalSteps The total number of steps.
 * @param basePattern The base pattern of horizontal offsets.
 * @return A list of horizontal offsets corresponding to each step.
 */
fun generateStepOffsets(totalSteps: Int, basePattern: List<Dp>): List<Dp> {
    return List(totalSteps) { index ->
        if (index < basePattern.size) {
            basePattern[index]
        } else {
            // Calculate the offset by cycling through the base pattern, excluding the first element to maintain symmetry
            val cycleIndex = (index - 1) % (basePattern.size - 1) + 1
            basePattern[cycleIndex]
        }
    }
}

