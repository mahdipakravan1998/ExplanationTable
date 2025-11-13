package com.example.explanationtable.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

/**
 * Three-dot loading indicator that grows/shrinks from LEFT to RIGHT, then repeats.
 * - Fixed-size cells prevent horizontal jitter.
 * - Forces LTR locally so "left" is visually left even in RTL UIs.
 */
@Composable
fun LoadingDots(
    color: Color,
    dotMinSize: Dp = 6.dp,
    dotMaxSize: Dp = 12.dp,
    space: Dp = 6.dp,
    cycleDurationMs: Int = 500
) {
    val transition = rememberInfiniteTransition(label = "LoadingDotsTransition")
    val minScale = dotMinSize.value / dotMaxSize.value
    val delayStep = cycleDurationMs / 4 // stagger so left leads, then middle, then right

    val leftScale by transition.animateFloat(
        initialValue = minScale,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(offsetMillis = 0)
        ),
        label = "LeftDotScale"
    )

    val middleScale by transition.animateFloat(
        initialValue = minScale,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(offsetMillis = delayStep)
        ),
        label = "MiddleDotScale"
    )

    val rightScale by transition.animateFloat(
        initialValue = minScale,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
            initialStartOffset = StartOffset(offsetMillis = delayStep * 2)
        ),
        label = "RightDotScale"
    )

    // Ensure left-to-right visual order regardless of app RTL.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotCell(color = color, maxSize = dotMaxSize, scale = leftScale)
            DotCell(color = color, maxSize = dotMaxSize, scale = middleScale)
            DotCell(color = color, maxSize = dotMaxSize, scale = rightScale)
        }
    }
}

@Composable
private fun DotCell(color: Color, maxSize: Dp, scale: Float) {
    // Fixed-size cell prevents any horizontal movement of centers.
    Box(
        modifier = Modifier.size(maxSize),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(maxSize) // full circle
                .scale(scale)  // scale around center
                .background(color, CircleShape)
        )
    }
}
