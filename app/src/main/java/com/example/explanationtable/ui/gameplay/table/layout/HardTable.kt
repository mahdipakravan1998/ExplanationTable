package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty

/**
 * Thin wrapper around TableLayout configured for the 5×4 "hard" puzzle.
 *
 * Fixed cells:
 *  • (0, 0) → TextSeparatedSquare
 *  • (0, 3) → TextSeparatedSquare
 *  • (4, 3) → TextSeparatedSquare
 */
@Composable
fun HardTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: com.example.explanationtable.model.LevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    // Dimensions for hard:
    val rowsCount = 5
    val colsCount = 4

    // Fixed positions for hard:
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 3),
        CellPosition(4, 3)
    )

    // Tell TableLayout which fixed position uses which cell type
    val fixedCellTypes = mapOf(
        CellPosition(0, 0) to FixedCellType.TEXT_SEPARATED,
        CellPosition(0, 3) to FixedCellType.TEXT_SEPARATED,
        CellPosition(4, 3) to FixedCellType.TEXT_SEPARATED
    )

    TableLayout(
        isDarkTheme = isDarkTheme,
        difficulty = Difficulty.HARD,
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
