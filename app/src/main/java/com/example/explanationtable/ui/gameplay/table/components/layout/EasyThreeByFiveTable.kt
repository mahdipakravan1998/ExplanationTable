package com.example.explanationtable.ui.gameplay.table.components.layout

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.utils.createShuffledTable
import com.example.explanationtable.ui.gameplay.table.utils.derangeList
import com.example.explanationtable.ui.gameplay.table.utils.getMovableData
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Renders the easy-level 3x5 table with shuffled movable cells.
 */
@Composable
fun EasyThreeByFiveTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }

    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }

    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }

    val shuffledMovableData = remember {
        derangeList(movableData)
    }

    val currentTableData = remember {
        mutableStateMapOf<CellPosition, List<String>>().apply {
            putAll(
                createShuffledTable(
                    shuffledMovableData,
                    movablePositions,
                    mapOf()
                )
            )
        }
    }

    val correctlyPlacedCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    val transitioningCells = remember { mutableStateMapOf<CellPosition, List<String>>() }

    var firstSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var isSelectionComplete by remember { mutableStateOf(false) }

    // Handles the swapping of selected squares
    fun handleSquareClick(position: CellPosition) {
        if (firstSelectedCell == null) {
            firstSelectedCell = position
        } else if (secondSelectedCell == null && position != firstSelectedCell) {
            secondSelectedCell = position
            isSelectionComplete = true

            val firstCell = firstSelectedCell
            val secondCell = secondSelectedCell
            if (firstCell != null && secondCell != null) {
                val temp = currentTableData[firstCell]
                currentTableData[firstCell] = currentTableData[secondCell] ?: listOf("?")
                currentTableData[secondCell] = temp ?: listOf("?")
            }

            movablePositions.forEach { cellPosition ->
                val originalData = originalTableData.rows[cellPosition.row]?.get(cellPosition.col)
                if (currentTableData[cellPosition] == originalData) {
                    transitioningCells[cellPosition] = currentTableData[cellPosition]!!
                    currentTableData.remove(cellPosition)
                }
            }
        }
    }

    // Resets the selection after cells are swapped
    @Composable
    fun resetSelection() {
        if (isSelectionComplete) {
            LaunchedEffect(Unit) {
                delay(300)
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }
        }
    }

    // Transition cells to the correctly placed state
    LaunchedEffect(transitioningCells.keys.toList()) {
        transitioningCells.keys.toList().forEach { pos ->
            val data = transitioningCells[pos] ?: return@forEach
            launch {
                delay(150)
                correctlyPlacedCells[pos] = data
                transitioningCells.remove(pos)
            }
        }
    }

    // Render the table UI
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (rowIndex in 0 until 5) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.wrapContentWidth().wrapContentHeight()
            ) {
                for (colIndex in 0 until 3) {
                    val currentPosition = CellPosition(rowIndex, colIndex)

                    when {
                        fixedPositions.contains(currentPosition) -> {
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
                                isCorrect = true
                            )
                        }
                        transitioningCells.containsKey(currentPosition) -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = transitioningCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = 80.dp,
                                signSize = 16.dp,
                                clickable = false,
                                isCorrect = false,
                                isTransitioning = true
                            )
                        }
                        else -> {
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

    if (isSelectionComplete) {
        resetSelection()
    }
}
