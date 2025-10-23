package com.example.explanationtable.ui.stages.content

import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.ColorMatrix
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.util.computeCenterOffset
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import androidx.lifecycle.viewmodel.compose.viewModel

/**
 * Layout constants for the stages list (700x700 assets; pixel-perfect near ~175dp base).
 * Public and stable; values are referenced by geometry computations and side-art scaling.
 */
object StageListDefaults {
    val ButtonContainerHeight = 77.dp
    val ButtonVerticalPadding = 8.dp
    val ListVerticalPadding = 16.dp

    // Uniform visual height for all side art (before per-art overrides)
    val SideImageDesiredHeight = 136.dp

    // Both chests appear slightly smaller for balanced composition
    const val ChestScaleFactor: Float = 0.60f

    // Row baseline height so scaling won’t affect spacing
    val SideImageBaseHeight = ButtonContainerHeight

    // Optional width cap (usually unused with square 700x700 canvases)
    val SideImageMaxWidth: Dp = Dp.Unspecified

    // Generic edge padding used by all side art
    val SideImageEdgePadding = 32.dp

    // Extra inward inset ONLY for gold chests (tweak to taste)
    val ChestSideInset = 24.dp
}

/**
 * Stages list screen with side-art, chest interaction, and a floating callout over the unlocked stage.
 * Behavior and visuals are preserved exactly; internals are clarified and memoized.
 */
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
    // Stage counts and global maps are driven by VMs (no IO in UI).
    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }
    LaunchedEffect(Unit) { stageViewModel.fetchAllStageCounts() }

    val totalSteps by stageViewModel.stageCount.collectAsStateWithLifecycle(initialValue = 0)
    val allCounts by stageViewModel.allStageCounts.collectAsStateWithLifecycle(initialValue = emptyMap())

    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    // ---- LOCAL, SCREEN-ONLY TOP PADDING (52dp when first step unlocked; else 16dp) ----
    val topPaddingDp = StageListDefaults.ListVerticalPadding + if (unlockedStage == 1) 36.dp else 0.dp

    val claimedChests: Set<Int> by stageViewModel
        .claimedChests(difficulty)
        .collectAsStateWithLifecycle(initialValue = emptySet())

    // Tracks chests opened in-session for optimistic UI of "opened" art
    val justOpenedRemember = remember { mutableStateListOf<Int>() }

    // Offsets + extreme stages for the current total count
    val stepOffsets = remember(totalSteps) { generateStepOffsets(totalSteps) }
    val extremeStages = remember(stepOffsets) { extremeStageIndices(stepOffsets) }
    val extremeSeq = remember(difficulty) { extremeSequenceFor(difficulty) }

    // Map extreme stage number → slot (bee/pencil/chest) by cycling the sequence
    val extremeSlotMap: Map<Int, Slot> = remember(extremeStages, extremeSeq) {
        buildMap {
            extremeStages.forEachIndexed { ordZero, stageNum ->
                val slot = if (extremeSeq.isNotEmpty()) extremeSeq[ordZero % extremeSeq.size] else Slot.BEE
                put(stageNum, slot)
            }
        }
    }

    // Global offsets across difficulties ensure non-repeating art order
    val offsets = remember(difficulty, allCounts) { globalOffsetsFor(difficulty, allCounts) }

    // Resolve extreme art assignment Map<stageNumber, drawableId?>; null → chest or none
    val extremeArtAssignment: Map<Int, Int?> = remember(difficulty, extremeSlotMap, offsets) {
        var localBee = 0
        var localPencil = 0
        buildMap {
            extremeStages.forEach { stage ->
                when (extremeSlotMap[stage]) {
                    Slot.BEE -> {
                        localBee += 1
                        val globalBeeOrd = offsets.bees + localBee
                        put(stage, resolveBeeDrawable(globalBeeOrd))
                    }
                    Slot.PENCIL -> {
                        localPencil += 1
                        val globalPencilOrd = offsets.pencils + localPencil
                        put(stage, resolvePencilDrawable(globalPencilOrd))
                    }
                    Slot.CHEST, null -> put(stage, null)
                }
            }
        }
    }

    // Density-derived scaling
    val density = LocalDensity.current
    val basePx = with(density) { StageListDefaults.SideImageBaseHeight.toPx() }
    val uniformScale = remember(density) {
        val desiredPx = with(density) { StageListDefaults.SideImageDesiredHeight.toPx() }
        if (basePx > 0f) desiredPx / basePx else 1f
    }
    val chestScale = remember(uniformScale) { uniformScale * StageListDefaults.ChestScaleFactor }

    // Grayscale filter reused for locked side-art
    val grayscaleFilter = remember {
        val m = ColorMatrix().apply { setToSaturation(0f) }
        ColorFilter.colorMatrix(m)
    }

    // Viewport + scroll targeting
    var viewportHeightPx by remember { mutableStateOf(0) }
    var targetOffsetPx by remember { mutableStateOf(0) }
    var requestCalibration by remember { mutableStateOf(false) } // trigger a one-shot calibration after initial scroll

    // Track our programmatic scroll so gating also applies during animateScrollTo
    var isProgrammaticScroll by remember { mutableStateOf(false) }

    // Center on the unlocked stage whenever inputs change
    LaunchedEffect(unlockedStage, totalSteps, viewportHeightPx, topPaddingDp) {
        if (totalSteps > 0 && viewportHeightPx > 0) {
            val scrollTarget = computeCenterOffset(
                unlockedStage = unlockedStage,
                viewportHeightPx = viewportHeightPx,
                density = density,
                buttonHeightDp = StageListDefaults.ButtonContainerHeight,
                buttonPaddingDp = StageListDefaults.ButtonVerticalPadding,
                listPaddingDp = topPaddingDp
            )
            targetOffsetPx = scrollTarget
            onTargetOffsetChanged(scrollTarget)

            // Programmatic centering of the unlocked stage
            isProgrammaticScroll = true
            try {
                scrollState.animateScrollTo(
                    scrollTarget,
                    animationSpec = tween(600, easing = EaseInOutSine)
                )
            } finally {
                isProgrammaticScroll = false
            }

            // Ask for calibration right after the programmatic scroll settles
            requestCalibration = true
        }
    }

    // Geometry derived from the button & bubble code. 70.dp baseline ellipse → ~0.9 height factor.
    val frontEllipseHeightPx = with(density) { (70.dp * 0.9f).toPx() }

    // Root overlay state (for the floating bubble) + size for math anchoring
    var rootTopLeftInWindow by remember { mutableStateOf(Offset.Zero) }
    var rootWidthPx by remember { mutableStateOf(0) }
    // Anchor (global) computed via math + calibration
    var stageAnchorInWindow by remember { mutableStateOf<Offset?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .clipToBounds() // keep bubble under the TopBar
            .onGloballyPositioned { coords ->
                rootTopLeftInWindow = coords.boundsInWindow().topLeft
                rootWidthPx = coords.size.width
            }
    ) {
        // ===== math constants used by both scroll-driven anchor and one-shot calibration =====
        val listPadTopPx = with(density) { topPaddingDp.toPx() }
        val buttonContainerH = with(density) { StageListDefaults.ButtonContainerHeight.toPx() }
        val buttonPadV = with(density) { StageListDefaults.ButtonVerticalPadding.toPx() }
        val rowHeightPx = buttonContainerH + 2f * buttonPadV
        val unlockedIndex = unlockedStage - 1
        val offsetXPx = with(density) { if (unlockedIndex in stepOffsets.indices) stepOffsets[unlockedIndex].toPx() else 0f }

        // Calibration: constant correction added to math so the bubble is exactly tangent.
        var anchorXCorrectionPx by remember(unlockedStage, rootWidthPx, frontEllipseHeightPx) { mutableStateOf(0f) }
        var anchorYCorrectionPx by remember(unlockedStage, rootWidthPx, frontEllipseHeightPx) { mutableStateOf(0f) }
        var needCalibrate by remember(unlockedStage, rootWidthPx, viewportHeightPx, stepOffsets) { mutableStateOf(true) }

        // Motion gating: true during drag/fling and during our programmatic scroll
        val isUserScrolling by remember { derivedStateOf { scrollState.isScrollInProgress } }
        val isContentMoving = isUserScrolling || isProgrammaticScroll

        // ---------- Scrollable list ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coords ->
                    if (viewportHeightPx == 0) {
                        viewportHeightPx = coords.size.height
                        onViewportHeightChanged(viewportHeightPx)
                    }
                }
                .padding(top = topPaddingDp, bottom = StageListDefaults.ListVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val stepOffsetsLocal = stepOffsets // avoid capturing a mutable snapshot inside the loop
            stepOffsetsLocal.forEachIndexed { index, offset ->
                val stageNumber = index + 1
                val isExtreme = isExtremeOffset(offset)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .heightIn(min = StageListDefaults.ButtonContainerHeight)
                        .padding(vertical = StageListDefaults.ButtonVerticalPadding)
                ) {
                    // Side art on extreme lanes
                    if (isExtreme) {
                        val slot = extremeSlotMap[stageNumber] ?: Slot.BEE
                        val resolvedArtId: Int? = when (slot) {
                            Slot.CHEST -> {
                                val isUnlocked = stageNumber <= unlockedStage
                                val isClaimed = claimedChests.contains(stageNumber)
                                val isJustOpened = justOpenedRemember.contains(stageNumber)
                                when {
                                    !isUnlocked -> ChestLocked
                                    isClaimed || isJustOpened -> ChestOpened
                                    else -> ChestUnlocked
                                }
                            }
                            Slot.BEE, Slot.PENCIL -> extremeArtAssignment[stageNumber]
                        }

                        if (resolvedArtId != null) {
                            val placeLeft = (offset == 80.dp)

                            var scale = when (slot) {
                                Slot.CHEST -> chestScale
                                Slot.BEE, Slot.PENCIL -> uniformScale
                            }
                            if (slot != Slot.CHEST) {
                                PerArtScaleOverrides[resolvedArtId]?.let { scale *= it }
                            }

                            val applyBw = (slot == Slot.BEE || slot == Slot.PENCIL) && stageNumber > unlockedStage
                            val colorFilter = if (applyBw) grayscaleFilter else null

                            val isChestClickable =
                                slot == Slot.CHEST &&
                                        stageNumber <= unlockedStage &&
                                        !claimedChests.contains(stageNumber)

                            // Stable onClick via remember to avoid reallocation
                            val onChestClick = remember(
                                stageNumber, difficulty, isChestClickable, justOpenedRemember.size
                            ) {
                                {
                                    if (isChestClickable) {
                                        if (!justOpenedRemember.contains(stageNumber)) {
                                            justOpenedRemember.add(stageNumber)
                                        }
                                        stageViewModel.claimChest(difficulty, stageNumber)
                                    }
                                }
                            }

                            Image(
                                painter = painterResource(id = resolvedArtId),
                                contentDescription = when (slot) {
                                    Slot.CHEST -> when {
                                        stageNumber > unlockedStage -> "Locked gold chest"
                                        claimedChests.contains(stageNumber) -> "Opened gold chest"
                                        else -> "Gold chest"
                                    }
                                    Slot.BEE -> "Bee character"
                                    Slot.PENCIL -> "Pencil character"
                                },
                                contentScale = ContentScale.Fit,
                                colorFilter = colorFilter,
                                modifier = Modifier
                                    .align(if (placeLeft) Alignment.CenterStart else Alignment.CenterEnd)
                                    .padding(
                                        start = if (placeLeft)
                                            StageListDefaults.SideImageEdgePadding +
                                                    (if (slot == Slot.CHEST) StageListDefaults.ChestSideInset else 0.dp)
                                        else
                                            StageListDefaults.SideImageEdgePadding,
                                        end = if (!placeLeft)
                                            StageListDefaults.SideImageEdgePadding +
                                                    (if (slot == Slot.CHEST) StageListDefaults.ChestSideInset else 0.dp)
                                        else
                                            StageListDefaults.SideImageEdgePadding
                                    )
                                    .height(StageListDefaults.SideImageBaseHeight)
                                    .then(
                                        if (StageListDefaults.SideImageMaxWidth != Dp.Unspecified)
                                            Modifier.widthIn(max = StageListDefaults.SideImageMaxWidth)
                                        else Modifier
                                    )
                                    .graphicsLayer {
                                        scaleX = scale
                                        scaleY = scale
                                        transformOrigin = TransformOrigin(
                                            pivotFractionX = if (placeLeft) 0f else 1f,
                                            pivotFractionY = 0.5f
                                        )
                                    }
                                    .then(
                                        if (isChestClickable) {
                                            Modifier.clickable(onClick = onChestClick)
                                        } else Modifier
                                    )
                            )
                        }
                    }

                    // Stage button (center + horizontal offset)
                    Box(
                        modifier = Modifier
                            .align(Alignment.Center)
                            .offset(x = offset)
                            // One-shot calibration hook (only on the unlocked row, only when idle)
                            .then(
                                if (stageNumber == unlockedStage &&
                                    (needCalibrate || requestCalibration) &&
                                    !isContentMoving &&
                                    rootWidthPx > 0
                                ) {
                                    Modifier.onGloballyPositioned { coords ->
                                        // Measured (ground truth) position
                                        val b = coords.boundsInWindow()
                                        val measuredAnchorTop = b.top + (b.height - frontEllipseHeightPx) / 2f
                                        val measuredAnchorX = b.center.x

                                        // Math anchor at this exact scroll
                                        val scrollY = scrollState.value
                                        val rowTopLocal =
                                            listPadTopPx + (unlockedIndex * rowHeightPx) - scrollY
                                        val anchorTopLocal =
                                            rowTopLocal + buttonPadV + (buttonContainerH - frontEllipseHeightPx) / 2f
                                        val anchorXLocal = (rootWidthPx.toFloat() / 2f) + offsetXPx
                                        val mathAnchorTop = rootTopLeftInWindow.y + anchorTopLocal
                                        val mathAnchorX = rootTopLeftInWindow.x + anchorXLocal

                                        // Compute & store constant correction
                                        anchorXCorrectionPx = measuredAnchorX - mathAnchorX
                                        anchorYCorrectionPx = measuredAnchorTop - mathAnchorTop

                                        // Apply immediately so it takes effect even without another scroll tick
                                        stageAnchorInWindow = Offset(
                                            x = mathAnchorX + anchorXCorrectionPx,
                                            y = mathAnchorTop + anchorYCorrectionPx
                                        )

                                        needCalibrate = false
                                        requestCalibration = false
                                    }
                                } else Modifier
                            )
                    ) {
                        if (stageNumber <= unlockedStage) {
                            val onStageClick = remember(stageNumber, difficulty) {
                                { navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}") }
                            }
                            DifficultyStepButton(
                                difficulty = difficulty,
                                stepNumber = stageNumber,
                                onClick = onStageClick
                            )
                        } else {
                            LockedStepButton(isDarkTheme = isDarkTheme, stepNumber = stageNumber)
                        }
                    }
                }
            }
            Spacer(Modifier.height(StageListDefaults.ButtonVerticalPadding))
        }

        // --- Scroll-driven math anchor (fast) + constant calibration (exact) ---
        LaunchedEffect(
            unlockedStage, stepOffsets, rootWidthPx, viewportHeightPx, frontEllipseHeightPx, topPaddingDp
        ) {
            if (rootWidthPx == 0 || unlockedStage <= 0 || unlockedStage > stepOffsets.size) return@LaunchedEffect

            snapshotFlow { scrollState.value }
                .distinctUntilChanged()
                .conflate() // drop intermediate deltas while we're updating
                .collectLatest { scrollY ->
                    // Sync with the choreographer frame to avoid jitter
                    withFrameNanos { /* frame boundary */ }

                    val rowTopLocal = listPadTopPx + (unlockedIndex * rowHeightPx) - scrollY
                    val anchorTopLocal = rowTopLocal + buttonPadV + (buttonContainerH - frontEllipseHeightPx) / 2f
                    val anchorXLocal = (rootWidthPx.toFloat() / 2f) + offsetXPx

                    stageAnchorInWindow = Offset(
                        x = rootTopLeftInWindow.x + anchorXLocal + anchorXCorrectionPx,
                        y = rootTopLeftInWindow.y + anchorTopLocal + anchorYCorrectionPx
                    )
                }
        }

        // ---------- Floating CalloutBubble overlay (isolated & gated animation) ----------
        FloatingCalloutBubble(
            isDarkTheme = isDarkTheme,
            anchorInWindow = stageAnchorInWindow,
            rootTopLeftInWindow = rootTopLeftInWindow,
            viewportHeightPx = viewportHeightPx,
            isContentMoving = isContentMoving
        )
    }
}
