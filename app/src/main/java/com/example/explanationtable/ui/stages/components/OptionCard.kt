package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A customizable option card component with a press animation and shadow effect.
 *
 * @param label The text label displayed on the card.
 * @param onClick Callback triggered when the card is tapped.
 * @param backgroundColor The primary color of the card.
 * @param shadowColor The color used for the shadow effect.
 * @param textColor The color of the label text.
 * @param imageResId Resource ID for the image displayed on the card.
 */
@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    imageResId: Int
) {
    // Constant offset value used for the shadow and press animation.
    val shadowOffset = 4.dp

    // State to track whether the card is pressed.
    var isPressed by remember { mutableStateOf(false) }

    // Animate the vertical offset of the card when pressed.
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 30) // Duration can be adjusted as needed.
    )

    // Modifier that handles pointer input and gesture detection for press animations.
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            // Wait for the initial press down.
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait until the gesture is lifted or cancelled.
            val upEvent = waitForUpOrCancellation()
            isPressed = false

            // If the gesture completes normally (not cancelled), invoke the onClick callback.
            if (upEvent != null) {
                onClick()
            }
        }
    }

    // Main container for the card, applying full width, fixed height, and gesture detection.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(gestureModifier) // Apply custom gesture detection.
    ) {
        // Background shadow card with a fixed offset.
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset), // Apply fixed shadow offset.
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation.
        ) { /* Empty content for shadow effect */ }

        // Foreground card that animates vertically based on press state.
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffsetY), // Animated offset for press effect.
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation.
        ) {
            // Layout container for the card content.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Box for the image with centered alignment.
                Box(
                    modifier = Modifier.size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }

                // Spacer to add horizontal separation between the image and text.
                Spacer(modifier = Modifier.width(16.dp))

                // Text label displayed on the card.
                Text(
                    text = label,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
