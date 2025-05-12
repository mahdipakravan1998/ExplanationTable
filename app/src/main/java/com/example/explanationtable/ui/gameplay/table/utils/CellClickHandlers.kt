package com.example.explanationtable.ui.gameplay.table.utils

import androidx.compose.runtime.MutableState
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import kotlin.collections.forEach

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
    incorrectMoveCountState: MutableState<Int>
) {
    var firstSelectedCell = firstSelectedCellState.value
    var secondSelectedCell = secondSelectedCellState.value
    var isSelectionComplete = isSelectionCompleteState.value

    // If the previously selected cell was removed (resolved by hint), reset selection
    if (firstSelectedCell != null && !currentTableData.containsKey(firstSelectedCell!!)) {
        firstSelectedCell = null
        secondSelectedCell = null
        isSelectionComplete = false
    }

    if (firstSelectedCell == null) {
        firstSelectedCell = position
    } else if (secondSelectedCell == null && position != firstSelectedCell) {
        secondSelectedCell = position
        isSelectionComplete = true

        // Swap the data between the two selected cells.
        val first = firstSelectedCell
        val second = secondSelectedCell
        if (first != null && second != null) {
            val temp = currentTableData[first]
            currentTableData[first] = currentTableData[second] ?: listOf("?")
            currentTableData[second] = temp ?: listOf("?")
        }

        // Increment player move count.
        playerMovesState.value = playerMovesState.value + 1

        // Check for any movable cell that now has correct data and count them.
        var newlyCorrectCount = 0
        movablePositions.forEach { pos ->
            val originalData = originalTableData.rows[pos.row]?.get(pos.col)
            if (currentTableData[pos] == originalData) {
                transitioningCells[pos] = currentTableData[pos]!!
                currentTableData.remove(pos)
                newlyCorrectCount++
            }
        }

        // Update move tracking based on newly correct cells.
        if (newlyCorrectCount > 0) {
            correctMoveCountState.value = correctMoveCountState.value + 1
        } else {
            incorrectMoveCountState.value = incorrectMoveCountState.value + 1
        }
    }

    firstSelectedCellState.value = firstSelectedCell
    secondSelectedCellState.value = secondSelectedCell
    isSelectionCompleteState.value = isSelectionComplete
}