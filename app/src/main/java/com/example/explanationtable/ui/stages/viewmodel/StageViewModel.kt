package com.example.explanationtable.ui.stages.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

/**
 * ViewModel for the stages-list screen.
 *
 * Exposes:
 * - stageCount (current difficulty)
 * - allStageCounts (all difficulties) → used to compute global Bee/Pencil ordinals.
 * - claimedChests(difficulty): Flow<Set<Int>>
 * - claimChest(difficulty, stageNumber)
 */
class StageViewModel(application: Application) : AndroidViewModel(application) {

    private val stageRepository = StageRepository(
        dataStore = DataStoreManager(application)
    )

    // Current difficulty count (legacy use)
    private val _stageCount = MutableStateFlow(0)
    val stageCount: StateFlow<Int> = _stageCount

    fun fetchStagesCount(difficulty: Difficulty) {
        viewModelScope.launch {
            stageRepository.getStagesCount(difficulty).collect { count ->
                _stageCount.value = count
            }
        }
    }

    // All difficulties → needed for global ordinal math
    private val _allStageCounts = MutableStateFlow(
        Difficulty.entries.associateWith { 0 }
    )
    val allStageCounts: StateFlow<Map<Difficulty, Int>> = _allStageCounts

    fun fetchAllStageCounts() {
        viewModelScope.launch {
            val flows = Difficulty.entries.map { d ->
                stageRepository.getStagesCount(d).map { c -> d to c }
            }
            combine(flows) { it.toMap() }.collect { map ->
                _allStageCounts.value = map
            }
        }
    }

    /** Observe claimed chest stage numbers for a difficulty. */
    fun claimedChests(difficulty: Difficulty) =
        stageRepository.observeClaimedChests(difficulty)

    /** Attempt to claim a chest (one-time). No-op if already claimed or locked. */
    fun claimChest(difficulty: Difficulty, stageNumber: Int) {
        viewModelScope.launch {
            stageRepository.claimChestIfEligible(difficulty, stageNumber)
        }
    }
}
