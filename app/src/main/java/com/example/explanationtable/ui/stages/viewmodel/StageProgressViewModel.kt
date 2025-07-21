package com.example.explanationtable.ui.stages.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.ProgressRepository
import com.example.explanationtable.repository.ProgressRepositoryImpl
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel managing the user’s progress through stages, per difficulty.
 * Exposes a StateFlow mapping each Difficulty to its last unlocked stage.
 */
class StageProgressViewModel(application: Application) : AndroidViewModel(application) {

    // Repository backed by DataStore
    private val repository: ProgressRepository =
        ProgressRepositoryImpl(DataStoreManager(application))

    // Default all difficulties to stage 1 until real values load
    private val initialUnlocked: Map<Difficulty, Int> =
        Difficulty.entries.associateWith { 1 }

    /**
     * Internal StateFlow combining each difficulty’s `getLastUnlockedStage` Flow.
     */
    private val _lastUnlocked: StateFlow<Map<Difficulty, Int>> =
        Difficulty.entries
            .map { difficulty ->
                repository
                    .getLastUnlockedStage(difficulty)
                    .map { unlockedStage -> difficulty to unlockedStage }
            }
            .let { difficultyFlows ->
                combine(difficultyFlows) { latestPairs ->
                    latestPairs.toMap()
                }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.Eagerly,
                initialValue = initialUnlocked
            )

    /** Public read‐only view of the last‐unlocked map (used by the UI). */
    val lastUnlocked: StateFlow<Map<Difficulty, Int>> = _lastUnlocked

    /**
     * Marks [stage] as completed for the given [difficulty].
     * Delegates to the repository on a background coroutine.
     */
    fun markStageCompleted(difficulty: Difficulty, stage: Int) {
        viewModelScope.launch {
            repository.markStageCompleted(difficulty, stage)
        }
    }
}
