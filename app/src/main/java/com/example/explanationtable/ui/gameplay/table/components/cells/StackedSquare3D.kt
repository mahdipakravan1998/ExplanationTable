package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BlueJay
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.DarkBlueBackground
import com.example.explanationtable.ui.theme.DarkBlueBorder
import com.example.explanationtable.ui.theme.DarkBlueText
import com.example.explanationtable.ui.theme.DarkGreenBackground
import com.example.explanationtable.ui.theme.DarkGreenBorder
import com.example.explanationtable.ui.theme.DarkGreenText
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.Iguana
import com.example.explanationtable.ui.theme.SeaSponge
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.ui.theme.TreeFrog
import com.example.explanationtable.ui.theme.Turtle
import com.example.explanationtable.ui.theme.VazirmatnFontFamily
import com.example.explanationtable.ui.theme.Whale
import kotlinx.coroutines.delay

/**
 * A 3D-like stacked square with a letter displayed at the center.
 */
@Composable
fun StackedSquare3D(
    isDarkTheme: Boolean,
    letter: String,
    isSelected: Boolean,
    isTransitioningToCorrect: Boolean = false,
    modifier: Modifier = Modifier
) {
    val isHoldingSelection = remember { mutableStateOf(false) }
    val isResetting = remember { mutableStateOf(false) }

    val frontColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value -> if (isDarkTheme) DarkBlueBackground else Iguana
            isTransitioningToCorrect -> if (isDarkTheme) DarkGreenBackground else SeaSponge
            isResetting.value -> if (isDarkTheme) BackgroundDark else BackgroundLight
            else -> if (isDarkTheme) BackgroundDark else BackgroundLight
        },
        animationSpec = tween(durationMillis = 150), label = "" // Smooth transition
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value -> if (isDarkTheme) DarkBlueBorder else BlueJay
            isTransitioningToCorrect -> if (isDarkTheme) DarkGreenBorder else Turtle
            isResetting.value -> if (isDarkTheme) BorderDark else BorderLight
            else -> if (isDarkTheme) BorderDark else BorderLight
        },
        animationSpec = tween(durationMillis = 150), label = ""
    )

    val textColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value -> if (isDarkTheme) DarkBlueText else Whale
            isTransitioningToCorrect -> if (isDarkTheme) DarkGreenText else TreeFrog
            isResetting.value -> if (isDarkTheme) TextDarkMode else Eel
            else -> if (isDarkTheme) TextDarkMode else Eel
        },
        animationSpec = tween(durationMillis = 150), label = ""
    )

    LaunchedEffect(isSelected) {
        if (isSelected) {
            isHoldingSelection.value = true
            delay(150) // Shorter duration
            isHoldingSelection.value = false
            isResetting.value = true
            delay(100) // Match reset animation duration
            isResetting.value = false
        }
    }

    var scale by remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 100), label = ""
    )

    val offsetY = 2.dp
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(LocalDensity.current) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 50), label = ""
    )

    val density = LocalDensity.current
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown()
            isPressed = true
            waitForUpOrCancellation()
            isPressed = false
        }
    }

    Box(
        modifier = modifier
            .width(80.dp)
            .height(82.dp)
            .scale(scaleAnimation)
            .then(gestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = offsetY)
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp)
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp)
                    .size(75.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(frontColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    color = textColor,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    fontFamily = VazirmatnFontFamily
                )
            }
        }
    }
}
