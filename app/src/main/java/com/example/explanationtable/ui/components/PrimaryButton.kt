package com.example.explanationtable.ui.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.IconCircleDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundLight
import com.example.explanationtable.ui.theme.TreeFrog

/**
 * A button with an animated press effect used for claiming a prize.
 *
 * @param onClick Callback invoked when the button is clicked.
 * @param text The text displayed on the button.
 * @param isDarkTheme Boolean indicating if the dark theme is active.
 * @param modifier Modifier to be applied to the button container.
 */
@Composable
fun PrimaryButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    // Define your button colors internally based on the current theme.
    val buttonBackgroundColor =
        if (isDarkTheme) PrizeButtonBackgroundDark else PrizeButtonBackgroundLight
    val buttonShadowColor =
        if (isDarkTheme) IconCircleDark else TreeFrog
    val buttonTextColor =
        if (isDarkTheme) BackgroundDark else DialogBackgroundLight

    // Constants used in the button layout and animation.
    val shadowOffset = 4.dp       // Offset to simulate depth for shadow.
    val buttonHeight = 56.dp      // Fixed height for the button.
    val cornerRadius = 18.dp      // Corner radius for rounded card shape.
    val horizontalPadding = 16.dp // Horizontal padding inside the button.
    val animationDuration = 30    // Duration of the press animation in milliseconds.

    // State to track whether the button is pressed.
    var isPressed by remember { mutableStateOf(false) }

    // Animate the vertical offset based on the press state to create a press effect.
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = animationDuration), label = ""
    )

    // Gesture detector to handle press interactions.
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            // Wait for the first press event.
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait until the gesture is lifted or cancelled.
            val upEvent = waitForUpOrCancellation()
            isPressed = false

            // Invoke the onClick callback if the press was completed normally.
            if (upEvent != null) {
                onClick()
            }
        }
    }

    // Main container for the button, combining the shadow and the animated foreground.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .then(gestureModifier)
    ) {
        // Shadow layer: a Card offset downward to simulate depth.
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = buttonShadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            // No inner content required for the shadow.
        }
        // Foreground layer: the main button that animates vertically on press.
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffsetY),
            shape = RoundedCornerShape(cornerRadius),
            colors = CardDefaults.cardColors(containerColor = buttonBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            // Centered text label within the button.
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = buttonTextColor
                )
            }
        }
    }
}
