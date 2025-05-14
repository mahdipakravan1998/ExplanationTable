package com.example.explanationtable.ui.gameplay.table.layout

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.components.cells.ColoredSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.TextSeparatedSquare
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.effects.CellsCorrectlyPlacedEffect
import com.example.explanationtable.ui.gameplay.table.effects.MinMoveSolverEffect
import com.example.explanationtable.ui.gameplay.table.effects.GameOverEffect
import com.example.explanationtable.ui.gameplay.table.effects.TableDataInitializedEffect
import com.example.explanationtable.ui.gameplay.table.effects.TransitioningCellsEffect
import com.example.explanationtable.ui.gameplay.table.utils.createShuffledTable
import com.example.explanationtable.ui.gameplay.table.utils.derangeList
import com.example.explanationtable.ui.gameplay.table.utils.getMovableData
import com.example.explanationtable.ui.gameplay.table.utils.handleCellClick

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
    val firstSelectedCellState = remember { mutableStateOf<CellPosition?>(null) }
    var firstSelectedCell by firstSelectedCellState
    val secondSelectedCellState = remember { mutableStateOf<CellPosition?>(null) }
    var secondSelectedCell by secondSelectedCellState
    val isSelectionCompleteState = remember { mutableStateOf(false) }
    var isGameOver by remember { mutableStateOf(false) }

    val playerMovesState = remember { mutableStateOf(0) }
    var playerMoves by playerMovesState

    val correctMoveCountState = remember { mutableStateOf(0) }
    var correctMoveCount by correctMoveCountState
    val incorrectMoveCountState = remember { mutableStateOf(0) }
    var incorrectMoveCount by incorrectMoveCountState

    var minMovesForThisScramble by remember { mutableStateOf<Int?>(null) }

    val coroutineScope = rememberCoroutineScope() // Get a coroutine scope
    val isProcessingSwap = remember { mutableStateOf(false) }

    CellsCorrectlyPlacedEffect(
        registerCellsCorrectlyPlacedCallback,
        firstSelectedCellState,
        secondSelectedCellState,
        isSelectionCompleteState,
        currentTableData,
        transitioningCells,
        correctMoveCountState
    )

    TableDataInitializedEffect(
        originalTableData,
        currentTableData,
        onTableDataInitialized
    )

    MinMoveSolverEffect(
        shuffledMovableData,
        movableData
    ) { result -> minMovesForThisScramble = result }

    TransitioningCellsEffect(
        transitioningCells,
        correctlyPlacedCells
    )

    GameOverEffect(
        correctlyPlacedCells.size,
        movablePositions.size,
        isGameOver,
        { isGameOver = it },
        gameStartTime,
        { minMovesForThisScramble },
        correctMoveCount,
        incorrectMoveCount,
        playerMoves,
        onGameComplete
    )

    // --- UI Rendering ---
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(spacing)
    ) {
        for (rowIndex in 0 until 5) {
            Row(
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight(),
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
                                    ColoredSquare(
                                        text = text,
                                        modifier = Modifier.size(cellSize)
                                    )
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
                            val isSelected =
                                currentPosition == firstSelectedCell || currentPosition == secondSelectedCell
                            SquareWithDirectionalSign(
                                isDarkTheme = isDarkTheme,
                                position = currentPosition,
                                shuffledTableData = currentTableData,
                                isSelected = isSelected,
                                handleSquareClick = {
                                    if (!isProcessingSwap.value) {
                                        handleCellClick(
                                            position = currentPosition,
                                            currentTableData = currentTableData,
                                            firstSelectedCellState = firstSelectedCellState,
                                            secondSelectedCellState = secondSelectedCellState,
                                            isSelectionCompleteState = isSelectionCompleteState,
                                            playerMovesState = playerMovesState,
                                            originalTableData = originalTableData,
                                            movablePositions = movablePositions,
                                            transitioningCells = transitioningCells,
                                            correctMoveCountState = correctMoveCountState,
                                            incorrectMoveCountState = incorrectMoveCountState,
                                            coroutineScope = coroutineScope,
                                            onResetSelection = {
                                                firstSelectedCellState.value = null
                                                secondSelectedCellState.value = null
                                                isSelectionCompleteState.value = false
                                                isProcessingSwap.value = false
                                            },
                                            isProcessingSwap = isProcessingSwap
                                        )
                                    }
                                },
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
