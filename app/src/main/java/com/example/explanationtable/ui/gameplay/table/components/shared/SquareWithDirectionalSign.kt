package com.example.explanationtable.ui.gameplay.table.components.shared

import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.gameplay.table.components.cells.BrightGreenSquare
import com.example.explanationtable.ui.gameplay.table.components.cells.StackedSquare3D
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign0_1
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign1_0
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign1_2
import com.example.explanationtable.ui.gameplay.table.components.cells.directions.DirectionalSign3_2
import kotlinx.coroutines.delay

/**
 * Renders squares with optional directional signs and animations.
 */
@Composable
fun SquareWithDirectionalSign(
    isDarkTheme: Boolean,
    position: CellPosition,
    shuffledTableData: Map<CellPosition, List<String>>,
    isSelected: Boolean,
    handleSquareClick: () -> Unit,
    squareSize: Dp = 80.dp,
    signSize: Dp = 16.dp,
    clickable: Boolean = false,
    isCorrect: Boolean = false,
    isTransitioning: Boolean = false
) {
    val density = LocalDensity.current
    var isPressed by remember { mutableStateOf(false) }

    // Animated vertical offset for the press effect
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(density) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30)
    )
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    // Scale for explosion effect
    var scale by remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 120, easing = FastOutLinearInEasing)
    )

    // Transition scale when transitioning to correct state
    val transitionScale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.15f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing)
    )

    val shouldReturnToOriginalSize = remember { mutableStateOf(false) }

    // Trigger return to original size after enlargement
    LaunchedEffect(shouldReturnToOriginalSize.value) {
        if (shouldReturnToOriginalSize.value) {
            delay(50) // Short delay before resetting size
            scale = 1f
            shouldReturnToOriginalSize.value = false
        }
    }

    Box(
        modifier = Modifier
            .size(squareSize)
            .scale(scaleAnimation * transitionScale) // Apply combined scaling
            .pointerInput(Unit) {
                if (clickable) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val upOrCancel = waitForUpOrCancellation()
                        isPressed = false
                        scale = 1.1f // Enlarge button on release
                        shouldReturnToOriginalSize.value = true
                        if (upOrCancel != null) handleSquareClick()
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val letter = shuffledTableData[position]?.joinToString(", ") ?: "?"

        // Render appropriate square based on correctness
        if (isCorrect) {
            BrightGreenSquare(letter = letter, modifier = Modifier.fillMaxSize())
        } else {
            StackedSquare3D(
                isDarkTheme = isDarkTheme,
                letter = letter,
                isSelected = isSelected,
                isTransitioningToCorrect = isTransitioning,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Render directional signs based on cell position
        when (position) {
            CellPosition(0, 1) -> {
                DirectionalSign0_1(
                    isDarkTheme = isDarkTheme,
                    isOnCorrectSquare = isCorrect,
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.TopEnd)
                        .offset(y = pressOffsetDp)
                        .padding(end = 4.dp, top = 16.dp)
                )
            }
            CellPosition(1, 0) -> {
                DirectionalSign1_0(
                    isDarkTheme = isDarkTheme,
                    isOnCorrectSquare = isCorrect,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp)
                        .padding(top = 4.dp)
                )
            }
            CellPosition(1, 2) -> {
                DirectionalSign1_2(
                    isDarkTheme = isDarkTheme,
                    isOnCorrectSquare = isCorrect,
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .offset(y = pressOffsetDp)
                        .padding(top = 4.dp)
                )
            }
            CellPosition(3, 2) -> {
                DirectionalSign3_2(
                    isDarkTheme = isDarkTheme,
                    isOnCorrectSquare = isCorrect,
                    modifier = Modifier
                        .size(signSize)
                        .align(Alignment.BottomCenter)
                        .offset(y = pressOffsetDp)
                        .padding(bottom = 4.dp)
                )
            }
            else -> { /* No directional sign for other positions */ }
        }
    }
}
