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
 * @param isDarkTheme Boolean flag to determine if dark theme is active.
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
    // --- Initialization of Fixed and Movable Cell Data ---

    // Define positions that remain fixed and are not swappable.
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    // Retrieve the original table data based on the stage number.
    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }

    // Extract the movable cell data (excluding fixed positions).
    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }
    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }

    // Shuffle the movable data using a derangement algorithm to ensure no item remains in its original spot.
    val shuffledMovableData = remember { derangeList(movableData) }

    // Create the initial state of the table with shuffled movable data.
    val currentTableData = remember {
        mutableStateMapOf<CellPosition, List<String>>().apply {
            putAll(createShuffledTable(shuffledMovableData, movablePositions, emptyMap()))
        }
    }

    // --- State Tracking for Cell Transitions and Selections ---

    // Map to track cells that have been correctly placed.
    val correctlyPlacedCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    // Map to track cells that are in the process of transitioning.
    val transitioningCells = remember { mutableStateMapOf<CellPosition, List<String>>() }

    // Variables to track the two selected cells for swapping.
    var firstSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var isSelectionComplete by remember { mutableStateOf(false) }

    // State variable to ensure onGameComplete is only called once.
    var isGameOver by remember { mutableStateOf(false) }

    // --- Cell Click Handling Logic ---

    /**
     * Processes a cell click event.
     * - Records the first cell selection.
     * - On the second distinct selection, swaps the data between cells,
     *   then checks for any cells that now match their original content.
     *
     * @param position The position of the clicked cell.
     */
    fun handleSquareClick(position: CellPosition) {
        if (firstSelectedCell == null) {
            // Record the first cell selected.
            firstSelectedCell = position
        } else if (secondSelectedCell == null && position != firstSelectedCell) {
            // Record the second selection and mark selection as complete.
            secondSelectedCell = position
            isSelectionComplete = true

            // Swap the data between the two selected cells.
            val firstCell = firstSelectedCell
            val secondCell = secondSelectedCell
            if (firstCell != null && secondCell != null) {
                val temp = currentTableData[firstCell]
                currentTableData[firstCell] = currentTableData[secondCell] ?: listOf("?")
                currentTableData[secondCell] = temp ?: listOf("?")
            }

            // Check each movable cell for correct placement and mark it for transition if correct.
            movablePositions.forEach { cellPosition ->
                val originalData = originalTableData.rows[cellPosition.row]?.get(cellPosition.col)
                if (currentTableData[cellPosition] == originalData) {
                    transitioningCells[cellPosition] = currentTableData[cellPosition]!!
                    currentTableData.remove(cellPosition)
                }
            }
        }
    }

    /**
     * Resets the selected cells after a swap is complete.
     * A brief delay is introduced for visual feedback.
     */
    @Composable
    fun resetSelection() {
        if (isSelectionComplete) {
            LaunchedEffect(Unit) {
                delay(200) // Visual feedback delay before resetting the selection.
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }
        }
    }

    // --- Handle Transitioning Cells ---
    // Move cells marked as transitioning to the correctly placed state after a short animation delay.
    LaunchedEffect(transitioningCells.keys.toList()) {
        transitioningCells.keys.toList().forEach { pos ->
            val data = transitioningCells[pos] ?: return@forEach
            launch {
                delay(50) // Delay for transition animation.
                correctlyPlacedCells[pos] = data
                transitioningCells.remove(pos)
            }
        }
    }

    // --- Check for Game Completion ---
    LaunchedEffect(correctlyPlacedCells.size) {
        if (!isGameOver && correctlyPlacedCells.size == movablePositions.size) {
            isGameOver = true
            onGameComplete()
        }
    }

    // --- UI Rendering ---

    // Render the table layout using a Column to arrange rows vertically.
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp) // Spacing between rows.
    ) {
        // Loop through each row (5 rows total).
        for (rowIndex in 0 until 5) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(12.dp), // Spacing between cells.
                modifier = Modifier.wrapContentWidth().wrapContentHeight()
            ) {
                // Loop through each column (3 columns total).
                for (colIndex in 0 until 3) {
                    val currentPosition = CellPosition(rowIndex, colIndex)

                    when {
                        // --- Fixed Cells Rendering ---
                        // Render cells that are fixed (non-movable).
                        fixedPositions.contains(currentPosition) -> {
                            when (currentPosition) {
                                // Render these fixed cells using a colored square.
                                CellPosition(0, 0), CellPosition(4, 2) -> {
                                    ColoredSquare(
                                        text = originalTableData.rows[rowIndex]?.get(colIndex)
                                            ?.joinToString(", ") ?: "?",
                                        modifier = Modifier.size(80.dp)
                                    )
                                }
                                // Render a specific fixed cell with two separate text areas.
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
                        // --- Correctly Placed Cells Rendering ---
                        // Render cells that have been correctly placed (final state).
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
                        // --- Transitioning Cells Rendering ---
                        // Render cells that are transitioning to the correctly placed state.
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
                        // --- Movable (Active) Cells Rendering ---
                        // Render active movable cells that are available for user interaction.
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

    // Reset cell selections after a swap if selection is complete.
    if (isSelectionComplete) {
        resetSelection()
    }
}
