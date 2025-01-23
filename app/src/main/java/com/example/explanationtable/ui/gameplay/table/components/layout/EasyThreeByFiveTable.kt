package com.example.explanationtable.ui.gameplay.table.components.layout

import android.util.Log
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.shared.SquareWithDirectionalSign
import com.example.explanationtable.ui.gameplay.table.utils.createShuffledTable
import com.example.explanationtable.ui.gameplay.table.utils.derangeList
import com.example.explanationtable.ui.gameplay.table.utils.getMovableData
import kotlin.collections.component1
import kotlin.collections.component2

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