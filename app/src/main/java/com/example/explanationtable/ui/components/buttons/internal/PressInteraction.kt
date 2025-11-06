package com.example.explanationtable.ui.components.buttons.internal

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation

/**
 * Encapsulates a press-driven offset and the gesture [Modifier] that drives it.
 *
 * The contract mirrors the original buttons:
 * - When pressed: offset animates to [shadowOffset].
 * - On release: offset returns to 0.dp and [onClick] fires after [clickDelayMillis].
 * - Animation duration is [animationDurationMillis].
 *
 * This isolates gesture/animation logic to keep UI components focused on drawing.
 */
@Stable
data class PressGesture(
    /** Current vertical press offset to apply to the top layer. */
    val pressOffset: Dp,
    /** Gesture modifier to attach to the clickable container. */
    val modifier: Modifier
)

/**
 * Remember a shared press gesture/animation state.
 *
 * @param shadowOffset How far the pressed top layer should visually "drop".
 * @param animationDurationMillis Duration of the press animation in ms.
 * @param clickDelayMillis Delay after finger-up before invoking [onClick].
 * @param enabled Whether the gesture is enabled.
 * @param onClick Click callback (kept fresh via rememberUpdatedState).
 */
@Composable
fun rememberPressGesture(
    shadowOffset: Dp,
    animationDurationMillis: Int,
    clickDelayMillis: Int = 50,
    enabled: Boolean = true,
    onClick: () -> Unit
): PressGesture {
    var isPressed by remember { mutableStateOf(false) }

    // Animate the press offset exactly like the original implementation.
    val pressOffset by animateDpAsState(
        targetValue = if (isPressed) shadowOffset else 0.dp,
        animationSpec = tween(durationMillis = animationDurationMillis),
        label = "press-offset"
    )

    // Keep latest onClick without restarting pointerInput on recomposition.
    val latestOnClick = rememberUpdatedState(onClick)

    val gesture = if (!enabled) {
        Modifier
    } else {
        Modifier.pointerInput(shadowOffset, animationDurationMillis, clickDelayMillis) {
            coroutineScope {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    isPressed = true
                    val up = waitForUpOrCancellation()
                    isPressed = false
                    if (up != null) {
                        launch {
                            delay(clickDelayMillis.toLong())
                            latestOnClick.value.invoke()
                        }
                    }
                }
            }
        }
    }

    return PressGesture(pressOffset = pressOffset, modifier = gesture)
}
