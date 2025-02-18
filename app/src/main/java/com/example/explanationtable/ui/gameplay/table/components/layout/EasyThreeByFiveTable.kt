package com.example.explanationtable.ui.gameplay.table.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
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
 * Composable function that renders the easy-level 3x5 table with shuffled movable cells.
 *
 * @param isDarkTheme Flag indicating if dark theme is active.
 * @param stageNumber Stage number to select the corresponding table data.
 * @param modifier Modifier for customizing the layout.
 * @param onGameComplete Callback invoked when the game is completed (all cells are correctly placed).
 */
@Composable
fun EasyThreeByFiveTable(
    isDarkTheme: Boolean,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: () -> Unit = {}
) {
    // --- Layout Constants ---
    val cellSize = 80.dp
    val spacing = 12.dp
    val signSize = 16.dp

    // --- Fixed Cell Positions Setup ---
    // These positions remain fixed and non-swappable.
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    // --- Table Data Initialization ---
    // Retrieve the original table data based on the stage number.
    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }
    // Extract movable cell data excluding the fixed positions.
    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }
    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }
    // Shuffle movable data ensuring no item stays in its original position.
    val shuffledMovableData = remember { derangeList(movableData) }
    // Initialize the current table state with the shuffled movable data.
    val currentTableData = remember {
        mutableStateMapOf<CellPosition, List<String>>().apply {
            putAll(createShuffledTable(shuffledMovableData, movablePositions, emptyMap()))
        }
    }

    // --- State Tracking ---
    // Map for cells that are correctly placed.
    val correctlyPlacedCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    // Map for cells currently undergoing transition.
    val transitioningCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    // Variables to track selected cells for swapping.
    var firstSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var isSelectionComplete by remember { mutableStateOf(false) }
    // Flag to ensure onGameComplete is invoked only once.
    var isGameOver by remember { mutableStateOf(false) }

    // --- Cell Click Handling ---
    /**
     * Handles click events on a cell.
     * - On first click, records the cell.
     * - On second (distinct) click, swaps the two cells and checks for correct placement.
     *
     * @param position The position of the clicked cell.
     */
    fun handleCellClick(position: CellPosition) {
        if (firstSelectedCell == null) {
            firstSelectedCell = position
        } else if (secondSelectedCell == null && position != firstSelectedCell) {
            secondSelectedCell = position
            isSelectionComplete = true

            // Swap the data between the two selected cells.
            val first = firstSelectedCell
            val second = secondSelectedCell
            if (first != null && second != null) {
                val temp = currentTableData[first]
                currentTableData[first] = currentTableData[second] ?: listOf("?")
                currentTableData[second] = temp ?: listOf("?")
            }

            // Check for any movable cell that now has correct data.
            movablePositions.forEach { pos ->
                val originalData = originalTableData.rows[pos.row]?.get(pos.col)
                if (currentTableData[pos] == originalData) {
                    transitioningCells[pos] = currentTableData[pos]!!
                    currentTableData.remove(pos)
                }
            }
        }
    }

    /**
     * Resets the cell selections after a swap, adding a brief delay for visual feedback.
     */
    @Composable
    fun resetSelection() {
        if (isSelectionComplete) {
            LaunchedEffect(Unit) {
                delay(200) // Visual feedback delay.
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }
        }
    }

    // --- Transition Animation ---
    // Animate cells marked as transitioning to the correctly placed state.
    LaunchedEffect(transitioningCells.keys.toList()) {
        transitioningCells.keys.toList().forEach { pos ->
            val data = transitioningCells[pos] ?: return@forEach
            launch {
                delay(50) // Animation delay.
                correctlyPlacedCells[pos] = data
                transitioningCells.remove(pos)
            }
        }
    }

    // --- Game Completion Check ---
    // Invoke onGameComplete once all movable cells are correctly placed.
    LaunchedEffect(correctlyPlacedCells.size) {
        if (!isGameOver && correctlyPlacedCells.size == movablePositions.size) {
            isGameOver = true
            onGameComplete()
        }
    }

    // --- UI Rendering ---
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        // Render each row.
        for (rowIndex in 0 until 5) {
            Row(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                // Render each column in the current row.
                for (colIndex in 0 until 3) {
                    val currentPosition = CellPosition(rowIndex, colIndex)
                    when {
                        // --- Fixed Cells Rendering ---
                        // Render cells that are fixed (non-movable).
                        currentPosition in fixedPositions -> {
                            when (currentPosition) {
                                // Render fixed cells using a colored square.
                                CellPosition(0, 0), CellPosition(4, 2) -> {
                                    val text = originalTableData.rows[rowIndex]?.get(colIndex)
                                        ?.joinToString(", ") ?: "?"
                                    ColoredSquare(text = text, modifier = Modifier.size(cellSize))
                                }
                                // Render a fixed cell with separate top and bottom text.
                                CellPosition(0, 2) -> {
                                    val cellData = originalTableData.rows[rowIndex]?.get(colIndex)
                                    val topText = cellData?.getOrNull(0) ?: "?"
                                    val bottomText = cellData?.getOrNull(1) ?: "?"
                                    TextSeparatedSquare(
                                        topText = topText,
                                        bottomText = bottomText,
                                        modifier = Modifier.size(cellSize)
                                    )
                                }
                            }
                        }
                        // --- Correctly Placed Cells Rendering ---
                        // Render cells that have been correctly placed.
                        currentPosition in correctlyPlacedCells -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = correctlyPlacedCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = false,
                                isCorrect = true
                            )
                        }
                        // --- Transitioning Cells Rendering ---
                        // Render cells that are transitioning.
                        currentPosition in transitioningCells -> {
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = transitioningCells,
                                isSelected = false,
                                handleSquareClick = {},
                                squareSize = cellSize,
                                signSize = signSize,
                                clickable = false,
                                isCorrect = false,
                                isTransitioning = true
                            )
                        }
                        // --- Active Movable Cells Rendering ---
                        // Render active cells available for interaction.
                        else -> {
                            val isSelected = currentPosition == firstSelectedCell || currentPosition == secondSelectedCell
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = currentTableData,
                                isSelected = isSelected,
                                handleSquareClick = { handleCellClick(currentPosition) },
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

    // Reset the cell selection after a swap if needed.
    if (isSelectionComplete) {
        resetSelection()
    }
}
