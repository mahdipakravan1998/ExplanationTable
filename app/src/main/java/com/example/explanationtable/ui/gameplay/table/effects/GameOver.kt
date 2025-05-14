package com.example.explanationtable.ui.gameplay.table.effects

import androidx.compose.runtime.*
import com.example.explanationtable.domain.usecase.calculateFallbackAccuracy

@Composable
fun GameOverEffect(
    correctlyPlacedCount: Int,
    totalMovable: Int,
    isGameOver: Boolean,
    setIsGameOver: (Boolean) -> Unit,
    gameStartTime: Long,
    getMinMoves: () -> Int?,
    correctMoveCount: Int,
    incorrectMoveCount: Int,
    playerMoves: Int,
    onGameComplete: (optimalMoves: Int, userAccuracy: Int, playerMoves: Int, elapsedTime: Long) -> Unit
) {
    LaunchedEffect(correctlyPlacedCount) {
        if (!isGameOver && correctlyPlacedCount == totalMovable) {
            setIsGameOver(true)
            val gameEndTime = System.currentTimeMillis()
            val elapsedTime = gameEndTime - gameStartTime
            val optimalMoves = getMinMoves() ?: 0
            val fallbackAccuracy = calculateFallbackAccuracy(correctMoveCount, incorrectMoveCount)
            onGameComplete(optimalMoves, fallbackAccuracy, playerMoves, elapsedTime)
        }
    }
}
