package com.example.explanationtable.ui.stages.content

import android.util.Log
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.preflight.ReadinessHooks
import com.example.explanationtable.ui.stages.util.computeCenterOffset
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged

private const val TAG_CONTENT = "StagesListContent"

/**
 * Layout constants for the stages list (700x700 assets; pixel-perfect near ~175dp base).
 * Public and stable; values are referenced by geometry computations and side-art scaling.
 */
object StageListDefaults {
    val ButtonContainerHeight = 77.dp
    val ButtonVerticalPadding = 8.dp
    val ListVerticalPadding = 16.dp

    // NEW: extra gap below the last step, in addition to the nav bar inset.
    // This is the “plus some other distance” you asked for.
    val BottomSafeExtraPadding = 24.dp

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
 *
 * Behaviour for the bubble and scrolling is the same as your original version:
 * - Bubble stays above the last unlocked stage.
 * - Bubble bobs only when content is NOT moving (no scrolling / animation).
 *
 * Additional parameters (readinessHooks, firstRenderInstantCenter, enableProgrammaticCentering)
 * are used only for the off-screen preflight pipeline.
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
    progressViewModel: StageProgressViewModel = viewModel(),
    readinessHooks: ReadinessHooks = ReadinessHooks(),
    firstRenderInstantCenter: Boolean = false,
    enableProgrammaticCentering: Boolean = true,
    allowBubbleCalibration: Boolean = true
) {
    // Stage counts and global maps are driven by VMs (no IO in UI).
    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }
    LaunchedEffect(Unit) { stageViewModel.fetchAllStageCounts() }

    val totalSteps by stageViewModel.stageCount.collectAsStateWithLifecycle(initialValue = 0)
    val allCounts by stageViewModel.allStageCounts.collectAsStateWithLifecycle(initialValue = emptyMap())

    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    // ---- READINESS FLAGS (one-shot per difficulty) ----
    var stageDataReadySignalSent by remember(difficulty) { mutableStateOf(false) }
    var viewportMeasuredSignalSent by remember(difficulty) { mutableStateOf(false) }
    var targetOffsetSignalSent by remember(difficulty) { mutableStateOf(false) }
    var initialSnapSettledSignalSent by remember(difficulty) { mutableStateOf(false) }
    var bubbleCalibratedSignalSent by remember(difficulty) { mutableStateOf(false) }
    var visualsSettledSignalSent by remember(difficulty) { mutableStateOf(false) }

    // Approximate "data ready" and "visuals settled" once we have at least one step.
    LaunchedEffect(totalSteps, difficulty, unlockedStage) {
        if (!stageDataReadySignalSent && totalSteps > 0) {
            stageDataReadySignalSent = true
            Log.d(TAG_CONTENT, "StageDataReady → totalSteps=$totalSteps, difficulty=$difficulty")
            readinessHooks.onStageDataReady?.invoke()
        }
        if (!visualsSettledSignalSent && totalSteps > 0) {
            visualsSettledSignalSent = true
            Log.d(
                TAG_CONTENT,
                "VisualsSettled → difficulty=$difficulty, unlockedStage=$unlockedStage"
            )
            readinessHooks.onVisualsSettled?.invoke()
        }
    }

    // ---- LOCAL, SCREEN-ONLY TOP PADDING (52dp when first step unlocked; else 16dp) ----
    val topPaddingDp =
        StageListDefaults.ListVerticalPadding + if (unlockedStage == 1) 36.dp else 0.dp

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
                val slot =
                    if (extremeSeq.isNotEmpty()) extremeSeq[ordZero % extremeSeq.size] else Slot.BEE
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

    // Unified "content is moving" flag: true whenever the scroll offset is changing
    // (user drag, fling, or any animateScrollTo), false shortly after it settles.
    var isContentMoving by remember { mutableStateOf(false) }

    LaunchedEffect(scrollState) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .collectLatest {
                // Any scroll offset change → content is moving
                isContentMoving = true

                // If no new scroll event arrives for a short time, consider it settled
                delay(80)
                isContentMoving = false
            }
    }

    // Center on the unlocked stage whenever inputs change (original behaviour),
    // with optional instant-centre for preflight (firstRenderInstantCenter).
    LaunchedEffect(
        unlockedStage,
        totalSteps,
        viewportHeightPx,
        topPaddingDp,
        firstRenderInstantCenter,
        enableProgrammaticCentering
    ) {
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

            if (!targetOffsetSignalSent) {
                targetOffsetSignalSent = true
                Log.d(
                    TAG_CONTENT,
                    "TargetOffsetComputed → difficulty=$difficulty, unlockedStage=$unlockedStage, target=$scrollTarget"
                )
                readinessHooks.onTargetOffsetComputed?.invoke(scrollTarget)
            }

            if (enableProgrammaticCentering) {
                if (firstRenderInstantCenter) {
                    // Preflight can use this to snap instantly off-screen
                    scrollState.scrollTo(scrollTarget)
                } else {
                    scrollState.animateScrollTo(
                        scrollTarget,
                        animationSpec = tween(600, easing = EaseInOutSine)
                    )
                }

                // Ask for calibration right after the programmatic scroll settles
                requestCalibration = true

                if (!initialSnapSettledSignalSent) {
                    initialSnapSettledSignalSent = true
                    Log.d(
                        TAG_CONTENT,
                        "InitialSnapSettled → scroll=${scrollState.value}, target=$scrollTarget"
                    )
                    readinessHooks.onInitialSnapSettled?.invoke()
                }
            } else {
                Log.d(
                    TAG_CONTENT,
                    "Programmatic centering disabled → target=$scrollTarget, current=${scrollState.value}"
                )
            }
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
        val offsetXPx = with(density) {
            if (unlockedIndex in stepOffsets.indices) stepOffsets[unlockedIndex].toPx() else 0f
        }

        // --- NEW: compute bottom padding including nav bar inset ---
        val navigationBarsPadding = WindowInsets.navigationBars.asPaddingValues()
        val navigationBarBottomPadding = navigationBarsPadding.calculateBottomPadding()
        val listBottomPaddingDp =
            StageListDefaults.ListVerticalPadding +
                    navigationBarBottomPadding +
                    StageListDefaults.BottomSafeExtraPadding
        // ---------------------------------------------

        // Calibration: constant correction added to math so the bubble is exactly tangent.
        var anchorXCorrectionPx by remember(unlockedStage, rootWidthPx, frontEllipseHeightPx) {
            mutableStateOf(0f)
        }
        var anchorYCorrectionPx by remember(unlockedStage, rootWidthPx, frontEllipseHeightPx) {
            mutableStateOf(0f)
        }
        var needCalibrate by remember(unlockedStage, rootWidthPx, viewportHeightPx, stepOffsets) {
            mutableStateOf(true)
        }

        // ---------- Scrollable list ----------
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .onGloballyPositioned { coords ->
                    if (viewportHeightPx == 0) {
                        viewportHeightPx = coords.size.height
                        onViewportHeightChanged(viewportHeightPx)

                        if (!viewportMeasuredSignalSent && viewportHeightPx > 0) {
                            viewportMeasuredSignalSent = true
                            Log.d(
                                TAG_CONTENT,
                                "ViewportMeasured → heightPx=$viewportHeightPx"
                            )
                            readinessHooks.onViewportMeasured?.invoke(viewportHeightPx)
                        }
                    }
                }
                .padding(top = topPaddingDp, bottom = listBottomPaddingDp),
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

                            val applyBw =
                                (slot == Slot.BEE || slot == Slot.PENCIL) && stageNumber > unlockedStage
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
                                if (allowBubbleCalibration &&
                                    stageNumber == unlockedStage &&
                                    (needCalibrate || requestCalibration) &&
                                    !isContentMoving &&
                                    rootWidthPx > 0
                                ) {
                                    Modifier.onGloballyPositioned { coords ->
                                        // Measured (ground truth) position
                                        val b = coords.boundsInWindow()
                                        val measuredAnchorTop =
                                            b.top + (b.height - frontEllipseHeightPx) / 2f
                                        val measuredAnchorX = b.center.x

                                        // Math anchor at this exact scroll
                                        val scrollY = scrollState.value
                                        val rowTopLocal =
                                            listPadTopPx + (unlockedIndex * rowHeightPx) - scrollY
                                        val anchorTopLocal =
                                            rowTopLocal + buttonPadV +
                                                    (buttonContainerH - frontEllipseHeightPx) / 2f
                                        val anchorXLocal =
                                            (rootWidthPx.toFloat() / 2f) + offsetXPx
                                        val mathAnchorTop =
                                            rootTopLeftInWindow.y + anchorTopLocal
                                        val mathAnchorX =
                                            rootTopLeftInWindow.x + anchorXLocal

                                        // Compute & store constant correction
                                        anchorXCorrectionPx =
                                            measuredAnchorX - mathAnchorX
                                        anchorYCorrectionPx =
                                            measuredAnchorTop - mathAnchorTop

                                        // Apply immediately so it takes effect even without another scroll tick
                                        stageAnchorInWindow = Offset(
                                            x = mathAnchorX + anchorXCorrectionPx,
                                            y = mathAnchorTop + anchorYCorrectionPx
                                        )

                                        needCalibrate = false
                                        requestCalibration = false

                                        if (!bubbleCalibratedSignalSent) {
                                            bubbleCalibratedSignalSent = true
                                            Log.d(
                                                TAG_CONTENT,
                                                "BubbleCalibrated → " +
                                                        "measured=($measuredAnchorX,$measuredAnchorTop), " +
                                                        "math=($mathAnchorX,$mathAnchorTop), " +
                                                        "correction=($anchorXCorrectionPx,$anchorYCorrectionPx)"
                                            )
                                            readinessHooks.onBubbleCalibrated?.invoke()
                                        }
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
            unlockedStage,
            stepOffsets,
            rootWidthPx,
            viewportHeightPx,
            frontEllipseHeightPx,
            topPaddingDp
        ) {
            if (rootWidthPx == 0 || unlockedStage <= 0 || unlockedStage > stepOffsets.size) {
                Log.w(
                    TAG_CONTENT,
                    "Skipping scroll-driven anchor; invalid geometry: rootWidthPx=$rootWidthPx, " +
                            "unlockedStage=$unlockedStage, steps=${stepOffsets.size}"
                )
                return@LaunchedEffect
            }

            snapshotFlow { scrollState.value }
                .distinctUntilChanged()
                .conflate() // drop intermediate deltas while we're updating
                .collectLatest { scrollY ->
                    // Sync with the choreographer frame to avoid jitter
                    withFrameNanos { /* frame boundary */ }

                    val rowTopLocal =
                        listPadTopPx + (unlockedIndex * rowHeightPx) - scrollY
                    val anchorTopLocal =
                        rowTopLocal + buttonPadV +
                                (buttonContainerH - frontEllipseHeightPx) / 2f
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
