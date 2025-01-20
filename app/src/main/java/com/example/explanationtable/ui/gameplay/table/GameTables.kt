package com.example.explanationtable.ui.gameplay.table

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.tooling.preview.Preview
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.EasyLevelTable
import kotlin.random.Random
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.runtime.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity

/**
 * Data class representing a cell's position in the table.
 */
data class CellPosition(val row: Int, val col: Int)

/**
 * Derangement algorithm to shuffle a list such that no element remains in its original position.
 * Uses Sattolo's algorithm for generating a cyclic permutation.
 */
fun derangeList(dataList: List<String>): List<String> {
    if (dataList.size < 2) {
        Log.d("GameTables", "Not enough data to derange.")
        return dataList
    }

    val shuffled = dataList.toMutableList()
    val n = shuffled.size

    // Apply Sattolo's algorithm
    for (i in n - 1 downTo 1) {
        val j = Random.nextInt(i) // j ∈ [0, i-1]
        // Swap elements at i and j
        val temp = shuffled[i]
        shuffled[i] = shuffled[j]
        shuffled[j] = temp
    }

    // Verify derangement: no element should be in its original position
    val isDeranged = shuffled.zip(dataList).all { (shuffledItem, originalItem) ->
        shuffledItem != originalItem
    }

    if (!isDeranged) {
        Log.d("GameTables", "Derangement failed, retrying...")
        return derangeList(dataList) // Retry if derangement failed
    }

    return shuffled
}

/**
 * Extracts movable cell data from the original table data.
 */
fun getMovableData(originalTable: EasyLevelTable, fixedPositions: Set<CellPosition>): List<Pair<CellPosition, String>> {
    val movableData = mutableListOf<Pair<CellPosition, String>>()
    for ((rowIndex, rowMap) in originalTable.rows) {
        for ((colIndex, dataList) in rowMap) {
            val position = CellPosition(rowIndex, colIndex)
            if (position !in fixedPositions) {
                // Assuming each movable cell has exactly one data item
                // If multiple items per cell are possible, adjust accordingly
                dataList.forEach { data ->
                    movableData.add(position to data)
                }
            }
        }
    }
    return movableData
}

/**
 * Creates a new shuffled table data map with fixed cells intact and movable cells shuffled.
 */
fun createShuffledTable(
    shuffledDataList: List<String>,
    movablePositions: List<CellPosition>,
    fixedCellsData: Map<CellPosition, List<String>>
): Map<CellPosition, List<String>> {
    val newTableData = mutableMapOf<CellPosition, List<String>>()

    // Assign fixed cells
    for ((position, data) in fixedCellsData) {
        newTableData[position] = data
    }

    // Assign shuffled movable cells
    for ((index, position) in movablePositions.withIndex()) {
        newTableData[position] = listOf(shuffledDataList[index])
    }

    return newTableData
}

/**
 * Composable function to display the game table.
 */
@Composable
fun GameTable(
    difficulty: Difficulty,
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    when (difficulty) {
        Difficulty.EASY -> EasyThreeByFiveTable(stageNumber, modifier)
        Difficulty.MEDIUM -> MediumTablePlaceholder(modifier)
        Difficulty.HARD -> HardTablePlaceholder(modifier)
    }
}

/**
 * Easy level table layout (3 columns by 5 rows).
 * Initializes the table with shuffled movable cells.
 */
@Composable
fun EasyThreeByFiveTable(
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    // Define the fixed positions
    val fixedPositions = remember {
        setOf(
            CellPosition(0, 0),
            CellPosition(0, 2),
            CellPosition(4, 2)
        )
    }

    // 1) Find the correct EasyLevelTable by id:
    val originalTableData = remember {
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()
    }

    // Log the original table data
    Log.d("GameTables", "Original Table Data for Stage $stageNumber:")
    originalTableData.rows.forEach { (row, cols) ->
        cols.forEach { (col, dataList) ->
            Log.d("GameTables", "Cell ($row, $col): ${dataList.joinToString(", ")}")
        }
    }

    // 2) Extract movable cells data
    val movableDataList = remember {
        getMovableData(originalTableData, fixedPositions)
    }

    // Log movable cells
    Log.d("GameTables", "Movable Cells Original Data:")
    movableDataList.forEach { (position, data) ->
        Log.d("GameTables", "Cell (${position.row}, ${position.col}): $data")
    }

    // 3) Separate positions and data
    val movablePositions = remember { movableDataList.map { it.first } }
    val movableData = remember { movableDataList.map { it.second } }

    // 4) Shuffle movable data using derangement
    val shuffledMovableData = remember {
        derangeList(movableData)
    }

    // Log shuffled movable data
    Log.d("GameTables", "Movable Cells Shuffled Data:")
    shuffledMovableData.forEachIndexed { index, data ->
        val position = movablePositions[index]
        Log.d("GameTables", "Cell (${position.row}, ${position.col}): $data")
    }

    // 5) Prepare fixed cells data
    val fixedCellsData = mapOf(
        CellPosition(0, 0) to (originalTableData.rows[0]?.get(0) ?: listOf("?")),
        CellPosition(0, 2) to (originalTableData.rows[0]?.get(2) ?: listOf("?")),
        CellPosition(4, 2) to (originalTableData.rows[4]?.get(2) ?: listOf("?"))
    )

    // Log fixed cells data
    Log.d("GameTables", "Fixed Cells Data:")
    fixedCellsData.forEach { (position, data) ->
        Log.d("GameTables", "Fixed Cell (${position.row}, ${position.col}): ${data.joinToString(", ")}")
    }

    // 6) Create shuffled table data
    val shuffledTableData = remember {
        createShuffledTable(
            shuffledMovableData,
            movablePositions,
            fixedCellsData
        )
    }

    // Log the new shuffled table data
    Log.d("GameTables", "Shuffled Table Data:")
    shuffledTableData.forEach { (position, dataList) ->
        Log.d("GameTables", "Cell (${position.row}, ${position.col}): ${dataList.joinToString(", ")}")
    }

    // 7) Render the table UI with adjusted spacing and horizontal centering
    Column(
        modifier = modifier
            .fillMaxWidth(), // Make the Column take up the full width
        horizontalAlignment = Alignment.CenterHorizontally, // Center content horizontally
        verticalArrangement = Arrangement.spacedBy(16.dp) // 16 dp vertical spacing
    ) {
        for (rowIndex in 0 until 5) { // 5 rows
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp), // 16 dp horizontal spacing
                modifier = Modifier
                    .wrapContentWidth() // Let the Row wrap its content width
                    .wrapContentHeight()
            ) {
                for (colIndex in 0 until 3) { // 3 columns
                    val currentPosition = CellPosition(rowIndex, colIndex)
                    SquareWithDirectionalSign(
                        position = currentPosition,
                        shuffledTableData = shuffledTableData,
                        squareSize = 80.dp, // Ensure this matches your square's internal size
                        signSize = 16.dp // Adjusted sign size to fit within the square
                    )
                }
            }
        }
    }
}

/**
 * Helper composable to render squares with optional directional signs.
 */
@Composable
fun SquareWithDirectionalSign(
    position: CellPosition,
    shuffledTableData: Map<CellPosition, List<String>>,
    squareSize: Dp = 80.dp,
    signSize: Dp = 16.dp
) {
    // Handle StackedSquare3D animation for the square
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(LocalDensity.current) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), // smooth transition
        label = "" // no label needed here
    )

    // Convert to dp for the UI
    val density = LocalDensity.current
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    Box(
        modifier = Modifier
            .size(squareSize)
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown()
                    isPressed = true
                    val upOrCancel = waitForUpOrCancellation()
                    isPressed = false
                    if (upOrCancel != null) {
                        // Optional: Handle click if needed
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Render the appropriate square type based on position
        when (position) {
            // Fixed Cells
            CellPosition(0, 0) -> {
                ColoredSquare(
                    text = shuffledTableData[position]?.joinToString(", ") ?: "?",
                    modifier = Modifier.fillMaxSize()
                )
            }
            CellPosition(0, 2) -> {
                val cellData = shuffledTableData[position]
                val topText = cellData?.getOrNull(0) ?: "?"
                val bottomText = cellData?.getOrNull(1) ?: "?"
                TextSeparatedSquare(
                    topText = topText,
                    bottomText = bottomText,
                    modifier = Modifier.fillMaxSize()
                )
            }
            CellPosition(4, 2) -> {
                ColoredSquare(
                    text = shuffledTableData[position]?.joinToString(", ") ?: "?",
                    modifier = Modifier.fillMaxSize()
                )
            }

            // Movable Cells
            else -> {
                val letter = shuffledTableData[position]?.joinToString(", ") ?: "?"
                StackedSquare3D(
                    letter = letter,
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        // Overlay Directional Signs based on position and apply the animated offset
        when (position) {
            CellPosition(0, 1) -> {
                DirectionalSign0_1(
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.TopEnd)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(end = 4.dp, top = 16.dp) // Reduced padding
                )
            }
            CellPosition(1, 0) -> {
                DirectionalSign1_0(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(top = 4.dp)
                )
            }
            CellPosition(1, 2) -> {
                DirectionalSign1_2(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(top = 4.dp)
                )
            }
            CellPosition(3, 2) -> {
                DirectionalSign3_2(
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.BottomCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(bottom = 4.dp)
                )
            }
            // Add more cases if needed
            else -> {
                // No directional sign for other positions
            }
        }
    }
}


/**
 * Placeholder for a future Medium table layout.
 * Only the order/arrangement of cells will differ.
 */
@Composable
fun MediumTablePlaceholder(modifier: Modifier = Modifier) {
    // TODO: Implement medium table arrangement here
    Box(modifier = modifier) {
        // Could be 3x5, 4x5, etc. with different arrangement
        // Just a placeholder to show how you can expand in the future
    }
}

/**
 * Placeholder for a future Hard table layout.
 * Only the order/arrangement of cells will differ.
 */
@Composable
fun HardTablePlaceholder(modifier: Modifier = Modifier) {
    // TODO: Implement hard table arrangement here
    Box(modifier = modifier) {
        // Could be 5x5 or something else
    }
}

/**
 * Preview of the Easy 3x5 table,
 * passing stageNumber = 1 as an example.
 */
@Preview(showBackground = true)
@Composable
fun EasyThreeByFiveTablePreview() {
    EasyThreeByFiveTable(stageNumber = 1)
}

/**
 * Preview of the delegating composable for the EASY difficulty,
 * also passing stageNumber = 1 as an example.
 */
@Preview(showBackground = true)
@Composable
fun GameTablePreview() {
    GameTable(difficulty = Difficulty.EASY, stageNumber = 1)
}
