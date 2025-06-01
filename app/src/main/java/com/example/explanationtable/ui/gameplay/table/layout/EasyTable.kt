package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty

/**
 * Thin wrapper around TableLayout configured for the 5×3 "easy" puzzle.
 *
 * Fixed cells (easy):
 *  • (0, 0) → ColoredSquare
 *  • (0, 2) → TextSeparatedSquare
 *  • (4, 2) → ColoredSquare
 */
@Composable
fun EasyTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: com.example.explanationtable.model.LevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    // Dimensions for easy:
    val rowsCount = 5
    val colsCount = 3

    // Fixed positions for easy:
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    // Tell TableLayout which fixed position uses which cell type
    val fixedCellTypes = mapOf(
        CellPosition(0, 0) to FixedCellType.COLORED,
        CellPosition(0, 2) to FixedCellType.TEXT_SEPARATED,
        CellPosition(4, 2) to FixedCellType.COLORED
    )

    TableLayout(
        isDarkTheme = isDarkTheme,
        difficulty = Difficulty.EASY,
        stageNumber = stageNumber,
        rowsCount = rowsCount,
        colsCount = colsCount,
        fixedPositions = fixedPositions,
        fixedCellTypes = fixedCellTypes,
        modifier = modifier,
        onGameComplete = onGameComplete,
        onTableDataInitialized = onTableDataInitialized,
        registerCellsCorrectlyPlacedCallback = registerCellsCorrectlyPlacedCallback
    )
}
