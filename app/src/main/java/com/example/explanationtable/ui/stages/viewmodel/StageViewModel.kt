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
 * ViewModel for the stages-list screen.
 *
 * Public contract (preserved):
 * - [stageCount]: current difficulty's stage count (emits 0 until [fetchStagesCount] is called).
 * - [allStageCounts]: map of all difficulties to their counts (all zeros until [fetchAllStageCounts] is called).
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

    /** Event stream for non-fatal errors. Safe for UI to ignore to keep behavior identical. */
    private val _events = MutableSharedFlow<UiEvent>(
        replay = 0,
        extraBufferCapacity = 4,
        onBufferOverflow = BufferOverflow.DROP_OLDEST
    )
    val events: SharedFlow<UiEvent> = _events.asSharedFlow()

    // ---- stageCount (per selected difficulty) ----

    /** Selected difficulty; null means "not requested yet" to preserve original on-demand behavior. */
    private val selectedDifficulty = MutableStateFlow<Difficulty?>(null)

    /**
     * Current-difficulty stage count.
     * - Emits 0 until [fetchStagesCount] is invoked (same as original behavior).
     * - Switching difficulty cancels prior stream via flatMapLatest.
     */
    @OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
    val stageCount: StateFlow<Int> =
        selectedDifficulty
            .flatMapLatest { difficulty ->
                if (difficulty == null) {
                    flowOf(0) // preserve "no fetch yet" behavior
                } else {
                    stageRepository
                        .getStagesCount(difficulty)
                        .distinctUntilChanged()
                        .catch { t ->
                            // Keep UI alive; report via events; emit safe fallback.
                            Log.e(TAG, "Failed to fetch stage count for $difficulty", t)
                            _events.tryEmit(UiEvent.Error(UiEvent.ErrorType.StageCountFailed, t))
                            emit(0)
                        }
                }
            }
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
     * - Stays at all zeros until [fetchAllStageCounts] is invoked (preserved behavior).
     * - Uses combine() of per-difficulty flows; each guarded with catch() for resilience.
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
                            .map { c -> d to c }
                            .catch { t ->
                                // Report but keep producing a safe value for this difficulty.
                                Log.e(TAG, "Failed to fetch stage count for $d", t)
                                _events.tryEmit(UiEvent.Error(UiEvent.ErrorType.AllStageCountsFailed, t))
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

    /** Observe claimed chest stage numbers for a difficulty. */
    fun claimedChests(difficulty: Difficulty) =
        stageRepository
            .observeClaimedChests(difficulty)
            .distinctUntilChanged() // reduce recompositions when set is unchanged

    /**
     * Attempt to claim a chest (one-time).
     * Errors are reported via [events] but not thrown—UI remains responsive (original behavior).
     */
    fun claimChest(difficulty: Difficulty, stageNumber: Int) {
        viewModelScope.launch(ioDispatcher) {
            try {
                stageRepository.claimChestIfEligible(difficulty, stageNumber)
            } catch (t: Throwable) {
                Log.e(TAG, "Failed to claim chest: $difficulty #$stageNumber", t)
                _events.emit(UiEvent.Error(UiEvent.ErrorType.ClaimChestFailed, t))
                // swallow to keep UI behavior identical
            }
        }
    }

    /** One-shot UI events for non-fatal errors. */
    sealed class UiEvent {
        data class Error(val type: ErrorType, val cause: Throwable? = null) : UiEvent()

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
