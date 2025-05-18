package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.repository.GameplayRepository
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.viewmodel.GameplayViewModel

/**
 * Composable function that renders the easy-level 3x5 table with shuffled movable cells.
 *
 * @param isDarkTheme Flag indicating if dark theme is active.
 * @param stageNumber Stage number to select the corresponding table data.
 * @param modifier Modifier for customizing the layout.
 * @param onGameComplete Callback invoked when the game is completed.
 *        It now provides four parameters:
 *         - optimalMoves: The minimum number of moves computed by A*.
 *         - userAccuracy: The user's accuracy score (fallback based on move tracking).
 *         - playerMoves: The total number of moves the player made.
 *         - elapsedTime: The elapsed time of the game.
 * @param onTableDataInitialized Callback with initialized table data
 * @param registerCellsCorrectlyPlacedCallback Callback to register the function to handle cells being correctly placed externally
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
    val viewModel: GameplayViewModel = viewModel(
        factory = GameplayViewModel.Factory(
            repository = GameplayRepository(),
            stageNumber = stageNumber,
            onGameComplete = onGameComplete,
            onTableDataInitialized = onTableDataInitialized,
            registerCallback = registerCellsCorrectlyPlacedCallback
        )
    )

    val cellSize = viewModel.cellSize
    val spacing = viewModel.spacing
    val signSize = viewModel.signSize

    // --- UI Rendering ---
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (row in 0 until 5) {
            Row(
                modifier = Modifier.wrapContentSize(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (col in 0 until 3) {
                    val pos = CellPosition(row, col)
                    when {
                        pos in viewModel.fixedPositions -> {
                            when (pos) {
                                CellPosition(0, 0), CellPosition(4, 2) -> {
                                    val text = viewModel.originalTableData.rows[row]?.get(col)
                                        ?.joinToString(", ") ?: "?"
                                    ColoredSquare(text = text, modifier = Modifier.size(cellSize))
                                }
                                CellPosition(0, 2) -> {
                                    val data = viewModel.originalTableData.rows[row]?.get(col)
                                    TextSeparatedSquare(
                                        topText = data?.getOrNull(0) ?: "?",
                                        bottomText = data?.getOrNull(1) ?: "?",
                                        modifier = Modifier.size(cellSize)
                                    )
                                }
                            }
                         }
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
                        else -> {
                            val isSel = (pos == viewModel.firstSelectedCellState.value
                                    || pos == viewModel.secondSelectedCellState.value)
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = pos,
                                shuffledTableData = viewModel.currentTableData,
                                isSelected = isSel,
                                handleSquareClick = { viewModel.onCellClicked(pos) },
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = true
                            )
                        }
                    }
                }
            }
        }
    }
}
