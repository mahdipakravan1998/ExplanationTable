package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.Immutable
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.White
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Immutable
data class StageButtonColors(
    val behindTopLeft: Color,
    val behindBottomRight: Color,
    val frontTopLeft: Color,
    val frontBottomRight: Color,
    val innerTopLeft: Color,
    val innerBottomRight: Color,
)

/**
 * Custom composable button for a difficulty step.
 * Visuals unchanged; GPU work reduced (no saveLayer/blends), and animated text uses graphicsLayer.
 */
@Composable
fun DifficultyStepButton(
    difficulty: Difficulty,
    stepNumber: Int,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
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

    val behindOffset = 7.dp
    val density = LocalDensity.current
    val behindOffsetPxStatic = with(density) { behindOffset.toPx() }
    val mainDiameterPx = with(density) { 70.dp.toPx() }
    val canvasBoxSizePx = with(density) { 77.dp.toPx() }

    // Cache the diagonal split path (top-left triangle on the 70.dp circle)
    val diagonalPath = remember(mainDiameterPx, canvasBoxSizePx) {
        Path().apply {
            val halfDiameter = mainDiameterPx / 2f
            val left = canvasBoxSizePx / 2f - halfDiameter
            val top = canvasBoxSizePx / 2f - halfDiameter
            val right = left + mainDiameterPx
            val bottom = top + mainDiameterPx
            moveTo(left, top)     // Top-left corner
            lineTo(right, bottom) // Diagonal to bottom-right corner
            lineTo(right, top)    // Top-right corner
            close()
        }
    }

    var isPressed by remember { mutableStateOf(false) }

    val animatedPressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) behindOffsetPxStatic else 0f,
        animationSpec = tween(durationMillis = 30)
    )

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
    } else Modifier

    Box(
        modifier = gestureModifier.size(77.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val outerRadius = mainDiameterPx / 2f
            val innerRadius = outerRadius * 0.77f

            val canvasCenter = center
            val behindCenter = Offset(canvasCenter.x, canvasCenter.y + behindOffsetPxStatic)
            val frontCenter = Offset(canvasCenter.x, canvasCenter.y + animatedPressOffsetPx)

            // Behind ellipse
            drawSplitCircleNoBorder(
                center = behindCenter,
                width = outerRadius * 2,
                height = outerRadius * 1.8f,
                colorTopLeft = colors.behindTopLeft,
                colorBottomRight = colors.behindBottomRight,
                diagonalPath = diagonalPath
            )

            // Front ellipse
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = outerRadius * 2,
                height = outerRadius * 1.8f,
                colorTopLeft = colors.frontTopLeft,
                colorBottomRight = colors.frontBottomRight,
                diagonalPath = diagonalPath
            )

            // Inner ellipse
            drawSplitCircleNoBorder(
                center = frontCenter,
                width = innerRadius * 2,
                height = innerRadius * 1.8f,
                colorTopLeft = colors.innerTopLeft,
                colorBottomRight = colors.innerBottomRight,
                diagonalPath = diagonalPath
            )
        }

        // Text translated on GPU (no layout thrash)
        Text(
            text = stepNumber.toPersianDigits(),
            style = AppTypography.headlineSmall.copy(color = White, fontWeight = FontWeight.Bold),
            modifier = Modifier
                .align(Alignment.Center)
                .graphicsLayer { translationY = animatedPressOffsetPx }
        )
    }
}

/**
 * Draws an ellipse split diagonally into two distinct color regions without saveLayer/blends.
 * We first paint the bottom-right color, then clip to the diagonal and overdraw the top-left.
 */
private fun DrawScope.drawSplitCircleNoBorder(
    center: Offset,
    width: Float,
    height: Float,
    colorTopLeft: Color,
    colorBottomRight: Color,
    diagonalPath: Path
) {
    // Full ellipse in bottom-right color
    drawOval(
        color = colorBottomRight,
        topLeft = Offset(center.x - width / 2, center.y - height / 2),
        size = Size(width, height),
        style = Fill
    )

    // Overdraw the top-left portion via clipPath (identical visual to prior saveLayer+Src)
    clipPath(diagonalPath, clipOp = ClipOp.Intersect) {
        drawOval(
            color = colorTopLeft,
            topLeft = Offset(center.x - width / 2, center.y - height / 2),
            size = Size(width, height),
            style = Fill
        )
    }
}
