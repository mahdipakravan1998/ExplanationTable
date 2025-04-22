package com.example.explanationtable.ui.gameplay.table.components.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.utils.createShuffledTable
import com.example.explanationtable.ui.gameplay.table.utils.derangeList
import com.example.explanationtable.ui.gameplay.table.utils.getMovableData
import com.example.explanationtable.ui.gameplay.table.utils.solveWithAStar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

// Fallback accuracy function: calculates a score (0-10) based on the ratio of incorrect to correct moves.
fun calculateFallbackAccuracy(correctMoves: Int, incorrectMoves: Int): Int {
    if (correctMoves == 0) return 0 // Avoid division by zero.
    val ratio = incorrectMoves.toFloat() / correctMoves.toFloat()
    val score = (10f / (1 + ratio)).toInt()
    return score.coerceIn(0, 10)
}

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
    // --- Layout Constants ---
    val cellSize = 80.dp
    val spacing = 12.dp
    val signSize = 16.dp

    // Record the start time when the game begins.
    val gameStartTime = remember { System.currentTimeMillis() }

    // --- Fixed Cell Positions Setup ---
    val fixedPositions = setOf(
        CellPosition(0, 0),
        CellPosition(0, 2),
        CellPosition(4, 2)
    )

    // --- Table Data Initialization ---
    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }
    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }
    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }
    val shuffledMovableData = remember { derangeList(movableData) }
    val currentTableData = remember {
        mutableStateMapOf<CellPosition, List<String>>().apply {
            putAll(createShuffledTable(shuffledMovableData, movablePositions, emptyMap()))
        }
    }

    // --- State Tracking ---
    val correctlyPlacedCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    val transitioningCells = remember { mutableStateMapOf<CellPosition, List<String>>() }
    var firstSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by remember { mutableStateOf<CellPosition?>(null) }
    var isSelectionComplete by remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    // Track player moves
    var playerMoves by remember { mutableStateOf(0) }

    // Track move performance for fallback accuracy calculation.
    var correctMoveCount by remember { mutableStateOf(0) }
    var incorrectMoveCount by remember { mutableStateOf(0) }

    // Track optimal moves computed by A*
    var minMovesForThisScramble by remember { mutableStateOf<Int?>(null) }

    // Function to handle external notification of correctly placed cells
    val handleExternallyCorrectCells: (List<CellPosition>) -> Unit = { correctPositions ->
        if (correctPositions.isNotEmpty()) {
            // Clear any selections that refer to cells now resolved by help
            if (firstSelectedCell in correctPositions) {
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }

            // Process each correctly placed cell
            correctPositions.forEach { pos ->
                // Only process if the cell is still in the current table (not already marked as correct)
                if (currentTableData.containsKey(pos)) {
                    // Get the data for this position
                    val cellData = currentTableData[pos]
                    if (cellData != null) {
                        // Mark as transitioning first (for animation)
                        transitioningCells[pos] = cellData
                        // Remove from current table data since it's now correctly placed
                        currentTableData.remove(pos)
                    }
                }
            }
            // Increment correct move count
            correctMoveCount++
        }
    }

    // Register the callback
    LaunchedEffect(Unit) {
        registerCellsCorrectlyPlacedCallback(handleExternallyCorrectCells)
    }

    // Pass the created state to the parent
    LaunchedEffect(Unit) {
        onTableDataInitialized(originalTableData, currentTableData)
    }

    LaunchedEffect(shuffledMovableData, movableData) {
        val result = withContext(Dispatchers.Default) {
            solveWithAStar(shuffledMovableData, movableData)
        }
        minMovesForThisScramble = result
    }

    // --- Cell Click Handling ---
    fun handleCellClick(position: CellPosition) {
        // If the previously selected cell was removed (resolved by hint), reset selection
        if (firstSelectedCell != null && !currentTableData.containsKey(firstSelectedCell!!)) {
            firstSelectedCell = null
            secondSelectedCell = null
            isSelectionComplete = false
        }

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

            // Increment player move count.
            playerMoves++

            // Check for any movable cell that now has correct data and count them.
            var newlyCorrectCount = 0
            movablePositions.forEach { pos ->
                val originalData = originalTableData.rows[pos.row]?.get(pos.col)
                if (currentTableData[pos] == originalData) {
                    transitioningCells[pos] = currentTableData[pos]!!
                    currentTableData.remove(pos)
                    newlyCorrectCount++
                }
            }

            // Update move tracking based on newly correct cells.
            if (newlyCorrectCount > 0) {
                correctMoveCount++
            } else {
                incorrectMoveCount++
            }
        }
    }

    @Composable
    fun resetSelection() {
        if (isSelectionComplete) {
            LaunchedEffect(Unit) {
                delay(200)
                firstSelectedCell = null
                secondSelectedCell = null
                isSelectionComplete = false
            }
        }
    }

    LaunchedEffect(transitioningCells.keys.toList()) {
        transitioningCells.keys.toList().forEach { pos ->
            val data = transitioningCells[pos] ?: return@forEach
            launch {
                delay(50)
                correctlyPlacedCells[pos] = data
                transitioningCells.remove(pos)
            }
        }
    }

    LaunchedEffect(correctlyPlacedCells.size) {
        if (!isGameOver && correctlyPlacedCells.size == movablePositions.size) {
            isGameOver = true
            val gameEndTime = System.currentTimeMillis()
            val elapsedTime = gameEndTime - gameStartTime

            // Retrieve the optimal moves computed by A* (if available).
            val optimalMoves = minMovesForThisScramble ?: 0
            // Calculate user accuracy using the fallback function.
            val fallbackAccuracy = calculateFallbackAccuracy(correctMoveCount, incorrectMoveCount)
            onGameComplete(optimalMoves, fallbackAccuracy, playerMoves, elapsedTime)
        }
    }

    // --- UI Rendering ---
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (rowIndex in 0 until 5) {
            Row(
                modifier = Modifier.wrapContentWidth().wrapContentHeight(),
                horizontalArrangement = Arrangement.spacedBy(spacing)
            ) {
                for (colIndex in 0 until 3) {
                    val currentPosition = CellPosition(rowIndex, colIndex)
                    when {
                        currentPosition in fixedPositions -> {
                            when (currentPosition) {
                                CellPosition(0, 0), CellPosition(4, 2) -> {
                                    val text = originalTableData.rows[rowIndex]?.get(colIndex)
                                        ?.joinToString(", ") ?: "?"
                                    ColoredSquare(text = text, modifier = Modifier.size(cellSize))
                                }
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

    if (isSelectionComplete) {
        resetSelection()
    }
}
