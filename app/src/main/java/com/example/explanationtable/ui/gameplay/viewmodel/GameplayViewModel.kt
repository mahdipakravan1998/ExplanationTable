package com.example.explanationtable.ui.gameplay.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.LevelTable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/**
 * ViewModel for managing the gameplay state, including table data,
 * game completion flow, and hint logic.
 */
class GameplayViewModel : ViewModel() {

    private companion object {
        /** Delay before marking the game as over (ms) to match final UI animation. */
        const val FINAL_ANIMATION_DELAY_MS = 2000L
        /** Delay before showing the prize (ms) to match prize animation. */
        const val PRIZE_ANIMATION_DELAY_MS = 300L
    }

    /**
     * Encapsulates the outcome and stats of a game once it's complete.
     */
    data class GameResult(
        val over: Boolean = false,
        val showPrize: Boolean = false,
        val optimalMoves: Int = 0,
        val accuracy: Int = 0,
        val playerMoves: Int = 0,
        val elapsedMs: Long = 0L
    )

    // ─── Game Result State ────────────────────────────────────────────────────────

    private val _result = MutableStateFlow(GameResult())
    /** Exposed read-only flow of the current game result. */
    val result: StateFlow<GameResult> = _result.asStateFlow()

    // ─── Table Data State ─────────────────────────────────────────────────────────

    private val _originalTable = MutableStateFlow<LevelTable?>(null)
    /** The immutable solution table for the current level. */
    val originalTable: StateFlow<LevelTable?> = _originalTable.asStateFlow()

    private val _currentTable = MutableStateFlow<MutableMap<CellPosition, List<String>>?>(null)
    /** The mutable cell-value map reflecting the player's current progress. */
    val currentTable: StateFlow<MutableMap<CellPosition, List<String>>?> = _currentTable.asStateFlow()

    // ─── Callback for Cell Animations ────────────────────────────────────────────

    /** Client-provided callback to animate correctly placed cells. */
    private var onCellsCorrectlyPlaced: (List<CellPosition>) -> Unit = {}

    // ─── Public API ────────────────────────────────────────────────────────────────

    /**
     * Resets all game result state back to defaults.
     */
    fun resetGame() {
        _result.value = GameResult()
    }

    /**
     * Invoked when the game is completed.
     *
     * Workflow:
     *  1. Wait for final UI animation.
     *  2. Update result with stats and mark `over = true`.
     *  3. Wait for prize animation.
     *  4. Update `showPrize = true`.
     *
     * @param optimalMoves Minimum possible moves for this level.
     * @param accuracy     Final accuracy percentage achieved.
     * @param playerMoves  Number of moves taken by the player.
     * @param elapsedMs    Time taken to complete the game in milliseconds.
     */
    fun onGameComplete(
        optimalMoves: Int,
        accuracy: Int,
        playerMoves: Int,
        elapsedMs: Long
    ) {
        viewModelScope.launch {
            delay(FINAL_ANIMATION_DELAY_MS)
            _result.update {
                it.copy(
                    over = true,
                    optimalMoves = optimalMoves,
                    accuracy = accuracy,
                    playerMoves = playerMoves,
                    elapsedMs = elapsedMs
                )
            }
            delay(PRIZE_ANIMATION_DELAY_MS)
            _result.update { it.copy(showPrize = true) }
        }
    }

    /**
     * Initializes the original solution and current table data.
     *
     * @param original The level’s solution table.
     * @param current  The mutable map of current cell entries.
     */
    fun setTableData(
        original: LevelTable,
        current: MutableMap<CellPosition, List<String>>
    ) {
        _originalTable.value = original
        _currentTable.value = current
    }

    /**
     * Registers a UI callback that animates cells when they are placed correctly.
     *
     * @param callback Called with the list of correct cell positions.
     */
    fun registerCellsCorrectlyPlacedCallback(callback: (List<CellPosition>) -> Unit) {
        onCellsCorrectlyPlaced = callback
    }

    /**
     * Handles reveals from the hint dialog.
     *
     * - If `correctPositions` is empty: treat as game-over,
     *   then show prize after animation.
     * - Otherwise: animate the correctly revealed cells.
     *
     * @param correctPositions Positions revealed as correct by the hint.
     */
    fun handleCellsRevealed(correctPositions: List<CellPosition>) {
        if (correctPositions.isEmpty()) {
            viewModelScope.launch {
                _result.update { it.copy(over = true) }
                delay(PRIZE_ANIMATION_DELAY_MS)
                _result.update { it.copy(showPrize = true) }
            }
        } else {
            onCellsCorrectlyPlaced(correctPositions)
        }
    }
}
