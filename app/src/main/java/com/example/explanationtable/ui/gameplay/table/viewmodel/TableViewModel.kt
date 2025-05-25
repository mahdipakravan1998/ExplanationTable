package com.example.explanationtable.ui.gameplay.table.viewmodel

import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.repository.TableRepository
import com.example.explanationtable.ui.gameplay.table.utils.handleCellClick
import com.example.explanationtable.ui.gameplay.table.utils.handleExternallyCorrectCells
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * ViewModel managing a single puzzle stage:
 * - Initializes and shuffles the table
 * - Tracks user selections, swaps, and correctness
 * - Computes optimal moves in background
 * - Notifies completion when done
 */
class TableViewModel(
    private val repository: TableRepository,
    stageNumber: Int,
    private val onGameComplete: (optimalMoves: Int, accuracy: Int, moves: Int, elapsedMs: Long) -> Unit,
    onTableDataInitialized: (LevelTable, MutableMap<CellPosition, List<String>>) -> Unit,
    registerCorrectPlacementCallback: ((List<CellPosition>) -> Unit) -> Unit
) : ViewModel() {

    // --- UI layout constants ---
    val cellSize = 80.dp
    val spacing = 12.dp
    val signSize = 16.dp

    // --- Immutable game setup ---
    private val gameStartTime = System.currentTimeMillis()
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )
    val originalTableData: LevelTable = repository.getOriginalTable(stageNumber)
    private val movablePositions: List<CellPosition>

    // --- UI-observable state ---
    val currentTableData = mutableStateMapOf<CellPosition, List<String>>()
    val correctlyPlacedCells = mutableStateMapOf<CellPosition, List<String>>()
    val transitioningCells = mutableStateMapOf<CellPosition, List<String>>()

    val firstSelectedCell = mutableStateOf<CellPosition?>(null)
    val secondSelectedCell = mutableStateOf<CellPosition?>(null)
    val isSelectionComplete = mutableStateOf(false)
    val isProcessingSwap = mutableStateOf(false)
    val isGameOver = mutableStateOf(false)

    val playerMoves = mutableStateOf(0)
    val correctMoves = mutableStateOf(0)
    val incorrectMoves = mutableStateOf(0)

    // Computed off-screen: the fewest moves to solve this scramble
    private var optimalMovesForScramble: Int? = null

    init {
        // 1) Build and shuffle the play grid
        val rawMovable = repository.getMovableData(originalTableData, fixedPositions)
        movablePositions = rawMovable.map { it.first }
        val movableValues = rawMovable.map { it.second }

        // Create a deranged shuffle so no piece stays in place
        val shuffledValues = repository.derangeList(movableValues)
        currentTableData.putAll(
            repository.createShuffledTable(shuffledValues, movablePositions, emptyMap())
        )

        // 2) Inform UI that initial table is ready
        onTableDataInitialized(originalTableData, currentTableData)

        // 3) Listen for externally-reported correct placements
        registerCorrectPlacementCallback { newlyCorrect ->
            handleExternallyCorrectCells(
                newlyCorrect,
                firstSelectedCell,
                secondSelectedCell,
                isSelectionComplete,
                currentTableData,
                transitioningCells,
                correctMoves
            )
        }

        // 4) Compute optimal solution length in background
        viewModelScope.launch {
            optimalMovesForScramble = repository.solveMinMoves(shuffledValues, movableValues)
        }

        // 5) Move cells from transitioning → correctlyPlaced after a brief delay
        viewModelScope.launch {
            snapshotFlow { transitioningCells.keys.toList() }
                .collect { positions ->
                    positions.forEach { pos ->
                        val data = transitioningCells[pos] ?: return@forEach
                        delay(50)
                        correctlyPlacedCells[pos] = data
                        transitioningCells.remove(pos)
                    }
                }
        }

        // 6) Detect game completion when all movable cells are placed
        viewModelScope.launch {
            snapshotFlow { correctlyPlacedCells.size }
                .collect { placedCount ->
                    if (!isGameOver.value && placedCount == movablePositions.size) {
                        isGameOver.value = true
                        val elapsed = System.currentTimeMillis() - gameStartTime
                        val optimal = optimalMovesForScramble ?: 0
                        val accuracy = repository.calculateAccuracy(
                            correctMoves.value,
                            incorrectMoves.value
                        )

                        onGameComplete(optimal, accuracy, playerMoves.value, elapsed)
                    }
                }
        }
    }

    /**
     * Called by the UI when the user taps a cell.
     * Prevents new taps only once a swap/animation really starts.
     */
    fun onCellClicked(position: CellPosition) {
        // 1) If we’re already mid-swap or animating, ignore everything
        if (isProcessingSwap.value) return

        // 2) If this is just the first selection, delegate without marking “busy”
        if (firstSelectedCell.value == null) {
            handleCellClick(
                position             = position,
                currentTableData     = currentTableData,
                firstSelectedCellState   = firstSelectedCell,
                secondSelectedCellState  = secondSelectedCell,
                isSelectionCompleteState = isSelectionComplete,
                playerMovesState         = playerMoves,
                originalTableData        = originalTableData,
                movablePositions         = movablePositions,
                transitioningCells       = transitioningCells,
                correctMoveCountState    = correctMoves,
                incorrectMoveCountState  = incorrectMoves,
                coroutineScope           = viewModelScope,
                // onResetSelection here should NOT touch isProcessingSwap
                onResetSelection = {
                    firstSelectedCell.value = null
                    secondSelectedCell.value = null
                    isSelectionComplete.value = false
                },
                isProcessingSwap = isProcessingSwap
            )
            return
        }

        // 3) Otherwise it’s the second tap → we really start the swap, so mark “busy”
        isProcessingSwap.value = true

        handleCellClick(
            position             = position,
            currentTableData     = currentTableData,
            firstSelectedCellState   = firstSelectedCell,
            secondSelectedCellState  = secondSelectedCell,
            isSelectionCompleteState = isSelectionComplete,
            playerMovesState         = playerMoves,
            originalTableData        = originalTableData,
            movablePositions         = movablePositions,
            transitioningCells       = transitioningCells,
            correctMoveCountState    = correctMoves,
            incorrectMoveCountState  = incorrectMoves,
            coroutineScope           = viewModelScope,
            // now when we reset, we clear the busy flag too
            onResetSelection = {
                firstSelectedCell.value = null
                secondSelectedCell.value = null
                isSelectionComplete.value = false
                isProcessingSwap.value = false
            },
            isProcessingSwap = isProcessingSwap
        )
    }


    /** Factory for dependency-injecting the repository and callbacks. */
    class Factory(
        private val repository: TableRepository,
        private val stageNumber: Int,
        private val onGameComplete: (Int, Int, Int, Long) -> Unit,
        private val onTableDataInitialized: (LevelTable, MutableMap<CellPosition, List<String>>) -> Unit,
        private val registerCallback: ((List<CellPosition>) -> Unit) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return TableViewModel(
                repository,
                stageNumber,
                onGameComplete,
                onTableDataInitialized,
                registerCallback
            ) as T
        }
    }
}
