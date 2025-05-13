package com.example.explanationtable.ui.gameplay.table.utils

import android.util.Log
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.snapshots.SnapshotStateMap
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable

object TableDebugger {
    private lateinit var tableData: SnapshotStateMap<CellPosition, List<String>>
    private lateinit var firstSelState: MutableState<CellPosition?>
    private lateinit var secondSelState: MutableState<CellPosition?>
    private lateinit var completeState: MutableState<Boolean>
    private lateinit var movesState: MutableState<Int>
    private lateinit var corrState: MutableState<Int>
    private lateinit var incorrState: MutableState<Int>
    private lateinit var original: EasyLevelTable
    private lateinit var movables: List<CellPosition>
    private lateinit var transCells: SnapshotStateMap<CellPosition, List<String>>

    fun init(
        currentTableData: SnapshotStateMap<CellPosition, List<String>>,
        firstSelected: MutableState<CellPosition?>,
        secondSelected: MutableState<CellPosition?>,
        isComplete: MutableState<Boolean>,
        playerMoves: MutableState<Int>,
        correctMoves: MutableState<Int>,
        incorrectMoves: MutableState<Int>,
        originalTableData: EasyLevelTable,
        movablePositions: List<CellPosition>,
        transitioningCells: SnapshotStateMap<CellPosition, List<String>>
    ) {
        tableData = currentTableData
        firstSelState = firstSelected
        secondSelState = secondSelected
        completeState = isComplete
        movesState = playerMoves
        corrState = correctMoves
        incorrState = incorrectMoves
        original = originalTableData
        movables = movablePositions
        transCells = transitioningCells
    }
}
