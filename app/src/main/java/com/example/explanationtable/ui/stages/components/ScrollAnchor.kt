package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight

/**
 * A pressable anchor that indicates “scroll to main content.”
 *
 * Layers three boxes (shadow, border, content) to simulate elevation
 * and animates a small downward shift on press.
 *
 * @param isDarkTheme  whether to use dark or light theme colors
 * @param flipVertical if true, arrow points down; otherwise, up
 * @param onClick      callback invoked when the anchor is tapped
 * @param modifier     for external layout/styling
 */
@Composable
fun ScrollAnchor(
    isDarkTheme: Boolean,
    flipVertical: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // -----------------------------------------------------------------------------------------
    // Press state and animation
    // -----------------------------------------------------------------------------------------
    var isPressed by remember { mutableStateOf(false) }

    // Duration for the press animation
    val animationSpec = tween<Dp>(durationMillis = 50)

    // How far the layers shift when pressed
    val defaultOffset = 2.dp

    // Animate between 0.dp and defaultOffset when pressed/released
    val pressOffset: Dp by animateDpAsState(
        targetValue = if (isPressed) defaultOffset else 0.dp,
        animationSpec = animationSpec
    )

    // -----------------------------------------------------------------------------------------
    // Constants: sizes, shapes, and colors
    // -----------------------------------------------------------------------------------------
    val cellSize = 52.dp
    val innerSize = 47.dp
    val outerHeight = 54.dp

    val outerShape = RoundedCornerShape(16.dp)
    val innerShape = RoundedCornerShape(13.dp)

    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight

    // -----------------------------------------------------------------------------------------
    // Handle tap gestures, updating press state and firing onClick
    // -----------------------------------------------------------------------------------------
    val gestureModifier = Modifier.pointerInput(Unit) {
        detectTapGestures(
            onPress = {
                // User presses down: show pressed state
                isPressed = true
                // Suspend until release or cancellation
                val released = tryAwaitRelease()
                // Always clear pressed state
                isPressed = false
                // If it was a full tap (not cancelled), invoke click callback
                if (released) onClick()
            }
        )
    }

    // -----------------------------------------------------------------------------------------
    // Layout: three stacked boxes to create depth & press effect
    // -----------------------------------------------------------------------------------------
    Box(
        modifier = modifier
            .size(width = cellSize, height = outerHeight)
            .then(gestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1) Shadow layer: static, offset by defaultOffset
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = defaultOffset)
                .size(cellSize)
                .clip(outerShape)
                .background(borderColor)
        )

        // 2) Border layer: moves down when pressed
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffset)
                .size(cellSize)
                .clip(outerShape)
                .background(borderColor)
        )

        // 3) Front/content layer: holds the arrow icon
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffset)
                .size(innerSize)
                .clip(innerShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            Image(
                painter = painterResource(id = R.drawable.ic_arrow),
                contentDescription = "Scroll to content",
                modifier = Modifier
                    .size(20.dp)
                    // Flip the arrow vertically if requested
                    .graphicsLayer { scaleY = if (flipVertical) 1f else -1f }
            )
        }
    }
}
