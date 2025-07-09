package com.example.explanationtable.repository

import com.example.explanationtable.data.easy.easyLevelComponentsData
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.StageData

/**
 * Repository for fetching stage review data.
 *
 * This class abstracts the data source, providing a clean API for the ViewModel
 * to fetch data without knowing its origin (e.g., hardcoded lists, database, or network).
 */
class StageReviewRepository {

    /**
     * Retrieves the component and table data for a given stage number.
     *
     * @param stageNumber The 1-indexed stage number.
     * @return A [StageData] object containing the required data if found, otherwise null.
     */
    fun getStageData(stageNumber: Int): StageData? {
        // Adjust for 0-based index access
        val index = stageNumber - 1

        val componentsData = easyLevelComponentsData.getOrNull(index)
        val tableData = easyLevelTables.getOrNull(index)

        // Both data sources must be present to proceed.
        return if (componentsData != null && tableData != null) {
            StageData(componentsData = componentsData, tableData = tableData)
        } else {
            null
        }
    }
}