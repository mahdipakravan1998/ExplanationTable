package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.repository.TableRepository
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.viewmodel.TableViewModel
import com.example.explanationtable.model.Difficulty

/**
 * A fully generic puzzle-grid renderer.  Renders an [rowsCount]×[colsCount] grid,
 * uses [fixedPositions] to know which cells are permanently fixed, and delegates swapping logic
 * to a [TableViewModel] created with the same dimension/fixed set.
 *
 * @param isDarkTheme              whether to use dark theme styling for directional signs
 * @param difficulty               EASY, MEDIUM, etc., so ViewModel can pick correct data
 * @param stageNumber              which stage to load (1-based ID from LevelTable.id)
 * @param rowsCount                number of rows (e.g. 5 for EASY, 4 for MEDIUM)
 * @param colsCount                number of columns (e.g. 3 for EASY, 4 for MEDIUM)
 * @param fixedPositions           set of cell positions that are “immutable” (corners, text‐separated, etc.)
 * @param fixedCellTypes           a map of each fixed CellPosition → one of: "COLORED" or "TEXT_SEPARATED".
 *                                 (You can extend this in the future if you want more fixed‐cell types.)
 * @param modifier                 optional compose Modifier
 * @param onGameComplete           callback: (optimalMoves, userAccuracy, playerMoves, elapsedTime)
 * @param onTableDataInitialized   callback to expose (originalTableData, currentShuffledData)
 * @param registerCellsCorrectlyPlacedCallback callback that the ViewModel uses to tell UI which cells
 *                                 have just become “externally correct” (e.g. when a pair swap completes).
 */
@Composable
fun TableLayout(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageNumber: Int,
    rowsCount: Int,
    colsCount: Int,
    fixedPositions: Set<CellPosition>,
    fixedCellTypes: Map<CellPosition, FixedCellType>,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: LevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    // Create (or retrieve) our ViewModel with exactly these parameters
    val viewModel: TableViewModel = viewModel(
        factory = TableViewModel.Factory(
            repository = TableRepository(),
            difficulty = difficulty,
            stageNumber = stageNumber,
            fixedPositions = fixedPositions,
            onGameComplete = onGameComplete,
            onTableDataInitialized = onTableDataInitialized,
            registerCallback = registerCellsCorrectlyPlacedCallback
        )
    )

    // Extract UI parameters from ViewModel
    val cellSize: Dp = viewModel.cellSize
    val spacing: Dp = viewModel.spacing
    val signSize: Dp = viewModel.signSize

    // Helper to render a fixed cell (Colored vs. TextSeparated)
    @Composable
    fun FixedCell(pos: CellPosition) {
        // Lookup the original data for that position
        val data = viewModel.originalTableData.rows[pos.row]?.get(pos.col)
        when (fixedCellTypes[pos]) {
            FixedCellType.COLORED -> {
                // A single‐text colored square (data!!.first() if present)
                val text = data?.joinToString(", ") ?: "?"
                ColoredSquare(text = text, modifier = Modifier.size(cellSize))
            }
            FixedCellType.TEXT_SEPARATED -> {
                // A two‐text square (top/bottom). We assume exactly 2 entries, but fallback if missing.
                val top = data?.getOrNull(0) ?: "?"
                val bottom = data?.getOrNull(1) ?: "?"
                TextSeparatedSquare(
                    topText = top,
                    bottomText = bottom,
                    modifier = Modifier.size(cellSize)
                )
            }
            null -> {
                // Should not happen: every fixed position must have a type
            }
        }
    }

    // Main table grid
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (row in 0 until rowsCount) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (col in 0 until colsCount) {
                    val pos = CellPosition(row, col)

                    when {
                        // 1) Permanently fixed positions (corners / text‐separated)
                        pos in fixedPositions -> {
                            FixedCell(pos)
                        }
                        // 2) Already correctly placed by the player
                        pos in viewModel.correctlyPlacedCells -> {
                            SquareWithDirectionalSign(
                                difficulty = difficulty,
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
                        // 3) Cells currently animating/transitioning
                        pos in viewModel.transitioningCells -> {
                            SquareWithDirectionalSign(
                                difficulty = difficulty,
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
                        // 4) All other movable/selectable cells
                        else -> {
                            val isSelected = pos == viewModel.firstSelectedCell.value
                                    || pos == viewModel.secondSelectedCell.value

                            SquareWithDirectionalSign(
                                difficulty = difficulty,
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

/**
 * Enum to describe what type of fixed cell to render in TableLayout.
 */
enum class FixedCellType {
    COLORED,
    TEXT_SEPARATED
}
