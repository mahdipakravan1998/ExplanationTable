package com.example.explanationtable.ui.stages.content

import androidx.annotation.DrawableRes
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.*
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.components.CalloutBubble
import com.example.explanationtable.ui.stages.components.DifficultyStepButton
import com.example.explanationtable.ui.stages.components.LockedStepButton
import com.example.explanationtable.ui.stages.util.computeCenterOffset
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlin.math.abs
import kotlin.math.roundToInt

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

private inline fun permutedIndex(start: Int, step: Int, poolSize: Int, ordinal1: Int): Int {
    val k = ordinal1 - 1
    val idx = (start + (k.toLong() * step.toLong())).mod(poolSize.toLong())
    return idx.toInt()
}

/* ---------- Global B/P ordinals across difficulties ---------- */

private val DifficultyOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)
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

/* ---------- UI ---------- */

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
    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }
    LaunchedEffect(Unit) { stageViewModel.fetchAllStageCounts() }

    val totalSteps by stageViewModel.stageCount.collectAsState(initial = 0)
    val allCounts by stageViewModel.allStageCounts.collectAsState()

    val unlockedMap by progressViewModel.lastUnlocked.collectAsState(initial = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val claimedChests: Set<Int> by stageViewModel
        .claimedChests(difficulty)
        .collectAsState(initial = emptySet())

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
            scrollState.animateScrollTo(scrollTarget, animationSpec = tween(600, easing = EaseInOutCubic))
        }
    }

    // Front ellipse visible-top inset: container 77dp, ellipse ~63dp → inset = 7dp
    val visibleTopInsetDp = remember { (StageListDefaults.ButtonContainerHeight - 70.dp * 0.9f) / 2f }
    val visibleTopInsetPx = with(density) { visibleTopInsetDp.toPx() }

    // Root overlay state (for the floating bubble)
    var rootTopLeftInWindow by remember { mutableStateOf(Offset.Zero) }
    var stageAnchorInWindow by remember { mutableStateOf<Offset?>(null) }
    var bubbleWidthPx by remember { mutableStateOf(0) }
    var bubbleHeightPx by remember { mutableStateOf(0) }

    // Bob animation
    val infinite = rememberInfiniteTransition(label = "callout-bob")
    val bobPhase by infinite.animateFloat(
        initialValue = -1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1100, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "bob-phase"
    )
    val bobAmplitudePx = with(density) { 6.dp.toPx() } // A
    val bobOffsetYpx = bobPhase * bobAmplitudePx       // [-A, +A]

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onGloballyPositioned { coords ->
                rootTopLeftInWindow = coords.boundsInWindow().topLeft
            }
    ) {
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
            stepOffsets.forEachIndexed { index, offset ->
                val stageNumber = index + 1
                val isExtreme = (offset == 80.dp || offset == (-80).dp)

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
                            // Report anchor: (centerX, TOP of the visible front ellipse) in window coords.
                            .then(
                                if (stageNumber == unlockedStage)
                                    Modifier.onGloballyPositioned { coords ->
                                        val b = coords.boundsInWindow()
                                        val adjTopPx = b.top + visibleTopInsetPx
                                        stageAnchorInWindow = Offset(b.center.x, adjTopPx)
                                    }
                                else Modifier
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

        // ---------- Floating CalloutBubble overlay ----------
        val localAnchor = stageAnchorInWindow?.let {
            Offset(it.x - rootTopLeftInWindow.x, it.y - rootTopLeftInWindow.y)
        }
        if (localAnchor != null && viewportHeightPx > 0) {
            val wPx = if (bubbleWidthPx == 0) 1 else bubbleWidthPx
            val hPx = if (bubbleHeightPx == 0) 1 else bubbleHeightPx

            // Center horizontally on the stage center (no clamping)
            val placedLeftPx = localAnchor.x - wPx / 2f

            // Vertically: make the tip touch the button top at bob’s highest point
            val topAtRestPx = localAnchor.y - hPx + bobAmplitudePx
            val animatedTopPx = topAtRestPx + bobOffsetYpx

            Box(
                modifier = Modifier
                    .offset {
                        IntOffset(
                            x = placedLeftPx.roundToInt(),
                            y = animatedTopPx.roundToInt()
                        )
                    }
                    .zIndex(5f)
                    .onGloballyPositioned { coords ->
                        bubbleWidthPx = coords.size.width
                        bubbleHeightPx = coords.size.height
                    }
            ) {
                CalloutBubble(isDarkTheme = isDarkTheme, text = "شروع")
            }
        }
    }
}
