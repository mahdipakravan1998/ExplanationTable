package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for tracking and updating user progress through stages,
 * organized by difficulty level.
 *
 * Implementations MUST:
 * - Keep reads and writes off the Main thread.
 * - Enforce stage value invariants:
 *   - Unlocked stages in [1..maxStage(difficulty)].
 *   - Last played stages in [0..maxStage(difficulty)] and monotonic non-decreasing.
 * - Provide resilient defaults if underlying storage is missing or corrupt.
 */
interface ProgressRepository {

    /**
     * A cold [Flow] that emits the highest stage unlocked so far for [difficulty].
     *
     * Contract:
     * - The first emission is at least `1`.
     * - Values are clamped to [1..maxStage(difficulty)].
     * - Emissions are de-noised (i.e., [kotlinx.coroutines.flow.distinctUntilChanged]).
     * - Emits a new value whenever the unlocked stage advances.
     */
    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int>

    /**
     * One-shot getter for the highest stage unlocked for [difficulty].
     *
     * Contract:
     * - Returns at least `1`.
     * - Value is clamped to [1..maxStage(difficulty)].
     * - Provides a safe default (1) if storage is missing/corrupt.
     */
    suspend fun getLastUnlockedStageOnce(difficulty: Difficulty): Int

    /**
     * One-shot getter for the last played stage for [difficulty].
     *
     * Contract:
     * - Returns in [0..maxStage(difficulty)].
     * - Provides a safe default (0) if storage is missing/corrupt.
     */
    suspend fun getLastPlayedStageOnce(difficulty: Difficulty): Int

    /**
     * Records the last played stage for [difficulty].
     *
     * Contract:
     * - Stored value is **monotonic non-decreasing**.
     * - Input is clamped to [0..maxStage(difficulty)] prior to persistence.
     */
    suspend fun setLastPlayedStage(difficulty: Difficulty, stage: Int)

    /**
     * Marks [stage] as completed for [difficulty].
     *
     * Contract:
     * - Always records last played (monotonic, clamped).
     * - If [stage] equals the current highest unlocked stage, advances unlock to [stage + 1],
     *   capped at max stage for the difficulty.
     * - No-op for unlock if [stage] is not equal to the current unlocked stage.
     */
    suspend fun markStageCompleted(
        difficulty: Difficulty,
        stage: Int
    )
}
