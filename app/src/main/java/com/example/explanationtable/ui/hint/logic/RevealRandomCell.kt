package com.example.explanationtable.ui.hint.logic

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

/**
 * Reveals a random cell by swapping it with a matching cell.
 *
 * @param currentTableData Mutable map of current positions to data lists.
 * @param originalTableData Original table with correct data.
 * @return List of positions that were newly correctly placed.
 */
fun revealRandomCell(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable
): List<CellPosition> {
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }

    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // 1) All unsolved cells
    val unsolvedCells = currentTableData.keys.filter { !isCellCorrectlyPlaced(it) }
    if (unsolvedCells.isEmpty()) return emptyList()

    // 2) Pick one at random
    val randomCell = unsolvedCells.random()
    val targetData = getTargetData(randomCell) ?: return emptyList()

    // 3) Find a matching source to swap with
    val sourceCell = currentTableData.entries
        .firstOrNull { (otherPos, data) ->
            otherPos != randomCell &&
                    data == targetData &&
                    !isCellCorrectlyPlaced(otherPos)
        }?.key

    if (sourceCell != null) {
        // Swap
        val tmp = currentTableData[randomCell]!!
        currentTableData[randomCell] = currentTableData[sourceCell]!!
        currentTableData[sourceCell] = tmp

        // 4) Collect any newly correct
        val newlyCorrect = mutableListOf<CellPosition>()
        if (isCellCorrectlyPlaced(randomCell)) newlyCorrect += randomCell
        if (isCellCorrectlyPlaced(sourceCell)) newlyCorrect += sourceCell
        return newlyCorrect
    }
    return emptyList()
}
