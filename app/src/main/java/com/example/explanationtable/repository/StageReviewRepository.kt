package com.example.explanationtable.repository

import com.example.explanationtable.data.easy.easyLevelComponentsData
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.data.medium.mediumLevelComponentsData
import com.example.explanationtable.data.medium.mediumLevelTables
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.StageData

class StageReviewRepository {

    /**
     * Retrieves the component and table data for a given difficulty and stage number.
     *
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD).
     * @param stageNumber The 1-indexed stage number.
     * @return A [StageData] object if found, otherwise null.
     */
    fun getStageData(difficulty: Difficulty, stageNumber: Int): StageData? {
        val index = stageNumber - 1

        // pick the right pair of lists based on difficulty
        val (componentsList, tablesList) = when (difficulty) {
            Difficulty.EASY   -> easyLevelComponentsData   to easyLevelTables
            Difficulty.MEDIUM -> mediumLevelComponentsData to mediumLevelTables
            Difficulty.HARD   -> TODO()
        }

        val componentsData = componentsList.getOrNull(index)
        val tableData      = tablesList.getOrNull(index)

        return if (componentsData != null && tableData != null) {
            StageData(componentsData = componentsData, tableData = tableData)
        } else {
            null
        }
    }
}
