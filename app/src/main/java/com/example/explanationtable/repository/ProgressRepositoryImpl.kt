package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.math.max

/**
 * Repository for tracking and persisting user progress through stages,
 * backed by a [DataStoreManager].
 *
 * This implementation:
 * - Enforces monotonic non-decreasing "last played" values.
 * - Clamps all stage values to valid ranges derived from difficulty.
 * - Uses [ioDispatcher] for IO-bound operations.
 * - Protects against storage errors with safe defaults.
 */
class ProgressRepositoryImpl(
    private val dataStore: DataStoreManager,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ProgressRepository {

    /** Default max stage when not defined in [difficultyStepCountMap]. */
    private companion object {
        const val DEFAULT_STAGE_COUNT: Int = 9
    }

    /**
     * Resolve the maximum stage for a given [difficulty], falling back to a safe default.
     */
    private fun maxStageFor(difficulty: Difficulty): Int =
        difficultyStepCountMap[difficulty] ?: DEFAULT_STAGE_COUNT

    /**
     * Exposes updates to the last unlocked stage for [difficulty], clamped and de-noised.
     */
    override fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> {
        val maxStage = maxStageFor(difficulty)
        return dataStore
            .getLastUnlockedStage(difficulty)
            .map { unlocked -> unlocked.coerceIn(1, maxStage) }
            .distinctUntilChanged()
            .catch { emit(1.coerceIn(1, maxStage)) } // Safe default if datastore fails
            .flowOn(ioDispatcher)
    }

    /**
     * One-shot read of last unlocked, clamped and fault-tolerant.
     */
    override suspend fun getLastUnlockedStageOnce(difficulty: Difficulty): Int =
        withContext(ioDispatcher) {
            val maxStage = maxStageFor(difficulty)
            runCatching { dataStore.getLastUnlockedStageOnce(difficulty) }
                .getOrElse { 1 }
                .coerceIn(1, maxStage)
        }

    /**
     * One-shot read of last played, clamped and fault-tolerant.
     */
    override suspend fun getLastPlayedStageOnce(difficulty: Difficulty): Int =
        withContext(ioDispatcher) {
            val maxStage = maxStageFor(difficulty)
            runCatching { dataStore.getLastPlayedStageOnce(difficulty) }
                .getOrElse { 0 }
                .coerceIn(0, maxStage)
        }

    /**
     * Persist last played (monotonic & clamped).
     *
     * We read the current stored value, clamp input, then write the max(previous, clampedInput).
     * This protects against out-of-order writes or stale values.
     */
    override suspend fun setLastPlayedStage(difficulty: Difficulty, stage: Int) {
        withContext(ioDispatcher) {
            val maxStage = maxStageFor(difficulty)
            val current = runCatching { dataStore.getLastPlayedStageOnce(difficulty) }
                .getOrElse { 0 }
                .coerceIn(0, maxStage)

            val target = stage.coerceIn(0, maxStage)
            val monotonic = max(current, target)

            dataStore.setLastPlayedStage(difficulty, monotonic)
        }
    }

    /**
     * Marks [stage] as completed; may advance unlock; always records last played (monotonic).
     *
     * Unlock advancement rule:
     * - Advance only when the completed [stage] equals the current unlocked stage.
     * - The next unlocked stage is min(stage + 1, maxStage).
     */
    override suspend fun markStageCompleted(difficulty: Difficulty, stage: Int) {
        withContext(ioDispatcher) {
            val maxStage = maxStageFor(difficulty)

            // Always record last played first (monotonic, clamped).
            setLastPlayedStage(difficulty, stage)

            // Read current unlocked, clamp for safety.
            val currentUnlocked = runCatching { dataStore.getLastUnlockedStageOnce(difficulty) }
                .getOrElse { 1 }
                .coerceIn(1, maxStage)

            // Only advance when completing exactly the highest unlocked stage.
            val completed = stage.coerceIn(1, maxStage)
            if (completed == currentUnlocked) {
                val next = (completed + 1).coerceAtMost(maxStage)
                // Only write if there is an actual advancement (avoid redundant writes).
                if (next != currentUnlocked) {
                    dataStore.setLastUnlockedStage(difficulty, next)
                }
            }
        }
    }
}
