package com.example.explanationtable.repository

import android.content.Context
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.data.getHintOptions
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.LevelTable
import kotlinx.coroutines.flow.Flow
import com.example.explanationtable.ui.hint.logic.revealAllCells as revealAllCellsLogic
import com.example.explanationtable.ui.hint.logic.revealRandomCategory as revealRandomCategoryLogic
import com.example.explanationtable.ui.hint.logic.revealRandomCell as revealRandomCellLogic

/**
 * Repository responsible for hint-related operations, including data retrieval
 * and business logic for revealing hints.
 *
 * @param context Application context for accessing resources and DataStore.
 */
class HintRepository(private val context: Context) {

    // Lazily initialized manager for user preferences and in-game currency (diamonds).
    private val dataStoreManager: DataStoreManager by lazy { DataStoreManager(context) }

    /**
     * Retrieves all available hint options from the data layer.
     * @return List of [HintOption] instances describing each hint's type, cost, and description.
     */
    fun getHintOptions(): List<HintOption> =
        getHintOptions(context)

    /**
     * Reveals every remaining unsolved cell in the puzzle.
     */
    fun revealAllCells(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable
    ): List<CellPosition> =
        revealAllCellsLogic(currentTable, originalTable)

    /**
     * Reveals a random category (entire row or column) from the puzzle as a hint.
     * @param currentTable Map of already-revealed cells with their values.
     * @param originalTable The complete solution table for reference.
     * @return List of [CellPosition] objects representing newly revealed cells.
     */
    fun revealRandomCategory(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable,
        difficulty: Difficulty
    ): List<CellPosition> =
        revealRandomCategoryLogic(currentTable, originalTable, difficulty)

    /**
     * Reveals a single random cell from the puzzle as a hint.
     * @param currentTable Map of already-revealed cells with their values.
     * @param originalTable The complete solution table for reference.
     * @return List containing the single [CellPosition] that was revealed.
     */
    fun revealRandomCell(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable
    ): List<CellPosition> =
        revealRandomCellLogic(currentTable, originalTable)

    /**
     * Flow emitting the user's current diamond balance.
     */
    val diamondsFlow: Flow<Int> = dataStoreManager.diamonds

    /**
     * Retrieves the user's current diamond count.
     * @return Total number of diamonds available.
     */
    suspend fun getDiamondCount(): Int =
        dataStoreManager.getDiamondCount()

    /**
     * Deducts a specified number of diamonds from the user's balance.
     * @param amount Number of diamonds to deduct.
     */
    suspend fun spendDiamonds(amount: Int) {
        // Ensures the deduction is applied via DataStoreManager.
        dataStoreManager.spendDiamonds(amount)
    }
}
