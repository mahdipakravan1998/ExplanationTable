package com.example.explanationtable.model

/**
 * Represents a hint option with a display text and a fee map that associates a difficulty level
 * with a corresponding fee amount.
 *
 * @property displayText The text that will be displayed for the hint option.
 * @property feeMap A map that associates each difficulty level with a fee amount.
 */
data class HintOption(
    val displayText: String, // The text displayed for the hint option
    val feeMap: Map<Difficulty, Int> // A map of difficulty levels to associated fees
)
