package com.example.explanationtable.ui.stages.content

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
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
import com.example.explanationtable.ui.stages.util.computeCenterOffset
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel

// Pattern of horizontal offsets for stage buttons, creating a symmetrical zig-zag layout.
private val OFFSET_PATTERN = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

// Size and padding constants for layout calculations
private val BUTTON_CONTAINER_SIZE = 77.dp                // Height of each stage button container
private val BUTTON_VERTICAL_PADDING = 8.dp               // Vertical spacing around each button
private val LIST_VERTICAL_PADDING = 16.dp                // Vertical padding for the entire list

/**
 * Generate a list of horizontal offsets for [totalSteps] items, cycling through [basePattern].
 * The first pattern element is only used once to avoid duplicate 0.dp at the sequence ends.
 */
fun generateStepOffsets(
    totalSteps: Int,
    basePattern: List<Dp> = OFFSET_PATTERN
): List<Dp> = List(totalSteps) { index ->
    when {
        index < basePattern.size ->
            basePattern[index]  // Directly use the initial pattern segment
        else -> {
            // Cycle through basePattern[1..end] for subsequent items
            val cycleSize = basePattern.size - 1
            val cycleIndex = ((index - 1) % cycleSize) + 1
            basePattern[cycleIndex]
        }
    }
}

@Composable
fun StagesListContent(
    navController: NavController,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    scrollState: ScrollState,
    onTargetOffsetChanged: (Int) -> Unit = {},
    stageViewModel: StageViewModel = viewModel(),
    progressViewModel: StageProgressViewModel = viewModel()
) {
    // Fetch updated stage count whenever the difficulty setting changes
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Total number of stages available for this difficulty
    val totalSteps by stageViewModel.stageCount.collectAsState()

    // Map of last unlocked stage index per difficulty; default to 1 if not present
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedStage = unlockedMap[difficulty] ?: 1

    // Precompute horizontal offsets once per totalSteps change
    val stepOffsets = remember(totalSteps) {
        generateStepOffsets(totalSteps)
    }

    // Holds the pixel height of the scrolling column, captured via onGloballyPositioned
    var columnHeightPx by remember { mutableStateOf(0) }

    // Density for dp-to-px conversions
    val density = LocalDensity.current

    // Exposed target offset for parent callback
    var targetCenterOffset by remember { mutableIntStateOf(0) }

    // Automatically animate scroll to center the unlocked stage when layout & data are ready
    LaunchedEffect(unlockedStage, totalSteps, columnHeightPx) {
        if (totalSteps > 0 && columnHeightPx > 0) {
            val scrollTo = computeCenterOffset(
                unlockedStage = unlockedStage,
                columnHeightPx = columnHeightPx,
                density = density,
                buttonContainerSize = BUTTON_CONTAINER_SIZE,
                buttonVerticalPadding = BUTTON_VERTICAL_PADDING,
                listVerticalPadding = LIST_VERTICAL_PADDING
            )
            targetCenterOffset = scrollTo
            onTargetOffsetChanged(scrollTo)
            scrollState.animateScrollTo(
                scrollTo,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = EaseInOutCubic
                )
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .onGloballyPositioned { coords ->
                // Capture the Column height once for scroll calculations
                if (columnHeightPx == 0) {
                    columnHeightPx = coords.size.height
                }
            }
            .padding(vertical = LIST_VERTICAL_PADDING),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Render each stage button at its computed horizontal offset
        stepOffsets.forEachIndexed { index, offset ->
            val stageNumber = index + 1

            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .padding(vertical = BUTTON_VERTICAL_PADDING)
            ) {
                if (stageNumber <= unlockedStage) {
                    // Unlocked stage: clickable gameplay button
                    DifficultyStepButton(
                        difficulty = difficulty,
                        stepNumber = stageNumber,
                        onClick = {
                            navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}")
                        }
                    )
                } else {
                    // Locked stage: placeholder button indicating locked status
                    LockedStepButton(
                        isDarkTheme = isDarkTheme,
                        stepNumber = stageNumber
                    )
                }
            }
        }

        // Ensure the last button has space below it
        Spacer(modifier = Modifier.height(BUTTON_VERTICAL_PADDING))
    }
}
