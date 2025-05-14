package com.example.explanationtable.ui.gameplay.table.utils

import androidx.compose.runtime.MutableState
import com.example.explanationtable.model.CellPosition
import kotlin.collections.contains
import kotlin.collections.forEach

fun handleExternallyCorrectCells(
    correctPositions: List<CellPosition>,
    firstSelectedCellState: MutableState<CellPosition?>,
    secondSelectedCellState: MutableState<CellPosition?>,
    isSelectionCompleteState: MutableState<Boolean>,
    currentTableData: MutableMap<CellPosition, List<String>>,
    transitioningCells: MutableMap<CellPosition, List<String>>,
    correctMoveCountState: MutableState<Int>,
) {
    if (correctPositions.isNotEmpty()) {
        // Clear any selections that refer to cells now resolved by help
        if (firstSelectedCellState.value in correctPositions) {
            firstSelectedCellState.value = null
            secondSelectedCellState.value = null
            isSelectionCompleteState.value = false
        }

        // Process each correctly placed cell
        correctPositions.forEach { pos ->
            // Only process if the cell is still in the current table (not already marked as correct)
            if (currentTableData.containsKey(pos)) {
                // Get the data for this position
                val cellData = currentTableData[pos]
                if (cellData != null) {
                    // Mark as transitioning first (for animation)
                    transitioningCells[pos] = cellData
                    // Remove from current table data since it's now correctly placed
                    currentTableData.remove(pos)
                }
            }
        }
        // Increment correct move count
        correctMoveCountState.value++
    }
}