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
 * A composable representing a 3D-like stacked square with a centered letter.
 *
 * This composable creates a tactile, layered effect using multiple Box layers,
 * smooth color transitions via Compose's animation APIs, and gesture handling for press feedback.
 *
 * @param isDarkTheme Determines the theme for color selection.
 * @param letter The letter to be displayed at the center.
 * @param isSelected Indicates if the square is currently selected.
 * @param isTransitioningToCorrect Indicates if the square is transitioning to a correct state.
 * @param modifier Modifier for external styling and layout adjustments.
 */
@Composable
fun StackedSquare3D(
    isDarkTheme: Boolean,
    letter: String,
    isSelected: Boolean,
    isTransitioningToCorrect: Boolean = false,
    modifier: Modifier = Modifier
) {
    // --- State and Animation Setup ---

    // Temporary state to drive the selection animation.
    val isHoldingSelection = remember { mutableStateOf(false) }

    // Animate the front background color based on the selection and transition states.
    val frontColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value ->
                if (isDarkTheme) DarkBlueBackground else Iguana
            isTransitioningToCorrect ->
                if (isDarkTheme) DarkGreenBackground else SeaSponge
            else ->
                if (isDarkTheme) BackgroundDark else BackgroundLight
        },
        animationSpec = tween(durationMillis = 150)
    )

    // Animate the border color based on the selection and transition states.
    val borderColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value ->
                if (isDarkTheme) DarkBlueBorder else BlueJay
            isTransitioningToCorrect ->
                if (isDarkTheme) DarkGreenBorder else Turtle
            else ->
                if (isDarkTheme) BorderDark else BorderLight
        },
        animationSpec = tween(durationMillis = 150)
    )

    // Animate the text color based on the selection and transition states.
    val textColor by animateColorAsState(
        targetValue = when {
            isSelected || isHoldingSelection.value ->
                if (isDarkTheme) DarkBlueText else Whale
            isTransitioningToCorrect ->
                if (isDarkTheme) DarkGreenText else TreeFrog
            else ->
                if (isDarkTheme) TextDarkMode else Eel
        },
        animationSpec = tween(durationMillis = 150)
    )

    // Trigger a temporary selection effect when the square is selected.
    LaunchedEffect(isSelected) {
        if (isSelected) {
            isHoldingSelection.value = true
            delay(150)
            isHoldingSelection.value = false
        }
    }

    // A constant scale animation (currently static at 1f) for future enhancements.
    val scaleAnimation by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 100)
    )

    // --- Layout Dimensions ---
    val cellSize = 80.dp
    val innerSize = 75.dp
    val outerHeight = 82.dp
    val cornerRadius = 16.dp
    val innerCornerRadius = 13.dp
    val defaultOffsetY = 2.dp

    // --- Press Feedback Animation Setup ---

    // Track whether the cell is currently pressed.
    var isPressed by remember { mutableStateOf(false) }
    val density = LocalDensity.current // For dp <-> px conversions

    // Animate vertical offset to simulate a tactile press effect.
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(density) { defaultOffsetY.toPx() } else 0f,
        animationSpec = tween(durationMillis = 50)
    )
    // Convert the animated offset from pixels back to dp.
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    // Define gesture handling to update the press state.
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown() // Wait for the initial touch down.
            isPressed = true
            waitForUpOrCancellation() // Await the touch release or cancellation.
            isPressed = false
        }
    }

    // --- Layout Composition ---
    Box(
        modifier = modifier
            .width(cellSize)
            .height(outerHeight)
            .scale(scaleAnimation)
            .then(gestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            // Shadow Layer: creates a 3D-like shadow effect.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = defaultOffsetY)
                    .size(cellSize)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(borderColor)
            )

            // Dynamic Border Layer: shifts with press offset for tactile feedback.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp)
                    .size(cellSize)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(borderColor)
            )

            // Front Layer: contains the animated background and centered text.
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp)
                    .size(innerSize)
                    .clip(RoundedCornerShape(innerCornerRadius))
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
