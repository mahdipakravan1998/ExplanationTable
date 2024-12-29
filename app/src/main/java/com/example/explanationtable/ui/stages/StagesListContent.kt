// File: StagesListContent.kt
package com.example.explanationtable.ui.stages

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap

/**
 * A composable that displays a scrollable list of step buttons with horizontal offsets.
 *
 * @param difficulty The current difficulty level.
 */
@Composable
fun StagesListContent(
    difficulty: Difficulty
) {
    // Retrieve the number of steps for the given difficulty, defaulting to 9 if not found
    val totalSteps = difficultyStepCountMap[difficulty] ?: 9

    // Define the fixed pattern of horizontal offsets
    val fixedStepOffsets = listOf(
        0.dp,    // Step 1
        40.dp,   // Step 2
        80.dp,   // Step 3
        40.dp,   // Step 4
        0.dp,    // Step 5
        (-40).dp, // Step 6
        (-80).dp, // Step 7
        (-40).dp, // Step 8
        0.dp     // Step 9
    )

    // Generate the stepOffsets list by repeating the fixed pattern as needed
    val stepOffsets = List(totalSteps) { index ->
        fixedStepOffsets[index % fixedStepOffsets.size]
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Iterate over each offset and place a button accordingly
        stepOffsets.forEachIndexed { index, offsetX ->
            Box(
                modifier = Modifier
                    .offset(x = offsetX)
                    .padding(vertical = 12.dp)
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
