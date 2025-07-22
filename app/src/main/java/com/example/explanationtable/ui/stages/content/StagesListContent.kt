package com.example.explanationtable.ui.stages.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel

// A reusable, symmetric pattern of horizontal offsets (in dp) for stage buttons.
private val BASE_OFFSET_PATTERN = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

/**
 * Produces a list of horizontal offsets for each stage button,
 * cycling through a base pattern (excluding the first element to avoid duplicate 0.dp ends).
 *
 * @param totalSteps Number of offsets (i.e., stages) to generate.
 * @param basePattern The repeating pattern of offsets.
 * @return List of Dp offsets, one per stage.
 */
fun generateStepOffsets(
    totalSteps: Int,
    basePattern: List<Dp> = BASE_OFFSET_PATTERN
): List<Dp> = List(totalSteps) { index ->
    if (index < basePattern.size) {
        // Within the initial pattern length, take directly.
        basePattern[index]
    } else {
        // Beyond the initial pattern, cycle through basePattern[1..end]
        val cycleIndex = (index - 1) % (basePattern.size - 1) + 1
        basePattern[cycleIndex]
    }
}

@Composable
fun StagesListContent(
    navController: NavController,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageViewModel: StageViewModel = viewModel(),
    progressViewModel: StageProgressViewModel = viewModel()
) {
    // Observe total number of stages
    val totalSteps by stageViewModel.stageCount.collectAsState()

    // Observe map of last unlocked stages per difficulty, default to 1 if missing
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedForThis = unlockedMap[difficulty] ?: 1

    // Precompute offsets for all stages in one go
    val stepOffsets = generateStepOffsets(totalSteps)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display each stage button with its calculated offset
        stepOffsets.forEachIndexed { index, offset ->
            val stageNumber = index + 1

            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .padding(vertical = 24.dp)
            ) {
                if (stageNumber <= unlockedForThis) {
                    // Unlocked: show a DifficultyStepButton that navigates into gameplay
                    DifficultyStepButton(
                        difficulty = difficulty,
                        stepNumber = stageNumber,
                        onClick = {
                            navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}")
                        }
                    )
                } else {
                    // Locked: show a placeholder LockedStepButton
                    LockedStepButton(
                        isDarkTheme = isDarkTheme,
                        stepNumber = stageNumber
                    )
                }
            }
        }

        // Bottom padding to ensure last button isn't cut off
        Spacer(modifier = Modifier.height(24.dp))
    }
}
