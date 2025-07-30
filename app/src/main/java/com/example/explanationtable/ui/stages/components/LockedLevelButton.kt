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
private val BUTTON_SIZE: Dp = 77.dp
private val MAIN_DIAMETER: Dp = 70.dp
private val PRESS_OFFSET: Dp = 7.dp
private const val ANIMATION_DURATION_MS = 30

@Composable
fun LockedStepButton(
    isDarkTheme: Boolean,
    stepNumber: Int,
    enabled: Boolean = true
) {
    // 1) Resolve colors according to the current theme
    val bottomColor = if (isDarkTheme) Raven else Seal
    val topColor    = if (isDarkTheme) Pigeon else Swan
    val iconTint    = if (isDarkTheme) Heron else Hare

    // 2) LocalDensity for px/dp conversions inside Canvas
    val density = LocalDensity.current

    // 3) Track press state and animate offset in Dp
    var isPressed by remember { mutableStateOf(false) }
    val animatedOffset by animateDpAsState(
        targetValue = if (isPressed) PRESS_OFFSET else 0.dp,
        animationSpec = tween(durationMillis = ANIMATION_DURATION_MS)
    )

    // 4) If enabled, handle pointer gestures to toggle isPressed
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

    // 5) Root container: fixed size, optional alpha when disabled
    Box(
        modifier = gestureModifier
            .size(BUTTON_SIZE)
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        // 6) Draw the two-layered oval “button” (shadow + face)
        Canvas(modifier = Modifier.matchParentSize()) {
            // Convert main diameter to pixels
            val diameterPx = with(density) { MAIN_DIAMETER.toPx() }
            val radius    = diameterPx / 2f
            val width     = radius * 2f
            val height    = radius * 1.8f

            // Compute center positions
            val centerX = size.width / 2f
            val centerY = size.height / 2f
            val shadowCenterY = centerY + with(density) { PRESS_OFFSET.toPx() }
            val faceCenterY   = centerY + with(density) { animatedOffset.toPx() }

            // Bottom “shadow” layer
            drawOval(
                color   = bottomColor,
                topLeft = Offset(centerX - width  / 2f, shadowCenterY - height / 2f),
                size    = Size(width, height),
                style   = Fill
            )
            // Top “face” layer
            drawOval(
                color   = topColor,
                topLeft = Offset(centerX - width  / 2f, faceCenterY   - height / 2f),
                size    = Size(width, height),
                style   = Fill
            )
        }

        // 7) Render the step number text in the center of the button,
        // adjusting its vertical position based on the press animation.
        Text(
            text = stepNumber.toPersianDigits(),
            style = AppTypography.headlineMedium.copy(
                color = iconTint,
                fontWeight = FontWeight.Bold
            ),
            modifier = Modifier
                .align(Alignment.Center)
                .offset(y = animatedOffset)
        )
    }
}
