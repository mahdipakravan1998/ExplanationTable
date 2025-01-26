package com.example.explanationtable.ui.gameplay.table.components.layout

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.cells.BrightGreenSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.utils.createShuffledTable
import com.example.explanationtable.ui.gameplay.table.utils.derangeList
import com.example.explanationtable.ui.gameplay.table.utils.getMovableData
import kotlinx.coroutines.delay

/**
 * Easy level table layout (3 columns by 5 rows).
 * Initializes the table with shuffled movable cells.
 */
@Composable
fun EasyThreeByFiveTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    // Define the fixed positions of certain cells
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    // Retrieve the original table data based on the current stage number
    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }

    // Extract movable cells data
    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }

    // Separate positions and data for movable cells
    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }

    // Shuffle movable data using derangement to prevent matching positions
    val shuffledMovableData = remember {
        derangeList(movableData)
    }

    // Use SnapshotStateMap for recomposition-safe mutable state handling
    val currentTableData = remember {
        mutableStateMapOf<CellPosition, List<String>>().apply {
            putAll(
                createShuffledTable(
                    shuffledMovableData,
                    movablePositions,
                    mapOf() // Fixed positions are excluded
                )
            )
        }
    }

    // Track correctly placed cells
    val correctlyPlacedCells = remember { mutableStateMapOf<CellPosition, List<String>>() }

    // Track the selection of squares
    var firstSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var isSelectionComplete by remember { mutableStateOf(false) }

    // Handle square selection and swapping
    fun handleSquareClick(position: CellPosition) {
        if (firstSelectedCell == null) {
            firstSelectedCell = position
        } else if (secondSelectedCell == null && position != firstSelectedCell) {
            secondSelectedCell = position
            isSelectionComplete = true

            // Swap letters between the two selected cells
            val firstCell = firstSelectedCell
            val secondCell = secondSelectedCell
            if (firstCell != null && secondCell != null) {
                val temp = currentTableData[firstCell]
                currentTableData[firstCell] = currentTableData[secondCell] ?: listOf("?")
                currentTableData[secondCell] = temp ?: listOf("?")
            }

            // Check if any movable cell is now correctly placed
            movablePositions.forEach { cellPosition ->
                val originalData = originalTableData.rows[cellPosition.row]?.get(cellPosition.col)
                if (currentTableData[cellPosition] == originalData) {
                    correctlyPlacedCells[cellPosition] = originalData ?: listOf("?")
                    currentTableData.remove(cellPosition) // Remove from movable cells
                }
            }
        }
    }

    // Function to reset the selection after two cells are selected
    @Composable
    fun resetSelection() {
        if (isSelectionComplete) {
            // Allow the selected cells to hold their color before resetting
            val firstCell = firstSelectedCell
            val secondCell = secondSelectedCell

            // Start the deselection/reset logic asynchronously
            LaunchedEffect(firstCell, secondCell) {
                delay(500) // Hold the selected color for 500ms (non-blocking)
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }
        }
    }


// Render the table UI
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (rowIndex in 0 until 5) { // 5 rows
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.wrapContentWidth().wrapContentHeight()
            ) {
                for (colIndex in 0 until 3) { // 3 columns
                    val currentPosition = CellPosition(rowIndex, colIndex)

                    when {
                        fixedPositions.contains(currentPosition) -> {
                            // Render fixed cells based on their predefined types
                            when (currentPosition) {
                                CellPosition(0, 0), CellPosition(4, 2) -> {
                                    ColoredSquare(
                                        text = originalTableData.rows[rowIndex]?.get(colIndex)?.joinToString(", ") ?: "?",
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                CellPosition(0, 2) -> {
                                    val cellData = originalTableData.rows[rowIndex]?.get(colIndex)
                                    val topText = cellData?.getOrNull(0) ?: "?"
                                    val bottomText = cellData?.getOrNull(1) ?: "?"
                                    TextSeparatedSquare(
                                        topText = topText,
                                        bottomText = bottomText,
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                            }
                        }
                        // Modified: Use SquareWithDirectionalSign for correct cells
                        correctlyPlacedCells.containsKey(currentPosition) -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = correctlyPlacedCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = 80.dp,
                                signSize = 16.dp,
                                clickable = false,
                                isCorrect = true // New parameter
                            )
                        }
                        else -> {
                            // Render movable or stacked square
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = currentTableData,
                                isSelected = (firstSelectedCell == currentPosition || secondSelectedCell == currentPosition),
                                handleSquareClick = { handleSquareClick(currentPosition) },
                                squareSize = 80.dp,
                                signSize = 16.dp,
                                clickable = true
                            )
                        }
                    }
                }
            }
        }
    }

    // Reset selection after both cells are selected
    if (isSelectionComplete) {
        resetSelection()
    }
}