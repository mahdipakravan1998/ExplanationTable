package com.example.explanationtable.ui.gameplay.table.components.shared

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.cells.StackedSquare3D
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign0_1
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign1_0
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign1_2
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign3_2

/**
 * Helper composable to render squares with optional directional signs.
 */
@Composable
fun SquareWithDirectionalSign(
    isDarkTheme: Boolean,
    position: CellPosition,
    shuffledTableData: Map<CellPosition, List<String>>,
    isSelected: Boolean, // New parameter to indicate selection
    handleSquareClick: () -> Unit, // Handle click to select square
    squareSize: Dp = 80.dp,
    signSize: Dp = 16.dp,
    clickable: Boolean = false // New parameter to control clickability
) {
    // Handle StackedSquare3D animation for the square
    val density = LocalDensity.current
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(density) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), // smooth transition
        label = "" // no label needed here
    )

    // Convert to dp for the UI
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    Box(
        modifier = Modifier
            .size(squareSize)
            .pointerInput(Unit) {
                if (clickable) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val upOrCancel = waitForUpOrCancellation()
                        isPressed = false
                        if (upOrCancel != null) {
                            handleSquareClick() // Trigger the square click handler
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        // Render movable cells (StackedSquare3D)
        val letter = shuffledTableData[position]?.joinToString(", ") ?: "?"
        StackedSquare3D(
            isDarkTheme = isDarkTheme,
            letter = letter,
            isSelected = isSelected,
            modifier = Modifier.fillMaxSize()
        )

        // Overlay Directional Signs based on position and apply the animated offset
        when (position) {
            CellPosition(0, 1) -> {
                DirectionalSign0_1(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.TopEnd)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(end = 4.dp, top = 16.dp) // Reduced padding
                )
            }
            CellPosition(1, 0) -> {
                DirectionalSign1_0(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(top = 4.dp)
                )
            }
            CellPosition(1, 2) -> {
                DirectionalSign1_2(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(top = 4.dp)
                )
            }
            CellPosition(3, 2) -> {
                DirectionalSign3_2(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.BottomCenter)
                        .offset(y = pressOffsetDp) // Apply the same animated offset here
                        .padding(bottom = 4.dp)
                )
            }
            // Add more cases if needed
            else -> {
                // No directional sign for other positions
            }
        }
    }
}