package com.example.explanationtable.ui.stages.content

import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
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

/**
 * Layout constants for the stages list.
 */
object StageListDefaults {
    val ButtonContainerHeight = 77.dp
    val ButtonVerticalPadding = 8.dp
    val ListVerticalPadding = 16.dp
}

/**
 * A repeating zig-zag offset pattern for stage buttons.
 * The first entry is unique; subsequent cycles skip the zero to avoid duplication.
 */
private val DefaultOffsetPattern: List<Dp> = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

/**
 * Generate horizontal offsets for each stage button so they form
 * a zig-zag pattern. If totalSteps exceeds the base pattern length,
 * the pattern (excluding its first element) repeats as needed.
 *
 * @param totalSteps number of stages
 * @param basePattern zig-zag pattern template
 * @return list of offsets, one per stage
 */
fun generateStepOffsets(
    totalSteps: Int,
    basePattern: List<Dp> = DefaultOffsetPattern
): List<Dp> = buildList {
    if (totalSteps <= basePattern.size) {
        // If fewer stages than pattern entries, just truncate the pattern
        addAll(basePattern.take(totalSteps))
        return@buildList
    }

    // Emit full base pattern once
    addAll(basePattern)

    // Then repeat the remainder of the pattern (excluding the first element)
    val cycle = basePattern.drop(1)
    repeat(totalSteps - basePattern.size) { index ->
        add(cycle[index % cycle.size])
    }
}

@Composable
fun StagesListContent(
    navController: NavController,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    scrollState: ScrollState,
    onTargetOffsetChanged: (Int) -> Unit = {},
    onViewportHeightChanged: (Int) -> Unit = {},
    stageViewModel: StageViewModel = viewModel(),
    progressViewModel: StageProgressViewModel = viewModel()
) {
    // Refresh stage count whenever difficulty changes
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Collect total number of stages (initially zero)
    val totalSteps by stageViewModel.stageCount.collectAsState(initial = 0)

    // Collect last unlocked stage per difficulty; default to stage 1
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState(initial = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    // Compute horizontal offsets once per totalSteps change
    val stepOffsets = remember(totalSteps) {
        generateStepOffsets(totalSteps)
    }

    // Track viewport height in pixels (for centering logic)
    var viewportHeightPx by remember { mutableStateOf(0) }
    val density = LocalDensity.current

    // Target scroll position (px) for centering the unlocked stage
    var targetOffsetPx by remember { mutableIntStateOf(0) }

    // When prerequisites are ready, animate scroll to center the unlocked stage
    LaunchedEffect(unlockedStage, totalSteps, viewportHeightPx) {
        if (totalSteps > 0 && viewportHeightPx > 0) {
            val scrollTarget = computeCenterOffset(
                unlockedStage = unlockedStage,
                viewportHeightPx = viewportHeightPx,
                density = density,
                buttonHeightDp = StageListDefaults.ButtonContainerHeight,
                buttonPaddingDp = StageListDefaults.ButtonVerticalPadding,
                listPaddingDp = StageListDefaults.ListVerticalPadding
            )
            targetOffsetPx = scrollTarget
            onTargetOffsetChanged(scrollTarget)

            scrollState.animateScrollTo(
                scrollTarget,
                animationSpec = tween(
                    durationMillis = 600,
                    easing = EaseInOutCubic
                )
            )
        }
    }

    // Main scrollable column containing all stage buttons
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .onGloballyPositioned { coords ->
                // Capture viewport height once and notify
                if (viewportHeightPx == 0) {
                    viewportHeightPx = coords.size.height
                    onViewportHeightChanged(viewportHeightPx)
                }
            }
            .padding(vertical = StageListDefaults.ListVerticalPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        stepOffsets.forEachIndexed { index, offset ->
            val stageNumber = index + 1

            Box(
                modifier = Modifier
                    .offset(x = offset)
                    .padding(vertical = StageListDefaults.ButtonVerticalPadding)
            ) {
                if (stageNumber <= unlockedStage) {
                    // Unlocked: show clickable difficulty-themed button
                    DifficultyStepButton(
                        difficulty = difficulty,
                        stepNumber = stageNumber,
                        onClick = {
                            navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}")
                        }
                    )
                } else {
                    // Locked: show non-interactive placeholder
                    LockedStepButton(
                        isDarkTheme = isDarkTheme,
                        stepNumber = stageNumber
                    )
                }
            }
        }

        // Add bottom padding so last button isn't flush to the bottom
        Spacer(modifier = Modifier.height(StageListDefaults.ButtonVerticalPadding))
    }
}
