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
    println("Current Table Data:")
    currentTableData.forEach { (pos, data) ->
        println("Cell at [row: ${pos.row}, col: ${pos.col}]: ${data.joinToString(",")}")
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
        return targetData != null && currentData == targetData
    }

    // Helper function to get the target data for a position
    fun getTargetData(pos: CellPosition): List<String>? {
        return originalTableData.rows[pos.row]?.get(pos.col)
    }

    // Helper function to count how many positions in a category are filled (even if incorrectly)
    fun countFilledPositions(category: Set<CellPosition>): Int {
        return category.count { pos -> pos in currentTableData }
    }

    // Identify which category has the most cells already placed
    val mostFilledCategory = categories.maxByOrNull { countFilledPositions(it) }

    // Count how many cells from each category are already correctly placed
    val categorySolvedCounts = categories.map { category ->
        category.count { pos -> isCellCorrectlyPlaced(pos) }
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

    // Find categories that are partially solved (at least one cell is correct, but not all)
    val partiallySolvedCategories = categories.mapIndexedNotNull { index, category ->
        if (categorySolvedCounts[index] > 0 && categorySolvedCounts[index] < category.size) {
            index
        } else {
            null
        }
    }

    // Determine which category to work on
    val selectedCategoryIndex = if (partiallySolvedCategories.isNotEmpty()) {
        // Prioritize partially solved categories
        partiallySolvedCategories.random()
    } else if (mostFilledCategory != null) {
        // If no partially solved categories, pick the one with most cells filled
        categories.indexOf(mostFilledCategory)
    } else {
        // Random fallback
        categoryUnsolvedCounts.indices.filter { categoryUnsolvedCounts[it] > 0 }.random()
    }

    val selectedCategory = categories[selectedCategoryIndex]

    println("Number of categories with unsolved cells: ${categoryUnsolvedCounts.count { it > 0 }}")

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

    // Track all available data values to choose from
    val availableData = currentTableData.values.toMutableSet()

    // If there are missing positions in the current table data,
    // we first need to ensure all positions in the selected category exist
    val positionsToCreate = positions.filter { it !in currentTableData }

    // Create positions that don't exist yet by using values from somewhere else in the table
    if (positionsToCreate.isNotEmpty()) {
        for (pos in positionsToCreate) {
            val targetData = getTargetData(pos) ?: continue

            // Find an existing cell with the correct data
            var sourceData: List<String>? = null
            var sourcePos: CellPosition? = null

            // First, try to find the exact target data
            for ((otherPos, data) in currentTableData) {
                if (data == targetData) {
                    sourcePos = otherPos
                    sourceData = data
                    break
                }
            }

            // If no exact match, use any available data
            if (sourcePos == null && currentTableData.isNotEmpty()) {
                val randomEntry = currentTableData.entries.random()
                sourcePos = randomEntry.key
                sourceData = randomEntry.value
            }

            if (sourcePos != null && sourceData != null) {
                // Create the new position with the data
                currentTableData[pos] = sourceData
                println("Created position [row: ${pos.row}, col: ${pos.col}] with data from [row: ${sourcePos.row}, col: ${sourcePos.col}]")

                // If we happened to place the correct data, mark it
                if (sourceData == targetData) {
                    newlyCorrectlyPlacedCells.add(pos)
                }
            }
        }
    }

    // Now process each position in the selected category to place correct values
    for (pos in positions) {
        // Skip positions that are already correctly placed
        if (isCellCorrectlyPlaced(pos)) {
            println("Cell at [row: ${pos.row}, col: ${pos.col}] is already correct. Skipping.")
            continue
        }

        val targetData = getTargetData(pos) ?: continue

        // Find another position that has the target data
        var sourcePos: CellPosition? = null
        for ((otherPos, data) in currentTableData) {
            if (otherPos != pos && data == targetData) {
                // Don't disturb positions that are already correctly placed
                val otherPosTarget = getTargetData(otherPos)
                if (otherPosTarget != null && data == otherPosTarget) {
                    // This position has the right data and it's in the right place
                    // Let's try to find another source
                    continue
                }

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
            // If we couldn't find the exact value, try to make a best-effort swap
            // that at least gets us closer to the solution by placing a value
            // that doesn't exist elsewhere in the category

            // Get all other positions in this category
            val otherPositionsInCategory = positions.filter { it != pos }

            // Find all values already in this category
            val valuesInCategory = otherPositionsInCategory
                .filter { it in currentTableData }
                .mapNotNull { currentTableData[it] }
                .toSet()

            // Find positions with values not in this category
            val positionsWithUniqueValues = currentTableData.entries
                .filter { (checkPos, value) ->
                    checkPos != pos &&
                            checkPos !in selectedCategory &&
                            value !in valuesInCategory
                }
                .map { it.key }

            if (positionsWithUniqueValues.isNotEmpty()) {
                val swapPos = positionsWithUniqueValues.random()
                // Swap with a position that has a value not in this category
                val temp = currentTableData[pos]!!
                currentTableData[pos] = currentTableData[swapPos]!!
                currentTableData[swapPos] = temp

                println("Made best-effort swap between [row: ${pos.row}, col: ${pos.col}] and [row: ${swapPos.row}, col: ${swapPos.col}]")

                // Check if positions are now correctly placed
                if (isCellCorrectlyPlaced(pos)) {
                    newlyCorrectlyPlacedCells.add(pos)
                }

                if (isCellCorrectlyPlaced(swapPos)) {
                    newlyCorrectlyPlacedCells.add(swapPos)
                }
            } else {
                println("No candidate found for cell [row: ${pos.row}, col: ${pos.col}] with target ${targetData.joinToString(",")}")
            }
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