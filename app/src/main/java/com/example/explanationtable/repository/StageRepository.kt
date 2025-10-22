package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for stages-related data.
 *
 * - No Android framework types.
 * - Exposes cold Flows and suspend functions.
 */
interface StageRepository {

    /** Emits the number of stages for the given [difficulty]. */
    fun getStagesCount(difficulty: Difficulty): Flow<Int>

    /** Emits the set of claimed chest stage numbers for the given [difficulty]. */
    fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>>

    /**
     * Attempts to claim a chest at [stageNumber] for [difficulty].
     * Returns true if diamonds were awarded (first claim), false otherwise.
     */
    suspend fun claimChestIfEligible(difficulty: Difficulty, stageNumber: Int): Boolean
}
