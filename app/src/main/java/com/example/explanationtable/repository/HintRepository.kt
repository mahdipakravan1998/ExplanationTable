package com.example.explanationtable.repository

import android.content.Context
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.data.getHintOptions as fetchHintOptions
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.hint.logic.revealRandomCategory as logicRevealRandomCategory
import com.example.explanationtable.ui.hint.logic.revealRandomCell as logicRevealRandomCell

/**
 * Single source of truth for hint-related data and business logic.
 *
 * @param context Application context for accessing DataStore and resources.
 */
class HintRepository(private val context: Context) {

    // Lazily initialized manager for user preferences and currency (e.g., diamonds).
    private val dataStoreManager: DataStoreManager by lazy { DataStoreManager(context) }

    /**
     * Fetches all available hint options (type, cost, description) from the data layer.
     *
     * @return A list of [HintOption]s that the UI can display.
     */
    fun getHintOptions(): List<HintOption> =
        fetchHintOptions(context)

    /**
     * Reveals an entire random category (row or column) of cells.
     *
     * @param currentTableData Map of already-revealed cells to their values.
     * @param originalTableData The complete solution table for reference.
     * @return The list of [CellPosition]s that were newly revealed.
     */
    fun revealRandomCategory(
        currentTableData: MutableMap<CellPosition, List<String>>,
        originalTableData: EasyLevelTable
    ): List<CellPosition> =
        logicRevealRandomCategory(currentTableData, originalTableData)

    /**
     * Reveals a single random cell as a hint.
     *
     * @param currentTableData Map of already-revealed cells to their values.
     * @param originalTableData The complete solution table for reference.
     * @return A list containing the single [CellPosition] that was revealed.
     */
    fun revealRandomCell(
        currentTableData: MutableMap<CellPosition, List<String>>,
        originalTableData: EasyLevelTable
    ): List<CellPosition> =
        logicRevealRandomCell(currentTableData, originalTableData)

    /**
     * Reads the user's current diamond balance.
     *
     * @return The total number of diamonds available.
     */
    suspend fun getDiamondCount(): Int =
        dataStoreManager.getDiamondCount()

    /**
     * Deducts a specified amount of diamonds from the user's balance.
     *
     * @param amount The number of diamonds to spend.
     */
    suspend fun spendDiamonds(amount: Int) =
        dataStoreManager.spendDiamonds(amount)
}
