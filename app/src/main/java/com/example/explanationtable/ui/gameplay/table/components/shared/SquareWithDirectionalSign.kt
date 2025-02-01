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
import androidx.compose.runtime.mutableIntStateOf
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
    shuffledTableData: Map<CellPosition, List<String>>?,
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

    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(density) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), label = ""
    )
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    var scale by remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing), label = ""
    )

    val transitionScale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.05f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing), label = ""
    )

    val clickQueue = remember { mutableIntStateOf(0) }

    LaunchedEffect(clickQueue.intValue) {
        if (scale == 1.05f) {
            delay(50)
            scale = 1f
        }
    }

    Box(
        modifier = Modifier
            .size(squareSize)
            .scale(scaleAnimation * transitionScale)
            .pointerInput(Unit) {
                if (clickable) {
                    awaitEachGesture {
                        awaitFirstDown()
                        isPressed = true
                        val upOrCancel = waitForUpOrCancellation()
                        isPressed = false

                        scale = 1.05f
                        clickQueue.value += 1

                        if (upOrCancel != null) {
                            handleSquareClick()
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        val letter = shuffledTableData?.get(position)?.joinToString(", ") ?: "?"

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
