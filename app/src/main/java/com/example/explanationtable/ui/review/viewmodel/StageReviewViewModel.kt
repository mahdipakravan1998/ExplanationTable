package com.example.explanationtable.ui.review.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageReviewRepository
import com.example.explanationtable.ui.review.state.ReviewTableRow
import com.example.explanationtable.ui.review.state.StageReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the “Stage Review” screen.
 *
 * Loads data from [StageReviewRepository] and emits a [StageReviewUiState] that the UI observes.
 */
class StageReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StageReviewRepository()

    // Backing stateflow for UI
    private val _uiState = MutableStateFlow(StageReviewUiState())
    val uiState: StateFlow<StageReviewUiState> = _uiState.asStateFlow()

    companion object {
        // Define row→(tableRow, tableCol, itemIndex) mappings for each difficulty.
        private val EASY_SCHEMA = mapOf(
            0 to Triple(0, 0, 0),
            1 to Triple(0, 2, 0),
            2 to Triple(0, 2, 1),
            3 to Triple(4, 2, 0)
        )
        private val MEDIUM_SCHEMA = mapOf(
            0 to Triple(0, 3, 1),
            1 to Triple(0, 3, 0),
            2 to Triple(0, 1, 1),
            3 to Triple(0, 1, 0),
            4 to Triple(3, 3, 0)
        )
        private val HARD_SCHEMA = mapOf(
            0 to Triple(0, 3, 1),
            1 to Triple(0, 3, 0),
            2 to Triple(0, 0, 0),
            3 to Triple(0, 0, 1),
            4 to Triple(4, 3, 0),
            5 to Triple(4, 3, 1)
        )
    }

    /**
     * Public API to load data for a given [difficulty] and [stageNumber].
     * Emits loading, error, or populated state into [_uiState].
     */
    fun loadStageData(difficulty: Difficulty, stageNumber: Int) {
        viewModelScope.launch {
            // Grab Application once for all string lookups
            val app = getApplication<Application>()

            // 1) Show loading state
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }

            // 2) Fetch data
            val stageData = repository.getStageData(difficulty, stageNumber)
            if (stageData == null) {
                // 3a) No data → show error message
                val errorMsg = app.getString(R.string.error_no_data, stageNumber)
                _uiState.update { it.copy(isLoading = false, errorMessage = errorMsg) }
                return@launch
            }

            // 3b) Build rows and then update UI state
            val rows = buildTableRows(
                difficulty    = difficulty,
                componentsMap = stageData.componentsData.components,
                tableMap      = stageData.tableData.rows
            )

            _uiState.update {
                it.copy(
                    isLoading    = false,
                    errorMessage = null,
                    headerLeft   = app.getString(R.string.header_left),
                    headerRight  = app.getString(R.string.header_right),
                    rows         = rows
                )
            }
        }
    }

    /**
     * Converts the raw maps into a list of [ReviewTableRow], preserving order by index 0..n-1.
     */
    private fun buildTableRows(
        difficulty: Difficulty,
        componentsMap: Map<Int, List<String>>,
        tableMap: Map<Int, Map<Int, List<String>>>
    ): List<ReviewTableRow> {
        // We assume componentsMap keys are 0..N-1
        return List(componentsMap.size) { index ->
            val leftText  = componentsMap[index]?.firstOrNull().orEmpty()
            val rightText = resolveRightText(difficulty, index, tableMap)
            ReviewTableRow(leftText = leftText, rightText = rightText)
        }
    }

    /**
     * Looks up the (row, col, item) triple for this [difficulty] and [rowIndex], then
     * safely retrieves the string or returns "" if any lookup fails.
     */
    private fun resolveRightText(
        difficulty: Difficulty,
        rowIndex: Int,
        tableMap: Map<Int, Map<Int, List<String>>>
    ): String {
        // Pick the correct schema map
        val schema = when (difficulty) {
            Difficulty.EASY   -> EASY_SCHEMA
            Difficulty.MEDIUM -> MEDIUM_SCHEMA
            Difficulty.HARD   -> HARD_SCHEMA
        }

        // Look up the triple and then fetch from the nested maps safely
        return schema[rowIndex]
            ?.let { (tableRow, tableCol, itemIdx) ->
                tableMap[tableRow]
                    ?.get(tableCol)
                    ?.getOrNull(itemIdx)
            }
            .orEmpty()
    }
}
