package com.example.explanationtable.repository

import com.example.explanationtable.data.easy.easyLevelComponentsData
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.data.medium.mediumLevelComponentsData
import com.example.explanationtable.data.medium.mediumLevelTables
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.StageData

/**
 * Repository responsible for retrieving stage review data
 * (components + tables) for a given [Difficulty] and 1-based stage number.
 */
class StageReviewRepository {

    /**
     * Retrieves the [StageData] for the specified [difficulty] and [stageNumber].
     *
     * @param difficulty The difficulty level (EASY, MEDIUM, HARD).
     * @param stageNumber 1-based stage number.
     * @return A [StageData] instance if both component and table data exist; otherwise null.
     * @throws NotImplementedError when [Difficulty.HARD] is requested.
     */
    fun getStageData(difficulty: Difficulty, stageNumber: Int): StageData? {
        // Convert 1-based stageNumber to zero-based index
        val stageIndex = stageNumber - 1

        // Select the appropriate data lists based on the difficulty
        val (componentsList, tablesList) = when (difficulty) {
            Difficulty.EASY -> easyLevelComponentsData to easyLevelTables
            Difficulty.MEDIUM -> mediumLevelComponentsData to mediumLevelTables
            Difficulty.HARD ->
                // HARD difficulty is not yet supported
                throw NotImplementedError("Stage data for HARD difficulty is not implemented.")
        }

        // Safely attempt to retrieve the component and table data at the given index
        val componentData = componentsList.getOrNull(stageIndex)
        val tableData = tablesList.getOrNull(stageIndex)

        // Only return a StageData if both parts are present
        return if (componentData != null && tableData != null) {
            StageData(
                componentsData = componentData,
                tableData = tableData
            )
        } else {
            // One or both lists didn't have an entry at the requested index
            null
        }
    }
}
