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
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel responsible for managing hint logic, difficulty, and diamond balance.
 */
class HintViewModel(application: Application) : AndroidViewModel(application) {

    // Repository handling diamond logic and reveal algorithms.
    private val repository = HintRepository(application)

    // --- Difficulty State ---
    private val _difficulty = MutableStateFlow(Difficulty.EASY)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    // --- Available Hint Options ---
    private val _hintOptions = MutableStateFlow<List<HintOption>>(emptyList())
    val hintOptions: StateFlow<List<HintOption>> = _hintOptions.asStateFlow()

    // --- Diamond Balance ---
    val diamondBalance: StateFlow<Int> = repository
        .diamondsFlow
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 0
        )

    // --- Cells to Reveal Flow ---
    private val _selectedCells = MutableSharedFlow<List<CellPosition>>()
    val selectedCells: SharedFlow<List<CellPosition>> = _selectedCells.asSharedFlow()

    // --- Backing for Puzzle Tables ---
    private var originalTable: LevelTable? = null
    private var currentTable: MutableMap<CellPosition, List<String>>? = null

    /**
     * Loads hint options from the repository asynchronously.
     */
    fun loadHintOptions() = viewModelScope.launch {
        _hintOptions.value = repository.getHintOptions()
    }

    /**
     * Sets the completed solution table for reference when revealing hints.
     */
    fun setOriginalTableState(table: LevelTable?) {
        originalTable = table
    }

    /**
     * Sets the player's current (partially-filled) table state.
     */
    fun setCurrentTableState(table: MutableMap<CellPosition, List<String>>?) {
        currentTable = table
    }

    /**
     * Updates the difficulty level, which affects the cost of hints.
     */
    fun setDifficulty(level: Difficulty) {
        _difficulty.value = level
    }

    /**
     * Handles a user selecting a hint option:
     *   1. Verify sufficient diamond balance.
     *   2. Deduct the appropriate cost.
     *   3. Compute which cells to reveal.
     *   4. Emit those cells to the UI.
     */
    fun onOptionSelected(option: HintOption) = viewModelScope.launch {
        val cost = option.feeMap[_difficulty.value] ?: 0
        val balance = repository.getDiamondCount()

        if (balance < cost) {
            // Not enough diamonds: no action taken.
            return@launch
        }

        // Deduct diamonds for this hint.
        repository.spendDiamonds(cost)

        // Cache localized strings once for comparison.
        val ctx = getApplication<Application>()
        val singleWordKey    = ctx.getString(R.string.hint_single_word)
        val singleLetterKey  = ctx.getString(R.string.hint_single_letter)
        val completeStageKey = ctx.getString(R.string.hint_complete_stage)

        // Decide which cells to reveal based on the selected option.
        val cellsToReveal = when (option.displayText) {
            singleWordKey    -> revealRandomCategory()
            singleLetterKey  -> revealRandomCell()
            completeStageKey -> emptyList()
            else              -> emptyList()
        }

        // Emit positions to reveal in the UI.
        _selectedCells.emit(cellsToReveal)
    }

    /**
     * Reveal all cells belonging to one random category.
     *
     * @return List of positions to reveal, or empty if tables are unset.
     */
    private fun revealRandomCategory(): List<CellPosition> =
        originalTable
            ?.let { orig ->
                currentTable?.let { curr ->
                    repository.revealRandomCategory(curr, orig, _difficulty.value)
                }
            }
            ?: emptyList()

    /**
     * Reveal one random cell from the puzzle.
     *
     * @return Single-element list of the chosen position, or empty if tables are unset.
     */
    private fun revealRandomCell(): List<CellPosition> =
        originalTable
            ?.let { orig ->
                currentTable?.let { curr ->
                    repository.revealRandomCell(curr, orig)
                }
            }
            ?: emptyList()
}
