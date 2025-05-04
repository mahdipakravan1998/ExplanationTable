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
 * Displays an interactive hint option with a gem fee and hint text.
 * Tapping the item triggers a subtle press animation and invokes [onClick].
 *
 * @param modifier        Optional [Modifier] for this component.
 * @param hintOption      Provides [displayText] and a fee map per [Difficulty].
 * @param difficulty      Current difficulty, used to look up the fee.
 * @param isDarkTheme     Whether the dark theme is active.
 * @param backgroundColor Inner background color for the hint content.
 * @param onClick         Callback invoked when the user taps the item.
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
    // -- Fee lookup -------------------------------------------------------------
    // Get the fee for the current difficulty (default to 0 if missing).
    val fee = remember(difficulty) { hintOption.feeMap[difficulty] ?: 0 }

    // -- Theme-based colors -----------------------------------------------------
    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // -- Press state & animation -----------------------------------------------
    var isPressed by remember { mutableStateOf(false) }
    val verticalOffset by animateDpAsState(
        targetValue = if (isPressed) HintOptionDefaults.PRESSED_OFFSET else 0.dp,
        animationSpec = tween(durationMillis = HintOptionDefaults.ANIMATION_DURATION)
    )

    // -- Combined modifier: size, gesture, and animation ------------------------
    val interactiveModifier = modifier
        .fillMaxWidth()
        .height(HintOptionDefaults.ITEM_HEIGHT)
        .pointerInput(Unit) {
            detectTapGestures(
                onPress = {
                    // Press begins: animate down
                    isPressed = true
                    // Await release or cancellation
                    tryAwaitRelease()
                    // Press ends: animate up, then invoke click
                    isPressed = false
                    onClick()
                }
            )
        }

    // -- Composable layout -----------------------------------------------------
    Box(
        modifier = interactiveModifier,
        contentAlignment = Alignment.TopCenter
    ) {
        // 1) Static shadow layer for depth
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = HintOptionDefaults.PRESSED_OFFSET)
                .clip(RoundedCornerShape(HintOptionDefaults.CORNER_RADIUS))
                .background(borderColor)
        )

        // 2) Animated border + content layers
        Box {
            // Border: moves with press animation
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = verticalOffset)
                    .clip(RoundedCornerShape(HintOptionDefaults.CORNER_RADIUS))
                    .background(borderColor)
            )

            // Inner content: fee + text
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(HintOptionDefaults.BORDER_WIDTH)
                    .offset(y = verticalOffset)
                    .clip(RoundedCornerShape(HintOptionDefaults.CORNER_RADIUS - HintOptionDefaults.BORDER_WIDTH))
                    .background(backgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    modifier = Modifier.padding(HintOptionDefaults.CONTENT_PADDING),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Fee display: Persian digits + gem icon
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(end = HintOptionDefaults.ICON_PADDING)
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

                    Spacer(modifier = Modifier.weight(1f))

                    // Hint text
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

/**
 * Default dimensions and animation specs for [HintOptionItem].
 */
private object HintOptionDefaults {
    val ITEM_HEIGHT = 72.dp
    val CORNER_RADIUS = 12.dp
    val BORDER_WIDTH = 2.dp
    val PRESSED_OFFSET = 2.dp
    val ANIMATION_DURATION = 30 // ms
    val CONTENT_PADDING = 16.dp
    val ICON_PADDING = 8.dp
}
