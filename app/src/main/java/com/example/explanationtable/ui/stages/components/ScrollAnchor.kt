package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.R

/**
 * A small “scroll to content” button that shows a simple arrow icon.
 *
 * @param isDarkTheme  whether to use dark-theme colors
 * @param onClick      callback when the user taps it (should scroll to your main content)
 * @param modifier     for external layout/styling
 */
@Composable
fun ScrollAnchor(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // --- Press Animation State ---
    var isPressed by remember { mutableStateOf(false) }
    val density = LocalDensity.current
    val defaultOffsetY = 2.dp

    // animate vertical offset on press for tactile feedback
    val pressOffsetPx by animateFloatAsState(
        targetValue = if (isPressed) with(density) { defaultOffsetY.toPx() } else 0f,
        animationSpec = tween(durationMillis = 50)
    )
    val pressOffsetDp = with(density) { pressOffsetPx.toDp() }

    // --- Dimensions & Colors ---
    val cellSize = 52.dp
    val innerSize = 47.dp
    val outerHeight = 54.dp
    val cornerRadius = 16.dp
    val innerCornerRadius = 13.dp

    val frontColor  = if (isDarkTheme) BackgroundDark else BackgroundLight
    val borderColor = if (isDarkTheme) BorderDark    else BorderLight

    // --- Gesture Handling (press + click) ---
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown()
            isPressed = true
            val up = waitForUpOrCancellation()
            isPressed = false
            if (up != null) {
                onClick()
            }
        }
    }

    // --- Layout ---
    Box(
        modifier = modifier
            .width(cellSize)
            .height(outerHeight)
            .then(gestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // shadow layer
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = defaultOffsetY)
                .size(cellSize)
                .clip(RoundedCornerShape(cornerRadius))
                .background(borderColor)
        )
        // border layer (moves on press)
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffsetDp)
                .size(cellSize)
                .clip(RoundedCornerShape(cornerRadius))
                .background(borderColor)
        )
        // front layer with icon
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffsetDp)
                .size(innerSize)
                .clip(RoundedCornerShape(innerCornerRadius))
                .background(frontColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = "Scroll to content",
                modifier = Modifier.size(20.dp)
            )
        }
    }
}
