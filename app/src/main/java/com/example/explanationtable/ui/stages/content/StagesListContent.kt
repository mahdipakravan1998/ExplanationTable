package com.example.explanationtable.ui.stages.content

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlinx.coroutines.delay

// A reusable, symmetric pattern of horizontal offsets (in dp) for stage buttons.
private val BASE_OFFSET_PATTERN = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

// --- Named Constants for Layout Calculations ---

// The size of the container Box for each stage button.
private val STAGE_BUTTON_CONTAINER_SIZE = 82.dp

// The vertical padding applied to each stage button's container, pushing items apart.
private val STAGE_BUTTON_VERTICAL_PADDING = 24.dp

// The vertical padding for the entire scrolling Column.
private val STAGES_LIST_VERTICAL_PADDING = 16.dp


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
    // 1. Fetch stage count when difficulty changes
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Observe total number of stages
    val totalSteps by stageViewModel.stageCount.collectAsState()

    // Observe map of last unlocked stages per difficulty, default to 1 if missing
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedForThis = unlockedMap[difficulty] ?: 1

    // Precompute offsets for all stages in one go
    val stepOffsets = generateStepOffsets(totalSteps)

    // 2. State to hold the measured height of the Column
    var columnHeightPx by remember { mutableStateOf(0) }

    // 3. Create a controllable ScrollState and get local density for pixel calculations
    val scrollState = rememberScrollState()
    val density = LocalDensity.current

    // 4. This effect scrolls to the last unlocked stage when its data and the column height are available
    LaunchedEffect(unlockedForThis, totalSteps, columnHeightPx) {
        // Ensure the list and the layout are ready before trying to scroll
        if (totalSteps > 0 && columnHeightPx > 0) {
            // ✅ **REFACTORED**: Calculation now uses named constants for clarity.
            // The total height of a single stage item in the list.
            val itemHeightDp = STAGE_BUTTON_CONTAINER_SIZE + (STAGE_BUTTON_VERTICAL_PADDING * 2)
            val itemHeightPx = with(density) { itemHeightDp.toPx() }

            // The padding applied to the top of the scrolling Column itself.
            val columnInnerTopPaddingPx = with(density) { STAGES_LIST_VERTICAL_PADDING.toPx() }

            // This calculation assumes an additional 16.dp of padding is being applied by a
            // parent container, for a total of 32.dp. The ideal fix is to remove the
            // outer padding, but this will work for the current layout.
            val totalTopPaddingPx = columnInnerTopPaddingPx * 2

            val targetIndex = unlockedForThis - 1

            // Calculate the absolute center position of the target item from the top of the Column
            val targetItemCenterPx = totalTopPaddingPx + (targetIndex * itemHeightPx) + (itemHeightPx / 2f)

            // Calculate the scroll amount needed to place the item's center in the viewport's center
            val targetScrollPx = (targetItemCenterPx - columnHeightPx / 2f).toInt()

            // Animate scroll to the target. The value is automatically clamped within valid bounds.
            scrollState.animateScrollTo(
                value = targetScrollPx,
                animationSpec = tween(
                    durationMillis = 600, // Slower, smoother scroll duration
                    easing = EaseInOutCubic // Starts and ends slow, accelerates in the middle
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            // 5. Use onGloballyPositioned to capture the Column's height
            .onGloballyPositioned { coordinates ->
                if (columnHeightPx == 0) { // Set the height only once
                    columnHeightPx = coordinates.size.height
                }
            }
            // ✅ **REFACTORED**: Using the named constant for padding.
            .padding(vertical = STAGES_LIST_VERTICAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Display each stage button with its calculated offset
        stepOffsets.forEachIndexed { index, offset ->
            val stageNumber = index + 1

            Box(
                modifier = Modifier
                    .offset(x = offset)
                    // ✅ **REFACTORED**: Using the named constant for padding.
                    .padding(vertical = STAGE_BUTTON_VERTICAL_PADDING)
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
