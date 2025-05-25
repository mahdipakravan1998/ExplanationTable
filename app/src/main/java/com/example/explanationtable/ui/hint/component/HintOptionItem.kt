package com.example.explanationtable.ui.hint.component

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.utils.toPersianDigits

/**
 * Renders a tappable hint option displaying a gem fee and hint text.
 *
 * On press:
 *  1. Applies a subtle downward offset (or disabled offset).
 *  2. On release, resets the offset and invokes [onClick].
 *
 * @param modifier        Optional [Modifier] for layout customization.
 * @param hintOption      Provides the text and fee mapping.
 * @param difficulty      Current difficulty level to select the fee.
 * @param isDarkTheme     Whether the dark theme is active.
 * @param backgroundColor Inner background color of the content box.
 * @param balance         Current user gem balance (to disable if insufficient).
 * @param onClick         Lambda to invoke when the item is tapped.
 */
@Composable
fun HintOptionItem(
    modifier: Modifier = Modifier,
    hintOption: HintOption,
    difficulty: Difficulty,
    isDarkTheme: Boolean,
    backgroundColor: Color,
    balance: Int,
    onClick: () -> Unit
) {
    // --- Compute the fee for the current difficulty (default 0 if absent) ---
    val fee by remember(hintOption, difficulty) {
        derivedStateOf { hintOption.feeMap[difficulty] ?: 0 }
    }

    // --- Determine enabled/disabled state based on balance ---
    val isDisabled by remember(balance, fee) {
        derivedStateOf { balance < fee }
    }

    // --- Colors based on theme ---
    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // --- Pressed state and animated vertical offset ---
    var isPressed by remember { mutableStateOf(false) }
    val offsetY by animateDpAsState(
        targetValue = if (isPressed || isDisabled) Defaults.PressedOffset else 0.dp,
        animationSpec = tween(durationMillis = Defaults.AnimationDuration)
    )

    // --- Modifier that handles full-width sizing, height, and tap gestures ---
    val tapModifier = modifier
        .fillMaxWidth()
        .height(Defaults.ItemHeight)
        .pointerInput(isDisabled) {
            detectTapGestures(
                onPress = {
                    if (!isDisabled) {
                        isPressed = true             // start press animation
                        tryAwaitRelease()            // wait for lift or cancel
                        isPressed = false            // end press animation
                        onClick()                    // trigger callback
                    }
                }
            )
        }

    // --- Root container that stacks shadow, border, and content layers ---
    Box(
        modifier = tapModifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // 1) Static “shadow” layer for depth when unpressed
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = Defaults.PressedOffset)
                .clip(RoundedCornerShape(Defaults.CornerRadius))
                .background(borderColor)
        )

        // 2) Moving border layer and inner content
        Box {
            // Border moves up/down with press
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = offsetY)
                    .clip(RoundedCornerShape(Defaults.CornerRadius))
                    .background(borderColor)
            )

            // Inner content: fee + hint text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = offsetY)
                    .padding(Defaults.BorderWidth)
                    .clip(RoundedCornerShape(Defaults.CornerRadius - Defaults.BorderWidth))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(Defaults.ContentPadding),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // --- Fee display (Persian digits + gem icon) ---
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = Defaults.IconPadding)
                    ) {
                        Text(
                            text = fee.toPersianDigits().toString(),
                            style = MaterialTheme.typography.titleMedium,
                            color = textColor
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Image(
                            painter = painterResource(R.drawable.ic_gem),
                            contentDescription = "Gem icon"
                        )
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    // --- Hint text label ---
                    Text(
                        text = hintOption.displayText,
                        style = MaterialTheme.typography.titleMedium,
                        color = textColor
                    )
                }

                // Overlay to indicate disabled state
                if (isDisabled) {
                    Box(
                        modifier = Modifier
                            .matchParentSize()
                            .clip(RoundedCornerShape(Defaults.CornerRadius - Defaults.BorderWidth))
                            .background(borderColor.copy(alpha = 0.5f))
                    )
                }
            }
        }
    }
}

/**
 * Shared dimension, shape, and animation constants.
 */
private object Defaults {
    val ItemHeight:    Dp = 72.dp
    val CornerRadius:  Dp = 12.dp
    val BorderWidth:   Dp = 2.dp
    val PressedOffset: Dp = 2.dp
    const val AnimationDuration: Int = 30    // milliseconds
    val ContentPadding: Dp = 16.dp
    val IconPadding:    Dp = 8.dp
}
