package com.example.explanationtable.ui.stages.content

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.CalloutBubble
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.util.computeCenterOffset
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sqrt

/**
 * Layout constants for the stages list (700x700 sources; xxxhdpi pixel-perfect at ~175dp).
 */
object StageListDefaults {
    val ButtonContainerHeight = 77.dp
    val ButtonVerticalPadding = 8.dp
    val ListVerticalPadding = 16.dp

    // Uniform visual height for ALL side art
    val SideImageDesiredHeight = 136.dp

    // Make both chests slightly smaller
    const val ChestScaleFactor: Float = 0.60f

    // Row baseline height so scaling won't affect spacing
    val SideImageBaseHeight = ButtonContainerHeight

    // Optional width cap (usually unnecessary with square 700x700 canvases)
    val SideImageMaxWidth: Dp = Dp.Unspecified

    // Existing generic edge padding used by all side art
    val SideImageEdgePadding = 32.dp

    // Extra inward inset ONLY for gold chests (tweak to taste)
    val ChestSideInset = 24.dp

    // Bob amplitude for the callout bubble (downward travel distance from topmost point)
    val CalloutBounceAmplitude = 7.dp

    // Duration for one half-cycle (top->bottom or bottom->top) → 2000 total
    const val CalloutHalfCycleMillis = 1000
}

/** Zig-zag; extreme lanes exactly at ±80.dp. */
private val DefaultOffsetPattern: List<Dp> = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

fun generateStepOffsets(totalSteps: Int, basePattern: List<Dp> = DefaultOffsetPattern): List<Dp> =
    buildList {
        if (totalSteps <= basePattern.size) {
            addAll(basePattern.take(totalSteps)); return@buildList
        }
        addAll(basePattern)
        val cycle = basePattern.drop(1)
        repeat(totalSteps - basePattern.size) { i -> add(cycle[i % cycle.size]) }
    }

/* ---------- Sequence & assets ---------- */

private enum class Slot { BEE, PENCIL, CHEST }

/** Your exact extreme-slot sequences (by extreme ordinal, 1-based). */
private fun extremeSequenceFor(difficulty: Difficulty): List<Slot> = when (difficulty) {
    Difficulty.EASY -> listOf(
        // Easy (12) — P at 2,7,11
        Slot.BEE, Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST,
        Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.PENCIL, Slot.CHEST
    )
    Difficulty.MEDIUM -> listOf(
        // Medium (17) — P at 2,9,16
        Slot.BEE, Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST,
        Slot.BEE, Slot.CHEST, Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE,
        Slot.CHEST, Slot.BEE, Slot.CHEST, Slot.PENCIL, Slot.CHEST
    )
    Difficulty.HARD -> listOf(
        // Hard (25) — P at 2,13,24; late double-chest
        Slot.BEE, Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST,
        Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST,
        Slot.PENCIL, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.CHEST, Slot.BEE,
        Slot.CHEST, Slot.CHEST, Slot.BEE, Slot.CHEST, Slot.BEE, Slot.PENCIL, Slot.CHEST
    )
}

/** Bee/Pencil art pools (700x700 each). */
@DrawableRes private val PencilImages = listOf(
    R.drawable.char_pencil_traveler,
    R.drawable.char_pencil_shadow,
    R.drawable.char_pencil_podcast,
    R.drawable.char_pencil_museum,
    R.drawable.char_pencil_chef,
    R.drawable.char_pencil_campfire,
    R.drawable.char_pencil_waiter,
    R.drawable.char_pencil_beanstalk,
    R.drawable.char_pencil_armchair,
)
@DrawableRes private val BeeImages = listOf(
    R.drawable.char_bee_spacesuit,
    R.drawable.char_bee_samurai,
    R.drawable.char_bee_detective,
    R.drawable.char_bee_speeding,
    R.drawable.char_bee_surfboard,
    R.drawable.char_bee_miner,
    R.drawable.char_bee_sitting,
)

@DrawableRes private val ChestUnlocked = R.drawable.img_gold_chest
@DrawableRes private val ChestLocked = R.drawable.img_locked_gold_chest
@DrawableRes private val ChestOpened = R.drawable.img_opened_gold_chest

/** Per-art scale overrides (non-chest). */
private val PerArtScaleOverrides: Map<Int, Float> = mapOf(
    R.drawable.char_pencil_beanstalk to 1.20f
)

/** Extreme stage numbers (1-based) for ±80.dp lanes. */
private fun extremeStageIndices(offsets: List<Dp>): List<Int> =
    offsets.mapIndexedNotNull { idx, off -> if (off == 80.dp || off == (-80).dp) idx + 1 else null }

/* ---------- Cross-device deterministic, non-repeating variant selection ---------- */

private const val ART_SEED = "ART_GLOBAL_V1"

private tailrec fun gcd(a0: Int, b0: Int): Int {
    var a = if (a0 < 0) -a0 else a0
    var b = if (b0 < 0) -b0 else b0
    return if (b == 0) a else gcd(b, a % b)
}

private fun permutationParams(poolSize: Int, tag: String): Pair<Int, Int> {
    require(poolSize > 0)
    val h = abs("$tag|$ART_SEED".hashCode())
    val start = h % poolSize
    var step = 1 + (h / (poolSize.coerceAtLeast(1))) % (poolSize - 1).coerceAtLeast(1)
    if (gcd(step, poolSize) != 1) {
        var s = step
        repeat(poolSize) {
            s = (s % (poolSize - 1).coerceAtLeast(1)) + 1
            if (gcd(s, poolSize) == 1) { step = s; return@repeat }
        }
        step = 1
    }
    return start to step
}

private fun permutedIndex(start: Int, step: Int, poolSize: Int, ordinal1: Int): Int {
    val k = ordinal1 - 1
    val idx = (start + (k.toLong() * step.toLong())).mod(poolSize.toLong())
    return idx.toInt()
}

/* ---------- Global B/P ordinals across difficulties ---------- */

private val DifficultyOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

@Immutable
private data class Counts(val bees: Int, val pencils: Int)

private fun countInRepeatedPrefix(seq: List<Slot>, len: Int): Counts {
    if (len <= 0 || seq.isEmpty()) return Counts(0, 0)
    val beesPer = seq.count { it == Slot.BEE }
    val pencilsPer = seq.count { it == Slot.PENCIL }
    val cycles = len / seq.size
    val rem = len % seq.size
    val bees = cycles * beesPer + seq.take(rem).count { it == Slot.BEE }
    val pencils = cycles * pencilsPer + seq.take(rem).count { it == Slot.PENCIL }
    return Counts(bees, pencils)
}

private fun extremeCountFor(totalSteps: Int): Int =
    generateStepOffsets(totalSteps).count { it == 80.dp || it == (-80).dp }

private fun globalOffsetsFor(
    current: Difficulty,
    allStageCounts: Map<Difficulty, Int>
): Counts {
    var beeOffset = 0
    var pencilOffset = 0
    for (d in DifficultyOrder) {
        if (d == current) break
        val total = allStageCounts[d] ?: 0
        if (total <= 0) continue
        val extremes = extremeCountFor(total)
        val used = countInRepeatedPrefix(extremeSequenceFor(d), extremes)
        beeOffset += used.bees
        pencilOffset += used.pencils
    }
    return Counts(beeOffset, pencilOffset)
}

/* ---------- Symmetric smooth bob easing ---------- */
private val EaseInOutSine: Easing = Easing { x -> 0.5f - 0.5f * cos((Math.PI * x).toFloat()) }

/* ---------- A tiny leaf that owns the bob animation (gated by lifecycle, visibility, and movement) ---------- */
@Composable
private fun FloatingCalloutBubble(
    isDarkTheme: Boolean,
    anchorInWindow: Offset?,        // top of the button’s front ellipse (global coords)
    rootTopLeftInWindow: Offset,    // top-left of the root overlay box (global coords)
    viewportHeightPx: Int,
    isContentMoving: Boolean        // finger drag, fling, or programmatic scroll/settling
) {
    if (anchorInWindow == null || viewportHeightPx <= 0) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember { mutableStateOf(lifecycleOwner.lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED)) }
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            isResumed = when (event) {
                Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> false
                else -> isResumed
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val density = LocalDensity.current

    // Local (root-relative) anchor
    val localAnchor = remember(anchorInWindow, rootTopLeftInWindow) {
        Offset(
            x = anchorInWindow.x - rootTopLeftInWindow.x,
            y = anchorInWindow.y - rootTopLeftInWindow.y
        )
    }

    // Bubble size for centering; measured once and only written if changed
    var bubbleW by remember { mutableStateOf(0) }
    var bubbleH by remember { mutableStateOf(0) }

    // Exact geometric compensation for the triangular tip (must match CalloutBubble)
    val tipCompensationPx by remember(density) {
        mutableStateOf(run {
            val triBasePx = with(density) { 15.dp.toPx() }
            val triHPx    = with(density) { 10.dp.toPx() }
            val cornerPx  = with(density) { 10.dp.toPx() }
            val halfBase = triBasePx / 2f
            val triRound = min(min(cornerPx * 0.8f, triHPx * 0.7f), halfBase * 0.95f)
            val lenR = sqrt(halfBase * halfBase + triHPx * triHPx)
            val uy = triHPx / lenR
            (uy * triRound) / 2f
        })
    }

    // Visibility estimate (don’t animate off-screen). Give a small margin to avoid thrash.
    val marginPx = with(density) { 48.dp.toPx() }
    val anchorY = localAnchor.y
    val estimatedVisible = anchorY > -marginPx && anchorY < viewportHeightPx + marginPx

    // Same spec: 1000ms half-cycle, reverse, sine ease
    val bobAmplitudePx = with(density) { StageListDefaults.CalloutBounceAmplitude.toPx() }
    val shouldAnimate = isResumed && estimatedVisible && !isContentMoving

    val bobOffsetYpx = if (shouldAnimate) {
        val infinite = rememberInfiniteTransition(label = "callout-bob-leaf")
        val bobPhase by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(
                    durationMillis = StageListDefaults.CalloutHalfCycleMillis,
                    easing = EaseInOutSine
                ),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bob-phase"
        )
        bobPhase * bobAmplitudePx
    } else 0f

    // Center horizontally on anchor; align visible tip to anchor at the *topmost* point
    val leftPx = (localAnchor.x - (if (bubbleW == 0) 1 else bubbleW) / 2f)
    val topAtRestPx = localAnchor.y - (if (bubbleH == 0) 1 else bubbleH) + tipCompensationPx
    val animatedTopPx = topAtRestPx + bobOffsetYpx

    // If completely off-screen, skip composing the bubble entirely
    if (!estimatedVisible) return

    Box(
        modifier = Modifier
            .graphicsLayer {
                // GPU translations only; no layout changes → minimal recompositions
                translationX = leftPx
                translationY = animatedTopPx
            }
            .zIndex(5f)
            .onGloballyPositioned { coords ->
                val nw = coords.size.width
                val nh = coords.size.height
                if (nw != bubbleW) bubbleW = nw
                if (nh != bubbleH) bubbleH = nh
            }
    ) {
        CalloutBubble(isDarkTheme = isDarkTheme, text = "شروع")
    }
}

/* ---------- UI ---------- */

@Composable
fun StagesListContent(
    navController: NavController,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    scrollState: ScrollState,
    onTargetOffsetChanged: (Int) -> Unit = {},
    onViewportHeightChanged: (Int) -> Unit = {},
    stageViewModel: StageViewModel = androidx.lifecycle.viewmodel.compose.viewModel(),
    progressViewModel: StageProgressViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }
    LaunchedEffect(Unit) { stageViewModel.fetchAllStageCounts() }

    val totalSteps by stageViewModel.stageCount.collectAsStateWithLifecycle(initialValue = 0)
    val allCounts by stageViewModel.allStageCounts.collectAsStateWithLifecycle(initialValue = emptyMap())

    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val claimedChests: Set<Int> by stageViewModel
        .claimedChests(difficulty)
        .collectAsStateWithLifecycle(initialValue = emptySet())

    val justOpenedRemember = remember { mutableStateListOf<Int>() }

    val stepOffsets = remember(totalSteps) { generateStepOffsets(totalSteps) }
    val extremeStages = remember(stepOffsets) { extremeStageIndices(stepOffsets) }
    val extremeSeq = remember(difficulty) { extremeSequenceFor(difficulty) }

    val extremeSlotMap: Map<Int, Slot> = remember(extremeStages, extremeSeq) {
        buildMap {
            extremeStages.forEachIndexed { ordZero, stageNum ->
                val slot = if (extremeSeq.isNotEmpty()) extremeSeq[ordZero % extremeSeq.size] else Slot.BEE
                put(stageNum, slot)
            }
        }
    }

    val offsets = remember(difficulty, allCounts) { globalOffsetsFor(difficulty, allCounts) }
    val (beeStart, beeStep) = remember { permutationParams(BeeImages.size, "BEE|GLOBAL") }
    val (pencilStart, pencilStep) = remember { permutationParams(PencilImages.size, "PENCIL|GLOBAL") }

    val extremeArtAssignment: Map<Int, Int?> = remember(
        difficulty, extremeSlotMap, offsets, beeStart, beeStep, pencilStart, pencilStep
    ) {
        var localBee = 0
        var localPencil = 0
        buildMap {
            extremeStages.forEach { stage ->
                when (extremeSlotMap[stage]) {
                    Slot.BEE -> {
                        localBee += 1
                        val globalBeeOrd = offsets.bees + localBee
                        val idx = permutedIndex(beeStart, beeStep, BeeImages.size, globalBeeOrd)
                        put(stage, BeeImages[idx])
                    }
                    Slot.PENCIL -> {
                        localPencil += 1
                        val globalPencilOrd = offsets.pencils + localPencil
                        val idx = permutedIndex(pencilStart, pencilStep, PencilImages.size, globalPencilOrd)
                        put(stage, PencilImages[idx])
                    }
                    Slot.CHEST, null -> put(stage, null)
                }
            }
        }
    }

    val density = LocalDensity.current
    val basePx = with(density) { StageListDefaults.SideImageBaseHeight.toPx() }
    val uniformScale = remember(density) {
        val desiredPx = with(density) { StageListDefaults.SideImageDesiredHeight.toPx() }
        if (basePx > 0f) desiredPx / basePx else 1f
    }
    val chestScale = remember(uniformScale) { uniformScale * StageListDefaults.ChestScaleFactor }

    val grayscaleFilter = remember {
        val m = ColorMatrix().apply { setToSaturation(0f) }
        ColorFilter.colorMatrix(m)
    }

    var viewportHeightPx by remember { mutableStateOf(0) }
    var targetOffsetPx by remember { mutableIntStateOf(0) }
    var requestCalibration by remember { mutableStateOf(false) } // trigger a one-shot calibration after initial scroll

    // Track our own programmatic scroll so gating also applies during animateScrollTo
    var isProgrammaticScroll by remember { mutableStateOf(false) }

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

    // ---------- Geometry derived directly from your button & bubble code ----------
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
        val listPadTopPx = with(density) { StageListDefaults.ListVerticalPadding.toPx() }
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
                .padding(vertical = StageListDefaults.ListVerticalPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val stepOffsetsLocal = stepOffsets
            stepOffsetsLocal.forEachIndexed { index, offset ->
                val stageNumber = index + 1
                val isExtreme = (offset == 80.dp || offset == (-80).dp)

                Box(
                    modifier = Modifier
                        .fillMaxSize()
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
                                            Modifier.clickable {
                                                if (!justOpenedRemember.contains(stageNumber)) {
                                                    justOpenedRemember.add(stageNumber)
                                                }
                                                stageViewModel.claimChest(difficulty, stageNumber)
                                            }
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
                            DifficultyStepButton(
                                difficulty = difficulty,
                                stepNumber = stageNumber,
                                onClick = { navController.navigate("GAMEPLAY/$stageNumber/${difficulty.name}") }
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
            unlockedStage, stepOffsets, rootWidthPx, viewportHeightPx, frontEllipseHeightPx
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
