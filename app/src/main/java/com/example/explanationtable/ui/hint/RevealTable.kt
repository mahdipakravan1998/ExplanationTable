package com.example.explanationtable.ui.hint

import kotlinx.coroutines.* // Import Kotlin coroutines package
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

// Define a suspend function for batch processing
suspend fun revealAllCellsHelp(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable,
    // Callback to notify about correctly placed cells
    onCellsCorrectlyPlaced: (List<CellPosition>) -> Unit = {}
) {
    // Helper function to check if a cell is correctly placed
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }

    // Helper function to get the target data for a given position
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // Get all positions in the table that need to be solved
    val allPositions = currentTableData.keys.toList()

    // Early exit if everything is already solved
    val unsolvedCounts = allPositions.count { !isCellCorrectlyPlaced(it) }
    if (unsolvedCounts == 0) return

    // Collect positions that are not correctly placed yet
    val positions = allPositions.sortedWith(compareBy({ it.row }, { it.col }))

    // Remember which were already correct
    val initiallyCorrect = positions.filter { isCellCorrectlyPlaced(it) }.toSet()

    // We'll collect every position that ever becomes correct,
    // then filter out those that didn't stay correct by the end.
    val newlyCorrect = mutableListOf<CellPosition>()

    // Define chunk size to process cells in batches (e.g., 10 cells at a time)
    val chunkSize = 1
    var startIndex = 0

    // Repeat passes until the table is fully solved or no swaps possible
    var madeProgress: Boolean
    do {
        madeProgress = false

        // Process cells in chunks
        val chunk = positions.subList(startIndex, minOf(startIndex + chunkSize, positions.size))

        for (pos in chunk) {
            if (isCellCorrectlyPlaced(pos)) continue

            val target = getTargetData(pos) ?: continue

            // Find any source cell that holds the right data
            // and isn't already correctly placed itself.
            val source = currentTableData.entries
                .firstOrNull { (otherPos, data) ->
                    otherPos != pos &&
                            data == target &&
                            !isCellCorrectlyPlaced(otherPos)
                }
                ?.key

            if (source != null) {
                // Swap them
                val tmp = currentTableData[pos]!!
                currentTableData[pos] = currentTableData[source]!!
                currentTableData[source] = tmp

                // Record newly correct placements
                if (isCellCorrectlyPlaced(pos)) {
                    newlyCorrect += pos
                    madeProgress = true
                }
                if (isCellCorrectlyPlaced(source)) {
                    newlyCorrect += source
                    madeProgress = true
                }
            }
        }

        // Notify callback for newly correct cells
        val trulyNew = newlyCorrect
            .distinct()
            .filter { it !in initiallyCorrect }
            .filter { isCellCorrectlyPlaced(it) }

        if (trulyNew.isNotEmpty()) {
            onCellsCorrectlyPlaced(trulyNew)
        }

        // Move to the next chunk
        startIndex += chunkSize

        // Add delay to avoid blocking the UI thread (adjust delay time as needed)
        delay(500) // Delay for 500 milliseconds between processing each chunk

    } while (madeProgress && startIndex < positions.size)

    // If the table is now fully solved, notify with the final positions
    if (startIndex >= positions.size && newlyCorrect.isNotEmpty()) {
        onCellsCorrectlyPlaced(newlyCorrect.distinct())
    }
}
