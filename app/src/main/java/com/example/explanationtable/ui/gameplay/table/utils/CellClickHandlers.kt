package com.example.explanationtable.ui.gameplay.table.utils

import android.util.Log
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
    // Your existing guard for when selection is already complete can be here or removed
    // if the new logic handles it well. For now, let's assume the guard from my
    // previous snippet is here if you want that extra logging.
    // if (isSelectionCompleteState.value && firstSelectedCellState.value != null && secondSelectedCellState.value != null) { ... return }


    Log.d("TableDebug", "▶ handleCellClick(position=$position)")
    val beforeFirst = firstSelectedCellState.value
    val beforeSecond = secondSelectedCellState.value
    // No need to use a local 'isSelectionComplete' variable for the core logic if we directly use isSelectionCompleteState.value

    // hint-driven reset (keep this logic)
    if (firstSelectedCellState.value != null && !currentTableData.containsKey(firstSelectedCellState.value)) {
        Log.d("TableDebug", "    hint removed firstSelectedCell=${firstSelectedCellState.value}; resetting selection now")
        onResetSelection() // Call the passed-in reset function
        // It's important to update the states immediately for the current click processing
        firstSelectedCellState.value = null
        secondSelectedCellState.value = null
        isSelectionCompleteState.value = false
    }
    if (secondSelectedCellState.value != null && !currentTableData.containsKey(secondSelectedCellState.value)) {
        Log.d("TableDebug", "    hint removed secondSelectedCell=${secondSelectedCellState.value}; specific reset")
        if (firstSelectedCellState.value != null && currentTableData.containsKey(firstSelectedCellState.value)) {
            secondSelectedCellState.value = null
            isSelectionCompleteState.value = false // Allow re-selection of the second cell
        } else {
            onResetSelection() // Full reset if first is also gone
            firstSelectedCellState.value = null
            secondSelectedCellState.value = null
            isSelectionCompleteState.value = false
        }
    }


    // first click
    if (firstSelectedCellState.value == null) {
        if (currentTableData.containsKey(position)) {
            firstSelectedCellState.value = position
            Log.d("TableDebug", "    picked firstSelectedCell=${firstSelectedCellState.value}")
        } else {
            Log.d("TableDebug", "    clicked on a non-movable or already correct cell ($position) for first click, ignoring.")
        }
    }
    // second click
    else if (secondSelectedCellState.value == null && position != firstSelectedCellState.value) {
        if (currentTableData.containsKey(position)) {
            isProcessingSwap.value = true // Lock before processing swap and scheduling reset
            Log.d("TableDebug", "isProcessingSwap set to true.")

            secondSelectedCellState.value = position
            // isSelectionCompleteState.value = true; // This will now be set by the reset mechanism implicitly

            Log.d("TableDebug", "    picked secondSelectedCell=${secondSelectedCellState.value} → swapping")

            val dataA = currentTableData[firstSelectedCellState.value]
            val dataB = currentTableData[secondSelectedCellState.value]
            Log.d("TableDebug", "    swap dataA=$dataA, dataB=$dataB")

            if (dataA != null && dataB != null && firstSelectedCellState.value != null && secondSelectedCellState.value != null) {
                currentTableData[firstSelectedCellState.value!!] = dataB
                currentTableData[secondSelectedCellState.value!!] = dataA
            } else {
                Log.e("TableDebug", "Error: Data for one or both selected cells is null during swap.")
                onResetSelection() // Reset on error
                return
            }

            playerMovesState.value++
            Log.d("TableDebug", "    playerMoves=${playerMovesState.value}")

            var newlyCorrect = 0
            // Check newly correct logic from previous snippet
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
            Log.d("TableDebug", "    newlyCorrect=$newlyCorrect")

            if (newlyCorrect > 0) {
                correctMoveCountState.value++
            } else {
                incorrectMoveCountState.value++
            }
            Log.d("TableDebug", "    correctMoves=${correctMoveCountState.value}, incorrectMoves=${incorrectMoveCountState.value}")

            // --- TRIGGER RESET AFTER SWAP ---
            isSelectionCompleteState.value = true // Mark as complete so UI shows selection briefly
            coroutineScope.launch {
                Log.d("TableDebug", "Scheduling reset from handleCellClick")
                delay(200) // Or your desired delay
                onResetSelection()
                Log.d("TableDebug", "Reset performed from handleCellClick")
            }
            // --- END TRIGGER RESET ---

        } else {
            Log.d("TableDebug", "    clicked on a non-movable or already correct cell ($position) for second click, ignoring.")
        }
    } else if (position == firstSelectedCellState.value) {
        // Optional: deselect logic if clicking the same cell again
        // If implementing deselection here, ensure isProcessingSwap is handled appropriately
        // if this action should also be atomic.
    }

    // No need to write back to states here as we are modifying them directly (e.g., firstSelectedCellState.value = ...)
    // However, ensure 'isSelectionCompleteState' is correctly managed, especially if not doing the immediate reset above.
    // The local variables 'firstSelectedCell', 'secondSelectedCell', 'isSelectionComplete' are less needed now.
    Log.d("TableDebug", "    after handleCellClick execution → first=${firstSelectedCellState.value}, second=${secondSelectedCellState.value}, complete=${isSelectionCompleteState.value}")
    Log.d("TableDebug", "    currentTableData=$currentTableData")
}