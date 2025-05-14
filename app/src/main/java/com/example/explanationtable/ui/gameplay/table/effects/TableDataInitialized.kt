package com.example.explanationtable.ui.gameplay.table.effects

import androidx.compose.runtime.*
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable

@Composable
fun TableDataInitializedEffect(
    originalTableData: EasyLevelTable,
    currentTableData: SnapshotStateMap<CellPosition, List<String>>,
    onTableDataInitialized: (EasyLevelTable, MutableMap<CellPosition, List<String>>) -> Unit
) {
    LaunchedEffect(Unit) {
        onTableDataInitialized(originalTableData, currentTableData)
    }
}
