package com.example.explanationtable.ui.hint.logic

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.model.CellPosition

/**
 * Reveals a random cell by swapping it with a matching cell from the unsolved cells.
 *
 * @param currentTableData A mutable map that stores the current state of the table.
 * @param originalTableData The original table containing the correct data.
 * @return A list of cell positions that are now correctly placed.
 */
fun revealRandomCell(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable
): List<CellPosition> {

    /**
     * Checks if the given cell is correctly placed based on the original table data.
     *
     * @param pos The position of the cell to check.
     * @return True if the cell is correctly placed, false otherwise.
     */
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        // A cell is correctly placed if its data matches the original table or if it has no data yet.
        return targetData != null && currentData == targetData || currentData == null
    }

    /**
     * Retrieves the correct data for a given cell position.
     *
     * @param pos The position of the cell.
     * @return The correct data for the cell if available, null otherwise.
     */
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // 1) Gather all unsolved cells (cells that are not correctly placed).
    val unsolvedCells = currentTableData.keys.filterNot { isCellCorrectlyPlaced(it) }
    if (unsolvedCells.isEmpty()) return emptyList() // No unsolved cells, return an empty list.

    // 2) Select a random unsolved cell.
    val randomCell = unsolvedCells.random()
    val targetData = getTargetData(randomCell) ?: return emptyList() // If no target data exists, return an empty list.

    // 3) Find a matching cell to swap with.
    val sourceCell = currentTableData.entries
        .firstOrNull { (otherPos, data) ->
            otherPos != randomCell && // Ensure we're not swapping the same cell
                    data == targetData && // Data matches the target
                    !isCellCorrectlyPlaced(otherPos) // Source cell is also unsolved
        }?.key

    // If a matching source cell is found, perform the swap.
    if (sourceCell != null) {
        val tempData = currentTableData[randomCell]!!
        // Swap data between the random cell and the source cell.
        currentTableData[randomCell] = currentTableData[sourceCell]!!
        currentTableData[sourceCell] = tempData

        // 4) Collect positions that are now correctly placed after the swap.
        val newlyCorrect = mutableListOf<CellPosition>()
        if (isCellCorrectlyPlaced(randomCell)) newlyCorrect += randomCell
        if (isCellCorrectlyPlaced(sourceCell)) newlyCorrect += sourceCell

        return newlyCorrect // Return the list of newly correctly placed cells.
    }

    // If no matching source cell was found, return an empty list.
    return emptyList()
}
