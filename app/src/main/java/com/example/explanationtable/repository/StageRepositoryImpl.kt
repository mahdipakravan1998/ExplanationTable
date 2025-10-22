package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.withContext

/**
 * Production implementation backed by [DataStoreManager].
 *
 * Threading strategy:
 * - **Reads**:
 *   - Constant lookups use [flowOf] and run on the collector's context.
 *   - DataStore flows are returned directly; DataStore performs IO safely.
 *   - We intentionally avoid `flowOn(Dispatchers.IO)` to prevent surprising downstream context hops.
 * - **Writes**:
 *   - Persist operations execute on [Dispatchers.IO] via [withContext].
 */
class StageRepositoryImpl(
    private val dataStore: DataStoreManager
) : StageRepository {

    // Default to 9 if the map is missing a key (behavior preserved).
    private companion object {
        private const val DEFAULT_STAGE_COUNT: Int = 9
    }

    override fun getStagesCount(difficulty: Difficulty): Flow<Int> {
        // Pure lookup exposed as a Flow for API consistency.
        // Use flowOf to avoid extra Flow builder overhead.
        val count = difficultyStepCountMap[difficulty] ?: DEFAULT_STAGE_COUNT
        return flowOf(count)
    }

    override fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>> {
        // Return DataStore flow directly. DataStore manages its own dispatchers.
        // Do not force flowOn(IO): collectors should control their downstream context.
        return dataStore.claimedChests(difficulty)
    }

    override suspend fun claimChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int
    ): Boolean = withContext(Dispatchers.IO) {
        // Keep write path explicitly on IO.
        val award = StageAwardsConfig.diamondsFor(difficulty)
        dataStore.awardChestIfEligible(
            difficulty = difficulty,
            stageNumber = stageNumber,
            diamondsAward = award
        )
    }
}
