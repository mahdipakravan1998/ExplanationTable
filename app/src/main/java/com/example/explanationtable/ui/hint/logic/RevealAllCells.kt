package com.example.explanationtable.ui.hint.logic

import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.model.CellPosition

/**
 * Reveals (solves) all remaining unsolved cells by swapping each cell with the cell
 * that contains its correct data until the entire table is solved.
 *
 * @param currentTableData A mutable map storing the current state of the table.
 * @param originalTableData The original table containing the correct data.
 * @return A list of cell positions that were newly correctly placed.
 */
fun revealAllCells(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: LevelTable
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
        // A cell is correctly placed if its data matches the original or if it's empty (null)
        return (targetData != null && currentData == targetData) || currentData == null
    }

    /**
     * Retrieves the correct data for a given cell position.
     *
     * @param pos The position of the cell.
     * @return The correct data for the cell if available, null otherwise.
     */
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    val newlyCorrect = mutableListOf<CellPosition>()
    var madeProgress: Boolean

    // Continue swapping until no more progress can be made (i.e., all cells are correct or stuck)
    do {
        madeProgress = false

        // 1) Gather all unsolved cells
        val unsolvedCells = currentTableData.keys.filterNot { isCellCorrectlyPlaced(it) }
        if (unsolvedCells.isEmpty()) break

        // 2) For each unsolved cell, find where its correct data currently resides and swap
        for (pos in unsolvedCells) {
            val targetData = getTargetData(pos) ?: continue

            // Find another unsolved cell that currently holds the data meant for `pos`
            val sourceCell = currentTableData.entries
                .firstOrNull { (otherPos, data) ->
                    otherPos != pos &&
                            data == targetData &&
                            !isCellCorrectlyPlaced(otherPos)
                }?.key

            if (sourceCell != null) {
                // Perform the swap
                val temp = currentTableData[pos]!!
                currentTableData[pos] = currentTableData[sourceCell]!!
                currentTableData[sourceCell] = temp

                // Record any cells that became correct due to the swap
                if (isCellCorrectlyPlaced(pos)) {
                    newlyCorrect += pos
                    madeProgress = true
                }
                if (isCellCorrectlyPlaced(sourceCell)) {
                    newlyCorrect += sourceCell
                    madeProgress = true
                }
            }
        }
    } while (madeProgress)

    // Return only distinct positions that were fixed
    return newlyCorrect.distinct()
}
