package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * StageRepository
 *
 * Responsibilities:
 *  • Provide stage counts per difficulty
 *  • Surface claimed gold chest state per difficulty
 *  • Perform one-time chest claims (atomically update claimed set + diamonds)
 */
class StageRepository(
    private val dataStore: DataStoreManager
) {

    fun getStagesCount(difficulty: Difficulty): Flow<Int> = flow {
        emit(difficultyStepCountMap[difficulty] ?: 9)
    }

    /** Observe claimed chest stage numbers for the given difficulty. */
    fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>> =
        dataStore.claimedChests(difficulty)

    /**
     * Claim a chest at [stageNumber] for [difficulty] if eligible.
     * Returns true if diamonds were awarded (first time), false otherwise.
     */
    suspend fun claimChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int
    ): Boolean {
        val award = when (difficulty) {
            Difficulty.EASY   -> 14
            Difficulty.MEDIUM -> 27
            Difficulty.HARD   -> 40
        }
        return dataStore.awardChestIfEligible(
            difficulty = difficulty,
            stageNumber = stageNumber,
            diamondsAward = award
        )
    }
}
