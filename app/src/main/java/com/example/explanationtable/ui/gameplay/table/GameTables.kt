package com.example.explanationtable.ui.gameplay.table

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.gameplay.table.components.layout.EasyThreeByFiveTable
import com.example.explanationtable.ui.gameplay.table.components.layout.HardTablePlaceholder
import com.example.explanationtable.ui.gameplay.table.components.layout.MediumTablePlaceholder

/**
 * Data class representing a cell's position in the table.
 */
data class CellPosition(val row: Int, val col: Int)

/**
 * Composable function to display the game table.
 */
@Composable
fun GameTable(
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    stageNumber: Int,
    modifier: Modifier = Modifier
) {
    when (difficulty) {
        Difficulty.EASY -> EasyThreeByFiveTable(isDarkTheme, stageNumber, modifier)
        Difficulty.MEDIUM -> MediumTablePlaceholder(isDarkTheme, modifier)
        Difficulty.HARD -> HardTablePlaceholder(isDarkTheme, modifier)
    }
}
