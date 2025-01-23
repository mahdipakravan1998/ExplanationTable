package com.example.explanationtable.ui.gameplay.table.components.layout

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

/**
 * Easy level table layout (3 columns by 5 rows).
 * Initializes the table with shuffled movable cells.
 */
@Composable
fun EasyThreeByFiveTable(
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    // Define the fixed positions of certain cells
    val fixedPositions = remember {
        setOf(
            CellPosition(0, 0),
            CellPosition(0, 2),
            CellPosition(4, 2)
        )
    }

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

    // Prepare fixed cells data
    val fixedCellsData = mapOf(
        CellPosition(0, 0) to (originalTableData.rows[0]?.get(0) ?: listOf("?")),
        CellPosition(0, 2) to (originalTableData.rows[0]?.get(2) ?: listOf("?")),
        CellPosition(4, 2) to (originalTableData.rows[4]?.get(2) ?: listOf("?"))
    )

    // Create shuffled table data by combining fixed and shuffled movable data
    val shuffledTableData = remember {
        createShuffledTable(
            shuffledMovableData,
            movablePositions,
            fixedCellsData
        )
    }

    // Render the table UI with adjusted spacing and horizontal centering
    Column(
        modifier = modifier
            .fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (rowIndex in 0 until 5) { // 5 rows
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier
                    .wrapContentWidth()
                    .wrapContentHeight()
            ) {
                for (colIndex in 0 until 3) { // 3 columns
                    val currentPosition = CellPosition(rowIndex, colIndex)
                    SquareWithDirectionalSign(
                        position = currentPosition,
                        shuffledTableData = shuffledTableData,
                        squareSize = 80.dp,
                        signSize = 16.dp
                    )
                }
            }
        }
    }
}