package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepository
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the stages-list screen (interface-driven).
 *
 * Exposes:
 * - stageCount (current difficulty)
 * - allStageCounts (all difficulties)
 * - claimedChests(difficulty): Flow<Set<Int>>
 * - claimChest(difficulty, stageNumber)
 */
class StageViewModel(
    private val stageRepository: StageRepository
) : ViewModel() {

    // Current difficulty count
    private val _stageCount = MutableStateFlow(0)
    val stageCount: StateFlow<Int> = _stageCount

    private var stageCountJob: Job? = null

    fun fetchStagesCount(difficulty: Difficulty) {
        stageCountJob?.cancel()
        stageCountJob = viewModelScope.launch {
            stageRepository
                .getStagesCount(difficulty)
                .catch { /* swallow or log; keep UI alive */ }
                .collect { count -> _stageCount.value = count }
        }
    }

    // All difficulties → needed for global ordinal math
    private val _allStageCounts = MutableStateFlow(
        Difficulty.entries.associateWith { 0 }
    )
    val allStageCounts: StateFlow<Map<Difficulty, Int>> = _allStageCounts

    private var allCountsJob: Job? = null

    fun fetchAllStageCounts() {
        allCountsJob?.cancel()
        allCountsJob = viewModelScope.launch {
            val flows = Difficulty.entries.map { d ->
                stageRepository.getStagesCount(d).map { c -> d to c }
            }
            combine(flows) { it.toMap() }
                .catch { /* swallow or log */ }
                .collect { map -> _allStageCounts.value = map }
        }
    }

    /** Observe claimed chest stage numbers for a difficulty. */
    fun claimedChests(difficulty: Difficulty) =
        stageRepository.observeClaimedChests(difficulty)

    /** Attempt to claim a chest (one-time). No-op if already claimed or locked. */
    fun claimChest(difficulty: Difficulty, stageNumber: Int) {
        viewModelScope.launch {
            runCatching {
                stageRepository.claimChestIfEligible(difficulty, stageNumber)
            }
            // Errors are intentionally swallowed; UI remains responsive.
        }
    }
}
