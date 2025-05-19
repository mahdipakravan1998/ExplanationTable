package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.repository.TableRepository
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.viewmodel.TableViewModel

private const val ROW_COUNT = 5      // Number of rows in the easy level
private const val COLUMN_COUNT = 3   // Number of columns in the easy level

/**
 * Renders a 3×5 "easy" gameplay table with shuffled, fixed, and transitionable cells.
 *
 * @param isDarkTheme           whether to use dark theme styling
 * @param stageNumber           selects which stage’s data to load
 * @param modifier              optional layout modifiers
 * @param onGameComplete        callback with (optimalMoves, userAccuracy, playerMoves, elapsedTime)
 * @param onTableDataInitialized callback with (originalTableData, currentTableData)
 * @param registerCellsCorrectlyPlacedCallback callback receiver for correctly-placed cell events
 */
@Composable
fun EasyThreeByFiveTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: EasyLevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    // Obtain or create the TableViewModel for this composable
    val viewModel: TableViewModel = viewModel(
        factory = TableViewModel.Factory(
            repository = TableRepository(),
            stageNumber = stageNumber,
            onGameComplete = onGameComplete,
            onTableDataInitialized = onTableDataInitialized,
            registerCallback = registerCellsCorrectlyPlacedCallback
        )
    )

    // Extract sizing/spacings from ViewModel
    val cellSize: Dp = viewModel.cellSize
    val spacing: Dp = viewModel.spacing
    val signSize: Dp = viewModel.signSize

    // Helper to render permanently fixed cells (e.g. corners, text-separated)
    @Composable
    fun FixedCell(pos: CellPosition) {
        // Lookup the original data for this position
        val data = viewModel.originalTableData.rows[pos.row]?.get(pos.col)
        when (pos) {
            // Single-text colored squares
            CellPosition(0, 0), CellPosition(4, 2) -> {
                val text = data?.joinToString(", ") ?: "?"
                ColoredSquare(text = text, modifier = Modifier.size(cellSize))
            }
            // Two-text (top/bottom) separated square
            CellPosition(0, 2) -> {
                TextSeparatedSquare(
                    topText = data?.getOrNull(0) ?: "?",
                    bottomText = data?.getOrNull(1) ?: "?",
                    modifier = Modifier.size(cellSize)
                )
            }
            else -> {
                // No-op: this branch should never be reached
            }
        }
    }

    // Main table layout
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Iterate rows
        for (row in 0 until ROW_COUNT) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Iterate columns
                for (col in 0 until COLUMN_COUNT) {
                    val pos = CellPosition(row, col)

                    // Decide which cell type to render
                    when {
                        // Permanently fixed positions (corners / special)
                        pos in viewModel.fixedPositions -> {
                            FixedCell(pos)
                        }

                        // Cells already correctly placed by the player
                        pos in viewModel.correctlyPlacedCells -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = pos,
                                shuffledTableData = viewModel.correctlyPlacedCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = false,
                                isCorrect = true
                            )
                        }

                        // Cells currently animating/transitioning
                        pos in viewModel.transitioningCells -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = pos,
                                shuffledTableData = viewModel.transitioningCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = false,
                                isCorrect = false,
                                isTransitioning = true
                            )
                        }

                        // All other movable/selectable cells
                        else -> {
                            val isSelected = pos == viewModel.firstSelectedCell.value
                                    || pos == viewModel.secondSelectedCell.value

                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = pos,
                                shuffledTableData = viewModel.currentTableData,
                                isSelected = isSelected,
                                handleSquareClick = { viewModel.onCellClicked(pos) },
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = !viewModel.isProcessingSwap.value
                            )
                        }
                    }
                }
            }
        }
    }
}
