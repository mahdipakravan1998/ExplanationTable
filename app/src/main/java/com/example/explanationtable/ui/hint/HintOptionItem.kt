package com.example.explanationtable.ui.hint

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.utils.toPersianDigits
import kotlinx.coroutines.coroutineScope

/**
 * Composable displaying an interactive hint option with press animation.
 *
 * This component shows a fee (with a gem icon) and hint text.
 * A press gesture triggers a subtle vertical offset animation that simulates a press effect.
 *
 * @param modifier Modifier to adjust the layout or behavior.
 * @param hintOption Data object containing hint details and fee mapping.
 * @param difficulty Current difficulty level used to determine the fee.
 * @param isDarkTheme Boolean flag indicating if dark theme is active.
 * @param backgroundColor Background color for the inner content.
 * @param onClick Callback invoked when the item is clicked.
 */
@Composable
fun HintOptionItem(
    modifier: Modifier = Modifier,
    hintOption: HintOption,
    difficulty: Difficulty,
    isDarkTheme: Boolean,
    backgroundColor: Color,
    onClick: () -> Unit
) {
    // Determine the fee based on the current difficulty level.
    val fee = remember(difficulty) { hintOption.feeMap[difficulty] ?: 0 }

    // Choose border and text colors based on the theme.
    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // UI layout constants.
    val itemHeight = 72.dp
    val shadowOffset = 2.dp
    val cornerRadius = 12.dp
    val borderWidth = 2.dp
    val animationDurationMillis = 30  // Duration in milliseconds for the press effect animation.

    // State variable tracking whether the item is pressed.
    var isPressed by remember { mutableStateOf(false) }
    // Animate vertical offset based on the press state.
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = animationDurationMillis)
    )

    // Gesture modifier to detect press and release events.
    val pressGestureModifier = Modifier.pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                // Wait for the first pointer down event.
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                // Wait for the pointer up or cancellation event.
                val upEvent = waitForUpOrCancellation()
                isPressed = false
                // Uncomment the following line to trigger the onClick callback if needed.
                // if (upEvent != null) { onClick() }
            }
        }
    }

    // Main container that applies gesture detection, dimensions, and animation.
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(itemHeight)
            .then(pressGestureModifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // Shadow layer: renders a subtle drop shadow effect for depth.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(borderColor)
        )
        // Content layers: border and inner content that both participate in the press animation.
        Box {
            // Border layer: shifts vertically based on the press animation.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = pressOffset)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(borderColor)
            )
            // Inner content layer: displays fee (with gem icon) on the left and hint text on the right.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .offset(y = pressOffset)
                    .clip(RoundedCornerShape(cornerRadius - borderWidth))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Left section: fee display alongside the gem icon.
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = 8.dp)
                    ) {
                        Text(
                            text = fee.toPersianDigits().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(id = R.drawable.ic_gem),
                            contentDescription = null
                        )
                    }
                    // Right section: displays the hint text.
                    Spacer(modifier = Modifier.weight(1f))
                    Text(
                        text = hintOption.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                }
            }
        }
    }
}
