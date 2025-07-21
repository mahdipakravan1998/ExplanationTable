package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.flow.Flow

/**
 * Repository for tracking and persisting user progress through stages,
 * backed by a [DataStoreManager].
 */
class ProgressRepositoryImpl(
    private val dataStore: DataStoreManager
) : ProgressRepository {

    /**
     * Exposes a cold [Flow] that emits updates to the last unlocked stage
     * for the given [difficulty].
     */
    override fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> =
        dataStore.getLastUnlockedStage(difficulty)

    /**
     * Marks the specified [stage] as completed for the given [difficulty].
     *
     * If the completed stage equals the current maximum unlocked stage,
     * unlocks the next stage—capped by the total number of stages defined
     * in [difficultyStepCountMap].
     */
    override suspend fun markStageCompleted(difficulty: Difficulty, stage: Int) {
        // One-time read of the currently unlocked maximum stage
        val currentMaxStage = dataStore.getLastUnlockedStageOnce(difficulty)

        // Only proceed if the user just completed the highest unlocked stage
        if (stage == currentMaxStage) {
            // Fetch the total number of stages for this difficulty,
            // or fall back to (stage + 1) so that nextStage == stage + 1
            val allowedMaxStage = difficultyStepCountMap[difficulty] ?: (stage + 1)

            // Compute the next stage while ensuring we don't exceed the allowed maximum
            val nextStage = (stage + 1).coerceAtMost(allowedMaxStage)

            // Persist the newly unlocked stage
            dataStore.setLastUnlockedStage(difficulty, nextStage)
        }
    }
}
