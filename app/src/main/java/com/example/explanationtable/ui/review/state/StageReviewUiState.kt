package com.example.explanationtable.ui.review.state

/**
 * Represents a single row in the stage review table.
 *
 * @property leftText The text to display in the left column.
 * @property rightText The text to display in the right column.
 */
data class ReviewTableRow(
    val leftText: String,
    val rightText: String
)

/**
 * Represents the complete state of the StageReviewTable UI.
 *
 * @property headerLeft The title for the left column header.
 * @property headerRight The title for the right column header.
 * @property rows The list of data rows to be displayed in the table.
 * @property errorMessage An optional error message to display if data loading fails.
 * @property isLoading A flag to indicate if data is currently being loaded.
 */
data class StageReviewUiState(
    val headerLeft: String = "",
    val headerRight: String = "",
    val rows: List<ReviewTableRow> = emptyList(),
    val errorMessage: String? = null,
    val isLoading: Boolean = true
)