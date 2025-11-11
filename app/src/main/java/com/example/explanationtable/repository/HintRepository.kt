package com.example.explanationtable.repository

import android.content.Context
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.data.getHintOptions
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.ui.hint.logic.revealAllCellsLogic
import com.example.explanationtable.ui.hint.logic.revealRandomCategory as revealRandomCategoryLogic
import com.example.explanationtable.ui.hint.logic.revealRandomCell as revealRandomCellLogic
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * Repository for hint-related operations and in-game currency (diamonds).
 *
 * ## Contract
 * - Exposes a **reactive** diamonds balance via [diamondsFlow].
 * - Provides **one-shot** suspend functions for reads/writes that are forced to the IO dispatcher.
 * - Spending diamonds uses [trySpendDiamonds] to ensure **atomic** updates.
 *
 * ## Threading
 * - All blocking operations are wrapped in [withContext] using [io] (default = [Dispatchers.IO]).
 *
 * ## Context safety
 * - Internally resolves and stores [applicationContext] to avoid leaking short-lived contexts.
 *
 * External behavior and method signatures are preserved for compatibility.
 */
class HintRepository(
    context: Context,
    private val io: CoroutineDispatcher = Dispatchers.IO
) {

    // Always keep the applicationContext to avoid accidentally leaking an Activity/Service context.
    private val appContext: Context = context.applicationContext

    // Lazily initialized manager for user preferences and currency persistence.
    // Using appContext ensures DataStore is keyed to the application lifecycle.
    private val dataStoreManager: DataStoreManager by lazy { DataStoreManager(appContext) }

    /** Returns the available hint options, localized via resources. */
    fun getHintOptions(): List<HintOption> = getHintOptions(appContext)

    /** Reveal all cells using pure logic (no IO). */
    fun revealAllCells(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable
    ): List<CellPosition> = revealAllCellsLogic(currentTable, originalTable)

    /** Reveal a random full category (row/column) using pure logic (no IO). */
    fun revealRandomCategory(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable,
        difficulty: Difficulty
    ): List<CellPosition> = revealRandomCategoryLogic(currentTable, originalTable, difficulty)

    /** Reveal a single random cell using pure logic (no IO). */
    fun revealRandomCell(
        currentTable: MutableMap<CellPosition, List<String>>,
        originalTable: LevelTable
    ): List<CellPosition> = revealRandomCellLogic(currentTable, originalTable)

    /** Reactive diamonds balance. Collect from a lifecycle-aware scope in the ViewModel/UI. */
    val diamondsFlow: Flow<Int> = dataStoreManager.diamonds

    /** One-shot read for the current diamonds balance, performed on [io] to avoid main-thread I/O. */
    suspend fun getDiamondCount(): Int = withContext(io) {
        dataStoreManager.getDiamondCount()
    }

    /**
     * Attempts to atomically spend [amount] diamonds.
     *
     * @return `true` if the spend succeeded (balance updated), `false` if insufficient funds.
     * @see spendDiamonds for legacy, non-atomic behavior (kept only for compatibility).
     */
    suspend fun trySpendDiamonds(amount: Int): Boolean = withContext(io) {
        dataStoreManager.trySpendDiamonds(amount)
    }

    /**
     * Legacy non-atomic spend.
     *
     * Prefer [trySpendDiamonds] to guarantee exactly-once semantics and avoid race conditions.
     */
    @Deprecated("Use trySpendDiamonds(amount) for atomic spend with success/failure.")
    suspend fun spendDiamonds(amount: Int) = withContext(io) {
        dataStoreManager.spendDiamonds(amount)
    }
}
