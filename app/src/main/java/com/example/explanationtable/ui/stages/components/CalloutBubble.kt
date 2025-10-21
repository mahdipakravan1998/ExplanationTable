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
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.ui.layout.SubcomposeLayout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.FeatherGreen
import kotlin.math.hypot
import kotlin.math.min
import kotlin.math.max

/**
 * Single, unified shape:
 *   Rounded rectangle + bottom pointer with *smooth outward* blends at the two base junctions
 *   (not inward-rounded corners). The same triangle-roundness is used at both junctions and the tip.
 *
 * The bubble wraps its text with fixed padding and draws a 2.dp border.
 */
@Composable
fun CalloutBubble(
    isDarkTheme: Boolean,
    text: String,
) {
    // Encapsulated styling
    val contentPaddingHorizontal: Dp = 16.dp
    val contentPaddingVertical: Dp = 12.dp
    val cornerRadius: Dp = 10.dp          // rectangle corner radius
    val triangleBase: Dp = 15.dp          // base width projected on the rectangle bottom
    val triangleHeight: Dp = 10.dp        // pointer height
    val borderWidth: Dp = 2.dp

    val fillColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val strokeColor = if (isDarkTheme) BorderDark else BorderLight
    val contentColor = FeatherGreen

    SubcomposeLayout { constraints ->
        val padHX = contentPaddingHorizontal.roundToPx()
        val padVY = contentPaddingVertical.roundToPx()
        val triHeightPx = triangleHeight.roundToPx()

        // 1) Measure TEXT first.
        val contentPlaceables = subcompose("content-measure") {
            CompositionLocalProvider(LocalContentColor provides contentColor) {
                ProvideTextStyle(value = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black,
                    fontSize = 18.sp)) {
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

        val bubblePlaceables = subcompose("bubble") {
            val density = LocalDensity.current
            val totalWidthDp = with(density) { totalWidthPx.toDp() }
            val totalHeightDp = with(density) { totalHeightPx.toDp() }
            val rectWidthDp = with(density) { rectWidthPx.toDp() }
            val rectHeightDp = with(density) { rectHeightPx.toDp() }

            Box(Modifier.size(width = rectWidthDp, height = totalHeightDp)) {
                // === SHAPE LAYER ===
                Canvas(modifier = Modifier.matchParentSize()) {
                    val bw = borderWidth.toPx()
                    val halfBw = bw / 2f

                    val rRect = cornerRadius.toPx()
                    val triBaseHalfWanted = triangleBase.toPx() / 2f
                    val triH = triangleHeight.toPx()

                    val w = size.width
                    val h = size.height
                    val rectBottomY = h - triH

                    val left = halfBw
                    val top = halfBw
                    val right = w - halfBw
                    val bottomRect = rectBottomY - halfBw

                    // Clamp rectangle radius
                    val cappedR = min(rRect, min((right - left) / 2f, (bottomRect - top) / 2f))

                    // Centered triangle base, limited by rounded corners.
                    val centerX = (left + right) / 2f
                    val halfBaseLimitByEdges = min(
                        centerX - (left + cappedR),
                        (right - cappedR) - centerX
                    ).coerceAtLeast(0f)
                    val halfBase = min(triBaseHalfWanted, halfBaseLimitByEdges)

                    val baseStartX = centerX - halfBase
                    val baseEndX = centerX + halfBase
                    val apexX = centerX
                    val apexY = bottomRect + triH

                    // A single triangle-roundness that controls:
                    //  - the outward blends at both base junctions,
                    //  - the tip rounding.
                    val triRound = run {
                        val linkToRect = cappedR * 0.8f
                        val maxByHeight = triH * 0.7f
                        val maxByHalfBase = halfBase * 0.95f
                        min(linkToRect, min(maxByHeight, maxByHalfBase))
                    }.coerceAtLeast(0.5f)

                    // Where the bottom edge departs to start the OUTWARD blend
                    val blendRightStartX = min(right - cappedR, baseEndX + triRound)
                    val blendLeftStartX  = max(left + cappedR, baseStartX - triRound)

                    // Slanted edge unit directions (base -> apex)
                    val vxR = apexX - baseEndX
                    val vyR = apexY - bottomRect
                    val lenR = hypot(vxR, vyR).coerceAtLeast(1e-3f)
                    val uxR = vxR / lenR
                    val uyR = vyR / lenR

                    val vxL = apexX - baseStartX
                    val vyL = apexY - bottomRect
                    val lenL = hypot(vxL, vyL).coerceAtLeast(1e-3f)
                    val uxL = vxL / lenL
                    val uyL = vyL / lenL

                    // Points on slants where the blends end (still outside the rectangle)
                    val blendRightEndX = baseEndX + uxR * triRound
                    val blendRightEndY = bottomRect + uyR * triRound

                    val blendLeftEndX = baseStartX + uxL * triRound
                    val blendLeftEndY = bottomRect + uyL * triRound

                    // Tip rounding extent measured along each slant
                    val tipLen = triRound
                    val rightTipStartX = apexX - uxR * tipLen
                    val rightTipStartY = apexY - uyR * tipLen
                    val leftTipStartX  = apexX - uxL * tipLen
                    val leftTipStartY  = apexY - uyL * tipLen

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

                        // ---- Bottom edge until OUTWARD blend begins (to the right of baseEndX)
                        lineTo(blendRightStartX, bottomRect)

                        // ---- OUTWARD smooth blend from bottom edge into RIGHT slanted edge
                        // Start tangent == horizontal (bottom edge), end tangent == slant.
                        // Control points chosen to keep the curve outside (below) the bottom edge,
                        // avoiding any inward rounding at the base corner itself.
                        val c1RightX = blendRightStartX - triRound
                        val c1RightY = bottomRect
                        val c2RightX = blendRightEndX - uxR * triRound
                        val c2RightY = blendRightEndY - uyR * triRound

                        cubicTo(
                            c1RightX, c1RightY,
                            c2RightX, c2RightY,
                            blendRightEndX, blendRightEndY
                        )

                        // ---- Along RIGHT slanted edge to near the tip
                        lineTo(rightTipStartX, rightTipStartY)

                        // ---- Rounded TIP (same roundness)
                        quadraticBezierTo(
                            apexX, apexY,
                            leftTipStartX, leftTipStartY
                        )

                        // ---- Along LEFT slanted edge away from tip
                        lineTo(blendLeftEndX, blendLeftEndY)

                        // ---- OUTWARD smooth blend from LEFT slanted edge back to bottom edge
                        val c1LeftX = blendLeftEndX - uxL * triRound
                        val c1LeftY = blendLeftEndY - uyL * triRound
                        val c2LeftX = blendLeftStartX + triRound
                        val c2LeftY = bottomRect

                        cubicTo(
                            c1LeftX, c1LeftY,
                            c2LeftX, c2LeftY,
                            blendLeftStartX, bottomRect
                        )

                        // ---- Continue bottom edge to bottom-left corner
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

                    // Single unified fill + one border (no per-part borders).
                    drawPath(path = path, color = fillColor)
                    drawPath(
                        path = path,
                        color = strokeColor,
                        style = Stroke(
                            width = bw,
                            cap = StrokeCap.Butt,
                            join = StrokeJoin.Round
                        )
                    )
                }

                // === CONTENT LAYER (clipped to rectangle body only) ===
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .size(width = rectWidthDp, height = rectHeightDp)
                        .padding(horizontal = contentPaddingHorizontal, vertical = contentPaddingVertical)
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(value = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Black,
                            fontSize = 18.sp)) {
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
        CalloutBubble(isDarkTheme = false, text = "Smooth outward blends into the pointer.")
    }
}

@Preview(name = "Callout (Dark)", showBackground = true, backgroundColor = 0xFF121212)
@Composable
fun CalloutBubblePreviewDark() {
    Column(Modifier.fillMaxWidth().padding(16.dp)) {
        CalloutBubble(isDarkTheme = true, text = "Dark, unified shape.")
    }
}
