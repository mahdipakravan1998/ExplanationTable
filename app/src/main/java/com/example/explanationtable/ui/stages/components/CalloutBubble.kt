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
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.FeatherGreen

/**
 * Rounded rectangle + bottom pointer as one unified shape
 * that WRAPS its text plus fixed per-side padding (no aspect lock).
 *
 * API intentionally minimal: only [isDarkTheme] and [text].
 * Geometry (padding, radius, triangle, border) is encapsulated here.
 */
@Composable
fun CalloutBubble(
    isDarkTheme: Boolean,
    text: String,
) {
    // Encapsulated styling
    val contentPaddingHorizontal: Dp = 16.dp
    val contentPaddingVertical: Dp = 12.dp
    val cornerRadius: Dp = 10.dp
    val triangleBase: Dp = 12.dp
    val triangleHeight: Dp = 10.dp
    val borderWidth: Dp = 2.dp

    val fillColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val strokeColor = if (isDarkTheme) BorderDark else BorderLight
    val contentColor = FeatherGreen

    androidx.compose.ui.layout.SubcomposeLayout { constraints ->
        val padHX = contentPaddingHorizontal.roundToPx()
        val padVY = contentPaddingVertical.roundToPx()
        val triHeightPx = triangleHeight.roundToPx()

        // 1) Measure TEXT first with relaxed constraints.
        val contentPlaceables = subcompose("content-measure") {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                    Text(text)
                }
            }
        }.map {
            it.measure(
                Constraints(
                    minWidth = 0,
                    minHeight = 0,
                    maxWidth = (constraints.maxWidth - padHX * 2).coerceAtLeast(0),
                    maxHeight = (constraints.maxHeight - triHeightPx - padVY * 2).coerceAtLeast(0)
                )
            )
        }

        val contentWidth = contentPlaceables.maxOfOrNull { it.width } ?: 0
        val contentHeight = contentPlaceables.maxOfOrNull { it.height } ?: 0

        // Rectangle exactly fits text + padding.
        val rectWidthPx = (contentWidth + 2 * padHX)
            .coerceAtLeast(constraints.minWidth)
            .coerceAtMost(constraints.maxWidth)
        val rectHeightPx = (contentHeight + 2 * padVY)
            .coerceAtLeast(constraints.minHeight)
            .coerceAtMost((constraints.maxHeight - triHeightPx).coerceAtLeast(0))

        val totalWidthPx = rectWidthPx
        val totalHeightPx = rectHeightPx + triHeightPx

        // 2) Draw bubble & place content.
        val bubblePlaceables = subcompose("bubble") {
            Box(Modifier.size(width = rectWidthPx.toDp(), height = totalHeightPx.toDp())) {
                // === SHAPE LAYER ===
                Canvas(modifier = Modifier.matchParentSize()) {
                    val bw = borderWidth.toPx()
                    val halfBw = bw / 2f

                    val r = cornerRadius.toPx()
                    val triBaseHalfWanted = triangleBase.toPx() / 2f
                    val triHeight = triangleHeight.toPx()

                    val w = size.width
                    val h = size.height
                    val rectBottomY = h - triHeight

                    val left = halfBw
                    val top = halfBw
                    val right = w - halfBw
                    val bottomRect = rectBottomY - halfBw

                    val cappedR = kotlin.math.min(r, kotlin.math.min((right - left) / 2f, (bottomRect - top) / 2f))

                    // Triangle centered under the rectangle. Allow base to shrink near rounded corners.
                    val centerX = (left + right) / 2f
                    val halfBaseLimitByEdges = kotlin.math.min(
                        centerX - (left + cappedR),
                        (right - cappedR) - centerX
                    ).coerceAtLeast(0f)
                    val halfBase = kotlin.math.min(triBaseHalfWanted, halfBaseLimitByEdges)

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
                        lineTo(centerX, bottomRect + triHeight)
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

                // === CONTENT LAYER (clipped to rectangle only) ===
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width = rectWidthPx.toDp(), height = rectHeightPx.toDp())
                        .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical)
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                            Text(text)
                        }
                    }
                }
            }
        }.map { it.measure(Constraints.fixed(totalWidthPx, totalHeightPx)) }

        layout(totalWidthPx, totalHeightPx) {
            bubblePlaceables.forEach { it.place(0, 0) }
        }
    }
}

@Preview(name = "Callout (Light)", showBackground = true, backgroundColor = 0xFFF4F4F4)
@Composable
fun CalloutBubblePreviewLight() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(isDarkTheme = false, text = "شروع")
        Spacer(Modifier.height(12.dp))
        CalloutBubble(isDarkTheme = false, text = "Wraps the text with 16×12 padding.")
    }
}

@Preview(name = "Callout (Dark)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CalloutBubblePreviewDark() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(isDarkTheme = true, text = "Dark, snug fit.")
    }
}
