package com.example.explanationtable.domain.usecase

// Fallback accuracy function: calculates a score (0-10) based on the ratio of incorrect to correct moves.
fun calculateFallbackAccuracy(correctMoves: Int, incorrectMoves: Int): Int {
    if (correctMoves == 0) return 0 // Avoid division by zero.
    val ratio = incorrectMoves.toFloat() / correctMoves.toFloat()
    val score = (10f / (1 + ratio)).toInt()
    return score.coerceIn(0, 10)
}