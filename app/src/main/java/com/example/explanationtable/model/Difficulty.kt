package com.example.explanationtable.model

import androidx.compose.ui.graphics.Color
import com.example.explanationtable.ui.theme.BeakUpper
import com.example.explanationtable.ui.theme.FeatherGreen

/**
 * Enum representing the available difficulty levels.
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

/**
 * Data class encapsulating the color scheme for a given difficulty level.
 *
 * @property backgroundColor The background color for the difficulty.
 * @property dividerColor The divider color for UI components.
 * @property textColor The text color to ensure readability.
 */
data class DifficultyColors(
    val backgroundColor: Color,
    val dividerColor: Color,
    val textColor: Color
)

/**
 * Returns the color scheme associated with the specified difficulty level.
 *
 * This function maps each [Difficulty] to a corresponding [DifficultyColors] instance,
 * ensuring a consistent color palette is used across the application.
 *
 * @param difficulty The difficulty level for which to retrieve colors.
 * @return A [DifficultyColors] object with predefined colors for the given difficulty.
 */
fun difficultyColors(difficulty: Difficulty): DifficultyColors = when (difficulty) {
    Difficulty.EASY -> DifficultyColors(
        backgroundColor = FeatherGreen,         // Background for EASY difficulty
        dividerColor = Color(0xFF47A302),         // Divider color for EASY difficulty
        textColor = Color.White                   // Text color (white for clarity)
    )
    Difficulty.MEDIUM -> DifficultyColors(
        backgroundColor = BeakUpper,             // Background for MEDIUM difficulty
        dividerColor = Color(0xFFE29100),         // Divider color for MEDIUM difficulty
        textColor = Color.White                   // Text color (white for clarity)
    )
    Difficulty.HARD -> DifficultyColors(
        backgroundColor = Color(0xFF14D4F4),      // Background for HARD difficulty
        dividerColor = Color(0xFF008FCC),         // Divider color for HARD difficulty
        textColor = Color.White                   // Text color (white for clarity)
    )
}

/**
 * Immutable mapping of [Difficulty] levels to the corresponding number of steps.
 *
 * This mapping centralizes the configuration for the number of steps required for each difficulty.
 */
val difficultyStepCountMap: Map<Difficulty, Int> = mapOf(
    Difficulty.EASY to 50,
    Difficulty.MEDIUM to 70,
    Difficulty.HARD to 100
)
