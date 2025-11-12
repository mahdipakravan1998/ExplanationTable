package com.example.explanationtable.ui.stages.components

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private const val PRESS_ANIM_DURATION_MS: Int = 30
private const val POST_RELEASE_DELAY_MS: Long = 120L  // allow press/release to finish cleanly

/**
 * A text-only option button with a press animation and shadow/lift effect.
 *
 * The label is centered horizontally and vertically. Height defaults to 56.dp
 * (Material touch-friendly size) and will never go below a 48.dp minimum touch target.
 *
 * @param label The text label displayed on the button.
 * @param onClick Callback triggered after press/release completes.
 * @param backgroundColor The primary color of the button surface.
 * @param shadowColor The color used for the shadow/background offset card.
 * @param textColor The color of the label text.
 * @param cardHeight The desired height of the button (defaults to 56.dp, min 48.dp).
 */
@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    cardHeight: Dp = 64.dp
) {
    val shadowOffset = 4.dp
    val clampedHeight = if (cardHeight < 56.dp) 56.dp else cardHeight

    var isPressed by remember { mutableStateOf(false) }

    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = PRESS_ANIM_DURATION_MS),
        label = "OptionCardPressOffset"
    )

    val gestureModifier = Modifier.pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val upEvent = waitForUpOrCancellation()
                isPressed = false
                if (upEvent != null) {
                    launch {
                        delay(POST_RELEASE_DELAY_MS)
                        onClick()
                    }
                }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(clampedHeight)
            .then(gestureModifier)
    ) {
        // Shadow/background card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) { /* shadow */ }

        // Foreground/lifting card
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffsetY),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            // Centered text-only content with horizontal padding
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        lineHeight = 22.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    textAlign = TextAlign.Center,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}
