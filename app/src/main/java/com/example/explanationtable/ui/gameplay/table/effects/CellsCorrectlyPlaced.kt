package com.example.explanationtable.ui.gameplay.table.effects

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.ui.gameplay.table.utils.handleExternallyCorrectCells

@Composable
fun CellsCorrectlyPlacedEffect(
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit,
    firstSelectedCellState: MutableState<CellPosition?>,
    secondSelectedCellState: MutableState<CellPosition?>,
    isSelectionCompleteState: MutableState<Boolean>,
    currentTableData: SnapshotStateMap<CellPosition, List<String>>,
    transitioningCells: SnapshotStateMap<CellPosition, List<String>>,
    correctMoveCountState: MutableState<Int>
) {
    LaunchedEffect(Unit) {
        registerCellsCorrectlyPlacedCallback { correctPositions ->
            handleExternallyCorrectCells(
                correctPositions,
                firstSelectedCellState,
                secondSelectedCellState,
                isSelectionCompleteState,
                currentTableData,
                transitioningCells,
                correctMoveCountState
            )
        }
    }
}
