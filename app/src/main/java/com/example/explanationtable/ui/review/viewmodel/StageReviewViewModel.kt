package com.example.explanationtable.ui.review.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.R
import com.example.explanationtable.repository.StageReviewRepository
import com.example.explanationtable.ui.review.state.ReviewTableRow
import com.example.explanationtable.ui.review.state.StageReviewUiState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for the stage review screen.
 *
 * This class is responsible for fetching and processing the data required by the
 * [StageReviewTable] composable. It follows the MVVM architecture by separating
 * business logic from the UI.
 *
 * @param application The application context, used for accessing string resources.
 */
class StageReviewViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = StageReviewRepository()

    private val _uiState = MutableStateFlow(StageReviewUiState())
    val uiState: StateFlow<StageReviewUiState> = _uiState.asStateFlow()

    /**
     * Loads the data for a specific stage number and updates the UI state.
     *
     * @param stageNumber The 1-indexed stage number to load data for.
     */
    fun loadStageData(stageNumber: Int) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val stageData = repository.getStageData(stageNumber)

            if (stageData == null) {
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        errorMessage = getApplication<Application>().getString(R.string.error_no_data, stageNumber)
                    )
                }
                return@launch
            }

            val componentsData = stageData.componentsData
            val tableData = stageData.tableData
            val rowCount = componentsData.components.size

            val tableRows = (0 until rowCount).map { i ->
                val leftColumnData = componentsData.components[i]?.firstOrNull() ?: ""

                val rightColumnData = when (i) {
                    0 -> tableData.rows[0]?.get(0)?.firstOrNull() ?: ""
                    1 -> tableData.rows[0]?.get(2)?.getOrNull(0) ?: ""
                    2 -> tableData.rows[0]?.get(2)?.getOrNull(1) ?: ""
                    3 -> tableData.rows[4]?.get(2)?.firstOrNull() ?: ""
                    else -> ""
                }
                ReviewTableRow(leftText = leftColumnData, rightText = rightColumnData)
            }

            _uiState.update {
                it.copy(
                    isLoading = false,
                    errorMessage = null,
                    headerLeft = getApplication<Application>().getString(R.string.header_left),
                    headerRight = getApplication<Application>().getString(R.string.header_right),
                    rows = tableRows
                )
            }
        }
    }
}