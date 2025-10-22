package com.example.explanationtable.ui.stages.components

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Constraints
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.FeatherGreen
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

/**
 * Draws a speech-bubble-like callout (rounded rectangle with a bottom pointer)
 * and centers a single-line or multi-line [text] inside the rectangle body.
 *
 * This overload preserves the **original public API** for existing call sites.
 *
 * @param isDarkTheme Whether the app is currently in dark theme (controls fill/border colors).
 * @param text The text to display inside the bubble.
 */
@Composable
fun CalloutBubble(
    isDarkTheme: Boolean,
    text: String,
) = CalloutBubble(
    modifier = Modifier,
    isDarkTheme = isDarkTheme,
    text = text
)

/**
 * Draws a speech-bubble-like callout with an optional [modifier].
 *
 * Performance notes:
 * - Uses a custom [Layout] to measure text once and compute the final size.
 * - Background shape is rendered via [Modifier.drawBehind] to avoid extra nodes.
 * - Geometry math is kept in a small stable holder and reused.
 *
 * UI is **identical** to the original: same paddings, radius, pointer size,
 * typography, and colors.
 */
@Composable
fun CalloutBubble(
    modifier: Modifier = Modifier,
    isDarkTheme: Boolean,
    text: String,
) {
    // --- Encapsulated styling (kept identical to the original component) ---
    val contentPaddingHorizontal: Dp = 16.dp
    val contentPaddingVertical: Dp = 12.dp
    val cornerRadius: Dp = 10.dp          // rectangle corner radius
    val triangleBase: Dp = 15.dp          // base width projected on the rectangle bottom
    val triangleHeight: Dp = 10.dp        // pointer height
    val borderWidth: Dp = 2.dp

    // Colors identical to original
    val fillColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val strokeColor = if (isDarkTheme) BorderDark else BorderLight
    val textColor = FeatherGreen

    // Typography identical to original
    val textStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Black,
        fontSize = 18.sp
    )

    val density = LocalDensity.current
    // Stable geometry cache (reused across draws unless density/params change)
    val geometry = remember(density, borderWidth, cornerRadius, triangleBase, triangleHeight) {
        BubbleGeometry(
            density = density,
            borderWidth = borderWidth,
            cornerRadius = cornerRadius,
            triangleBase = triangleBase,
            triangleHeight = triangleHeight
        )
    }

    // Draw the bubble path once per draw behind the content.
    val background = Modifier.drawBehind {
        // Rectangle body ends above the pointer (triangle) by exactly triangleHeight
        val rectBottomY = size.height - with(density) { triangleHeight.toPx() }
        val g = geometry.compute(
            w = size.width,
            rectBottomY = rectBottomY
        )
        // Fill then stroke the single unified path.
        drawPath(path = g.path, color = fillColor)
        drawPath(
            path = g.path,
            color = strokeColor,
            style = Stroke(
                width = with(density) { borderWidth.toPx() },
                cap = StrokeCap.Butt,
                join = StrokeJoin.Round
            )
        )
    }

    // Single-child custom layout: measure Text, then size the bubble around it.
    Layout(
        modifier = modifier.then(background),
        content = {
            Text(
                text = text,
                style = textStyle,
                color = textColor
            )
        }
    ) { measurables, constraints ->
        require(measurables.size == 1) { "CalloutBubble expects exactly one child (Text)" }

        val padHX = contentPaddingHorizontal.roundToPx()
        val padVY = contentPaddingVertical.roundToPx()
        val triHeightPx = triangleHeight.roundToPx()

        // Measure text to fit inside the rectangle (exclude pointer + paddings).
        val innerMaxWidth = (constraints.maxWidth - 2 * padHX).coerceAtLeast(0)
        val innerMaxHeight = (constraints.maxHeight - triHeightPx - 2 * padVY).coerceAtLeast(0)
        val textPlaceable = measurables.first().measure(
            Constraints(
                minWidth = 0,
                minHeight = 0,
                maxWidth = innerMaxWidth,
                maxHeight = innerMaxHeight
            )
        )

        // Rectangle body wraps text + padding; total height includes the pointer.
        val rectWidthPx = (textPlaceable.width + 2 * padHX)
            .coerceIn(constraints.minWidth, constraints.maxWidth)
        val rectHeightPx = (textPlaceable.height + 2 * padVY)
            .coerceIn(constraints.minHeight, (constraints.maxHeight - triHeightPx).coerceAtLeast(0))

        val totalWidthPx = rectWidthPx
        val totalHeightPx = rectHeightPx + triHeightPx

        layout(totalWidthPx, totalHeightPx) {
            // Center text within the rectangle body (respect paddings).
            val contentX = padHX + (rectWidthPx - 2 * padHX - textPlaceable.width) / 2
            val contentY = padVY + (rectHeightPx - 2 * padVY - textPlaceable.height) / 2
            textPlaceable.placeRelative(x = contentX, y = contentY)
        }
    }
}

/**
 * Small helper to cache bubble path math and avoid repeated conversions.
 *
 * The resulting path represents a **single continuous shape**:
 *   rounded rectangle + bottom pointer with smooth blended junctions.
 */
@Stable
private class BubbleGeometry(
    private val density: androidx.compose.ui.unit.Density,
    private val borderWidth: Dp,
    private val cornerRadius: Dp,
    private val triangleBase: Dp,
    private val triangleHeight: Dp
) {
    /**
     * Computes the bubble path for the given width [w] and the bottom Y of the
     * rectangle body [rectBottomY] (the triangle pointer is drawn below it).
     */
    fun compute(w: Float, rectBottomY: Float): Geo {
        // Dp -> px conversions once per compute with the same density.
        val bw: Float
        val rRect: Float
        val triBaseHalfWanted: Float
        val triH: Float
        with(density) {
            bw = borderWidth.toPx()
            rRect = cornerRadius.toPx()
            triBaseHalfWanted = triangleBase.toPx() / 2f
            triH = triangleHeight.toPx()
        }
        val halfBw = bw / 2f

        val left = halfBw
        val top = halfBw
        val right = w - halfBw
        val bottomRect = rectBottomY - halfBw

        // Cap corner radius so arcs never overlap.
        val cappedR = min(rRect, min((right - left) / 2f, (bottomRect - top) / 2f))
        val centerX = (left + right) / 2f

        // Ensure triangle base fits between rounded corners.
        val halfBaseLimitByEdges = min(
            centerX - (left + cappedR),
            (right - cappedR) - centerX
        ).coerceAtLeast(0f)
        val halfBase = min(triBaseHalfWanted, halfBaseLimitByEdges)

        val baseStartX = centerX - halfBase
        val baseEndX = centerX + halfBase
        val apexX = centerX
        val apexY = bottomRect + triH

        // Blend amount near junctions — proportional to geometry.
        val triRound: Float = run {
            val linkToRect: Float = cappedR * 0.8f
            val maxByHeight: Float = triH * 0.7f
            val maxByHalfBase: Float = halfBase * 0.95f
            min(linkToRect, min(maxByHeight, maxByHalfBase))
        }.coerceAtLeast(0.5f)

        val blendRightStartX = min(right - cappedR, baseEndX + triRound)
        val blendLeftStartX  = max(left + cappedR, baseStartX - triRound)

        // Right edge vector from base end to tip (normalized).
        val vxR = apexX - baseEndX
        val vyR = apexY - bottomRect
        val lenR = max(1e-3f, hypot(vxR.toDouble(), vyR.toDouble()).toFloat())
        val uxR = vxR / lenR
        val uyR = vyR / lenR

        // Left edge vector from base start to tip (normalized).
        val vxL = apexX - baseStartX
        val vyL = apexY - bottomRect
        val lenL = max(1e-3f, hypot(vxL.toDouble(), vyL.toDouble()).toFloat())
        val uxL = vxL / lenL
        val uyL = vyL / lenL

        val blendRightEndX = baseEndX + uxR * triRound
        val blendRightEndY = bottomRect + uyR * triRound
        val blendLeftEndX = baseStartX + uxL * triRound
        val blendLeftEndY = bottomRect + uyL * triRound

        val tipLen = triRound
        val rightTipStartX = apexX - uxR * tipLen
        val rightTipStartY = apexY - uyR * tipLen
        val leftTipStartX  = apexX - uxL * tipLen
        val leftTipStartY  = apexY - uyL * tipLen

        val path = Path().apply {
            // Top edge + top-right corner
            moveTo(left + cappedR, top)
            lineTo(right - cappedR, top)
            arcTo(
                Rect(right - 2 * cappedR, top, right, top + 2 * cappedR),
                -90f, 90f, false
            )
            // Right edge + bottom-right corner (of rectangle)
            lineTo(right, bottomRect - cappedR)
            arcTo(
                Rect(right - 2 * cappedR, bottomRect - 2 * cappedR, right, bottomRect),
                0f, 90f, false
            )

            // Blend into the triangle on the right side
            lineTo(blendRightStartX, bottomRect)
            val c1RightX = blendRightStartX - triRound
            val c1RightY = bottomRect
            val c2RightX = blendRightEndX - uxR * triRound
            val c2RightY = blendRightEndY - uyR * triRound
            cubicTo(c1RightX, c1RightY, c2RightX, c2RightY, blendRightEndX, blendRightEndY)

            // Tip curve (right → tip → left) using quadraticTo (non-deprecated)
            lineTo(rightTipStartX, rightTipStartY)
            quadraticTo(apexX, apexY, leftTipStartX, leftTipStartY)

            // Blend out on the left side
            lineTo(blendLeftEndX, blendLeftEndY)
            val c1LeftX = blendLeftEndX - uxL * triRound
            val c1LeftY = blendLeftEndY - uyL * triRound
            val c2LeftX = blendLeftStartX + triRound
            val c2LeftY = bottomRect
            cubicTo(c1LeftX, c1LeftY, c2LeftX, c2LeftY, blendLeftStartX, bottomRect)

            // Bottom-left corner + left edge + top-left corner
            lineTo(left + cappedR, bottomRect)
            arcTo(
                Rect(left, bottomRect - 2 * cappedR, left + 2 * cappedR, bottomRect),
                90f, 90f, false
            )
            lineTo(left, top + cappedR)
            arcTo(
                Rect(left, top, left + 2 * cappedR, top + 2 * cappedR),
                180f, 90f, false
            )
            close()
        }

        return Geo(path)
    }

    class Geo(val path: Path)
}
