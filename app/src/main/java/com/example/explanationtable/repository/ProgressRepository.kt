package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for tracking and updating user progress through stages,
 * organized by difficulty level.
 */
interface ProgressRepository {

    /**
     * Returns a [Flow] that emits the highest stage unlocked so far for the given [difficulty].
     *
     * - Starts at `1` if the user hasn’t completed any stages yet.
     * - Emits a new value whenever the unlocked stage advances.
     *
     * @param difficulty the difficulty level whose progress is being observed
     * @return a [Flow] emitting the current highest unlocked stage number
     */
    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int>

    /**
     * Marks the specified [stage] as completed for the given [difficulty].
     *
     * - If [stage] equals the current unlocked stage, advances the unlock to [stage] + 1.
     * - Ensures the unlocked stage never exceeds the maximum defined for that difficulty.
     *
     * @param difficulty the difficulty level being updated
     * @param stage the stage number that was completed
     */
    suspend fun markStageCompleted(
        difficulty: Difficulty,
        stage: Int
    )
}
