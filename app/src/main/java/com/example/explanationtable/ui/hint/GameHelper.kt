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
        return targetData != null && currentData == targetData || currentData == null
    }

    // Helper function to get the target data for a position
    fun getTargetData(pos: CellPosition): List<String>? {
        return originalTableData.rows[pos.row]?.get(pos.col)
    }

    // Create Set A - All incorrectly placed cells in the entire table
    val setA = currentTableData.keys.filter { !isCellCorrectlyPlaced(it) }.toSet()

    // Count how many cells from each category are already correctly placed
    val categorySolvedCounts = categories.map { category ->
        category.count { pos ->
            isCellCorrectlyPlaced(pos)
        }
    }

    // Calculate how many cells in each category still need to be placed
    val categoryUnsolvedCounts = categories.mapIndexed { index, category ->
        category.size - categorySolvedCounts[index]
    }

    // If all categories are fully solved, exit
    if (categoryUnsolvedCounts.all { it == 0 }) {
        return
    }

    // Filter out categories that are already fully solved (0 unresolved cells)
    val unresolvedCategories = categoryUnsolvedCounts
        .mapIndexed { index, count -> categories[index] to count }
        .filter { it.second > 0 }
        .map { it.first }

    // If no unresolved categories, exit
    if (unresolvedCategories.isEmpty()) {
        return
    }

    // Randomly select an unresolved category
    val selectedCategory = unresolvedCategories.random()

    // For a predictable order, sort the positions.
    val positions = selectedCategory.toList().sortedWith(compareBy({ it.row }, { it.col }))

    // ---------------------------------------------------
    // Debug helper: prints the whole table in grid form
    fun printTable(label: String) {
        println("=== $label ===")
        // Directional overrides
        val LRE = "\u202A"  // Left‑to‑Right Embedding
        val PDF = "\u202C"  // Pop Directional Formatting

        // 1) Safely get your rows
        val rowsMap = originalTableData.rows
        if (rowsMap.isEmpty()) {
            println("[table is empty]")
            return
        }

        // 2) Sort the row indices
        val rowIndices = rowsMap.keys.toList().sorted()

        // 3) Derive column count from the first row
        val firstRowList = rowsMap[rowIndices[0]]
            ?: run { println("[first row is null]"); return }
        val colCount = firstRowList.size

        // 4) Print header
        print("     ")
        for (c in 0 until colCount) {
            // wrap “C0”, “C1”, etc. in LRE/PDF too (just in case)
            val header = LRE + "C$c" + PDF
            print("|   $header    ")
        }
        println("|")

        // 5) Print each row, forcing each cell LTR
        for (r in rowIndices) {
            print("R$r  ")
            for (c in 0 until colCount) {
                val data = currentTableData[CellPosition(r, c)]
                val raw = data?.joinToString(",") ?: "∅"
                // wrap the cell text
                val displayed = LRE + raw.padEnd(6) + PDF
                print("| $displayed ")
            }
            println("|")
        }
        println()
    }

    // ---------------------------------------------------


    // Track correctly placed cells before we start
    val initiallyCorrectPositions = positions.filter { isCellCorrectlyPlaced(it) }.toSet()
    println("Initially correctly placed: $initiallyCorrectPositions")
    printTable("Initial table")

    val newlyCorrectlyPlacedCells = mutableListOf<CellPosition>()

    for (pos in positions) {
        println(">> Processing pos=$pos")
        println("   isCellCorrectlyPlaced($pos) = ${isCellCorrectlyPlaced(pos)}")
        if (isCellCorrectlyPlaced(pos)) {
            println("   → already correct, skipping\n")
            continue
        }
        if (pos !in setA) {
            println("   → not in setA (incorrect cells), skipping\n")
            continue
        }

        val targetData = getTargetData(pos)
        val currentData = currentTableData[pos]
        println("   targetData at $pos = $targetData")
        println("   currentData at $pos = $currentData")
        if (targetData == null) {
            println("   → no target data, skipping\n")
            continue
        }

        // find a source
        var sourcePos: CellPosition? = null
        for ((otherPos, data) in currentTableData) {
            if (otherPos != pos && data == targetData && otherPos in setA) {
                sourcePos = otherPos
                break
            }
        }
        println("   sourcePos found = $sourcePos")

        if (sourcePos != null) {
            printTable("Before swap (pos=$pos, source=$sourcePos)")
            // perform swap
            val tmp = currentTableData[pos]!!
            currentTableData[pos] = currentTableData[sourcePos]!!
            currentTableData[sourcePos] = tmp
            println("   → swapped contents of $pos and $sourcePos")
            printTable("After swap  (pos=$pos, source=$sourcePos)")

            // check placements
            val nowCorrectPos = isCellCorrectlyPlaced(pos)
            val nowCorrectSrc = isCellCorrectlyPlaced(sourcePos)
            println("   isCellCorrectlyPlaced($pos) = $nowCorrectPos")
            println("   isCellCorrectlyPlaced($sourcePos) = $nowCorrectSrc")
            if (nowCorrectPos) newlyCorrectlyPlacedCells.add(pos)
            if (nowCorrectSrc) newlyCorrectlyPlacedCells.add(sourcePos)
        } else {
            println("   → no matching source found, skipping swap\n")
        }

        println()
    }

    val trulyNewCorrectPositions = newlyCorrectlyPlacedCells
        .filter { it !in initiallyCorrectPositions }
        .filter { pos -> isCellCorrectlyPlaced(pos) }    // ← only keep those still correct
        .distinct()
    println("newlyCorrectlyPlacedCells      = $newlyCorrectlyPlacedCells")
    println("initiallyCorrectPositions      = $initiallyCorrectPositions")
    println("trulyNewCorrectPositions       = $trulyNewCorrectPositions")

    if (trulyNewCorrectPositions.isNotEmpty()) {
        println("Notifying callback with $trulyNewCorrectPositions")
        onCellsCorrectlyPlaced(trulyNewCorrectPositions)
    } else {
        println("No new correct positions to report")
    }
    printTable("")
}
