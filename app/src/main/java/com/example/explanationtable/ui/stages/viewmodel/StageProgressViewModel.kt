package com.example.explanationtable.ui.stages.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.ProgressRepository
import com.example.explanationtable.repository.ProgressRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class StageProgressViewModel(
    application: Application
) : AndroidViewModel(application) {

    // now we can pass a real Context into DataStoreManager
    private val repo: ProgressRepository =
        ProgressRepositoryImpl(DataStoreManager(application))

    private val _lastUnlocked = MutableStateFlow(
        Difficulty.entries.associateWith { 1 }
    )
    val lastUnlocked: StateFlow<Map<Difficulty, Int>> = _lastUnlocked

    init {
        // Collect each difficulty’s unlocked‐stage flow
        Difficulty.entries.forEach { diff ->
            viewModelScope.launch {
                repo.getLastUnlockedStage(diff).collect { unlocked ->
                    _lastUnlocked.update { it + (diff to unlocked) }
                }
            }
        }
    }

    fun markStageCompleted(difficulty: Difficulty, stage: Int) {
        viewModelScope.launch {
            repo.markStageCompleted(difficulty, stage)
        }
    }
}
