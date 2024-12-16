package com.example.explanationtable.ui.modifiers

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Inner shadow modifier applied to all sides of a shape.
 */
fun Modifier.innerShadowAllSides(
    color: Color = Color.Black.copy(alpha = 0.23f),
    blur: Dp = 7.dp,
    spread: Dp = 4.dp,
    shape: Shape = RoundedCornerShape(6.dp)
) = this.then(
    Modifier.drawWithContent {
        drawContent()
        val blurPx = blur.toPx()
        val spreadPx = spread.toPx()

        val outline = shape.createOutline(size, layoutDirection, this)
        val path = Path()
        when (outline) {
            is Outline.Rectangle -> path.addRect(outline.rect)
            is Outline.Rounded   -> path.addRoundRect(outline.roundRect)
            is Outline.Generic   -> path.addPath(outline.path)
        }

        clipPath(path) {
            val topShadow = Brush.verticalGradient(
                colors = listOf(color, Color.Transparent),
                startY = 0f,
                endY = blurPx
            )
            val bottomShadow = Brush.verticalGradient(
                colors = listOf(Color.Transparent, color),
                startY = size.height - (blurPx + spreadPx),
                endY = size.height
            )
            val leftShadow = Brush.horizontalGradient(
                colors = listOf(color, Color.Transparent),
                startX = 0f,
                endX = blurPx
            )
            val rightShadow = Brush.horizontalGradient(
                colors = listOf(Color.Transparent, color),
                startX = size.width - (blurPx + spreadPx),
                endX = size.width
            )
            // Draw top shadow
            drawRect(
                brush = topShadow,
                size = Size(size.width, blurPx + spreadPx)
            )
            // Draw bottom shadow
            drawRect(
                brush = bottomShadow,
                topLeft = Offset(0f, size.height - (blurPx + spreadPx)),
                size = Size(size.width, blurPx + spreadPx)
            )
            // Draw left shadow
            drawRect(
                brush = leftShadow,
                size = Size(blurPx + spreadPx, size.height)
            )
            // Draw right shadow
            drawRect(
                brush = rightShadow,
                topLeft = Offset(size.width - (blurPx + spreadPx), 0f),
                size = Size(blurPx + spreadPx, size.height)
            )
        }
    }
)
