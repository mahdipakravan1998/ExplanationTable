package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.Hare
import com.example.explanationtable.ui.theme.Heron
import com.example.explanationtable.ui.theme.Pigeon
import com.example.explanationtable.ui.theme.Raven
import com.example.explanationtable.ui.theme.Seal
import com.example.explanationtable.ui.theme.Swan
import com.example.explanationtable.utils.toPersianDigits

// Button dimensions and animation constants
private val BUTTON_SIZE: Dp = 77.dp           // Canvas box size
private val MAIN_DIAMETER: Dp = 70.dp         // Baseline "circle" diameter used to shape the oval
private val PRESS_OFFSET: Dp = 7.dp           // Visual gap between back/front layers and press translation
private const val ANIMATION_DURATION_MS = 30  // Press animation duration (~2 frames at 60Hz)

/**
 * A locked level/stage button that shows a two-layer oval and the step number.
 *
 * - **No click action** by design (it's locked), but when [enabled] it still provides a short
 *   press animation for visual feedback. When disabled, it dims and ignores press.
 * - Visual output and timing are kept identical to the provided implementation.
 * - Accessibility semantics are added without advertising a false click action.
 *
 * @param isDarkTheme Theme toggle to resolve palette colors.
 * @param stepNumber The numeric step shown in the center. Rendered with Persian digits.
 * @param enabled Whether the press animation is active and the control appears fully opaque.
 */
@Composable
fun LockedStepButton(
    isDarkTheme: Boolean,
    stepNumber: Int,
    enabled: Boolean = true
) {
    // 1) Resolve colors according to the current theme, and remember them for stability
    val (bottomColor, topColor, iconTint) = remember(isDarkTheme) {
        val bottom = if (isDarkTheme) Raven else Seal
        val top = if (isDarkTheme) Pigeon else Swan
        val tint = if (isDarkTheme) Heron else Hare
        Triple(bottom, top, tint)
    }

    // 2) Density-derived constants, memoized so we don't convert on every recomposition/draw
    val density = LocalDensity.current
    val diameterPx = remember(density) { with(density) { MAIN_DIAMETER.toPx() } }
    val pressOffsetPxStatic = remember(density) { with(density) { PRESS_OFFSET.toPx() } }

    // 3) Press state and animated vertical offset (in Dp, to reuse directly for Text.offset)
    var isPressed by remember { mutableStateOf(false) }
    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) PRESS_OFFSET else 0.dp,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS),
        label = "lockedButtonPressOffset"
    )

    // 4) If enabled, consume pointer gestures only to toggle pressed state (no click action)
    val gestureModifier = if (enabled) {
        Modifier.pointerInput(Unit) {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                waitForUpOrCancellation()
                isPressed = false
            }
        }
    } else {
        Modifier
    }

    // 5) Root container: fixed size, semantics for a11y, and alpha when disabled
    Box(
        modifier = gestureModifier
            .size(BUTTON_SIZE)
            .semantics {
                // Announce the step number as the element's label. We do not advertise clickable.
                contentDescription = stepNumber.toPersianDigits()
                if (!enabled) disabled()
            }
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        // 6) Draw the two-layered oval “button” (shadow/back + face/front)
        Canvas(modifier = Modifier.matchParentSize()) {
            val radius = diameterPx / 2f
            val width = radius * 2f
            val height = radius * 1.8f

            // Centers
            val centerX = size.width / 2f
            val centerY = size.height / 2f

            // Front (face) follows the animated press offset; back (shadow) uses the static press offset
            val faceCenterY = centerY + with(density) { animatedOffset.toPx() }
            val shadowCenterY = centerY + pressOffsetPxStatic

            // Bottom “shadow” layer (appears lower to create depth)
            drawOval(
                color = bottomColor,
                topLeft = Offset(centerX - width / 2f, shadowCenterY - height / 2f),
                size = Size(width, height),
                style = Fill
            )
            // Top “face” layer
            drawOval(
                color = topColor,
                topLeft = Offset(centerX - width / 2f, faceCenterY - height / 2f),
                size = Size(width, height),
                style = Fill
            )
        }

        // 7) Center text translated vertically by the same animated offset (layout-safe Dp offset)
        Text(
            text = stepNumber.toPersianDigits(),
            style = AppTypography.headlineSmall.copy(
                color = iconTint,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = animatedOffset)
        )
    }
}
