package com.example.explanationtable.ui.stages.content

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Zig-zag offset pattern for stage buttons. Extreme lanes are exactly at ±80.dp.
 * The generator cycles the base pattern without repeating the first element back-to-back.
 */
private val DefaultOffsetPattern: List<Dp> = listOf(
    0.dp, 40.dp, 80.dp, 40.dp, 0.dp,
    (-40).dp, (-80).dp, (-40).dp, 0.dp
)

/** True if an offset is one of the two extreme lanes (±80.dp). */
fun isExtremeOffset(offset: Dp): Boolean = offset == 80.dp || offset == (-80).dp

/**
 * Returns per-stage horizontal offsets for [totalSteps] by cycling a base pattern.
 * Keeps the exact authored shape, including extreme lanes at ±80.dp.
 */
fun generateStepOffsets(totalSteps: Int, basePattern: List<Dp> = DefaultOffsetPattern): List<Dp> =
    buildList {
        if (totalSteps <= basePattern.size) {
            addAll(basePattern.take(totalSteps)); return@buildList
        }
        addAll(basePattern)
        val cycle = basePattern.drop(1) // avoid repeating the first value twice
        repeat(totalSteps - basePattern.size) { i -> add(cycle[i % cycle.size]) }
    }

/** Extreme stage numbers (1-based) for ±80.dp lanes. */
fun extremeStageIndices(offsets: List<Dp>): List<Int> =
    offsets.mapIndexedNotNull { idx, off -> if (isExtremeOffset(off)) idx + 1 else null }

/** Number of extreme lanes within [totalSteps] using the current offset generator. */
fun extremeCountFor(totalSteps: Int): Int =
    generateStepOffsets(totalSteps).count { isExtremeOffset(it) }

@Immutable
data class Counts(val bees: Int, val pencils: Int)
