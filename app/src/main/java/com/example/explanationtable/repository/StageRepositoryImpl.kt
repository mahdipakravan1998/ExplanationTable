package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import java.util.concurrent.ConcurrentHashMap

/**
 * Production [StageRepository] backed by [DataStoreManager] with a hot in-memory mirror.
 *
 * Behavior preserved:
 * - Public API and semantics unchanged.
 * - DataStore remains the authoritative persistence layer.
 *
 * Performance goals:
 * - Instant first emission for observers via in-memory StateFlow.
 * - Optimistic UI update on claim, with confirmation/rollback when persistence completes.
 */
class StageRepositoryImpl(
    private val dataStore: DataStoreManager
) : StageRepository {

    private companion object {
        /** Used if [difficultyStepCountMap] lacks a key. */
        private const val DEFAULT_STAGE_COUNT: Int = 9
    }

    // ---- Android 21+ compatibility helper (avoid API 24 computeIfAbsent) ----
    private fun <K : Any, V : Any> ConcurrentHashMap<K, V>.getOrPutCompat(
        key: K,
        defaultValue: () -> V
    ): V {
        val existing = this[key]
        if (existing != null) return existing
        val newValue = defaultValue()
        val prev = this.putIfAbsent(key, newValue)
        return prev ?: newValue
    }

    // Application-lifetime scope for bridges and internal bookkeeping.
    private val repoScope = CoroutineScope(
        SupervisorJob() + Dispatchers.Default + CoroutineName("StageRepository")
    )

    // Hot in-memory mirrors: latest claimed chests per difficulty.
    private val chestCache: ConcurrentHashMap<Difficulty, MutableStateFlow<Set<Int>>> =
        ConcurrentHashMap()

    // Serialize mutations per difficulty (coalesces concurrent writes).
    private val writeLocks: ConcurrentHashMap<Difficulty, Mutex> = ConcurrentHashMap()

    // Tracks optimistic additions not yet observed from DataStore to avoid flicker/regress.
    private val pendingOptimisticAdds: ConcurrentHashMap<Difficulty, MutableSet<Int>> =
        ConcurrentHashMap()

    init {
        warm() // Start bridging immediately without blocking callers.
    }

    /** Prepares caches/locks and bridges each difficulty stream from DataStore into a hot StateFlow. */
    private fun warm() {
        for (difficulty in Difficulty.entries) {
            val state = chestCache.getOrPutCompat(difficulty) { MutableStateFlow(emptySet()) }
            val lock = writeLocks.getOrPutCompat(difficulty) { Mutex() }
            val pending = pendingOptimisticAdds.getOrPutCompat(difficulty) { mutableSetOf() }

            // Bridge DataStore -> in-memory mirror (read-through).
            repoScope.launch(Dispatchers.IO) {
                dataStore
                    .claimedChests(difficulty)
                    .distinctUntilChanged()
                    .collect { dsSet ->
                        // Keep updates consistent with optimistic writes and avoid regressions/flicker.
                        lock.withLock {
                            if (pending.isNotEmpty()) {
                                // Remove items that have now been confirmed by DataStore.
                                pending.removeAll(dsSet)
                            }
                            val combined = if (pending.isEmpty()) dsSet else dsSet + pending
                            if (combined != state.value) {
                                // Replace wholesale to keep immutability guarantees and minimize downstream diffs.
                                state.value = combined.toSet()
                            }
                        }
                    }
            }
        }
    }

    /** Returns the number of stages for the given [difficulty] as a cold [Flow] (API preserved). */
    override fun getStagesCount(difficulty: Difficulty): Flow<Int> {
        val count = difficultyStepCountMap[difficulty] ?: DEFAULT_STAGE_COUNT
        return flowOf(count)
    }

    /** Observe the set of claimed stage numbers for [difficulty] as a hot [Flow] with immediate first emission. */
    override fun observeClaimedChests(difficulty: Difficulty): Flow<Set<Int>> {
        return chestCache.getOrPutCompat(difficulty) { MutableStateFlow(emptySet()) }
    }

    /**
     * Claim a chest optimistically and persist the result.
     *
     * @return `true` if the claim is ultimately persisted and rewarded; `false` if already
     * claimed or ineligible per the authoritative DataStore logic.
     */
    override suspend fun claimChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int
    ): Boolean {
        val lock = writeLocks.getOrPutCompat(difficulty) { Mutex() }
        val state = chestCache.getOrPutCompat(difficulty) { MutableStateFlow(emptySet()) }
        val pending = pendingOptimisticAdds.getOrPutCompat(difficulty) { mutableSetOf() }

        // ---------- Phase 1: optimistic update under lock (no I/O here) ----------
        val alreadyClaimed = lock.withLock {
            val current = state.value
            if (stageNumber in current) {
                true // Coalesce redundant writes: already claimed -> no-op.
            } else {
                // Optimistic update: immediate feedback for tap-to-claim.
                state.value = current + stageNumber
                pending.add(stageNumber)
                false
            }
        }
        if (alreadyClaimed) return false

        // ---------- Phase 2: persist on IO without holding the lock ----------
        val award = StageAwardsConfig.diamondsFor(difficulty)
        val persisted = withContext(Dispatchers.IO) {
            dataStore.awardChestIfEligible(
                difficulty = difficulty,
                stageNumber = stageNumber,
                diamondsAward = award
            )
        }

        // ---------- Phase 3: reconcile under lock ----------
        return lock.withLock {
            pending.remove(stageNumber)
            if (persisted) {
                // Authoritative store accepted; keep optimistic value until DataStore confirms.
                true
            } else {
                // Authoritative store rejected; rollback optimistic change.
                state.update { it - stageNumber }
                false
            }
        }
    }
}
