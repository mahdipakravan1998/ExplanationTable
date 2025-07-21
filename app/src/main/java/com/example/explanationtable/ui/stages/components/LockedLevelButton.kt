package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.Hare
import com.example.explanationtable.ui.theme.Seal
import com.example.explanationtable.ui.theme.Swan
import kotlinx.coroutines.coroutineScope

@Composable
fun LockedStepButton(
    isDarkTheme: Boolean,
    enabled: Boolean = true
) {
    // Fixed “off” colors
    val bottomColor = if (isDarkTheme) Color(0xFF2C383F) else Seal
    val topColor    = if (isDarkTheme) Color(0xFF37464F) else Swan
    val iconColor   = if (isDarkTheme) Color(0xFF52656D) else Hare

    // Press‐offset (same as original)
    val behindOffset = 7.dp
    val density      = LocalDensity.current

    // Press state & animation
    var isPressed by remember { mutableStateOf(false) }
    val animatedPressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { behindOffset.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30)
    )
    val animatedPressOffsetDp = with(density) { animatedPressOffsetPx.toDp() }

    // Only handle gestures if “enabled”
    val gestureModifier = if (enabled) {
        Modifier.pointerInput(Unit) {
            coroutineScope {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    waitForUpOrCancellation()
                    isPressed = false
                }
            }
        }
    } else {
        Modifier
    }

    Box(
        modifier = gestureModifier
            .size(82.dp)
            .alpha(if (enabled) 1f else 0.5f),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            // Match original dimensions: 75.dp wide, ~0.9× tall
            val mainDiameterDp = 75.dp
            val mainDiameterPx = mainDiameterDp.toPx()
            val outerRadius    = mainDiameterPx / 2f
            val width          = outerRadius * 2f
            val height         = outerRadius * 1.8f

            val canvasCenter   = center
            val behindCenter   = Offset(canvasCenter.x, canvasCenter.y + behindOffset.toPx())
            val frontCenter    = Offset(canvasCenter.x, canvasCenter.y + animatedPressOffsetPx)

            // Bottom “shadow” layer
            drawOval(
                color   = bottomColor,
                topLeft = Offset(behindCenter.x - width  / 2f, behindCenter.y - height / 2f),
                size    = Size(width, height),
                style   = Fill
            )
            // Top “face” layer
            drawOval(
                color   = topColor,
                topLeft = Offset(frontCenter.x  - width  / 2f, frontCenter.y  - height / 2f),
                size    = Size(width, height),
                style   = Fill
            )
        }

        // Fixed lock icon, moving with press offset
        Icon(
            painter            = painterResource(id = R.drawable.ic_lock),
            contentDescription = "Locked",
            tint               = iconColor,
            modifier           = Modifier
                .size(40.dp)
                .align(Alignment.Center)
                .offset(y = animatedPressOffsetDp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun LockedStepButtonPreview() {
    LockedStepButton(isDarkTheme = false)
}
