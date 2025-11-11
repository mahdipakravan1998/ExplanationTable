package com.example.explanationtable.ui.components.buttons

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
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.IconCircleDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundLight
import com.example.explanationtable.ui.theme.TreeFrog
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Base pressable primary CTA with shared behavior.
 *
 * Visual contracts preserved:
 * - Colors are unchanged and selected by isDarkTheme exactly as before.
 * - Press animation duration is EXACTLY 30ms.
 * - A small post-up delay before invoking onClick is preserved (tunable per-variant).
 *
 * Enhancements:
 * - Accessibility semantics (role=Button, screen-reader click).
 * - `@Immutable` style for recomposition efficiency.
 * - `rememberUpdatedState` for the onClick lambda to avoid stale captures.
 */
@Immutable
data class PrimaryButtonStyle(
    val height: Dp,
    val cornerRadius: Dp,
    val horizontalPadding: Dp,
    val shadowOffset: Dp,
    val fontSize: TextUnit
)

// === original animation duration preserved ===
private const val PRIMARY_BUTTON_ANIMATION_DURATION_MS: Int = 30

// Default tiny motion buffer so release animation completes before action.
private const val DEFAULT_CLICK_DELAY_MS: Long = 120L

@Composable
fun PrimaryButtonBase(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    style: PrimaryButtonStyle,
    modifier: Modifier = Modifier,
    /**
     * Extra time to wait after finger-up before invoking [onClick].
     * Ensures release animation finishes and gives the destination a tiny warm-up window.
     */
    postReleaseDelayMs: Long = DEFAULT_CLICK_DELAY_MS
) {
    // Stable, up-to-date reference to onClick for gesture/semantics blocks.
    val onClickState = rememberUpdatedState(onClick)
    val scope = rememberCoroutineScope()

    // === DO NOT CHANGE: original color logic preserved ===
    val buttonBackgroundColor =
        if (isDarkTheme) PrizeButtonBackgroundDark else PrizeButtonBackgroundLight
    val buttonShadowColor =
        if (isDarkTheme) IconCircleDark else TreeFrog
    val buttonTextColor =
        if (isDarkTheme) BackgroundDark else DialogBackgroundLight

    var isPressed by remember { mutableStateOf(false) }

    val pressOffsetY by animateDpAsState(
        targetValue = if (isPressed) style.shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = PRIMARY_BUTTON_ANIMATION_DURATION_MS),
        label = "PrimaryButtonPressOffset"
    )

    // Pointer input replicates original press behavior without ripple.
    val gestureModifier = Modifier.pointerInput(Unit) {
        coroutineScope {
            awaitEachGesture {
                awaitFirstDown(requireUnconsumed = false)
                isPressed = true
                val up = waitForUpOrCancellation()
                isPressed = false
                if (up != null) {
                    // Wait long enough for the release animation to finish gracefully.
                    launch {
                        delay(postReleaseDelayMs)
                        onClickState.value.invoke()
                    }
                }
            }
        }
    }

    // Accessibility semantics without changing visuals.
    val semanticsModifier = Modifier.semantics(mergeDescendants = true) {
        role = Role.Button
        onClick(label = text) {
            // Mirror the same tiny buffer for a11y-triggered clicks.
            scope.launch {
                delay(postReleaseDelayMs)
                onClickState.value.invoke()
            }
            true
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(style.height)
            .then(gestureModifier)
            .then(semanticsModifier)
    ) {
        // Shadow layer
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = style.shadowOffset),
            shape = RoundedCornerShape(style.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = buttonShadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) { /* Decorative shadow card */ }

        // Foreground layer
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = pressOffsetY),
            shape = RoundedCornerShape(style.cornerRadius),
            colors = CardDefaults.cardColors(containerColor = buttonBackgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = style.horizontalPadding),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = style.fontSize
                    ),
                    color = buttonTextColor
                )
            }
        }
    }
}
