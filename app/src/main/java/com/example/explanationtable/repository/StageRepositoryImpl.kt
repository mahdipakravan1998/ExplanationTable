package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext

/**
 * Production implementation that depends only on DataStoreManager.
 */
class StageRepositoryImpl(
    private val dataStore: DataStoreManager
) : StageRepository {

    override fun getStagesCount(difficulty: Difficulty): Flow<Int> = flow {
        // Pure lookup; exposed as Flow for a consistent read API.
        emit(difficultyStepCountMap[difficulty] ?: 9)
    }.flowOn(Dispatchers.IO)

    override fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>> =
        dataStore.claimedChests(difficulty).flowOn(Dispatchers.IO)

    override suspend fun claimChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int
    ): Boolean = withContext(Dispatchers.IO) {
        val award = when (difficulty) {
            Difficulty.EASY   -> 14
            Difficulty.MEDIUM -> 27
            Difficulty.HARD   -> 40
        }
        dataStore.awardChestIfEligible(
            difficulty = difficulty,
            stageNumber = stageNumber,
            diamondsAward = award
        )
    }
}
