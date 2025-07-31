package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Paint
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.White
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// Data class representing the gradient colors used for the button's three layered circles.
data class StageButtonColors(
    // Colors for the background (behind) circle.
    val behindTopLeft: Color,
    val behindBottomRight: Color,
    // Colors for the front circle.
    val frontTopLeft: Color,
    val frontBottomRight: Color,
    // Colors for the inner (smaller) circle.
    val innerTopLeft: Color,
    val innerBottomRight: Color,
)

/**
 * A custom composable button that visually represents a difficulty step.
 *
 * The button comprises three layered circles with diagonal color splits that animate when pressed.
 * The color scheme is determined by the provided [difficulty]. When clicked, the [onClick] callback is triggered.
 *
 * @param difficulty The difficulty level, which dictates the button's color scheme.
 * @param stepNumber The step number displayed on the button (converted to Persian digits).
 * @param onClick Callback invoked upon a successful click gesture.
 * @param enabled If false, the button will not react to clicks and is rendered with reduced opacity.
 */
@Composable
fun DifficultyStepButton(
    difficulty: Difficulty,
    stepNumber: Int,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    // Select the color scheme based on the provided difficulty level.
    val colors = when (difficulty) {
        Difficulty.EASY -> StageButtonColors(
            behindTopLeft = Color(0xFF75B11F),
            behindBottomRight = Color(0xFF63A700),
            frontTopLeft = Color(0xFF95E321),
            frontBottomRight = Color(0xFF87E003),
            innerTopLeft = Color(0xFF88CF1F),
            innerBottomRight = Color(0xFF78C900)
        )
        Difficulty.MEDIUM -> StageButtonColors(
            behindTopLeft = Color(0xFFFFD040),
            behindBottomRight = Color(0xFFFFC100),
            frontTopLeft = Color(0xFFFEEA66),
            frontBottomRight = Color(0xFFFEE333),
            innerTopLeft = Color(0xFFFED540),
            innerBottomRight = Color(0xFFFEC701)
        )
        Difficulty.HARD -> StageButtonColors(
            behindTopLeft = Color(0xFF1E9CD1),
            behindBottomRight = Color(0xFF008FCC),
            frontTopLeft = Color(0xFF5DCBFE),
            frontBottomRight = Color(0xFF46C4FF),
            innerTopLeft = Color(0xFF38BAF8),
            innerBottomRight = Color(0xFF1CB0F6)
        )
    }

    // Constant offset for the behind circle.
    val behindOffset = 7.dp
    val density = LocalDensity.current

    // State to track whether the button is pressed.
    var isPressed by remember { mutableStateOf(false) }

    // Animate the vertical offset for the front circle when pressed.
    val animatedPressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { behindOffset.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30)
    )
    // Convert the animated offset from pixels to dp for positioning the text.
    val animatedPressOffsetDp = with(density) { animatedPressOffsetPx.toDp() }

    // Apply pointer input only if enabled.
    val gestureModifier = if (enabled) {
        Modifier.pointerInput(Unit) {
            coroutineScope {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true

                    val upOrCancel = waitForUpOrCancellation()
                    isPressed = false

                    if (upOrCancel != null) {
                        launch {
                            delay(50) // Allow 30ms animation to complete
                            onClick()
                        }
                    }
                }
            }
        }
    } else {
        Modifier
    }

    // Box layout that holds the button's visual elements.
    Box(
        modifier = gestureModifier
            .size(77.dp),
        contentAlignment = Alignment.Center
    ) {
        // Canvas to draw the three layered circles with diagonal split colors.
        Canvas(modifier = Modifier.matchParentSize()) {
            // Define the dimensions of the main (outer) circle.
            val mainDiameterDp = 70.dp
            val mainDiameterPx = mainDiameterDp.toPx()
            val outerRadius = mainDiameterPx / 2f
            // The inner circle's radius is set to 77% of the outer circle's radius.
            val innerRadius = outerRadius * 0.77f

            // Calculate the canvas center.
            val canvasCenter = center
            // Determine centers for the behind and front circles with their vertical offsets.
            val behindCenter = Offset(canvasCenter.x, canvasCenter.y + behindOffset.toPx())
            val frontCenter = Offset(canvasCenter.x, canvasCenter.y + animatedPressOffsetPx)

            // Define a diagonal clipping path.
            // The path forms a triangle that splits the circle from the top-left to the bottom-right.
            val diagonalPath = Path().apply {
                val halfDiameter = mainDiameterPx / 2f
                val left = canvasCenter.x - halfDiameter
                val top = canvasCenter.y - halfDiameter
                val right = left + mainDiameterPx
                val bottom = top + mainDiameterPx

                moveTo(left, top)     // Top-left corner
                lineTo(right, bottom) // Diagonal to bottom-right corner
                lineTo(right, top)    // Top-right corner
                close()
            }

            // Draw the behind circle (first layer) with an elliptical shape.
            drawSplitCircleNoBorder(
                center = behindCenter,
                width = outerRadius * 2,
                height = outerRadius * 1.8f,
                colorTopLeft = colors.behindTopLeft,
                colorBottomRight = colors.behindBottomRight,
                diagonalPath = diagonalPath
            )

            // Draw the front circle (second layer) using the same dimensions.
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = outerRadius * 2,
                height = outerRadius * 1.8f,
                colorTopLeft = colors.frontTopLeft,
                colorBottomRight = colors.frontBottomRight,
                diagonalPath = diagonalPath
            )

            // Draw the inner circle (third layer) with reduced dimensions.
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = innerRadius * 2,
                height = innerRadius * 1.8f,
                colorTopLeft = colors.innerTopLeft,
                colorBottomRight = colors.innerBottomRight,
                diagonalPath = diagonalPath
            )
        }

        // Render the step number text in the center of the button,
        // adjusting its vertical position based on the press animation.
        Text(
            text = stepNumber.toPersianDigits(),
            style = AppTypography.headlineSmall.copy(
                color = White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = animatedPressOffsetDp)
        )
    }
}

/**
 * Draws an ellipse split diagonally into two distinct color regions.
 *
 * The ellipse is first drawn completely in the bottom-right color, then its top-left
 * region is overdrawn (via clipping with [diagonalPath]) with the top-left color.
 *
 * @param center The center point of the ellipse.
 * @param width The overall width of the ellipse.
 * @param height The overall height of the ellipse.
 * @param colorTopLeft The color for the top-left region.
 * @param colorBottomRight The color for the bottom-right region.
 * @param diagonalPath The clipping path that defines the diagonal split.
 */
private fun DrawScope.drawSplitCircleNoBorder(
    center: Offset,
    width: Float,
    height: Float,
    colorTopLeft: Color,
    colorBottomRight: Color,
    diagonalPath: Path
) {
    // Save the current canvas state into a new drawing layer.
    drawContext.canvas.saveLayer(
        bounds = Rect(Offset.Zero, size),
        paint = Paint()
    )

    // Draw the full ellipse using the bottom-right color.
    drawOval(
        color = colorBottomRight,
        topLeft = Offset(center.x - width / 2, center.y - height / 2),
        size = Size(width, height),
        style = Fill,
        blendMode = BlendMode.Src
    )

    // Clip the drawing area to the diagonal path and overdraw with the top-left color.
    clipPath(diagonalPath, clipOp = ClipOp.Intersect) {
        drawOval(
            color = colorTopLeft,
            topLeft = Offset(center.x - width / 2, center.y - height / 2),
            size = Size(width, height),
            style = Fill,
            blendMode = BlendMode.Src
        )
    }

    // Restore the previous canvas state.
    drawContext.canvas.restore()
}
