package com.example.explanationtable.ui.stages.content

import androidx.annotation.DrawableRes
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import kotlin.math.abs

/**
 * Slots placed only on extreme lanes (±80.dp).
 */
internal enum class Slot { BEE, PENCIL, CHEST }

/** Bee/Pencil art pools (700x700 each). */
@DrawableRes internal val PencilImages = listOf(
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
@DrawableRes internal val BeeImages = listOf(
    R.drawable.char_bee_spacesuit,
    R.drawable.char_bee_samurai,
    R.drawable.char_bee_detective,
    R.drawable.char_bee_speeding,
    R.drawable.char_bee_surfboard,
    R.drawable.char_bee_miner,
    R.drawable.char_bee_sitting,
)

@DrawableRes internal val ChestUnlocked = R.drawable.img_gold_chest
@DrawableRes internal val ChestLocked = R.drawable.img_locked_gold_chest
@DrawableRes internal val ChestOpened = R.drawable.img_opened_gold_chest

/** Per-art scale overrides (non-chest). */
internal val PerArtScaleOverrides: Map<Int, Float> = mapOf(
    R.drawable.char_pencil_beanstalk to 1.20f
)

/**
 * Extreme-lane slot sequence per difficulty. Kept identical to authored sequences.
 * The sequence is consumed cyclically across all extreme stages for the given difficulty.
 */
internal fun extremeSequenceFor(difficulty: Difficulty): List<Slot> = when (difficulty) {
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

/* ---------- Cross-device deterministic, non-repeating variant selection ---------- */

private const val ART_SEED = "ART_GLOBAL_V1"

/** Euclid’s algorithm — gcd used to ensure step and pool size are coprime. */
private tailrec fun gcd(a0: Int, b0: Int): Int {
    var a = if (a0 < 0) -a0 else a0
    var b = if (b0 < 0) -b0 else b0
    return if (b == 0) a else gcd(b, a % b)
}

/**
 * Computes a deterministic permutation (start, step) for selecting items from a pool of [poolSize],
 * keyed by [tag] and a private seed. Ensures full-period traversal by forcing `gcd(step, poolSize)=1`.
 */
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

/** Returns the index into a pool for the 1-based [ordinal1] step along the permutation. */
private fun permutedIndex(start: Int, step: Int, poolSize: Int, ordinal1: Int): Int {
    val k = ordinal1 - 1
    val idx = (start + (k.toLong() * step.toLong())).mod(poolSize.toLong())
    return idx.toInt()
}

private val DifficultyOrder = listOf(Difficulty.EASY, Difficulty.MEDIUM, Difficulty.HARD)

/**
 * Fast counts of used BEE and PENCIL slots in the prefix of a repeating [seq] up to [len] extremes.
 */
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

/**
 * Computes global offsets of used BEE/PENCIL slots for all difficulties prior to [current],
 * so the art permutations are globally non-repeating across difficulties.
 */
internal fun globalOffsetsFor(
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

/** Resolve the drawable id for a BEE/PENCIL ordinal using a deterministic global permutation. */
internal fun resolveBeeDrawable(globalOrdinal: Int): Int {
    val (start, step) = permutationParams(BeeImages.size, "BEE|GLOBAL")
    val idx = permutedIndex(start, step, BeeImages.size, globalOrdinal)
    return BeeImages[idx]
}

internal fun resolvePencilDrawable(globalOrdinal: Int): Int {
    val (start, step) = permutationParams(PencilImages.size, "PENCIL|GLOBAL")
    val idx = permutedIndex(start, step, PencilImages.size, globalOrdinal)
    return PencilImages[idx]
}
