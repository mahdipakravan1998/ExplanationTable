package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow

/**
 * Repository contract for stages-related domain operations.
 *
 * Design:
 * - No Android framework types.
 * - Exposes cold [Flow]s for reads and suspend functions for writes.
 * - UI and ViewModel remain agnostic of storage/IO details.
 */
interface StageRepository {

    /**
     * Emits the number of stages for the given [difficulty].
     *
     * Notes:
     * - Exposed as a [Flow] for a consistent read API across the data layer.
     * - The value is static per difficulty; collect will emit the same value.
     */
    fun getStagesCount(difficulty: Difficulty): Flow<Int>

    /**
     * Emits updates to the set of claimed chest stage numbers for the given [difficulty].
     *
     * Notes:
     * - Backed by DataStore; emissions occur when persisted data changes.
     * - The returned [Flow] is cold and cancellable.
     */
    fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>>

    /**
     * Attempts to claim a chest at [stageNumber] for [difficulty].
     *
     * @return `true` if diamonds were awarded (i.e., it was the first claim); `false` otherwise.
     *
     * Threading:
     * - IO-bound persistence is performed off the main thread by the implementation.
     */
    suspend fun claimChestIfEligible(difficulty: Difficulty, stageNumber: Int): Boolean
}
