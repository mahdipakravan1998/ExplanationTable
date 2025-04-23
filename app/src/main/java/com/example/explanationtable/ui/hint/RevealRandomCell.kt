package com.example.explanationtable.ui.hint

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

// Add a callback parameter to report correctly placed cell
fun revealRandomCell(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable,
    // New parameter: callback to notify about correctly placed cells
    onCellCorrectlyPlaced: (List<CellPosition>) -> Unit = {}
) {
    // 1) Helper function to check if a cell is correctly placed
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }

    // 2) Helper function to get the target data for a specific cell
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // 3) Create a list of all unsolved cells
    val unsolvedCells = currentTableData.keys.filter { !isCellCorrectlyPlaced(it) }

    // 4) If there are no unsolved cells, return early
    if (unsolvedCells.isEmpty()) return

    // 5) Select a random unsolved cell
    val randomCell = unsolvedCells.random()

    // 6) Get the target data for the random cell
    val targetData = getTargetData(randomCell) ?: return

    // 7) Find any other cell with the same data that isn't correctly placed
    val sourceCell = currentTableData.entries
        .firstOrNull { (otherPos, data) ->
            otherPos != randomCell && data == targetData && !isCellCorrectlyPlaced(otherPos)
        }
        ?.key

    // 8) If a source cell is found, swap the data
    if (sourceCell != null) {
        // Swap the cells
        val tmp = currentTableData[randomCell]!!
        currentTableData[randomCell] = currentTableData[sourceCell]!!
        currentTableData[sourceCell] = tmp

        // 9) Check if the random cell is now correctly placed
        val newlyCorrect = mutableListOf<CellPosition>()
        if (isCellCorrectlyPlaced(randomCell)) {
            newlyCorrect += randomCell
        }

        // 10) Check if the source cell (the one that was swapped with the random cell) is correctly placed
        if (isCellCorrectlyPlaced(sourceCell)) {
            newlyCorrect += sourceCell
        }

        // 11) Notify callback
        if (newlyCorrect.isNotEmpty()) {
            onCellCorrectlyPlaced(newlyCorrect)
        }
    }
}
