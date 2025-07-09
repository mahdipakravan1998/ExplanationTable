package com.example.explanationtable.model

/**
 * A data class to hold the combined data for a specific stage review.
 *
 * This class acts as a Data Transfer Object (DTO) to pass data cleanly
 * from the repository to the ViewModel.
 *
 * @property componentsData The table data related to components.
 * @property tableData The main level table data.
 */
data class StageData(
    val componentsData: LevelComponentsTable,
    val tableData: LevelTable
)