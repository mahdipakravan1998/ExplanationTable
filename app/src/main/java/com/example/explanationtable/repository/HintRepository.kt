package com.example.explanationtable.repository

import android.content.Context
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.data.getHintOptions as fetchHintOptions
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.model.CellPosition
import kotlinx.coroutines.flow.first
import com.example.explanationtable.ui.hint.logic.revealRandomCategory as logicRevealRandomCategory
import com.example.explanationtable.ui.hint.logic.revealRandomCell as logicRevealRandomCell

/**
 * Single source of truth for hint data and logic.
 */
class HintRepository(private val context: Context) {

    // DataStore-backed manager for diamonds, theme, etc.
    private val dataStoreManager = DataStoreManager(context)

    /** Delegates to the data module’s getHintOptions(...) */
    fun getHintOptions(): List<HintOption> =
        fetchHintOptions(context)

    /** Delegates to your hint-logic to reveal a random *category* of cells */
    fun revealRandomCategory(
        currentTableData: MutableMap<CellPosition, List<String>>,
        originalTableData: EasyLevelTable
    ): List<CellPosition> =
        logicRevealRandomCategory(currentTableData, originalTableData)

    /** Delegates to your hint-logic to reveal a random *cell* */
    fun revealRandomCell(
        currentTableData: MutableMap<CellPosition, List<String>>,
        originalTableData: EasyLevelTable
    ): List<CellPosition> =
        logicRevealRandomCell(currentTableData, originalTableData)

    /** Read the user’s current diamond count */
    suspend fun getDiamondCount(): Int =
        dataStoreManager.getDiamondCount()

    /** Deduct diamonds from storage */
    suspend fun spendDiamonds(amount: Int) =
        dataStoreManager.spendDiamonds(amount)
}
