package com.example.explanationtable.ui.hint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.R
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.repository.HintRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing hint logic and state.
 */
class HintViewModel(application: Application) : AndroidViewModel(application) {

    // Repository for diamond balance and reveal logic
    private val repository = HintRepository(application)

    // Exposed flow of available hint options
    private val _hintOptions = MutableStateFlow<List<HintOption>>(emptyList())
    val hintOptions: StateFlow<List<HintOption>> = _hintOptions.asStateFlow()

    // Exposed flow of cells to reveal when a hint is used
    private val _selectedCells = MutableSharedFlow<List<CellPosition>>(replay = 0)
    val selectedCells: SharedFlow<List<CellPosition>> = _selectedCells.asSharedFlow()

    // Backing storage for the game’s tables
    private var originalTable: LevelTable? = null
    private var currentTable: MutableMap<CellPosition, List<String>>? = null

    // Current difficulty setting (default EASY)
    private val _difficulty = MutableStateFlow(Difficulty.EASY)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    /**
     * Asynchronously load and emit available hint options from the repository.
     */
    fun loadHintOptions() {
        viewModelScope.launch {
            _hintOptions.value = repository.getHintOptions()
        }
    }

    /**
     * Store the original completed table for reference when revealing hints.
     */
    fun setOriginalTableState(table: LevelTable?) {
        originalTable = table
    }

    /**
     * Store the current, partially-filled table for comparison when revealing hints.
     */
    fun setCurrentTableState(table: MutableMap<CellPosition, List<String>>?) {
        currentTable = table
    }

    /**
     * Update the difficulty level, affecting diamond costs.
     */
    fun setDifficulty(diff: Difficulty) {
        _difficulty.value = diff
    }

    val diamondBalance: StateFlow<Int> = repository
        .diamondsFlow
        .stateIn(
            viewModelScope,
            SharingStarted.Eagerly,
            initialValue = 0
        )

    /**
     * Handle when a user taps a hint option:
     * 1. Check diamond balance
     * 2. Deduct fee
     * 3. Compute which cells to reveal
     * 4. Emit those cells to the UI
     */
    fun onOptionSelected(option: HintOption) {
        viewModelScope.launch {
            val cost = option.feeMap[_difficulty.value] ?: 0
            val balance = repository.getDiamondCount()

            // Abort if insufficient diamonds
            if (balance < cost) return@launch

            // Deduct diamonds
            repository.spendDiamonds(cost)

            // Determine which cells to reveal based on option text
            val context = getApplication<Application>()
            val toReveal = when (option.displayText) {
                context.getString(R.string.hint_single_word)    -> revealRandomCategory()
                context.getString(R.string.hint_single_letter)  -> revealRandomCell()
                context.getString(R.string.hint_complete_stage) -> emptyList()
                else                                            -> emptyList()
            }

            // Notify observers which cells should be revealed
            _selectedCells.emit(toReveal)
        }
    }

    /**
     * Reveal all cells in one random category.
     * @return list of positions to reveal, or empty if tables are unset.
     */
    private fun revealRandomCategory(): List<CellPosition> =
        originalTable
            ?.let { orig -> currentTable?.let { curr -> repository.revealRandomCategory(curr, orig) } }
            ?: emptyList()

    /**
     * Reveal one random cell.
     * @return the position to reveal, or empty if tables are unset.
     */
    private fun revealRandomCell(): List<CellPosition> =
        originalTable
            ?.let { orig -> currentTable?.let { curr -> repository.revealRandomCell(curr, orig) } }
            ?: emptyList()
}
