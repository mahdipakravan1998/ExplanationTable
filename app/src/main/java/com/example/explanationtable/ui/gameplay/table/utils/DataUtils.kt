package com.example.explanationtable.ui.gameplay.table.utils

import android.util.Log
import com.example.explanationtable.model.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition
import kotlin.collections.component1
import kotlin.collections.component2
import kotlin.collections.iterator
import kotlin.random.Random

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
