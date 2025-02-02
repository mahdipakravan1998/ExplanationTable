package com.example.explanationtable.ui.gameplay.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.gameplay.table.components.layout.EasyThreeByFiveTable
import com.example.explanationtable.ui.gameplay.table.components.layout.HardTablePlaceholder
import com.example.explanationtable.ui.gameplay.table.components.layout.MediumTablePlaceholder

/**
 * Represents the position of a cell within the table layout.
 *
 * @property row The row index of the cell.
 * @property col The column index of the cell.
 */
data class CellPosition(val row: Int, val col: Int)

/**
 * Displays the game table based on the selected difficulty level.
 *
 * This composable delegates to specific layout components according to the difficulty:
 * - For [Difficulty.EASY]: Renders a fixed 3x5 table layout.
 * - For [Difficulty.MEDIUM]: Renders a placeholder layout for medium difficulty.
 * - For [Difficulty.HARD]: Renders a placeholder layout for hard difficulty.
 *
 * @param isDarkTheme Flag indicating whether the dark theme is active.
 * @param difficulty The game difficulty level.
 * @param stageNumber The current stage number (used for the easy layout).
 * @param modifier Optional [Modifier] for styling and layout adjustments.
 */
@Composable
fun GameTable(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    when (difficulty) {
        Difficulty.EASY -> {
            // Render the easy level table with a fixed 3x5 layout and stage number.
            EasyThreeByFiveTable(isDarkTheme, stageNumber, modifier)
        }
        Difficulty.MEDIUM -> {
            // Render a placeholder layout for medium difficulty.
            MediumTablePlaceholder(isDarkTheme, modifier)
        }
        Difficulty.HARD -> {
            // Render a placeholder layout for hard difficulty.
            HardTablePlaceholder(isDarkTheme, modifier)
        }
    }
}
