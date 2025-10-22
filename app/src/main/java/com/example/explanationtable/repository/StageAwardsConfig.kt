package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty

/**
 * Centralized configuration for per-difficulty rewards.
 *
 * Keeping these values in one place removes magic numbers from the repository
 * implementation and makes future tuning trivial.
 */
internal object StageAwardsConfig {

    /** Diamonds awarded on the first chest claim per difficulty. */
    val diamondAwardByDifficulty: Map<Difficulty, Int> = mapOf(
        Difficulty.EASY to 14,
        Difficulty.MEDIUM to 27,
        Difficulty.HARD to 40
    )

    /**
     * Returns the diamonds award for the provided [difficulty].
     * Throws if a new difficulty is introduced without a configured value,
     * making missing configuration a fail-fast error in debug builds.
     */
    fun diamondsFor(difficulty: Difficulty): Int =
        requireNotNull(diamondAwardByDifficulty[difficulty]) {
            "Missing diamonds configuration for difficulty=$difficulty"
        }
}
