package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.flow.Flow

class ProgressRepositoryImpl(
    private val dataStore: DataStoreManager
) : ProgressRepository {

    override fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> =
        dataStore.getLastUnlockedStage(difficulty)

    override suspend fun markStageCompleted(difficulty: Difficulty, stage: Int) {
        // only unlock next if they just finished the current max
        val maxUnlocked = dataStore.getLastUnlockedStageOnce(difficulty)
        if (stage == maxUnlocked) {
            val next = (stage + 1).coerceAtMost(
                difficultyStepCountMap[difficulty] ?: (stage + 1)
            )
            dataStore.setLastUnlockedStage(difficulty, next)
        }
    }
}
