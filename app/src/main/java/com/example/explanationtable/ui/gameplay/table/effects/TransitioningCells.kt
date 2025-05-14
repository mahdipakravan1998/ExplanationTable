package com.example.explanationtable.ui.gameplay.table.effects

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.explanationtable.model.CellPosition
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun TransitioningCellsEffect(
    transitioningCells: SnapshotStateMap<CellPosition, List<String>>,
    correctlyPlacedCells: SnapshotStateMap<CellPosition, List<String>>
) {
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
}
