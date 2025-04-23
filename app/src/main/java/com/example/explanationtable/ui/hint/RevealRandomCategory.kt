package com.example.explanationtable.ui.hint

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

// Add a callback parameter to report correctly placed cells
fun revealRandomCategory(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable,
    // New parameter: callback to notify about correctly placed cells
    onCellsCorrectlyPlaced: (List<CellPosition>) -> Unit = {}
) {
    // 1) Define the four fixed categories.
    val firstCategory = setOf(
        CellPosition(1, 0),
        CellPosition(2, 0),
        CellPosition(3, 0),
        CellPosition(4, 0)
    )
    val secondCategory = setOf(
        CellPosition(0, 1),
        CellPosition(1, 1),
        CellPosition(2, 1),
        CellPosition(3, 1),
        CellPosition(4, 1)
    )
    val thirdCategory = setOf(
        CellPosition(1, 2),
        CellPosition(2, 2),
        CellPosition(3, 2)
    )
    val fourthCategory = setOf(
        CellPosition(3, 0),
        CellPosition(3, 1),
        CellPosition(3, 2)
    )
    val categories = listOf(firstCategory, secondCategory, thirdCategory, fourthCategory)

    // 2) Helpers
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // 3) Early exit if everything is already solved
    val unsolvedCounts = categories.map { category ->
        category.count { !isCellCorrectlyPlaced(it) }
    }
    if (unsolvedCounts.all { it == 0 }) return

    // 4) Pick a random category that still has unsolved cells
    val unresolvedCategories = categories.zip(unsolvedCounts)
        .filter { it.second > 0 }
        .map { it.first }
    if (unresolvedCategories.isEmpty()) return
    val selectedCategory = unresolvedCategories.random()

    // 5) Prepare for iterative swapping
    val positions = selectedCategory
        .toList()
        .sortedWith(compareBy({ it.row }, { it.col }))

    // Remember which were already correct
    val initiallyCorrect = positions.filter { isCellCorrectlyPlaced(it) }.toSet()

    // We'll collect every position that ever becomes correct,
    // then filter out those that didn't stay correct by the end.
    val newlyCorrect = mutableListOf<CellPosition>()

    // 6) Repeat passes until this category is fully solved or no swaps possible
    var madeProgress: Boolean
    do {
        madeProgress = false

        for (pos in positions) {
            if (isCellCorrectlyPlaced(pos)) continue

            val target = getTargetData(pos) ?: continue

            // Find any source cell (even outside this category) that currently holds the right data
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

        // Stop if category is now fully solved
    } while (madeProgress && positions.any { !isCellCorrectlyPlaced(it) })

    // 7) Filter to only those that ended up correct and weren’t originally correct
    val trulyNew = newlyCorrect
        .distinct()
        .filter { it !in initiallyCorrect }
        .filter { isCellCorrectlyPlaced(it) }

    // 8) Notify callback
    if (trulyNew.isNotEmpty()) {
        onCellsCorrectlyPlaced(trulyNew)
    }
}
