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
 * A three-layered stacked square (3D-ish effect) with a letter in the center.
 */
@Composable
fun StackedSquare3D(
    isDarkTheme: Boolean,
    letter: String,
    isSelected: Boolean,
    isTransitioningToCorrect: Boolean = false,
    modifier: Modifier = Modifier
) {
    // States for color management
    val isHoldingSelection = remember { mutableStateOf(false) }
    val isResetting = remember { mutableStateOf(false) }

    // Colors based on the theme and state
    val frontColor by animateColorAsState(
        targetValue = when {
            // A) Selected state
            isSelected || isHoldingSelection.value -> {
                if (isDarkTheme) DarkBlueBackground else Iguana
            }
            // C) Transitioning to correct
            isTransitioningToCorrect -> {
                if (isDarkTheme) DarkGreenBackground else SeaSponge
            }
            // Resetting or default
            isResetting.value -> if (isDarkTheme) BackgroundDark else BackgroundLight
            else -> if (isDarkTheme) BackgroundDark else BackgroundLight
        },
        animationSpec = tween(durationMillis = 150), // Smooth transition
        label = "Front Color Animation"
    )

    val borderColor by animateColorAsState(
        targetValue = when {
            // A) Selected state
            isSelected || isHoldingSelection.value -> {
                if (isDarkTheme) DarkBlueBorder else BlueJay
            }
            // C) Transitioning to correct
            isTransitioningToCorrect -> {
                if (isDarkTheme) DarkGreenBorder else Turtle
            }
            // Resetting or default
            isResetting.value -> if (isDarkTheme) BorderDark else BorderLight
            else -> if (isDarkTheme) BorderDark else BorderLight
        },
        animationSpec = tween(durationMillis = 150),
        label = "Border Color Animation"
    )

    val textColor by animateColorAsState(
        targetValue = when {
            // A) Selected state
            isSelected || isHoldingSelection.value -> {
                if (isDarkTheme) DarkBlueText else Whale
            }
            // C) Transitioning to correct
            isTransitioningToCorrect -> {
                if (isDarkTheme) DarkGreenText else TreeFrog
            }
            // Resetting or default
            isResetting.value -> if (isDarkTheme) TextDarkMode else Eel
            else -> if (isDarkTheme) TextDarkMode else Eel
        },
        animationSpec = tween(durationMillis = 150),
        label = "Text Color Animation"
    )

    // Handle the selection and hold logic
    LaunchedEffect(isSelected) {
        if (isSelected) {
            // Hold the selected color for a while
            isHoldingSelection.value = true
            delay(250) // Keep the selected color visible for 500ms (non-blocking)
            isHoldingSelection.value = false

            // Start resetting after the hold period
            isResetting.value = true
            delay(150) // Matches `animationSpec` duration (non-blocking)
            isResetting.value = false
        }
    }

    // Handle the scale animation on click
    var scale by remember { mutableFloatStateOf(1f) }
    val scaleAnimation by animateFloatAsState(
        targetValue = scale,
        animationSpec = tween(durationMillis = 50),
        label = "Scale Animation"
    )

    // 1) Track a pressed state + animate
    val offsetY = 2.dp  // This is the amount you want to move down
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(LocalDensity.current) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), // smooth transition
        label = "" // no label needed here
    )

    // Convert to dp for the UI
    val density = LocalDensity.current
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    // 2) Handle pointer input (press detection)
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait for finger up or cancel => pressed = false
            val upOrCancel = waitForUpOrCancellation()
            isPressed = false

            // If the user actually lifted (not canceled), it's a click
            if (upOrCancel != null) {
                // Optional: Handle click if needed
            }
        }
    }

    Box(
        modifier = modifier
            .width(80.dp)
            .height(82.dp)
            .scale(scaleAnimation) // Apply scale animation
            .then(gestureModifier), // Add gesture modifier to detect clicks
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            // 3rd square (back)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = offsetY)
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            // 2nd square (middle)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp) // Apply the animated offset here
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            // 1st square (front)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp) // Apply the same animated offset here
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