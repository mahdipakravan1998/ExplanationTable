package com.example.explanationtable.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.disabled
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.sfx.LocalUiSoundManager
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A reusable elevated tile-style icon button with a custom "press down" animation.
 *
 * Visuals/behavior unchanged. Accessibility is provided by merging child semantics (icon label)
 * and setting button role and onClick here.
 */
@Composable
fun ActionTile(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: @Composable BoxScope.() -> Unit
) {
    // Timings: fast press animation + tiny motion buffer before firing action.
    val pressAnimDurationMs = 50
    val postReleaseDelayMs = 120L

    // Press state & animation
    var isPressed by remember { mutableStateOf(false) }
    val defaultOffset = 2.dp
    val pressOffset: Dp by animateDpAsState(
        targetValue = if (isPressed) defaultOffset else 0.dp,
        animationSpec = tween(durationMillis = pressAnimDurationMs),
        label = "pressOffset"
    )

    val latestOnClick by rememberUpdatedState(onClick)
    val scope = rememberCoroutineScope()
    val uiSoundManager = LocalUiSoundManager.current

    // Sizes, shapes, colors
    val cellSize = 52.dp
    val innerSize = 47.dp
    val outerHeight = 54.dp
    val outerShape = remember { RoundedCornerShape(16.dp) }
    val innerShape = remember { RoundedCornerShape(13.dp) }
    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val backgroundColor = if (isDarkTheme) BackgroundDark else BackgroundLight

    // Gestures (only when enabled)
    val gestureModifier =
        if (enabled) {
            Modifier.pointerInput(enabled) {
                detectTapGestures(
                    onPress = {
                        uiSoundManager.playClick()
                        isPressed = true
                        val released = tryAwaitRelease()
                        isPressed = false
                        if (released) {
                            // Give the release animation a beat to complete before acting.
                            scope.launch {
                                delay(postReleaseDelayMs)
                                latestOnClick()
                            }
                        }
                    }
                )
            }
        } else {
            Modifier
        }

    // Semantics: merge child descriptions (icon), add button role and onClick
    val semanticsModifier = Modifier.semantics(mergeDescendants = true) {
        role = Role.Button
        if (!enabled) disabled()
        if (enabled) {
            onClick(action = {
                // Mirror the same tiny buffer for accessibility-initiated clicks.
                scope.launch {
                    uiSoundManager.playClick()
                    delay(postReleaseDelayMs)
                    latestOnClick()
                }
                true
            })
        }
    }

    // Layout: layered boxes for depth & press effect
    Box(
        modifier = modifier
            .size(width = cellSize, height = outerHeight)
            .then(gestureModifier)
            .then(semanticsModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // 1) Shadow layer: static
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = defaultOffset)
                .size(cellSize)
                .clip(outerShape)
                .background(borderColor)
        )

        // 2) Border layer: moves when pressed
        Box(
            Modifier
                .align(Alignment.Center)
                .offset(y = pressOffset)
                .size(cellSize)
                .clip(outerShape)
                .background(borderColor)
        )

        // 3) Front/content: holds the icon
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
 * Helper to draw the arrow with the given [direction].
 */
@Composable
fun ArrowIcon(
    direction: ArrowDirection,
    contentDescription: String,
    size: Dp = 20.dp,
    modifier: Modifier = Modifier
) {
    Image(
        painter = painterResource(id = R.drawable.ic_arrow),
        contentDescription = contentDescription,
        modifier = modifier
            .size(size)
            .rotate(direction.rotationZ())
    )
}
