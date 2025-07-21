package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow

interface ProgressRepository {
    /** Emits the highest stage number unlocked so far (starts at 1). */
    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int>

    /**
     * Marks `stage` completed; if it equals the current unlocked,
     * then advances “last unlocked” to stage+1 (capped at max).
     */
    suspend fun markStageCompleted(difficulty: Difficulty, stage: Int)
}
