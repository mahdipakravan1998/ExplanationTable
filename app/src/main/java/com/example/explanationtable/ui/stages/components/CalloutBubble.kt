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
 * that WRAPS its content plus per-side padding (no fixed aspect).
 */
@Composable
fun CalloutBubble(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    // ---- NEW: per-side padding & 10.dp radius defaults ----
    contentPaddingHorizontal: Dp = 16.dp,
    contentPaddingVertical: Dp = 12.dp,
    cornerRadius: Dp = 10.dp,
    triangleBase: Dp = 12.dp,      // narrower base → pointer reaches center even near edges
    triangleHeight: Dp = 10.dp,
    borderWidth: Dp = 2.dp,
    // shift the triangle horizontally relative to rect center (useful when bubble is clamped near screen edges)
    triangleCenterBias: Dp = 0.dp,
    content: @Composable () -> Unit = {}
) {
    val fillColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val strokeColor = if (isDarkTheme) BorderDark else BorderLight
    val contentColor = FeatherGreen

    androidx.compose.ui.layout.SubcomposeLayout(modifier = modifier) { constraints ->
        val padHX = contentPaddingHorizontal.roundToPx()
        val padVY = contentPaddingVertical.roundToPx()
        val triHeightPx = triangleHeight.roundToPx()

        // 1) Measure the slot content first with relaxed constraints.
        val contentPlaceables = subcompose("content-measure") {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                    Box(Modifier.wrapContentSize()) { content() }
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

        // Rectangle exactly fits content + padding.
        val rectWidthPx = (contentWidth + 2 * padHX)
            .coerceAtLeast(constraints.minWidth)
            .coerceAtMost(constraints.maxWidth)
        val rectHeightPx = (contentHeight + 2 * padVY)
            .coerceAtLeast(constraints.minHeight)
            .coerceAtMost((constraints.maxHeight - triHeightPx).coerceAtLeast(0))

        val totalWidthPx = rectWidthPx
        val totalHeightPx = rectHeightPx + triHeightPx

        // 2) Draw the bubble and place the (re-measured) content.
        val bubblePlaceables = subcompose("bubble") {
            Box(Modifier.size(width = rectWidthPx.toDp(), height = totalHeightPx.toDp())) {
                // === SHAPE LAYER ===
                Canvas(modifier = Modifier.matchParentSize()) {
                    val bw = borderWidth.toPx()
                    val halfBw = bw / 2f

                    val r = cornerRadius.toPx()
                    val triBaseHalfWanted = triangleBase.toPx() / 2f
                    val triHeight = triangleHeight.toPx()
                    val biasPx = triangleCenterBias.toPx()

                    val w = size.width
                    val h = size.height
                    val rectBottomY = h - triHeight

                    val left = halfBw
                    val top = halfBw
                    val right = w - halfBw
                    val bottomRect = rectBottomY - halfBw

                    val cappedR = kotlin.math.min(r, kotlin.math.min((right - left) / 2f, (bottomRect - top) / 2f))

                    // Desired triangle center (biased), then clamp to rect bottom span
                    val desiredCenter = (left + right) / 2f + biasPx
                    val centerX = desiredCenter.coerceIn(left + cappedR, right - cappedR)

                    // Allow the base to SHRINK near edges so the tip can still align at centerX.
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

                // === CONTENT LAYER (inside the rounded rectangle body only) ===
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width = rectWidthPx.toDp(), height = rectHeightPx.toDp())
                        .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical)
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    // Recompose & remeasure content to fit the exact inner box
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = MaterialTheme.typography.bodyMedium) {
                            content()
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
        CalloutBubble(
            isDarkTheme = false,
            contentPaddingHorizontal = 16.dp,
            contentPaddingVertical = 12.dp,
            cornerRadius = 10.dp
        ) { Text("شروع") }
        Spacer(Modifier.height(12.dp))
        CalloutBubble(
            isDarkTheme = false,
            contentPaddingHorizontal = 16.dp,
            contentPaddingVertical = 12.dp,
            cornerRadius = 10.dp
        ) { Text("Wraps the text with 16x12 padding.") }
    }
}

@Preview(name = "Callout (Dark)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CalloutBubblePreviewDark() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(
            isDarkTheme = true,
            contentPaddingHorizontal = 16.dp,
            contentPaddingVertical = 12.dp,
            cornerRadius = 10.dp
        ) { Text("Dark, snug fit.") }
    }
}
