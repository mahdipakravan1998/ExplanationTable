package com.example.explanationtable.ui.hint.logic

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

/**
 * Reveals a random category of cells by swapping items until the category is solved.
 *
 * @param currentTableData Mutable map of current positions to data lists.
 * @param originalTableData Original table with correct data.
 * @return List of positions that were newly correctly placed.
 */
fun revealRandomCategory(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable
): List<CellPosition> {
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

    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }

    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // 1) Early exit if everything is already solved
    val unsolvedCounts = categories.map { category ->
        category.count { !isCellCorrectlyPlaced(it) }
    }
    if (unsolvedCounts.all { it == 0 }) return emptyList()

    // 2) Pick a random category with unsolved cells
    val unresolvedCategories = categories.zip(unsolvedCounts)
        .filter { it.second > 0 }
        .map { it.first }
    if (unresolvedCategories.isEmpty()) return emptyList()

    val selectedCategory = unresolvedCategories.random()
    val positions = selectedCategory
        .toList()
        .sortedWith(compareBy({ it.row }, { it.col }))

    // Track which were already correct
    val initiallyCorrect = positions.filter { isCellCorrectlyPlaced(it) }.toSet()
    val newlyCorrect = mutableListOf<CellPosition>()

    // 3) Swap until category is solved or no progress
    var madeProgress: Boolean
    do {
        madeProgress = false
        for (pos in positions) {
            if (isCellCorrectlyPlaced(pos)) continue
            val target = getTargetData(pos) ?: continue
            val source = currentTableData.entries
                .firstOrNull { (otherPos, data) ->
                    otherPos != pos &&
                            data == target &&
                            !isCellCorrectlyPlaced(otherPos)
                }?.key

            if (source != null) {
                // Swap
                val tmp = currentTableData[pos]!!
                currentTableData[pos] = currentTableData[source]!!
                currentTableData[source] = tmp

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
    } while (madeProgress && positions.any { !isCellCorrectlyPlaced(it) })

    // 4) Return only the positions that are newly correct and weren’t originally correct
    return newlyCorrect
        .distinct()
        .filter { it !in initiallyCorrect && isCellCorrectlyPlaced(it) }
}
