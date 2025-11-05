package com.example.explanationtable.ui.components

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
 * One reusable, elevated icon button used by all anchors.
 * - Preserves your layered look (shadow/border/content)
 * - Includes the same press animation
 * - The icon is provided via the [icon] slot
 *
 * Keep all styling consistent in one place.
 */
@Composable
fun ActionTile(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    icon: @Composable BoxScope.() -> Unit
) {
    // -----------------------------------------------------------------------------------------
    // Press state and animation
    // -----------------------------------------------------------------------------------------
    var isPressed by remember { mutableStateOf(false) }
    val animationSpec = tween<Dp>(durationMillis = 50)
    val defaultOffset = 2.dp
    val pressOffset: Dp by animateDpAsState(
        targetValue = if (isPressed) defaultOffset else 0.dp,
        animationSpec = animationSpec,
        label = "pressOffset"
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
                isPressed = true
                val released = tryAwaitRelease()
                isPressed = false
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

        // 3) Front/content layer: holds the icon
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffset)
                .size(innerSize)
                .clip(innerShape)
                .background(backgroundColor),
            contentAlignment = Alignment.Center
        ) {
            icon()
        }
    }
}

/** Arrow directions the wrappers can request. */
enum class ArrowDirection { Up, Down, Left, Right }

/** Rotation mapping assuming `ic_arrow` points **Down** in its source asset. */
private fun ArrowDirection.rotationZ(): Float = when (this) {
    ArrowDirection.Down -> 0f
    ArrowDirection.Left -> -90f
    ArrowDirection.Up -> 180f
    ArrowDirection.Right -> 90f
}

/**
 * Small helper used by wrappers to draw the arrow with the given [direction].
 * If your `ic_arrow` points a different way, adjust [rotationZ] mapping above.
 */
@Composable
fun ArrowIcon(
    direction: ArrowDirection,
    contentDescription: String,
    size: Dp = 20.dp,
) {
    Image(
        painter = painterResource(id = R.drawable.ic_arrow),
        contentDescription = contentDescription,
        modifier = Modifier
            .size(size)
            .graphicsLayer { rotationZ = direction.rotationZ() }
    )
}
