package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
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
import androidx.compose.ui.unit.Density
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
 *   Rounded rectangle + bottom pointer with *smooth outward* blends at the two base junctions.
 *
 * Geometry is cached with `remember(...)` to avoid re-allocation in draw.
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
                ProvideTextStyle(
                    value = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black, fontSize = 18.sp
                    )
                ) {
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
        val rectHeightPx = (contentHeight + 2 * padVY)
            .coerceAtLeast(constraints.minHeight)

        val totalWidthPx = rectWidthPx
        val totalHeightPx = rectHeightPx + triHeightPx

        val bubblePlaceables = subcompose("bubble") {
            val density = LocalDensity.current
            val rectWidthDp = with(density) { rectWidthPx.toDp() }
            val rectHeightDp = with(density) { rectHeightPx.toDp() }
            val totalHeightDp = with(density) { totalHeightPx.toDp() }

            // Precompute geometry once per size/theme/density
            val geo = remember(isDarkTheme, rectWidthPx, rectHeightPx, totalHeightPx, density) {
                BubbleGeometry(
                    density = density,
                    borderWidth = borderWidth,
                    cornerRadius = cornerRadius,
                    triangleBase = triangleBase,
                    triangleHeight = triangleHeight
                )
            }

            Box(Modifier.size(width = rectWidthDp, height = totalHeightDp)) {
                // === SHAPE LAYER ===
                Canvas(modifier = Modifier.matchParentSize()) {
                    val g = geo.compute(
                        w = size.width,
                        h = size.height,
                        rectBottomY = size.height - triangleHeight.toPx() // OK: DrawScope has Density
                    )

                    // Single unified fill + one border (no per-part borders).
                    drawPath(path = g.path, color = fillColor)
                    drawPath(
                        path = g.path,
                        color = strokeColor,
                        style = Stroke(
                            width = borderWidth.toPx(), // OK: DrawScope has Density
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
                        .padding(
                            horizontal = contentPaddingHorizontal,
                            vertical = contentPaddingVertical
                        )
                        .clip(RoundedCornerShape(cornerRadius)),
                    contentAlignment = Alignment.Center
                ) {
                    CompositionLocalProvider(LocalContentColor provides contentColor) {
                        ProvideTextStyle(
                            value = MaterialTheme.typography.titleLarge.copy(
                                fontWeight = FontWeight.Black, fontSize = 18.sp
                            )
                        ) {
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

/** Small helper to cache path math. */
private class BubbleGeometry(
    private val density: Density,
    private val borderWidth: Dp,
    private val cornerRadius: Dp,
    private val triangleBase: Dp,
    private val triangleHeight: Dp
) {
    fun compute(w: Float, h: Float, rectBottomY: Float): Geo {
        // All Dp -> px conversions are done with Density here (fixes unresolved toPx)
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

        val rectBottom = rectBottomY
        val left = halfBw
        val top = halfBw
        val right = w - halfBw
        val bottomRect = rectBottom - halfBw

        val cappedR = min(rRect, min((right - left) / 2f, (bottomRect - top) / 2f))
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

        // Make sure everything is Float; no Dp left here (fixes BigDecimal .times confusion)
        val triRound: Float = run {
            val linkToRect: Float = cappedR * 0.8f
            val maxByHeight: Float = triH * 0.7f
            val maxByHalfBase: Float = halfBase * 0.95f
            min(linkToRect, min(maxByHeight, maxByHalfBase))
        }.coerceAtLeast(0.5f)

        val blendRightStartX = min(right - cappedR, baseEndX + triRound)
        val blendLeftStartX  = max(left + cappedR, baseStartX - triRound)

        val vxR = apexX - baseEndX
        val vyR = apexY - bottomRect
        // hypot(Double, Double) -> Double in this environment; convert to Float (fixes type mismatch)
        val lenR = max(
            1e-3f,
            hypot(vxR.toDouble(), vyR.toDouble()).toFloat()
        )
        val uxR = vxR / lenR
        val uyR = vyR / lenR

        val vxL = apexX - baseStartX
        val vyL = apexY - bottomRect
        val lenL = max(
            1e-3f,
            hypot(vxL.toDouble(), vyL.toDouble()).toFloat()
        )
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
            // Top
            moveTo(left + cappedR, top)
            lineTo(right - cappedR, top)
            arcTo(
                Rect(right - 2 * cappedR, top, right, top + 2 * cappedR),
                -90f, 90f, false
            )
            lineTo(right, bottomRect - cappedR)
            arcTo(
                Rect(right - 2 * cappedR, bottomRect - 2 * cappedR, right, bottomRect),
                0f, 90f, false
            )

            lineTo(blendRightStartX, bottomRect)

            val c1RightX = blendRightStartX - triRound
            val c1RightY = bottomRect
            val c2RightX = blendRightEndX - uxR * triRound
            val c2RightY = blendRightEndY - uyR * triRound
            cubicTo(c1RightX, c1RightY, c2RightX, c2RightY, blendRightEndX, blendRightEndY)

            lineTo(rightTipStartX, rightTipStartY)
            quadraticBezierTo(apexX, apexY, leftTipStartX, leftTipStartY)
            lineTo(blendLeftEndX, blendLeftEndY)

            val c1LeftX = blendLeftEndX - uxL * triRound
            val c1LeftY = blendLeftEndY - uyL * triRound
            val c2LeftX = blendLeftStartX + triRound
            val c2LeftY = bottomRect
            cubicTo(c1LeftX, c1LeftY, c2LeftX, c2LeftY, blendLeftStartX, bottomRect)

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
