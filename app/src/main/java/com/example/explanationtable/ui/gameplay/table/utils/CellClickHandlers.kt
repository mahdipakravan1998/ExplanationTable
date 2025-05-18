package com.example.explanationtable.ui.gameplay.table.utils

import androidx.compose.runtime.MutableState
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Handles user taps on a game-cell:
 * 1. Validates that any previously selected cells are still in the table.
 * 2. Captures a first tap (selection) or a second tap (swap) and performs the swap.
 * 3. Updates move counts, checks correctness against the original solution,
 *    and triggers UI transitions with a slight delay.
 */
fun handleCellClick(
    position: CellPosition,
    currentTableData: MutableMap<CellPosition, List<String>>,
    firstSelectedCellState: MutableState<CellPosition?>,
    secondSelectedCellState: MutableState<CellPosition?>,
    isSelectionCompleteState: MutableState<Boolean>,
    playerMovesState: MutableState<Int>,
    originalTableData: EasyLevelTable,
    movablePositions: List<CellPosition>,
    transitioningCells: MutableMap<CellPosition, List<String>>,
    correctMoveCountState: MutableState<Int>,
    incorrectMoveCountState: MutableState<Int>,
    coroutineScope: CoroutineScope,
    onResetSelection: () -> Unit,
    isProcessingSwap: MutableState<Boolean>
) {
    // Helper to clear both selections and reset flags
    fun resetAllSelections() {
        onResetSelection()
        firstSelectedCellState.value = null
        secondSelectedCellState.value = null
        isSelectionCompleteState.value = false
        isProcessingSwap.value = false
    }

    // --- 1) VALIDATE EXISTING SELECTIONS ---
    // If the first selection no longer exists in the data, reset everything
    firstSelectedCellState.value
        ?.takeIf { it !in currentTableData }
        ?.let { resetAllSelections() }

    // If the second selection is invalid, but the first is still valid → clear only second.
    // Otherwise reset everything.
    secondSelectedCellState.value
        ?.takeIf { it !in currentTableData }
        ?.let {
            if (firstSelectedCellState.value?.let { it in currentTableData } == true) {
                secondSelectedCellState.value = null
                isSelectionCompleteState.value = false
            } else {
                resetAllSelections()
            }
        }

    // --- 2) FIRST CLICK: SELECT A CELL ---
    if (firstSelectedCellState.value == null) {
        // Only allow selecting cells that are present
        if (position in currentTableData) {
            firstSelectedCellState.value = position
        }
        return
    }

    // --- 3) SECOND CLICK: SWAP SELECTED WITH THIS CELL ---
    if (secondSelectedCellState.value == null
        && position != firstSelectedCellState.value
        && position in currentTableData
    ) {
        // Signal that a swap animation/logic is in progress
        isProcessingSwap.value = true
        secondSelectedCellState.value = position

        val firstPos = firstSelectedCellState.value!!    // non-null by guard above
        val secondPos = position
        val dataA = currentTableData[firstPos]
        val dataB = currentTableData[secondPos]

        // If either cell’s data is unexpectedly null, abort and reset
        if (dataA == null || dataB == null) {
            resetAllSelections()
            return
        }

        // Perform the actual swap
        currentTableData[firstPos] = dataB
        currentTableData[secondPos] = dataA

        // Track that the player made another move
        playerMovesState.value++

        // Check if either swapped cell now matches its original (correct) value
        var newlyCorrect = 0
        listOf(firstPos, secondPos).forEach { pos ->
            if (pos in movablePositions) {
                val currentData = currentTableData[pos]
                val originalData = originalTableData.rows[pos.row]?.get(pos.col)
                if (currentData != null && currentData == originalData) {
                    // Mark this cell for transition animation, then remove it from the active map
                    transitioningCells[pos] = currentData
                    currentTableData.remove(pos)
                    newlyCorrect++
                }
            }
        }

        // Increment correct/incorrect counters
        if (newlyCorrect > 0) correctMoveCountState.value++ else incorrectMoveCountState.value++

        // Signal that selection is complete, then reset after a short delay
        isSelectionCompleteState.value = true
        coroutineScope.launch {
            delay(200L)  // allow any UI animations to play
            onResetSelection()
        }
        return
    }

    // --- 4) DESELECT: CLICKING THE SAME CELL AGAIN ---
    // If the user taps the first-selected cell a second time (and no second cell is chosen),
    // treat it as “undo selection.”
    if (position == firstSelectedCellState.value
        && secondSelectedCellState.value == null
    ) {
        resetAllSelections()
        return
    }
}
