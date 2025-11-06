package com.example.explanationtable.ui.main.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import com.example.explanationtable.repository.ProgressRepository
import com.example.explanationtable.repository.ProgressRepositoryImpl
import com.example.explanationtable.repository.SettingsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * MainViewModel owns app-level UI state and one-shot navigation events for the main page.
 *
 * - Settings: exposed via StateFlow (theme, diamonds).
 * - Start Game: computes a **single route** based on progress policy (see KDoc on method).
 * - Behavior is preserved exactly, including legacy uppercase "GAMEPLAY" route emission.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Single DataStore manager shared by repositories (existing approach preserved)
    private val dataStoreManager = DataStoreManager(application)

    // Theme baseline from system
    private val systemDarkDefault: Boolean =
        (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    // Settings + Progress repositories
    private val settingsRepo = SettingsRepository(
        dataStore = dataStoreManager,
        systemDarkDefault = systemDarkDefault
    )
    private val progressRepo: ProgressRepository = ProgressRepositoryImpl(dataStoreManager)

    // Public UI state
    val isDarkTheme: StateFlow<Boolean> = settingsRepo.isDarkTheme
        .stateIn(viewModelScope, SharingStarted.Eagerly, systemDarkDefault)

    val diamonds: StateFlow<Int> = settingsRepo.diamonds
        .stateIn(viewModelScope, SharingStarted.Eagerly, /* initial */ 200)

    // One-shot navigation to gameplay
    private val _startGameRoutes = MutableSharedFlow<String>(replay = 0, extraBufferCapacity = 1)
    val startGameRoutes: SharedFlow<String> = _startGameRoutes

    /**
     * Resolve the target stage/difficulty and emit a single navigation route.
     *
     * Policy (preserved; fast, deterministic):
     * 1) Prefer a **newly unlocked but not yet played** stage (E → M → H).
     * 2) Else, if there is room to progress in any difficulty (E → M → H), go to last unlocked there.
     * 3) Else (all done), route to **EASY** at its max stage.
     *
     * Data sanity:
     * - Stage counts: from `difficultyStepCountMap`, default to [DEFAULT_STAGE_COUNT] if missing.
     * - Clamp unlocked into [1..count], played into [0..count].
     * - On any failure, fall back to `"GAMEPLAY/1/EASY"`.
     */
    fun onStartGameClick() {
        viewModelScope.launch {
            val route = try {
                withContext(Dispatchers.IO) { resolveStartGameRoute() }
            } catch (_: Throwable) {
                FALLBACK_ROUTE
            }
            _startGameRoutes.tryEmit(route)
        }
    }

    // --- Internal helpers ---

    private suspend fun resolveStartGameRoute(): String {
        // Reads are IO-bound; logic is trivial CPU
        val eU = progressRepo.getLastUnlockedStageOnce(Difficulty.EASY)
        val mU = progressRepo.getLastUnlockedStageOnce(Difficulty.MEDIUM)
        val hU = progressRepo.getLastUnlockedStageOnce(Difficulty.HARD)

        val eP = progressRepo.getLastPlayedStageOnce(Difficulty.EASY)
        val mP = progressRepo.getLastPlayedStageOnce(Difficulty.MEDIUM)
        val hP = progressRepo.getLastPlayedStageOnce(Difficulty.HARD)

        val easyMax = (difficultyStepCountMap[Difficulty.EASY] ?: DEFAULT_STAGE_COUNT).coerceAtLeast(1)
        val medMax  = (difficultyStepCountMap[Difficulty.MEDIUM] ?: DEFAULT_STAGE_COUNT).coerceAtLeast(1)
        val hardMax = (difficultyStepCountMap[Difficulty.HARD] ?: DEFAULT_STAGE_COUNT).coerceAtLeast(1)

        val eUnlocked = eU.coerceIn(1, easyMax)
        val mUnlocked = mU.coerceIn(1, medMax)
        val hUnlocked = hU.coerceIn(1, hardMax)

        val ePlayed = eP.coerceIn(0, easyMax)
        val mPlayed = mP.coerceIn(0, medMax)
        val hPlayed = hP.coerceIn(0, hardMax)

        // Priority 1: newly unlocked but not yet played
        val pickNew = when {
            eUnlocked > ePlayed -> eUnlocked to Difficulty.EASY
            mUnlocked > mPlayed -> mUnlocked to Difficulty.MEDIUM
            hUnlocked > hPlayed -> hUnlocked to Difficulty.HARD
            else -> null
        }

        val (finalStage, finalDiff) = pickNew ?: when {
            // Priority 2: still room to progress
            eUnlocked < easyMax -> eUnlocked to Difficulty.EASY
            mUnlocked < medMax  -> mUnlocked to Difficulty.MEDIUM
            hUnlocked < hardMax -> hUnlocked to Difficulty.HARD
            else                -> easyMax   to Difficulty.EASY // Priority 3: all done
        }

        // NOTE: Preserving legacy uppercase "GAMEPLAY" and enum .name casing.
        return "GAMEPLAY/$finalStage/${finalDiff.name}"
    }

    private companion object {
        const val DEFAULT_STAGE_COUNT = 9
        const val FALLBACK_ROUTE = "GAMEPLAY/1/EASY"
    }
}
