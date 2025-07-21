package com.example.explanationtable.ui.stages.content

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.collectAsState
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

@Composable
fun StagesListContent(
    navController: NavController,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageViewModel: StageViewModel = viewModel()
) {
    val stageProgress: StageProgressViewModel = viewModel()

    // Get the stage count from ViewModel
    val totalSteps by stageViewModel.stageCount.collectAsState()
    val unlockedMap by stageProgress.lastUnlocked.collectAsState()
    val unlockedForThis = unlockedMap[difficulty] ?: 1

    // Define a symmetric base pattern for horizontal offsets
    val baseOffsetPattern = listOf(
        0.dp, 40.dp, 80.dp, 40.dp, 0.dp, (-40).dp, (-80).dp, (-40).dp, 0.dp
    )

    // Generate the list of horizontal offsets for each stage button
    val stepOffsets = generateStepOffsets(totalSteps, baseOffsetPattern)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stepOffsets.forEachIndexed { index, offset ->
            val stageNumber = index + 1

            Box(modifier = Modifier.offset(x = offset).padding(vertical = 24.dp)) {
                if (stageNumber <= unlockedForThis) {
                    DifficultyStepButton(
                        difficulty = difficulty,
                        stepNumber = stageNumber,
                        onClick = {
                            navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}")
                        }
                    )
                } else {
                    LockedStepButton(isDarkTheme = isDarkTheme)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

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
