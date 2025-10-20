package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text

/**
 * Rounded rectangle (R=18.dp) + bottom-centered triangle (base=20.dp, height=10.dp) as one unified shape,
 * with a 2.dp border and a slot for content inside the rectangle body.
 *
 * Sizing:
 * - Rectangle body (excluding triangle) respects aspect ratio 129.25 : 45 (W:H).
 * - Triangle height is added below the rectangle.
 *
 * Colors:
 * - isDarkTheme -> fill = BackgroundDark, stroke = BorderDark
 * - else        -> fill = BackgroundLight, stroke = BorderLight
 *
 * Content:
 * - The content slot is clipped to the rectangle body (not including the triangle).
 * - Use [contentPadding] to keep text comfortably inside the rounded rect.
 */
@Composable
fun CalloutBubble(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    cornerRadius: Dp = 18.dp,
    triangleBase: Dp = 20.dp,
    triangleHeight: Dp = 10.dp,
    borderWidth: Dp = 2.dp,
    rectAspectRatio: Float = 129.25f / 45f,
    BackgroundDark: Color = Color(0xFF121212),
    BackgroundLight: Color = Color(0xFFFFFFFF),
    BorderDark: Color = Color(0xFF2A2A2A),
    BorderLight: Color = Color(0xFFE0E0E0),
    contentPadding: Dp = 12.dp,
    contentColorOnDark: Color = Color(0xFFFFFFFF),
    contentColorOnLight: Color = Color(0xDE000000),
    content: @Composable () -> Unit = {}
) {
    val fillColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val strokeColor = if (isDarkTheme) BorderDark else BorderLight
    val contentColor = if (isDarkTheme) contentColorOnDark else contentColorOnLight

    androidx.compose.ui.layout.SubcomposeLayout(modifier = modifier) { constraints ->
        val defaultWidthPx = 200.dp.roundToPx()
        val hasBoundedWidth = constraints.maxWidth < androidx.compose.ui.unit.Constraints.Infinity
        val widthPx = if (hasBoundedWidth) constraints.maxWidth else kotlin.math.max(constraints.minWidth, defaultWidthPx)

        val rectHeightPx = (widthPx / rectAspectRatio).toInt()
        val totalHeightPx = rectHeightPx + triangleHeight.roundToPx()
        val widthConstraints = androidx.compose.ui.unit.Constraints.fixed(widthPx, totalHeightPx)

        val rectHeightDp = rectHeightPx.toDp()

        val placeables = subcompose("bubble") {
            Box(Modifier.fillMaxSize()) {
                // === SHAPE LAYER ===
                Canvas(modifier = Modifier.matchParentSize()) {
                    val bw = borderWidth.toPx()
                    val halfBw = bw / 2f

                    val r = cornerRadius.toPx()
                    val triBasePx = triangleBase.toPx()
                    val triHeightPx = triangleHeight.toPx()

                    val w = size.width
                    val h = size.height

                    val rectBottomY = h - triHeightPx

                    val left = halfBw
                    val top = halfBw
                    val right = w - halfBw
                    val bottomRect = rectBottomY - halfBw

                    val cappedR = kotlin.math.min(r, kotlin.math.min((right - left) / 2f, (bottomRect - top) / 2f))

                    val centerX = (left + right) / 2f
                    val maxHalfBase = ((right - left) / 2f) - cappedR
                    val halfBase = kotlin.math.min(triBasePx / 2f, maxHalfBase.coerceAtLeast(0f))
                    val baseStartX = centerX - halfBase
                    val baseEndX = centerX + halfBase

                    val path = Path().apply {
                        // Top edge
                        moveTo(left + cappedR, top)
                        lineTo(right - cappedR, top)
                        // Top-right corner arc
                        arcTo(
                            Rect(right - 2 * cappedR, top, right, top + 2 * cappedR),
                            startAngleDegrees = -90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        // Right edge
                        lineTo(right, bottomRect - cappedR)
                        // Bottom-right corner arc
                        arcTo(
                            Rect(right - 2 * cappedR, bottomRect - 2 * cappedR, right, bottomRect),
                            startAngleDegrees = 0f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        // Bottom edge to triangle
                        lineTo(baseEndX, bottomRect)
                        // Triangle
                        lineTo(centerX, bottomRect + triHeightPx)
                        lineTo(baseStartX, bottomRect)
                        // Bottom edge to left corner
                        lineTo(left + cappedR, bottomRect)
                        // Bottom-left corner arc
                        arcTo(
                            Rect(left, bottomRect - 2 * cappedR, left + 2 * cappedR, bottomRect),
                            startAngleDegrees = 90f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        // Left edge
                        lineTo(left, top + cappedR)
                        // Top-left corner arc
                        arcTo(
                            Rect(left, top, left + 2 * cappedR, top + 2 * cappedR),
                            startAngleDegrees = 180f,
                            sweepAngleDegrees = 90f,
                            forceMoveTo = false
                        )
                        close()
                    }

                    // Fill and border
                    drawPath(path = path, color = fillColor)
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(
                            width = bw,
                            miter = 4f,
                            cap = androidx.compose.ui.graphics.StrokeCap.Butt,
                            join = androidx.compose.ui.graphics.StrokeJoin.Miter
                        )
                    )
                }

                // === CONTENT LAYER (inside the rounded rectangle body only) ===
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .fillMaxWidth()
                        .height(rectHeightDp)
                        .padding(contentPadding)
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = androidx.compose.material3.MaterialTheme.typography.bodyMedium) {
                            content()
                        }
                    }
                }
            }
        }.map { it.measure(widthConstraints) }

        layout(widthPx, totalHeightPx) {
            placeables.forEach { it.place(0, 0) }
        }
    }
}

@Preview(name = "Callout (Light)", showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
fun CalloutBubblePreviewLight() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(
            isDarkTheme = false,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("This is a light callout with a bottom pointer.\nIt centers content inside the rounded rectangle.")
        }
        Spacer(Modifier.height(16.dp))
        CalloutBubble(
            isDarkTheme = false,
            modifier = Modifier.width(280.dp) // fixed width to visualize aspect
        ) {
            Text("Fixed-width example (280.dp).")
        }
    }
}

@Preview(name = "Callout (Dark)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CalloutBubblePreviewDark() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(
            isDarkTheme = true,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Dark theme callout, same geometry.")
        }
    }
}
