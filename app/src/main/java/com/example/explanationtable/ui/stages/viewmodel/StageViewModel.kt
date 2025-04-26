package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * ViewModel for the stages‐list screen.
 * Internally instantiates a StageRepository so it has a no-arg constructor.
 */
class StageViewModel : ViewModel() {

    // Repository that knows how many stages each difficulty has
    private val stageRepository = StageRepository()

    // Backing state for the stage count
    private val _stageCount = MutableStateFlow(0)
    val stageCount: StateFlow<Int> = _stageCount

    /**
     * Load the number of stages for the given difficulty.
     */
    fun fetchStagesCount(difficulty: Difficulty) {
        viewModelScope.launch {
            stageRepository
                .getStagesCount(difficulty)
                .collect { count ->
                    _stageCount.value = count
                }
        }
    }
}
