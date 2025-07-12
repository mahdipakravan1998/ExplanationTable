package com.example.explanationtable.ui.hint.logic

import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty

/**
 * Reveals a random category of cells by swapping items until the category is solved.
 *
 * @param currentTableData Mutable map of current positions to data lists.
 * @param originalTableData Original table with correct data.
 * @return List of positions that were newly correctly placed.
 */
fun revealRandomCategory(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: LevelTable,
    difficulty: Difficulty
): List<CellPosition> {

    // Categories for EASY
    val easyCategories = listOf(
        setOf(CellPosition(1, 0), CellPosition(2, 0), CellPosition(3, 0), CellPosition(4, 0)),
        setOf(CellPosition(0, 1), CellPosition(1, 1), CellPosition(2, 1), CellPosition(3, 1), CellPosition(4, 1)),
        setOf(CellPosition(1, 2), CellPosition(2, 2), CellPosition(3, 2)),
        setOf(CellPosition(3, 0), CellPosition(3, 1), CellPosition(3, 2))
    )

    // Categories for MEDIUM
    val mediumCategories = listOf(
        setOf(CellPosition(1, 3), CellPosition(2, 3)),
        setOf(CellPosition(0, 2), CellPosition(1, 2), CellPosition(2, 2), CellPosition(3, 2)),
        setOf(CellPosition(1, 1), CellPosition(2, 1), CellPosition(3, 1)),
        setOf(CellPosition(0, 0), CellPosition(1, 0), CellPosition(2, 0), CellPosition(3, 0)),
        setOf(CellPosition(2, 3), CellPosition(2, 2), CellPosition(2, 1), CellPosition(2, 0))
    )

    // Pick the right set based on difficulty
    val categories = when (difficulty) {
        Difficulty.EASY   -> easyCategories
        Difficulty.MEDIUM -> mediumCategories
        else              -> return emptyList()
    }

    /**
     * Checks if a cell is correctly placed by comparing it with the original table data.
     *
     * @param pos Position of the cell.
     * @return true if the cell is correctly placed, false otherwise.
     */
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        return (targetData != null && currentData == targetData) || currentData == null
    }

    /**
     * Retrieves the target data for a given cell position from the original table data.
     *
     * @param pos Position of the cell.
     * @return The target data for the cell or null if not found.
     */
    fun getTargetData(pos: CellPosition): List<String>? =
        originalTableData.rows[pos.row]?.get(pos.col)

    // Step 1: Early exit if all cells are already correctly placed
    val unsolvedCounts = categories.map { category ->
        category.count { !isCellCorrectlyPlaced(it) }
    }
    if (unsolvedCounts.all { it == 0 }) return emptyList()

    // Step 2: Identify categories with unsolved cells and select one randomly
    val unresolvedCategories = categories.zip(unsolvedCounts)
        .filter { it.second > 0 }
        .map { it.first }
    if (unresolvedCategories.isEmpty()) return emptyList()

    val selectedCategory = unresolvedCategories.random()
    val positions = selectedCategory
        .toList()
        .sortedWith(compareBy({ it.row }, { it.col }))

    // Track initially correct positions
    val initiallyCorrect = positions.filter { isCellCorrectlyPlaced(it) }.toSet()
    val newlyCorrect = mutableListOf<CellPosition>()

    // Step 3: Swap cells to solve the category
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
                // Swap the data between the source and the current position
                val tmp = currentTableData[pos]!!
                currentTableData[pos] = currentTableData[source]!!
                currentTableData[source] = tmp

                // Check if swapping made progress
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

    // Step 4: Return only the newly correct positions that weren't originally correct
    return newlyCorrect
        .distinct()
        .filter { it !in initiallyCorrect && isCellCorrectlyPlaced(it) }
}
