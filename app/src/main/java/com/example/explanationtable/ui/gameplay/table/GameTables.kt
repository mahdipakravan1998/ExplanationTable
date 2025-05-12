package com.example.explanationtable.ui.gameplay.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.layout.EasyThreeByFiveTable
import com.example.explanationtable.ui.gameplay.table.layout.HardTablePlaceholder
import com.example.explanationtable.ui.gameplay.table.layout.MediumTablePlaceholder

/**
 * Composable function that renders the game table layout based on the selected difficulty.
 *
 * The layout component is chosen as follows:
 * - [Difficulty.EASY]: Displays a fixed 3x5 table layout using [EasyThreeByFiveTable].
 * - [Difficulty.MEDIUM]: Displays a placeholder layout for medium difficulty via [MediumTablePlaceholder].
 * - [Difficulty.HARD]: Displays a placeholder layout for hard difficulty via [HardTablePlaceholder].
 *
 * @param isDarkTheme Indicates whether the dark theme is active.
 * @param difficulty Current game difficulty level.
 * @param stageNumber Current stage number; applicable only for the easy layout.
 * @param modifier Optional modifier for styling and layout adjustments.
 * @param onGameComplete Optional callback invoked when the game is completed.
 *        It now provides four parameters:
 *         - optimalMoves: The optimal (minimum) moves computed by A*.
 *         - userAccuracy: The user's accuracy score computed from move tracking.
 *         - playerMoves: Total number of moves made by the player.
 *         - elapsedTime: The elapsed time of the game.
 */
@Composable
fun GameTable(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageNumber: Int,
    modifier: Modifier = Modifier,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit = { _, _, _, _ -> },
    onTableDataInitialized: (originalTableData: EasyLevelTable, currentTableData: MutableMap<CellPosition, List<String>>) -> Unit = { _, _ -> },
    registerCellsCorrectlyPlacedCallback: ((List<CellPosition>) -> Unit) -> Unit = {}
) {
    when (difficulty) {
        // For easy difficulty, render the fixed 3x5 table layout.
        Difficulty.EASY -> EasyThreeByFiveTable(
            isDarkTheme = isDarkTheme,
            stageNumber = stageNumber,
            modifier = modifier,
            onGameComplete = onGameComplete,
            onTableDataInitialized = onTableDataInitialized,
            registerCellsCorrectlyPlacedCallback = registerCellsCorrectlyPlacedCallback
        )
        // For medium difficulty, render a placeholder layout.
        Difficulty.MEDIUM -> MediumTablePlaceholder(
            isDarkTheme = isDarkTheme,
            modifier = modifier
        )
        // For hard difficulty, render a placeholder layout.
        Difficulty.HARD -> HardTablePlaceholder(
            isDarkTheme = isDarkTheme,
            modifier = modifier
        )
    }
}
