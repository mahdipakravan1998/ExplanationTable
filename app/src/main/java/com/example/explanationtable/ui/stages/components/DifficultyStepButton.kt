package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.White
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// ---- Internal constants (documented for clarity) ----
private val BUTTON_BOX_SIZE: Dp = 77.dp          // Canvas box size
private val MAIN_DIAMETER: Dp = 70.dp            // Main ellipse "diameter" baseline
private val BEHIND_OFFSET: Dp = 7.dp             // Static parallax offset for the back layer
private const val PRESS_ANIM_MS: Int = 30        // Press transition duration
// Increased to ensure the release motion completes before navigation starts.
private const val CLICK_DELAY_MS: Long = 120L    // Post-release motion buffer

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
 * Custom, accessible step button for a given [difficulty] and [stepNumber].
 * Visuals identical to original; now uses `clickable` for semantics and stability.
 */
@Composable
fun DifficultyStepButton(
    difficulty: Difficulty,
    stepNumber: Int,
    onClick: () -> Unit = {},
    enabled: Boolean = true
) {
    val latestOnClick by rememberUpdatedState(onClick)
    val clickScope = rememberCoroutineScope()

    // Memoized palette and density-derived px values
    val colors = remember(difficulty) { stageButtonColorsFor(difficulty) }
    val density = LocalDensity.current
    val behindOffsetPx = remember(density) { with(density) { BEHIND_OFFSET.toPx() } }
    val mainDiameterPx = remember(density) { with(density) { MAIN_DIAMETER.toPx() } }
    val canvasBoxSizePx = remember(density) { with(density) { BUTTON_BOX_SIZE.toPx() } }

    // Diagonal split path for top-left triangle over the 70.dp baseline circle inside the 77.dp box
    val diagonalPath = remember(mainDiameterPx, canvasBoxSizePx) {
        Path().apply {
            val half = mainDiameterPx / 2f
            val left = canvasBoxSizePx / 2f - half
            val top = canvasBoxSizePx / 2f - half
            val right = left + mainDiameterPx
            val bottom = top + mainDiameterPx
            moveTo(left, top)
            lineTo(right, bottom)
            lineTo(right, top)
            close()
        }
    }

    // Press state via interaction source (gives us accessibility click semantics via clickable)
    val interaction = remember { MutableInteractionSource() }
    val isPressed by interaction.collectIsPressedAsState()

    val animatedPressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) behindOffsetPx else 0f,
        animationSpec = tween(durationMillis = PRESS_ANIM_MS),
        label = "pressOffset"
    )

    Box(
        modifier = Modifier
            .size(BUTTON_BOX_SIZE)
            // IMPORTANT: Your Compose version expects a String here.
            .semantics { contentDescription = stepNumber.toPersianDigits() }
            .clickable(
                enabled = enabled,
                interactionSource = interaction,
                indication = null // preserve original visuals (no ripple)
            ) {
                // Small motion buffer so the release animation finishes before navigating.
                clickScope.launch {
                    delay(CLICK_DELAY_MS)
                    latestOnClick()
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val outerRadius = mainDiameterPx / 2f
            val innerRadius = outerRadius * 0.77f

            val canvasCenter = center
            val behindCenter = Offset(canvasCenter.x, canvasCenter.y + behindOffsetPx)
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
 * Draws an ellipse split diagonally into two color regions without saveLayer/blends.
 */
private fun DrawScope.drawSplitCircleNoBorder(
    center: Offset,
    width: Float,
    height: Float,
    colorTopLeft: Color,
    colorBottomRight: Color,
    diagonalPath: Path
) {
    drawOval(
        color = colorBottomRight,
        topLeft = Offset(center.x - width / 2, center.y - height / 2),
        size = Size(width, height),
        style = Fill
    )
    clipPath(diagonalPath, clipOp = ClipOp.Intersect) {
        drawOval(
            color = colorTopLeft,
            topLeft = Offset(center.x - width / 2, center.y - height / 2),
            size = Size(width, height),
            style = Fill
        )
    }
}

// Maps difficulty → colors. Extracted to enable stable memoization & single-responsibility.
private fun stageButtonColorsFor(difficulty: Difficulty): StageButtonColors =
    when (difficulty) {
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
