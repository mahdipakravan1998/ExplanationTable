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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
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
import androidx.compose.ui.geometry.Size

data class StageButtonColors(
    // Circle #1 (behind) colors
    val behindTopLeft: Color,
    val behindBottomRight: Color,

    // Circle #2 (front) colors
    val frontTopLeft: Color,
    val frontBottomRight: Color,

    // Circle #3 (inner/smaller) colors
    val innerTopLeft: Color,
    val innerBottomRight: Color,
)

@Composable
fun DifficultyStepButton(
    difficulty: Difficulty,
    stepNumber: Int,
    onClick: () -> Unit = {}
) {
    // 1) Pick colors based on difficulty
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

    // 2) Track a pressed state + animate
    // The behind circle is permanently offset by 7.dp
    val behindOffsetY = 7.dp

    val density = LocalDensity.current

    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(density) { behindOffsetY.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), label = "" // short, snappy animation
    )

    // Convert press offset to dp for text
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    // 3) Pointer input for immediate press detection
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            // Finger down => pressed = true
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait for finger up or cancel => pressed = false
            val upOrCancel = waitForUpOrCancellation()
            isPressed = false

            // If the user actually lifted (not canceled), it's a click
            if (upOrCancel != null) {
                onClick()
            }
        }
    }

    // 4) Draw everything in a Box
    Box(
        modifier = gestureModifier.size(82.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Main circle diameter (in dp -> px)
            val shapeDp = 75.dp
            val shapePx = shapeDp.toPx()
            val outerRadius = shapePx / 2f
            val innerRadius = outerRadius * 0.77f

            // Canvas center
            val canvasCenter = center

            // Offsets for each circle
            val behindCenter = Offset(canvasCenter.x, canvasCenter.y + behindOffsetY.toPx())
            val frontCenter = Offset(canvasCenter.x, canvasCenter.y + pressOffsetY)

            // 5) SINGLE diagonal path for all circles (ensures alignment):
            //    We'll define it once, using the main circle's bounding box
            //    around the *canvas* center (the original approach).
            val diagonalPath = Path().apply {
                val leftX = canvasCenter.x - (shapePx / 2)
                val topY = canvasCenter.y - (shapePx / 2)
                val rightX = leftX + shapePx
                val bottomY = topY + shapeDp.toPx()

                moveTo(leftX, topY)         // top-left
                lineTo(rightX, bottomY)     // bottom-right
                lineTo(rightX, topY)        // top-right
                close()
            }

            // Circle #1 (behind)
            drawSplitCircleNoBorder(
                center = behindCenter,
                width = outerRadius * 2, // Full width (can be customized)
                height = outerRadius * 1.8f, // Slightly smaller height for an elliptical effect
                colorTopLeft = colors.behindTopLeft,
                colorBottomRight = colors.behindBottomRight,
                diagonalPath = diagonalPath
            )

            // Circle #2 (front / largest)
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = outerRadius * 2, // Full width
                height = outerRadius * 1.8f, // Slightly smaller height
                colorTopLeft = colors.frontTopLeft,
                colorBottomRight = colors.frontBottomRight,
                diagonalPath = diagonalPath
            )

            // Circle #3 (inner / smaller)
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = innerRadius * 2, // Full width of inner circle
                height = innerRadius * 1.8f, // Slightly smaller height
                colorTopLeft = colors.innerTopLeft,
                colorBottomRight = colors.innerBottomRight,
                diagonalPath = diagonalPath
            )

        }

        // Step number text (moves with the front circle)
        Text(
            text = stepNumber.toPersianDigits(),
            style = AppTypography.headlineMedium.copy(
                color = White,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = pressOffsetDp)
        )
    }
}

/**
 * Draws one circle, split diagonally by [diagonalPath] into two colors:
 * [colorTopLeft] and [colorBottomRight].
 *
 * Because we reuse the same [diagonalPath] for all circles, the color-splitting
 * line is consistent across all circles.
 */
private fun DrawScope.drawSplitCircleNoBorder(
    center: Offset,
    width: Float,  // Custom width for the ellipse
    height: Float, // Custom height for the ellipse
    colorTopLeft: Color,
    colorBottomRight: Color,
    diagonalPath: Path
) {
    drawContext.canvas.saveLayer(
        bounds = Rect(Offset.Zero, size),
        paint = Paint()
    )

    // 1) Draw entire ellipse in the "bottom-right" color
    drawOval(
        color = colorBottomRight,
        topLeft = Offset(center.x - width / 2, center.y - height / 2), // Adjusted for ellipse center
        size = Size(width, height), // Custom width and height for the ellipse
        style = Fill,
        blendMode = BlendMode.Src
    )

    // 2) Clip to the diagonal path => top-left region
    clipPath(diagonalPath, clipOp = ClipOp.Intersect) {
        // 3) Overwrite top-left region with the "top-left" color
        drawOval(
            color = colorTopLeft,
            topLeft = Offset(center.x - width / 2, center.y - height / 2), // Adjusted for ellipse center
            size = Size(width, height), // Custom width and height for the ellipse
            style = Fill,
            blendMode = BlendMode.Src
        )
    }

    drawContext.canvas.restore()
}
