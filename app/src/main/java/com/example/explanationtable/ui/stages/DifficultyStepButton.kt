// File: DifficultyStepButton.kt
package com.example.explanationtable.ui.stages

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.model.Difficulty

/**
 * A data class holding the 6 colors we need to draw one "split circle button."
 *
 * We have 3 circles, each split diagonally into a top-left color and a bottom-right color.
 */
data class StageButtonColors(
    // circle #1 (behind) colors
    val behindTopLeft: Color,
    val behindBottomRight: Color,

    // circle #2 (front) colors
    val frontTopLeft: Color,
    val frontBottomRight: Color,

    // circle #3 (inner/smaller) colors
    val innerTopLeft: Color,
    val innerBottomRight: Color,
)

/**
 * A composable that draws the 3-circle split button
 * with color sets determined by the [difficulty].
 *
 * @param difficulty The current difficulty level.
 * @param stepNumber The number of the step to display on the button.
 * @param onClick Lambda to handle click events.
 */
@Composable
fun DifficultyStepButton(
    difficulty: Difficulty,
    stepNumber: Int,
    onClick: () -> Unit = {}
) {
    // 1) Choose the correct color set based on the difficulty
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
            behindTopLeft = Color(0xFFFFD040),  // #ffd040
            behindBottomRight = Color(0xFFFFC100),  // #ffc100

            frontTopLeft = Color(0xFFFEEA66),  // #feea66
            frontBottomRight = Color(0xFFFEE333),  // #fee333

            innerTopLeft = Color(0xFFFED540),  // #fed540
            innerBottomRight = Color(0xFFFEC701)   // #fec701
        )

        Difficulty.HARD -> StageButtonColors(
            behindTopLeft = Color(0xFF1E9CD1),  // #1e9cd1
            behindBottomRight = Color(0xFF008FCC),  // #008fcc

            frontTopLeft = Color(0xFF5DCBFE),  // #5dcbfe
            frontBottomRight = Color(0xFF46C4FF),  // #46c4ff

            innerTopLeft = Color(0xFF38BAF8),  // #38baf8
            innerBottomRight = Color(0xFF1CB0F6)   // #1cb0f6
        )
    }

    // 2) Draw the 3-circle shape with these colors
    Box(
        modifier = Modifier
            .size(115.dp)
            .clickable { onClick() }, // Make the button clickable
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val shapeDp = 100.dp
            val shapePx = shapeDp.toPx()
            val outerRadius = shapePx / 2f

            // Downward offset for the behind circle
            val behindOffsetY = 15f

            val canvasCenter = center
            val behindCenter = Offset(canvasCenter.x, canvasCenter.y + behindOffsetY)

            // Build the diagonal path that splits the circle
            val diagonalPath = Path().apply {
                val leftX = canvasCenter.x - (shapePx / 2)
                val topY = canvasCenter.y - (shapePx / 2)
                val rightX = leftX + shapePx
                val bottomY = topY + shapePx

                moveTo(leftX, topY)         // top-left
                lineTo(rightX, bottomY)     // bottom-right
                lineTo(rightX, topY)        // top-right
                close()
            }

            // Circle #1 (behind)
            drawSplitCircleNoBorder(
                center = behindCenter,
                radius = outerRadius,
                colorTopLeft = colors.behindTopLeft,
                colorBottomRight = colors.behindBottomRight,
                diagonalPath = diagonalPath
            )

            // Circle #2 (front, largest)
            drawSplitCircleNoBorder(
                center = canvasCenter,
                radius = outerRadius,
                colorTopLeft = colors.frontTopLeft,
                colorBottomRight = colors.frontBottomRight,
                diagonalPath = diagonalPath
            )

            // Circle #3 (inner/smaller, on top)
            val innerRadius = outerRadius * 0.77f
            drawSplitCircleNoBorder(
                center = canvasCenter,
                radius = innerRadius,
                colorTopLeft = colors.innerTopLeft,
                colorBottomRight = colors.innerBottomRight,
                diagonalPath = diagonalPath
            )
        }

        // Overlay step number centered within the inner circle
        Text(
            text = stepNumber.toString(),
            color = Color.White,
            fontSize = 36.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.align(Alignment.Center)
        )
    }
}

/**
 * Utility function to draw a split circle without borders.
 */
fun DrawScope.drawSplitCircleNoBorder(
    center: Offset,
    radius: Float,
    colorTopLeft: Color,
    colorBottomRight: Color,
    diagonalPath: Path
) {
    drawContext.canvas.saveLayer(
        bounds = Rect(Offset.Zero, size),
        paint = Paint()
    )

    // Step 1: Draw entire circle in bottom-right color
    drawCircle(
        color = colorBottomRight,
        center = center,
        radius = radius,
        style = Fill,
        blendMode = BlendMode.Src
    )

    // Step 2: Clip to the diagonal path (the top-left region)
    clipPath(diagonalPath, clipOp = ClipOp.Intersect) {
        // Step 3: Overwrite top-left with colorTopLeft
        drawCircle(
            color = colorTopLeft,
            center = center,
            radius = radius,
            style = Fill,
            blendMode = BlendMode.Src
        )
    }

    drawContext.canvas.restore()
}

@Preview(showBackground = true)
@Composable
fun PreviewDifficultyStepButton() {
    DifficultyStepButton(
        difficulty = Difficulty.EASY,
        stepNumber = 1
    )
}
