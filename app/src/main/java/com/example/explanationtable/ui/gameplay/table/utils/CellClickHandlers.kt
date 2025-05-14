package com.example.explanationtable.ui.gameplay.table.utils

import androidx.compose.runtime.MutableState
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    isProcessingSwap: MutableState<Boolean> // New parameter
) {
    // hint-driven reset (keep this logic)
    if (firstSelectedCellState.value != null && !currentTableData.containsKey(firstSelectedCellState.value)) {
        onResetSelection()
        firstSelectedCellState.value = null
        secondSelectedCellState.value = null
        isSelectionCompleteState.value = false
    }
    if (secondSelectedCellState.value != null && !currentTableData.containsKey(secondSelectedCellState.value)) {
        if (firstSelectedCellState.value != null && currentTableData.containsKey(firstSelectedCellState.value)) {
            secondSelectedCellState.value = null
            isSelectionCompleteState.value = false
        } else {
            onResetSelection()
            firstSelectedCellState.value = null
            secondSelectedCellState.value = null
            isSelectionCompleteState.value = false
        }
    }

    // first click
    if (firstSelectedCellState.value == null) {
        if (currentTableData.containsKey(position)) {
            firstSelectedCellState.value = position
        }
    }
    // second click
    else if (secondSelectedCellState.value == null && position != firstSelectedCellState.value) {
        if (currentTableData.containsKey(position)) {
            isProcessingSwap.value = true

            secondSelectedCellState.value = position

            val dataA = currentTableData[firstSelectedCellState.value]
            val dataB = currentTableData[secondSelectedCellState.value]

            if (dataA != null && dataB != null && firstSelectedCellState.value != null && secondSelectedCellState.value != null) {
                currentTableData[firstSelectedCellState.value!!] = dataB
                currentTableData[secondSelectedCellState.value!!] = dataA
            } else {
                onResetSelection()
                return
            }

            playerMovesState.value++

            var newlyCorrect = 0
            movablePositions.forEach { pos ->
                val original = originalTableData.rows[pos.row]?.get(pos.col)
                if (pos == firstSelectedCellState.value || pos == secondSelectedCellState.value) {
                    val cellDataToTransition = currentTableData[pos]
                    if (cellDataToTransition != null && cellDataToTransition == original) {
                        transitioningCells[pos] = cellDataToTransition
                        currentTableData.remove(pos)
                        newlyCorrect++
                    }
                }
            }

            if (newlyCorrect > 0) {
                correctMoveCountState.value++
            } else {
                incorrectMoveCountState.value++
            }

            // --- TRIGGER RESET AFTER SWAP ---
            isSelectionCompleteState.value = true
            coroutineScope.launch {
                delay(200)
                onResetSelection()
            }
            // --- END TRIGGER RESET ---

        }
    } else if (position == firstSelectedCellState.value) {
        // Optional: deselect logic if clicking the same cell again
    }

    // End of handleCellClick
}
