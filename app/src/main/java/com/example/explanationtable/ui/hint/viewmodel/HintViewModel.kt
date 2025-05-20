package com.example.explanationtable.ui.hint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.R
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.repository.HintRepository
import com.example.explanationtable.model.CellPosition
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for handling hint-related logic and state for the game.
 */
class HintViewModel(application: Application) : AndroidViewModel(application) {

    // Repository for fetching hint data
    private val repository = HintRepository(application)

    // StateFlow for hint options and selected cells
    private val _hintOptions = MutableStateFlow<List<HintOption>>(emptyList())
    val hintOptions: StateFlow<List<HintOption>> = _hintOptions.asStateFlow()

    private val _selectedCells = MutableSharedFlow<List<CellPosition>>()
    val selectedCells: SharedFlow<List<CellPosition>> = _selectedCells.asSharedFlow()

    // Table states for the game
    private var originalTable: EasyLevelTable? = null
    private var currentTable: MutableMap<CellPosition, List<String>>? = null

    // Difficulty level, defaulting to EASY
    private val _difficulty = MutableStateFlow(Difficulty.EASY)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    /**
     * Loads hint options from the repository asynchronously.
     */
    fun loadHintOptions() {
        viewModelScope.launch {
            _hintOptions.value = repository.getHintOptions()
        }
    }

    /**
     * Sets the original table state.
     * @param table The original game table state.
     */
    fun setOriginalTableState(table: EasyLevelTable?) {
        originalTable = table
    }

    /**
     * Sets the current table state.
     * @param table The current game table state.
     */
    fun setCurrentTableState(table: MutableMap<CellPosition, List<String>>?) {
        currentTable = table
    }

    /**
     * Sets the difficulty level.
     * @param diff The new difficulty level.
     */
    fun setDifficulty(diff: Difficulty) {
        _difficulty.value = diff
    }

    /**
     * Handles the selection of a hint option and triggers the appropriate action.
     * @param option The selected hint option.
     */
    fun onOptionSelected(option: HintOption) {
        viewModelScope.launch {
            val fee = option.feeMap[_difficulty.value] ?: 0
            val balance = repository.getDiamondCount()

            // Bail out if not enough diamonds
            if (balance < fee) return@launch

            // Deduct and then compute which cells to reveal
            repository.spendDiamonds(fee)

            // Reveal cells according to the option tapped
            val context = getApplication<Application>()
            val cells = when (option.displayText) {
                context.getString(R.string.hint_single_word)    -> getCellsForRandomCategory()
                context.getString(R.string.hint_single_letter)  -> getCellsForRandomCell()
                context.getString(R.string.hint_complete_stage) -> emptyList()
                else                                            -> emptyList()
            }

            // Notify the UI of which cells to reveal
            _selectedCells.emit(cells)
        }
    }

    /**
     * Fetches cells for revealing a random category.
     * @return List of revealed cell positions.
     */
    private fun getCellsForRandomCategory(): List<CellPosition> {
        return originalTable?.let { orig ->
            currentTable?.let { curr ->
                // Call the repository to reveal a random category of cells
                repository.revealRandomCategory(curr, orig)
            }
        } ?: emptyList()
    }

    /**
     * Fetches cells for revealing a random cell.
     * @return List of revealed cell positions.
     */
    private fun getCellsForRandomCell(): List<CellPosition> {
        return originalTable?.let { orig ->
            currentTable?.let { curr ->
                // Call the repository to reveal a random cell
                repository.revealRandomCell(curr, orig)
            }
        } ?: emptyList()
    }
}
