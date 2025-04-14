package com.example.explanationtable.ui.hint

import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition

fun revealRandomCategoryHelp(
    currentTableData: MutableMap<CellPosition, List<String>>,
    originalTableData: EasyLevelTable
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

    // Function to check if a category is completely sorted.
    fun isCategorySorted(category: Set<CellPosition>): Boolean {
        // Sort the positions for a predictable order.
        val positions = category.toList().sortedWith(compareBy({ it.row }, { it.col }))
        return positions.all { pos ->
            val targetData = originalTableData.rows[pos.row]?.get(pos.col)
            currentTableData[pos] == targetData
        }
    }

    // Randomly pick one category that is not already completely sorted.
    var selectedCategory = categories.random()
    var attempts = 0
    // If the selected category is sorted, try to find another.
    while (isCategorySorted(selectedCategory) && attempts < categories.size) {
        println("Category already sorted. Choosing another category...")
        selectedCategory = categories.random()
        attempts++
    }

    // If all categories are sorted, no need to swap.
    if (isCategorySorted(selectedCategory)) {
        println("All categories are completely sorted. No help needed!")
        println("=== revealRandomCategoryHelp: FINISHED ===")
        return
    }

    println("Selected Category:")
    selectedCategory.forEach { pos ->
        println("Cell at [row: ${pos.row}, col: ${pos.col}]")
    }

    // For a predictable order, sort the positions.
    val positions = selectedCategory.toList().sortedWith(compareBy({ it.row }, { it.col }))

    // Debug-print the target order for the selected category.
    println("Target Order for Selected Category:")
    for (pos in positions) {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col)
        println("Cell at [row: ${pos.row}, col: ${pos.col}]: ${targetData?.joinToString(",") ?: "null"}")
    }

    // For each cell in the selected category, if its current value does not match the target,
    // search the entire table for the cell holding the correct data and swap.
    for (pos in positions) {
        val targetData = originalTableData.rows[pos.row]?.get(pos.col) ?: continue
        if (currentTableData[pos] == targetData) continue  // Already correct; nothing to do.

        // Search through the entire currentTableData to find the target data.
        var candidatePos: CellPosition? = null
        for ((otherPos, data) in currentTableData) {
            if (otherPos == pos) continue
            if (data == targetData) {
                candidatePos = otherPos
                break
            }
        }
        if (candidatePos != null) {
            // Perform the swap in currentTableData.
            val temp = currentTableData[pos]!!
            currentTableData[pos] = currentTableData[candidatePos]!!
            currentTableData[candidatePos] = temp
            println("Swapped cell [row: ${pos.row}, col: ${pos.col}] with cell [row: ${candidatePos.row}, col: ${candidatePos.col}]")
        } else {
            println("No candidate found for cell [row: ${pos.row}, col: ${pos.col}] with target ${targetData.joinToString(",")}")
        }
    }

    // Debug-print the current table data after swaps in the selected category.
    println("Current Table Data AFTER Swapping:")
    currentTableData.forEach { (pos, data) ->
        println("Cell at [row: ${pos.row}, col: ${pos.col}]: ${data.joinToString(",")}")
    }
    println("=== revealRandomCategoryHelp: FINISHED ===")
}