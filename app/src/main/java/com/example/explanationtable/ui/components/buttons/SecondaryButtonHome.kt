package com.example.explanationtable.ui.components.buttons

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathFillType
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.components.buttons.internal.rememberPressGesture
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.TextDarkMode
import kotlin.math.max
import kotlin.math.round

/**
 * Home variant: transparent center with an even-odd ring and a tucked shadow skirt.
 *
 * Visual algorithm, geometry, sizes, animation, and timings are preserved.
 * Changes:
 * - Press/gesture logic centralized via [rememberPressGesture].
 * - Added semantics role for accessibility (no visual impact).
 */
@Composable
fun SecondaryButtonHome(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    val borderColor = if (isDarkTheme) TextDarkMode else DialogBackgroundLight
    val labelColor = borderColor

    // Sizing & animation (unchanged)
    val buttonHeight = 56.dp
    val shadowOffset = 3.dp          // bottom layer protrusion
    val cornerRadius = 20.dp
    val borderWidth = 2.25.dp
    val overlapInset = 1.75.dp       // bottom skirt tucks slightly under top ring
    val animationDuration = 30
    // Small motion buffer so release anim finishes before onClick.
    val clickDelayMs = 120

    val press = rememberPressGesture(
        shadowOffset = shadowOffset,
        animationDurationMillis = animationDuration,
        clickDelayMillis = clickDelayMs,
        onClick = onClick
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .semantics { this.role = Role.Button }
            .drawBehind {
                val w = size.width
                val h = size.height

                // Pixel snapping avoids half-px AA seams.
                fun snap(v: Float) = round(v)
                val so = snap(shadowOffset.toPx())
                val po = snap(press.pressOffset.toPx())
                val cr = snap(cornerRadius.toPx())
                val bw = max(1f, snap(borderWidth.toPx()))
                val overlapPx = snap(overlapInset.toPx())

                // --- Bottom skirt (outline base) ---
                val bottomOuter = RoundRect(
                    Rect(0f, so, w, h + so),
                    CornerRadius(cr, cr)
                )
                val bottomPath = Path().apply { addRoundRect(bottomOuter) }

                // Top clip area (trimmed so skirt can tuck under it)
                val topOuterForClip = RoundRect(
                    Rect(0f, po, w, (h + po - overlapPx).coerceAtLeast(po)),
                    CornerRadius(cr, cr)
                )
                val topClipPath = Path().apply { addRoundRect(topOuterForClip) }

                // Draw only the visible skirt portion (difference clip)
                clipPath(topClipPath, clipOp = ClipOp.Difference) {
                    drawPath(bottomPath, color = borderColor)
                }

                // --- EvenOdd ring in the same pass (no .border AA) ---
                fun ringPath(top: Float, bottom: Float): Path {
                    val outer = RoundRect(
                        Rect(0f, top, w, bottom),
                        CornerRadius(cr, cr)
                    )
                    val inner = RoundRect(
                        Rect(bw, top + bw, w - bw, bottom - bw),
                        CornerRadius(max(0f, cr - bw), max(0f, cr - bw))
                    )
                    return Path().apply {
                        fillType = PathFillType.EvenOdd
                        addRoundRect(outer)
                        addRoundRect(inner)
                    }
                }
                val topRing = ringPath(top = po, bottom = h + po)
                drawPath(topRing, color = borderColor)
            }
            .then(press.modifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // Transparent center; label only.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = press.pressOffset)
                .padding(borderWidth),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge.copy(
                    fontWeight = FontWeight.Black,
                    fontSize = 17.sp
                ),
                color = labelColor
            )
        }
    }
}
