package com.example.explanationtable.repository

import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.domain.usecase.calculateFallbackAccuracy
import com.example.explanationtable.ui.gameplay.table.utils.solveWithAStar
import kotlin.random.Random
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class GameplayRepository {

    /** Find the table for [stageNumber], or default to first. */
    fun getOriginalTable(stageNumber: Int): EasyLevelTable =
        easyLevelTables.find { it.id == stageNumber } ?: easyLevelTables.first()

    /**
     * Extracts movable cell data from the original table data.
     *
     * This function iterates over each cell in the table and collects the data
     * for cells that are not designated as fixed (i.e. their positions are not in [fixedPositions]).
     *
     * @param originalTable the original table containing rows of cell data.
     * @param fixedPositions a set of positions representing fixed cells.
     * @return a list of pairs where each pair contains the cell position and its associated data.
     */
    fun getMovableData(
        originalTable: EasyLevelTable,
        fixedPositions: Set<CellPosition>
    ): List<Pair<CellPosition, String>> {
        // Transform the nested table structure into a flat list of movable cell data.
        return originalTable.rows.flatMap { (rowIndex, rowMap) ->
            rowMap.flatMap { (colIndex, dataList) ->
                val position = CellPosition(rowIndex, colIndex)
                // Exclude fixed positions.
                if (position in fixedPositions) {
                    emptyList()
                } else {
                    // Map each data element to its cell position.
                    dataList.map { data -> position to data }
                }
            }
        }
    }

    /**
     * Generates a deranged (i.e. no element remains in its original position) version of the provided list.
     *
     * This function implements Sattolo's algorithm to produce a cyclic permutation.
     * If the produced permutation fails the derangement check, the algorithm is retried.
     *
     * @param dataList the original list of strings to derange.
     * @return a new list in which every element is moved from its original index.
     */
    fun derangeList(dataList: List<String>): List<String> {
        if (dataList.size < 2) {
            return dataList
        }

        // Loop until a valid derangement is produced.
        while (true) {
            // Create a mutable copy to shuffle.
            val shuffled = dataList.toMutableList()
            val n = shuffled.size

            // Sattolo's algorithm: iterate backwards from the last index.
            for (i in n - 1 downTo 1) {
                // Select a random index in the range [0, i).
                val j = Random.nextInt(i)
                // Swap the elements at indices i and j.
                val temp = shuffled[i]
                shuffled[i] = shuffled[j]
                shuffled[j] = temp
            }

            // Verify that no element remains in its original position.
            val isDeranged = shuffled.indices.all { index ->
                shuffled[index] != dataList[index]
            }
            if (isDeranged) {
                return shuffled
            }
        }
    }

    /**
     * Creates a new table data map with fixed cells intact and movable cells assigned with shuffled data.
     *
     * This function combines the fixed cells data with the shuffled movable data.
     * It assumes that the order of [movablePositions] aligns with the order of items in [shuffledDataList].
     *
     * @param shuffledDataList a list of shuffled strings to be placed in movable cells.
     * @param movablePositions a list of cell positions corresponding to the movable cells.
     * @param fixedCellsData a map containing fixed cell positions with their original data.
     * @return a new map representing the table with fixed and shuffled movable cell data.
     */
    fun createShuffledTable(
        shuffledDataList: List<String>,
        movablePositions: List<CellPosition>,
        fixedCellsData: Map<CellPosition, List<String>>
    ): Map<CellPosition, List<String>> {
        // Build a new mutable map and pre-populate it with fixed cell data.
        return mutableMapOf<CellPosition, List<String>>().apply {
            putAll(fixedCellsData)
            // Iterate over each movable position and assign it a single-item list from the shuffled data.
            movablePositions.forEachIndexed { index, position ->
                put(position, listOf(shuffledDataList[index]))
            }
        }
    }

    /** Off-main-thread A* solver. */
    suspend fun solveMinMoves(
        shuffled: List<String>,
        target: List<String>
    ): Int = withContext(Dispatchers.Default) {
        solveWithAStar(shuffled, target)
    }

    /** Fallback accuracy calculator. */
    fun calculateAccuracy(correct: Int, incorrect: Int): Int =
        calculateFallbackAccuracy(correct, incorrect)
}
