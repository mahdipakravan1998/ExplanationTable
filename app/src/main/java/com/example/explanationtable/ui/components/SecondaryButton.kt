package com.example.explanationtable.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A secondary button with a dynamic layered design.
 *
 * The button is composed of two main layers:
 * 1. The top layer is split into two attached sublayers:
 *    - A larger gradient border layer. The gradient goes from the top right (#77F4CA)
 *      to the bottom left (#63BDFF).
 *    - An inner layer (inset by 2.dp on each side) whose background is the page’s background color.
 * 2. A shadow layer in the background that also uses the same gradient.
 *
 * The button size and press animation are based on the PrimaryButton.
 *
 * @param onClick Invoked when the button is clicked.
 * @param text The text to display on the button.
 * @param modifier Modifier to be applied to the button container.
 */
@Composable
fun SecondaryButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    // Gradient colors for the border and shadow layers.
    val gradientColors = listOf(Color(0xFF77F4CA), Color(0xFF63BDFF))
    // Determine the text color based on the theme:
    // Day mode: "#2292A9", Night mode: "#43D1BB"
    val textColor = if (isDarkTheme) Color(0xFF43D1BB) else Color(0xFF2292A9)
    // Page background color from MaterialTheme.
    val pageBackgroundColor = MaterialTheme.colorScheme.background

    // Constants (using the same sizes as PrimaryButton)
    val buttonHeight = 56.dp
    val shadowOffset = 3.dp
    val cornerRadius = 18.dp
    val borderWidth = 2.dp
    val animationDuration = 30

    // State for press animation.
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetDp by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = animationDuration), label = ""
    )

    // Gesture detector to handle press interactions.
    val gestureModifier = Modifier.pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val upEvent = waitForUpOrCancellation()
                isPressed = false
                if (upEvent != null) {
                    launch {
                        delay(50) // Delay to allow animation to complete
                        onClick()
                    }
                }
            }
        }
    }

    // --- Layout Composition ---
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .then(gestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // Background Shadow Layer with gradient.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(
                    brush = Brush.linearGradient(
                        colors = gradientColors,
                        start = Offset(100f, 0f),
                        end = Offset(0f, 100f)
                    )
                )
        )
        // Top Layer (composed of the gradient border and inner layers).
        Box {
            // Gradient Border Layer (larger layer).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = pressOffsetDp)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(
                        brush = Brush.linearGradient(
                            colors = gradientColors,
                            start = Offset(100f, 0f),
                            end = Offset(0f, 100f)
                        )
                    )
            )
            // Inner Layer: inset by 2.dp on each side, using the page background color.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .offset(y = pressOffsetDp)
                    .clip(RoundedCornerShape(cornerRadius - borderWidth))
                    .background(pageBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}
