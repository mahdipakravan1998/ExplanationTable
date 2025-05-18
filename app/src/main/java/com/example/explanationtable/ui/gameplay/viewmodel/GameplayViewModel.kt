package com.example.explanationtable.ui.gameplay.viewmodel

import androidx.compose.runtime.*
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.repository.GameplayRepository
import com.example.explanationtable.ui.gameplay.table.utils.handleExternallyCorrectCells
import com.example.explanationtable.ui.gameplay.table.utils.handleCellClick
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class GameplayViewModel(
    private val repository: GameplayRepository,
    stageNumber: Int,
    private val onGameComplete: (Int, Int, Int, Long) -> Unit,
    onTableDataInitialized: (EasyLevelTable, MutableMap<CellPosition, List<String>>) -> Unit,
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit
) : ViewModel() {
    // --- Layout constants exposed to UI ---
    val cellSize = 80.dp
    val spacing = 12.dp
    val signSize = 16.dp

    // --- Immutable setup ---
    private val gameStartTime = System.currentTimeMillis()
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )
    val originalTableData: EasyLevelTable
    private val movablePositions: List<CellPosition>

    // --- UI state maps & counters ---
    val currentTableData = mutableStateMapOf<CellPosition, List<String>>()
    val correctlyPlacedCells = mutableStateMapOf<CellPosition, List<String>>()
    val transitioningCells = mutableStateMapOf<CellPosition, List<String>>()
    val firstSelectedCellState = mutableStateOf<CellPosition?>(null)
    val secondSelectedCellState = mutableStateOf<CellPosition?>(null)
    val isSelectionCompleteState = mutableStateOf(false)
    val isGameOverState = mutableStateOf(false)
    val playerMovesState = mutableStateOf(0)
    val correctMoveCountState = mutableStateOf(0)
    val incorrectMoveCountState = mutableStateOf(0)
    private var minMovesForThisScramble: Int? = null
    val isProcessingSwapState = mutableStateOf(false)

    init {
        // 1) Initialize original + shuffled table
        originalTableData = repository.getOriginalTable(stageNumber)
        val rawMovable = repository.getMovableData(originalTableData, fixedPositions)
        movablePositions = rawMovable.map { it.first }
        val movableData = rawMovable.map { it.second }
        val shuffled = repository.derangeList(movableData)
        currentTableData.putAll(
            repository.createShuffledTable(shuffled, movablePositions, emptyMap())
        )

        // 2) Register “externally placed” callback
        registerCellsCorrectlyPlacedCallback { correctList ->
            handleExternallyCorrectCells(
                correctList,
                firstSelectedCellState,
                secondSelectedCellState,
                isSelectionCompleteState,
                currentTableData,
                transitioningCells,
                correctMoveCountState
            )
        }

        // 3) Fire table-initialized
        onTableDataInitialized(originalTableData, currentTableData)

        // 4) Compute min-moves off main thread
        viewModelScope.launch {
            minMovesForThisScramble =
                repository.solveMinMoves(shuffled, movableData)
        }

        // 5) Transitioning → correctlyPlaced after 50ms
        // continuously process any new transitioning cells
        viewModelScope.launch {
            snapshotFlow { transitioningCells.keys.toList() }
                .collect { keys ->
                    keys.forEach { pos ->
                        val data = transitioningCells[pos] ?: return@forEach
                        delay(50)
                        correctlyPlacedCells[pos] = data
                        transitioningCells.remove(pos)
                    }
                }
        }

        // 6) Watch for game-over
        viewModelScope.launch {
            snapshotFlow { correctlyPlacedCells.size }
                .collect { placedCount ->
                    if (!isGameOverState.value && placedCount == movablePositions.size) {
                        isGameOverState.value = true
                        val elapsed = System.currentTimeMillis() - gameStartTime
                        val optimal = minMovesForThisScramble ?: 0
                        val accuracy = repository.calculateAccuracy(
                            correctMoveCountState.value,
                            incorrectMoveCountState.value
                        )

                        onGameComplete(optimal, accuracy, playerMovesState.value, elapsed)
                    }
                }
        }
    }

    /** Called from UI when a square is tapped. */
    fun onCellClicked(position: CellPosition) {
        if (!isProcessingSwapState.value) {
            handleCellClick(
                position,
                currentTableData,
                firstSelectedCellState,
                secondSelectedCellState,
                isSelectionCompleteState,
                playerMovesState,
                originalTableData,
                movablePositions,
                transitioningCells,
                correctMoveCountState,
                incorrectMoveCountState,
                viewModelScope,
                onResetSelection = {
                    firstSelectedCellState.value = null
                    secondSelectedCellState.value = null
                    isSelectionCompleteState.value = false
                    isProcessingSwapState.value = false
                },
                isProcessingSwap = isProcessingSwapState
            )
        }
    }

    /** Factory to inject Repository & callbacks. */
    class Factory(
        private val repository: GameplayRepository,
        private val stageNumber: Int,
        private val onGameComplete: (Int, Int, Int, Long) -> Unit,
        private val onTableDataInitialized: (EasyLevelTable, MutableMap<CellPosition, List<String>>) -> Unit,
        private val registerCallback: ((List<CellPosition>) -> Unit) -> Unit
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            GameplayViewModel(
                repository,
                stageNumber,
                onGameComplete,
                onTableDataInitialized,
                registerCallback
            ) as T
    }
}
