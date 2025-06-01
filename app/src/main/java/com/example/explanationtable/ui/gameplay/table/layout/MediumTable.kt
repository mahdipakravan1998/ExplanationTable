package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty

/**
 * Thin wrapper around TableLayout configured for the 4×4 "medium" puzzle.
 *
 * Fixed cells:
 *  • (0, 1) → TextSeparatedSquare
 *  • (0, 3) → TextSeparatedSquare
 *  • (3, 3) → ColoredSquare
 */
@Composable
fun MediumTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: com.example.explanationtable.model.LevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    // Dimensions for medium:
    val rowsCount = 4
    val colsCount = 4

    // Fixed positions for medium:
    val fixedPositions = setOf(
        CellPosition(0, 1),
        CellPosition(0, 3),
        CellPosition(3, 3)
    )

    // Tell TableLayout which fixed position uses which cell type
    val fixedCellTypes = mapOf(
        CellPosition(0, 1) to FixedCellType.TEXT_SEPARATED,
        CellPosition(0, 3) to FixedCellType.TEXT_SEPARATED,
        CellPosition(3, 3) to FixedCellType.COLORED
    )

    TableLayout(
        isDarkTheme = isDarkTheme,
        difficulty = Difficulty.MEDIUM,
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
