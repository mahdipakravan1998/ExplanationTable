package com.example.explanationtable.domain.rewards

import androidx.compose.runtime.Immutable
import com.example.explanationtable.model.Difficulty

/**
 * Immutable per-difficulty stage counts.
 * Create via [fromMap] to safely handle missing entries using a default.
 */
@Immutable
data class StageCounts(
    val easy: Int,
    val medium: Int,
    val hard: Int
) {
    companion object {
        /**
         * Builds [StageCounts] from a map keyed by [Difficulty], falling back to [defaultCount]
         * when an entry is missing.
         */
        fun fromMap(
            map: Map<Difficulty, Int>,
            defaultCount: Int = 9
        ): StageCounts = StageCounts(
            easy = map[Difficulty.EASY] ?: defaultCount,
            medium = map[Difficulty.MEDIUM] ?: defaultCount,
            hard = map[Difficulty.HARD] ?: defaultCount
        )
    }
}

/**
 * A resolved "next target" where navigation should land:
 * [difficulty] and [stage] (1-based).
 */
@Immutable
data class NextTarget(
    val difficulty: Difficulty,
    val stage: Int
)

/**
 * Resolves the next destination from a given (difficulty, stage) following the policy:
 *  - Advance within the same difficulty if not at its last stage.
 *  - From last EASY -> MEDIUM-1
 *  - From last MEDIUM -> HARD-1
 *  - From last HARD -> no next (returns null)
 *
 * IMPORTANT: This function **preserves existing behavior** and does **not** clamp input stage.
 * It assumes the caller provides a valid [stageNumber] for the current [currentDifficulty].
 */
fun resolveNextTarget(
    currentDifficulty: Difficulty,
    stageNumber: Int,
    counts: StageCounts
): NextTarget? {
    return when (currentDifficulty) {
        Difficulty.EASY -> {
            if (stageNumber < counts.easy) {
                NextTarget(Difficulty.EASY, stageNumber + 1)
            } else {
                NextTarget(Difficulty.MEDIUM, 1)
            }
        }
        Difficulty.MEDIUM -> {
            if (stageNumber < counts.medium) {
                NextTarget(Difficulty.MEDIUM, stageNumber + 1)
            } else {
                NextTarget(Difficulty.HARD, 1)
            }
        }
        Difficulty.HARD -> {
            if (stageNumber < counts.hard) {
                NextTarget(Difficulty.HARD, stageNumber + 1)
            } else {
                null // no next beyond last HARD stage
            }
        }
    }
}
