package com.example.explanationtable.ui.gameplay.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class GameplayViewModel : ViewModel() {

    data class GameResult(
        val over: Boolean = false,
        val showPrize: Boolean = false,
        val optimalMoves: Int = 0,
        val accuracy: Int = 0,
        val playerMoves: Int = 0,
        val elapsedMs: Long = 0L
    )

    private val _result = MutableStateFlow(GameResult())
    val result: StateFlow<GameResult> = _result.asStateFlow()

    private val _originalTable = MutableStateFlow<EasyLevelTable?>(null)
    val originalTable: StateFlow<EasyLevelTable?> = _originalTable.asStateFlow()

    private val _currentTable =
        MutableStateFlow<MutableMap<CellPosition, List<String>>?>(null)
    val currentTable: StateFlow<MutableMap<CellPosition, List<String>>?> =
        _currentTable.asStateFlow()

    // Holds the callback to notify when cells are correctly placed
    private var onCellsCorrect: (List<CellPosition>) -> Unit = {}

    // Matches the animation delay in the UI
    private val animationDurationMs = 300L

    /** Reset game result when stage or difficulty changes */
    fun resetGame() {
        _result.value = GameResult()
    }

    /**
     * Called by GameTable when the game is complete.
     * Replicates:
     *   1) 600 ms delay for final animation tick
     *   2) set over=true and stats
     *   3) animationDurationMs delay before showing prize
     */
    fun onGameComplete(
        optimal: Int,
        accuracy: Int,
        moves: Int,
        time: Long
    ) {
        viewModelScope.launch {
            delay(600)
            _result.update {
                it.copy(
                    over = true,
                    optimalMoves = optimal,
                    accuracy = accuracy,
                    playerMoves = moves,
                    elapsedMs = time
                )
            }
            delay(animationDurationMs)
            _result.update { it.copy(showPrize = true) }
        }
    }

    /** Store the initial and current table for hint logic */
    fun setTableData(
        orig: EasyLevelTable,
        current: MutableMap<CellPosition, List<String>>
    ) {
        _originalTable.value = orig
        _currentTable.value = current
    }

    /** Register the callback that GameTable provides for correct‐cell animations */
    fun registerCellsCorrectlyPlacedCallback(callback: (List<CellPosition>) -> Unit) {
        onCellsCorrect = callback
    }

    /** Called by the hint dialog when cells are revealed */
    fun handleCellsRevealed(correctPositions: List<CellPosition>) {
        if (correctPositions.isEmpty()) {
            _result.update { it.copy(over = true) }
        } else {
            onCellsCorrect(correctPositions)
        }
    }
}
