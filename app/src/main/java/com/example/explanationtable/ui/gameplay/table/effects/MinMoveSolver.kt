package com.example.explanationtable.ui.gameplay.table.effects

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import com.example.explanationtable.ui.gameplay.table.utils.solveWithAStar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Computes the minimum number of moves for the current scramble by invoking A* off the main thread.
 *
 * @param shuffledMovableData The current scrambled data as a flat list of strings.
 * @param movableData The target (sorted) data as a flat list of strings.
 * @param onResult Callback delivering the computed minimal move count.
 */
@Composable
fun MinMoveSolverEffect(
    shuffledMovableData: List<String>,
    movableData: List<String>,
    onResult: (Int) -> Unit
) {
    LaunchedEffect(shuffledMovableData, movableData) {
        val result = withContext(Dispatchers.Default) {
            solveWithAStar(shuffledMovableData, movableData)
        }
        onResult(result)
    }
}
