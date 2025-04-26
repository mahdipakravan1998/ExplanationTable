package com.example.explanationtable.repository

import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyStepCountMap
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

class StageRepository {
    fun getStagesCount(difficulty: Difficulty): Flow<Int> = flow {
        emit(difficultyStepCountMap[difficulty] ?: 9)
    }
}
