package com.example.explanationtable.ui.stages.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepository
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * Resolved metadata + readiness state for stage data of a given difficulty.
 *
 * This is the source of truth for "StageDataReady" at the UI layer:
 * - [isLoaded] becomes true as soon as a stage count is resolved for [difficulty],
 *   even when the count is legitimately 0 or comes from a fallback.
 * - [count] is the resolved stage count (may be 0).
 * - [isFallback] indicates that a non-fatal error occurred and [count] is a safe
 *   fallback value rather than a fully trusted one.
 *
 * Fallback semantics:
 * - On repository errors we log via Log.e and emit a safe fallback count (typically 0).
 * - These fallback emissions still satisfy the StageDataReady condition for the
 *   preflight readiness contract; geometry fallbacks in the UI handle empty/partial data.
 */
data class StageDataStatus(
    val difficulty: Difficulty? = null,
    val isLoaded: Boolean = false,
    val count: Int = 0,
    val isFallback: Boolean = false
)

/**
 * ViewModel for the stages-list screen.
 *
 * Public contract (preserved & extended):
 * - [stageDataStatus]: typed readiness signal for the currently selected difficulty.
 * - [stageCount]: current difficulty's stage count (0 until [fetchStagesCount] is called;
 *   may remain 0 even after loaded, for valid zero-stage or fallback cases).
 * - [allStageCounts]: map of all difficulties to their counts (all zeros until
 *   [fetchAllStageCounts] is called).
 * - [claimedChests]: observe claimed chest stage numbers for a difficulty.
 * - [claimChest]: attempt to claim a chest (one-time); no-ops if already claimed or ineligible.
 *
 * Improvements:
 * - Manual Job management removed; flows are derived + lifecycle-aware via stateIn().
 * - Errors surfaced as [events] without breaking existing UI (still safe to ignore).
 * - Redundant emissions trimmed via distinctUntilChanged().
 * - Write operations dispatched on [ioDispatcher] for correctness and testability.
 */
class StageViewModel(
    private val stageRepository: StageRepository,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) : ViewModel() {

    /** Event stream for non-fatal errors and chest awards. */
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ---- Selected difficulty & data readiness ----

    /** Selected difficulty; null means "not requested yet" to preserve original on-demand behavior. */
    private val selectedDifficulty = MutableStateFlow<Difficulty?>(null)

    /**
     * Resolved stage data + readiness for the current difficulty.
     *
     * Contract:
     * - While [selectedDifficulty] is null, this stays as `difficulty = null, isLoaded = false`.
     * - After [fetchStagesCount] is called, the first emission for that difficulty marks
     *   `isLoaded = true` and carries the resolved `count`, even when the value is 0.
     * - On non-fatal errors, we emit a fallback status with `count = 0, isFallback = true`
     *   and also surface [UiEvent.Error] of type [UiEvent.ErrorType.StageCountFailed].
     * - These fallback emissions still count as "loaded" for the preflight readiness
     *   contract; UI geometry fallbacks (e.g., empty-list handling) take over from there.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val stageDataStatus: StateFlow<StageDataStatus> =
        selectedDifficulty
            .flatMapLatest { difficulty ->
                if (difficulty == null) {
                    flowOf(StageDataStatus())
                } else {
                    stageRepository
                        .getStagesCount(difficulty)
                        .distinctUntilChanged()
                        .map { count ->
                            StageDataStatus(
                                difficulty = difficulty,
                                isLoaded = true,
                                count = count.coerceAtLeast(0),
                                isFallback = false
                            )
                        }
                        .catch { t ->
                            // Keep UI alive; report via events; emit safe fallback.
                            Log.e(TAG, "Failed to fetch stage count for $difficulty", t)
                            _events.tryEmit(
                                UiEvent.Error(
                                    UiEvent.ErrorType.StageCountFailed,
                                    t
                                )
                            )
                            emit(
                                StageDataStatus(
                                    difficulty = difficulty,
                                    isLoaded = true,
                                    count = 0,
                                    isFallback = true
                                )
                            )
                        }
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = StageDataStatus()
            )

    // ---- stageCount (derived from StageDataStatus) ----

    /**
     * Current-difficulty stage count, derived from [stageDataStatus].
     *
     * - Emits 0 until [fetchStagesCount] is invoked (same as original behavior).
     * - May legitimately remain 0 *after* load when the resolved count is 0 or a
     *   fallback due to a recoverable error.
     * - On repository errors, the fallback 0 still represents "loaded" data for
     *   the preflight contract; UI fallbacks handle empty geometries.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val stageCount: StateFlow<Int> =
        stageDataStatus
            .map { status ->
                if (status.difficulty == null || !status.isLoaded) 0 else status.count
            }
            .distinctUntilChanged()
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = 0
            )

    /**
     * Request stage count for a difficulty.
     * Replaces the previous collection (flatMapLatest handles cancellation).
     */
    fun fetchStagesCount(difficulty: Difficulty) {
        selectedDifficulty.value = difficulty
    }

    // ---- allStageCounts (across all difficulties) ----

    /** Activation gate to preserve on-demand behavior (zeros until fetchAllStageCounts() is called). */
    private val allCountsActive = MutableStateFlow(false)

    private val zeroCounts: Map<Difficulty, Int> =
        Difficulty.entries.associateWith { 0 }

    /**
     * All difficulties → counts.
     *
     * Readiness semantics:
     * - Before [fetchAllStageCounts] is invoked, this emits a map of all difficulties to 0.
     *   This "all zeros" map **does not** imply that data for each difficulty has not
     *   been loaded—it only reflects that the aggregated stream is inactive.
     * - Once activated, each per-difficulty stream emits at least one value (0 is a valid
     *   count, not a sentinel for "no data").
     * - On per-difficulty repository errors we log and emit a fallback 0 for that entry;
     *   these are also treated as loaded for the readiness contract.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val allStageCounts: StateFlow<Map<Difficulty, Int>> =
        allCountsActive
            .flatMapLatest { active ->
                if (!active) {
                    flowOf(zeroCounts)
                } else {
                    val perDifficultyFlows = Difficulty.entries.map { d ->
                        stageRepository
                            .getStagesCount(d)
                            .distinctUntilChanged()
                            .map { c -> d to c.coerceAtLeast(0) }
                            .catch { t ->
                                // Report but keep producing a safe value for this difficulty.
                                Log.e(TAG, "Failed to fetch stage count for $d", t)
                                _events.tryEmit(
                                    UiEvent.Error(
                                        UiEvent.ErrorType.AllStageCountsFailed,
                                        t
                                    )
                                )
                                emit(d to 0)
                            }
                    }
                    combine(perDifficultyFlows) { pairs -> pairs.toMap() }
                        .distinctUntilChanged()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(stopTimeoutMillis = 5_000),
                initialValue = zeroCounts
            )

    /** Activate the all-difficulties counts stream. Idempotent. */
    fun fetchAllStageCounts() {
        allCountsActive.value = true
    }

    // ---- Claimed chests & mutations ----

    /**
     * Observe claimed chest stage numbers for a difficulty.
     *
     * Repository contract:
     * - Always emits at least one value (an empty set is a valid initial emission).
     * - Non-fatal storage errors are handled internally and surfaced via [events];
     *   the flow itself keeps emitting safe defaults.
     */
    fun claimedChests(difficulty: Difficulty) =
        stageRepository
            .observeClaimedChests(difficulty)
            .distinctUntilChanged() // reduce recompositions when set is unchanged

    /**
     * Attempt to claim a chest (one-time).
     * Errors are reported via [events] but not thrown—UI remains responsive (original behavior).
     *
     * Emits [UiEvent.ChestAwarded] when diamonds were actually awarded (first claim).
     */
    fun claimChest(difficulty: Difficulty, stageNumber: Int) {
        viewModelScope.launch(ioDispatcher) {
            try {
                val awarded = stageRepository.claimChestIfEligible(difficulty, stageNumber)
                if (awarded) {
                    _events.emit(UiEvent.ChestAwarded(difficulty, stageNumber))
                }
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to claim chest: $difficulty #$stageNumber", t)
                _events.emit(UiEvent.Error(UiEvent.ErrorType.ClaimChestFailed, t))
                // swallow to keep UI behavior identical
            }
        }
    }

    /** One-shot UI events for non-fatal errors and chest awards. */
    sealed class UiEvent {
        data class Error(val type: ErrorType, val cause: Throwable? = null) : UiEvent()

        data class ChestAwarded(
            val difficulty: Difficulty,
            val stageNumber: Int
        ) : UiEvent()

        /** Typed errors keep ViewModel UI-agnostic (i18n/self-describing in UI). */
        enum class ErrorType {
            StageCountFailed,
            AllStageCountsFailed,
            ClaimChestFailed
        }
    }

    private companion object {
        private const val TAG: String = "StageViewModel"
    }
}
