package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
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

@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    imageResId: Int
) {
    // Define the shadow offset
    val shadowOffset = 4.dp

    // Press state and animation
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = 30) // Adjust duration as needed
    )

    // Handle pointer input for press detection
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            // Wait for the first down event
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait for the up or cancellation event
            val up = waitForUpOrCancellation()
            isPressed = false

            // If the gesture was not canceled, trigger the onClick callback
            if (up != null) {
                onClick()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .then(gestureModifier) // Apply gesture detection
    ) {
        // Background Rectangle (Shadow)
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset), // Fixed shadow offset
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation
        ) {}

        // Foreground Card with animated offset
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffsetY), // Apply animated offset
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

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