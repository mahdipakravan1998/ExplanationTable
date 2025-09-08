package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
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
 */
class StageViewModel : ViewModel() {

    private val stageRepository = StageRepository()

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
}
