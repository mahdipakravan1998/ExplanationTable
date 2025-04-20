package com.example.explanationtable.ui.hint

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

// Add a callback parameter to report correctly placed cells
fun revealRandomCategoryHelp(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable,
    // New parameter: callback to notify about correctly placed cells
    onCellsCorrectlyPlaced: (List<CellPosition>) -> Unit = {}
) {
    println("=== revealRandomCategoryHelp: START ===")

    // Debug-print original table data.
    println("Original Table Data:")
    originalTableData.rows.forEach { (row, columns) ->
        val builder = StringBuilder()
        for (col in 0 until columns.size) {
            val cellData = columns[col]?.joinToString(",") ?: "null"
            builder.append("col $col: [$cellData]")
            if (col != columns.size - 1) builder.append(" | ")
        }
        println("Row $row: ${builder.toString()}")
    }

    // Debug-print current table data.
    println("Current Table Data (Filtered for Non-null cells):")
    val maxRow = currentTableData.keys.maxByOrNull { it.row }?.row ?: -1
    val maxCol = currentTableData.keys.maxByOrNull { it.col }?.col ?: -1

    // Iterate over rows and columns to ensure it prints only non-null cells
    for (row in 0..maxRow) {
        val builder = StringBuilder()
        for (col in 0..maxCol) {
            val cellData = currentTableData[CellPosition(row, col)]?.joinToString(",") ?: "null"
            if (cellData != "null") {
                builder.append("col $col: [$cellData]")
                if (col != maxCol) builder.append(" | ")
            }
        }
        if (builder.isNotEmpty()) {
            println("Row $row: ${builder.toString()}")
        }
    }

    // Define the four fixed categories.
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

    // Helper function to check if a cell is correctly placed
    fun isCellCorrectlyPlaced(pos: CellPosition): Boolean {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        val currentData = currentTableData[pos]
        val result = targetData != null && currentData == targetData || currentData == null
        return result
    }

    // Helper function to get the target data for a position
    fun getTargetData(pos: CellPosition): List<String>? {
        return originalTableData.rows[pos.row]?.get(pos.col)
    }

    // Create Set A - All incorrectly placed cells in the entire table
    val setA = currentTableData.keys.filter { !isCellCorrectlyPlaced(it) }.toSet()

    // Count how many cells from each category are already correctly placed
    val categorySolvedCounts = categories.map { category ->
        // Count the correctly placed cells and print the result for each position
        val count = category.count { pos ->
            val isCorrect = isCellCorrectlyPlaced(pos)
            println("  Cell at [row: ${pos.row}, col: ${pos.col}] correctly placed: $isCorrect")
            isCorrect
        }

        println("  Number of correctly placed cells in this category: $count")
        count
    }

    // Calculate how many cells in each category still need to be placed
    val categoryUnsolvedCounts = categories.mapIndexed { index, category ->
        category.size - categorySolvedCounts[index]
    }

    // If all categories are fully solved, exit
    if (categoryUnsolvedCounts.all { it == 0 }) {
        println("All categories are completely solved. No help needed!")
        println("=== revealRandomCategoryHelp: FINISHED ===")
        return
    }

    // Filter out categories that are already fully solved (0 unresolved cells)
    val unresolvedCategories = categoryUnsolvedCounts
        .mapIndexed { index, count -> categories[index] to count }
        .filter { it.second > 0 }
        .map { it.first }

    // If no unresolved categories, exit
    if (unresolvedCategories.isEmpty()) {
        println("No unresolved categories left.")
        println("=== revealRandomCategoryHelp: FINISHED ===")
        return
    }

    // Randomly select an unresolved category
    val selectedCategory = unresolvedCategories.random()

    println("Number of unresolved categories: ${unresolvedCategories.size}")
    println("Selected Category:")
    selectedCategory.forEach { pos ->
        println("Cell at [row: ${pos.row}, col: ${pos.col}]")
    }

    // For a predictable order, sort the positions.
    val positions = selectedCategory.toList().sortedWith(compareBy({ it.row }, { it.col }))

    // Debug-print the target order for the selected category.
    println("Target Order for Selected Category:")
    for (pos in positions) {
        val targetData = getTargetData(pos)
        println("Cell at [row: ${pos.row}, col: ${pos.col}]: ${targetData?.joinToString(",") ?: "null"}")
    }

    // Track positions that were correctly placed before we started
    val initiallyCorrectPositions = positions.filter { isCellCorrectlyPlaced(it) }.toSet()

    // Track correctly placed cells after all operations are done
    val newlyCorrectlyPlacedCells = mutableListOf<CellPosition>()

    // Now process each position in the selected category to place correct values
    for (pos in positions) {
        // Skip positions that are already correctly placed
        if (isCellCorrectlyPlaced(pos)) {
            println("Cell at [row: ${pos.row}, col: ${pos.col}] is already correct. Skipping.")
            continue
        }

        // Ensure we only work with cells from Set A
        if (pos !in setA) {
            println("Cell at [row: ${pos.row}, col: ${pos.col}] is not part of Set A. Skipping.")
            continue
        }

        val targetData = getTargetData(pos) ?: continue

        // Find another position that has the target data in Set A
        var sourcePos: CellPosition? = null
        for ((otherPos, data) in currentTableData) {
            if (otherPos != pos && data == targetData && otherPos in setA) {
                sourcePos = otherPos
                break
            }
        }

        if (sourcePos != null) {
            // Swap the data between the two positions
            val temp = currentTableData[pos]!!
            currentTableData[pos] = currentTableData[sourcePos]!!
            currentTableData[sourcePos] = temp

            println("Swapped cell [row: ${pos.row}, col: ${pos.col}] with cell [row: ${sourcePos.row}, col: ${sourcePos.col}]")

            // Check if positions are now correctly placed
            if (isCellCorrectlyPlaced(pos)) {
                newlyCorrectlyPlacedCells.add(pos)
            }

            if (isCellCorrectlyPlaced(sourcePos)) {
                newlyCorrectlyPlacedCells.add(sourcePos)
            }
        } else {
            // Handle best-effort swaps if needed
            println("No exact candidate found for swap.")
        }
    }

    // Debug-print the current table data after swaps in the selected category.
    println("Current Table Data AFTER Swapping:")
    currentTableData.forEach { (pos, data) ->
        println("Cell at [row: ${pos.row}, col: ${pos.col}]: ${data.joinToString(",")}")
    }

    // Filter out positions that were already correct before we started
    val trulyNewCorrectPositions = newlyCorrectlyPlacedCells.filter { it !in initiallyCorrectPositions }.distinct()

    // If any cells are now correctly placed, notify via callback
    if (trulyNewCorrectPositions.isNotEmpty()) {
        println("Cells correctly placed: ${trulyNewCorrectPositions.size}")
        trulyNewCorrectPositions.forEach { pos ->
            println("  - Cell at [row: ${pos.row}, col: ${pos.col}]")
        }

        onCellsCorrectlyPlaced(trulyNewCorrectPositions)
    }

    println("=== revealRandomCategoryHelp: FINISHED ===")
}
