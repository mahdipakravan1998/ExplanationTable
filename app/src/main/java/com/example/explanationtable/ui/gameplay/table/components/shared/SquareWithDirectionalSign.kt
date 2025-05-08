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
import com.example.explanationtable.ui.gameplay.table.components.directions.DirectionalSign0_1
import com.example.explanationtable.ui.gameplay.table.components.directions.DirectionalSign1_0
import com.example.explanationtable.ui.gameplay.table.components.directions.DirectionalSign1_2
import com.example.explanationtable.ui.gameplay.table.components.directions.DirectionalSign3_2
import kotlinx.coroutines.delay

/**
 * Renders a square with optional directional signs and animations.
 *
 * @param isDarkTheme Indicates if the dark theme is active.
 * @param position The cell's position used to determine which directional sign to display.
 * @param shuffledTableData Optional mapping of cell positions to letter data.
 * @param isSelected True if the square is selected.
 * @param handleSquareClick Callback invoked when the square is clicked.
 * @param squareSize The size of the square (default is 80.dp).
 * @param signSize The size of the directional sign (default is 16.dp).
 * @param clickable Determines if the square should respond to click gestures.
 * @param isCorrect Indicates if the square is marked as correct.
 * @param isTransitioning Indicates if the square is transitioning into a correct state.
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
    // Retrieve current density for converting between dp and pixels.
    val density = LocalDensity.current

    // State variable to track if the square is pressed.
    var isPressed by remember { mutableStateOf(false) }

    // Animate the vertical offset (in pixels) for the press effect.
    val pressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30),
        label = ""
    )
    // Convert the animated offset from pixels to dp.
    val pressOffsetDp = with(density) { pressOffsetPx.toDp() }

    // State for the scaling effect triggered by a click.
    var currentScale by remember { mutableFloatStateOf(1f) }
    // Animate the click scale effect.
    val scaleAnimation by animateFloatAsState(
        targetValue = currentScale,
        animationSpec = tween(durationMillis = 100, easing = FastOutLinearInEasing),
        label = ""
    )

    // Animate an additional scale effect for the transitioning state.
    val transitionScale by animateFloatAsState(
        targetValue = if (isTransitioning) 1.05f else 1f,
        animationSpec = tween(durationMillis = 100, easing = FastOutSlowInEasing),
        label = ""
    )

    // Counter used to trigger a reset of the click scale effect after each click.
    val clickCounter = remember { mutableIntStateOf(0) }

    // After a click (scale set to 1.05f), delay briefly and reset the scale to 1f.
    LaunchedEffect(clickCounter.intValue) {
        if (currentScale == 1.05f) {
            delay(50)
            currentScale = 1f
        }
    }

    // Define a pointer input modifier to handle click gestures only if the square is clickable.
    val gestureModifier = if (clickable) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                // Wait for the initial down press.
                awaitFirstDown()
                isPressed = true

                // Wait until the pointer is lifted or the gesture is cancelled.
                val upOrCancel = waitForUpOrCancellation()
                isPressed = false

                // Apply the scaling effect and update the click counter.
                currentScale = 1.05f
                clickCounter.value++

                // If the gesture was completed (not cancelled), invoke the click handler.
                if (upOrCancel != null) {
                    handleSquareClick()
                }
            }
        }
    } else Modifier

    // Retrieve the letter(s) for the current cell; default to "?" if no data is available.
    val letter = shuffledTableData?.get(position)?.joinToString(", ") ?: "?"

    // Main container for the square and its directional sign.
    Box(
        modifier = Modifier
            .size(squareSize)
            .scale(scaleAnimation * transitionScale)
            .then(gestureModifier),
        contentAlignment = Alignment.Center
    ) {
        // Render the square with the appropriate style based on its correctness.
        if (isCorrect) {
            BrightGreenSquare(
                letter = letter,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            StackedSquare3D(
                isDarkTheme = isDarkTheme,
                letter = letter,
                isSelected = isSelected,
                isTransitioningToCorrect = isTransitioning,
                modifier = Modifier.fillMaxSize()
            )
        }

        // Conditionally render a directional sign based on the cell's position.
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
            else -> {
                // No directional sign is rendered for other cell positions.
            }
        }
    }
}
