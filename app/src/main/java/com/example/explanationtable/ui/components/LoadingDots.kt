package com.example.explanationtable.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationVector1D
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay

/**
 * Three-dot loading indicator that grows/shrinks from LEFT to RIGHT, then repeats.
 * - Fixed-size cells prevent horizontal jitter.
 * - Forces LTR locally so "left" is visually left even in RTL UIs.
 *
 * Implementation note:
 * This version uses three independent [Animatable] instances instead of
 * [rememberInfiniteTransition], which makes it more robust on devices where
 * infinite transitions sometimes appear stuck or don’t tick under heavy load.
 */
@Composable
fun LoadingDots(
    color: Color,
    dotMinSize: Dp = 6.dp,
    dotMaxSize: Dp = 12.dp,
    space: Dp = 6.dp,
    cycleDurationMs: Int = 500
) {
    val minScale = dotMinSize.value / dotMaxSize.value
    val delayStep = cycleDurationMs / 4 // stagger so left leads, then middle, then right

    // Each dot has its own Animatable scale.
    val leftAnim = remember { Animatable(minScale) }
    val middleAnim = remember { Animatable(minScale) }
    val rightAnim = remember { Animatable(minScale) }

    // Left dot: no initial delay.
    LaunchedEffect(Unit) {
        val spec = tween<Float>(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing)
        while (true) {
            leftAnim.animateTo(1f, animationSpec = spec)
            leftAnim.animateTo(minScale, animationSpec = spec)
        }
    }

    // Middle dot: start a bit later.
    LaunchedEffect(Unit) {
        delay(delayStep.toLong())
        val spec = tween<Float>(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing)
        while (true) {
            middleAnim.animateTo(1f, animationSpec = spec)
            middleAnim.animateTo(minScale, animationSpec = spec)
        }
    }

    // Right dot: start even later.
    LaunchedEffect(Unit) {
        delay((delayStep * 2L))
        val spec = tween<Float>(durationMillis = cycleDurationMs, easing = FastOutSlowInEasing)
        while (true) {
            rightAnim.animateTo(1f, animationSpec = spec)
            rightAnim.animateTo(minScale, animationSpec = spec)
        }
    }

    // Ensure left-to-right visual order regardless of app RTL.
    CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(space),
            verticalAlignment = Alignment.CenterVertically
        ) {
            DotCell(color = color, maxSize = dotMaxSize, scale = leftAnim.value)
            DotCell(color = color, maxSize = dotMaxSize, scale = middleAnim.value)
            DotCell(color = color, maxSize = dotMaxSize, scale = rightAnim.value)
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
                .scale(scale.coerceAtLeast(0f))  // scale around center, guard against negatives
                .background(color, CircleShape)
        )
    }
}
