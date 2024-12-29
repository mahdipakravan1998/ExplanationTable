// File: Difficulty.kt
package com.example.explanationtable.model

import androidx.compose.ui.graphics.Color

/**
 * Enum representing difficulty levels.
 */
enum class Difficulty {
    EASY,
    MEDIUM,
    HARD
}

/**
 * Data class holding color values for a given difficulty level.
 */
data class DifficultyColors(
    val backgroundColor: Color,
    val dividerColor: Color,
    val textColor: Color
)

/**
 * Returns a [DifficultyColors] object corresponding to the specified difficulty level.
 */
fun difficultyColors(difficulty: Difficulty): DifficultyColors {
    return when (difficulty) {
        Difficulty.EASY -> DifficultyColors(
            backgroundColor = Color(0xFF58CC02),
            dividerColor = Color(0xFF47A302),
            textColor = Color.White
        )
        Difficulty.MEDIUM -> DifficultyColors(
            backgroundColor = Color(0xFFFFC200),
            dividerColor = Color(0xFFE29100),
            textColor = Color.White
        )
        Difficulty.HARD -> DifficultyColors(
            backgroundColor = Color(0xFF14D4F4),
            dividerColor = Color(0xFF008FCC),
            textColor = Color.White
        )
    }
}

/**
 * Centralized mapping of Difficulty to number of steps.
 */
val difficultyStepCountMap = mapOf(
    Difficulty.EASY to 50,
    Difficulty.MEDIUM to 72,
    Difficulty.HARD to 100
)
