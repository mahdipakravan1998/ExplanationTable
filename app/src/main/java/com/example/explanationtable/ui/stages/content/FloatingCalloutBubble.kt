package com.example.explanationtable.ui.stages.content

import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.explanationtable.R
import com.example.explanationtable.ui.stages.components.CalloutBubble
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sqrt

/** Symmetric smooth bob easing. */
internal val EaseInOutSine: Easing = Easing { x -> 0.5f - 0.5f * cos((Math.PI * x).toFloat()) }

/**
 * A tiny leaf that owns the floating callout bubble’s bob animation.
 * Animation runs only when: lifecycle is RESUMED, anchor is on-screen, and content is not moving.
 *
 * @param anchorInWindow top point of the button’s front ellipse (global coords)
 * @param rootTopLeftInWindow top-left of the root overlay box (global coords)
 */
@Composable
internal fun FloatingCalloutBubble(
    isDarkTheme: Boolean,
    anchorInWindow: Offset?,
    rootTopLeftInWindow: Offset,
    viewportHeightPx: Int,
    isContentMoving: Boolean
) {
    if (anchorInWindow == null || viewportHeightPx <= 0) return

    val lifecycleOwner = LocalLifecycleOwner.current
    var isResumed by remember {
        mutableStateOf(
            lifecycleOwner.lifecycle.currentState.isAtLeast(
                Lifecycle.State.RESUMED
            )
        )
    }
    DisposableEffect(lifecycleOwner) {
        val obs = LifecycleEventObserver { _, event ->
            isResumed = when (event) {
                Lifecycle.Event.ON_RESUME -> true
                Lifecycle.Event.ON_PAUSE, Lifecycle.Event.ON_STOP, Lifecycle.Event.ON_DESTROY -> false
                else -> isResumed
            }
        }
        lifecycleOwner.lifecycle.addObserver(obs)
        onDispose { lifecycleOwner.lifecycle.removeObserver(obs) }
    }

    val density = LocalDensity.current

    // Local (root-relative) anchor
    val localAnchor = remember(anchorInWindow, rootTopLeftInWindow) {
        Offset(
            x = anchorInWindow.x - rootTopLeftInWindow.x,
            y = anchorInWindow.y - rootTopLeftInWindow.y
        )
    }

    // Bubble size for centering; measured once and only written if changed
    var bubbleW by remember { mutableStateOf(0) }
    var bubbleH by remember { mutableStateOf(0) }

    // Exact geometric compensation for the triangular tip (must match CalloutBubble)
    val tipCompensationPx = remember(density) {
        val triBasePx = with(density) { 15.dp.toPx() }
        val triHPx = with(density) { 10.dp.toPx() }
        val cornerPx = with(density) { 10.dp.toPx() }
        val halfBase = triBasePx / 2f
        val triRound = min(min(cornerPx * 0.8f, triHPx * 0.7f), halfBase * 0.95f)
        val lenR = sqrt(halfBase * halfBase + triHPx * triHPx)
        val uy = triHPx / lenR
        (uy * triRound) / 2f
    }

    // Visibility estimate (don’t animate off-screen). Small margin avoids thrash.
    val marginPx = with(density) { 48.dp.toPx() }
    val anchorY = localAnchor.y
    val estimatedVisible = anchorY > -marginPx && anchorY < viewportHeightPx + marginPx

    // Same spec: 1000ms half-cycle, reverse, sine ease
    val bobAmplitudePx = with(density) { 7.dp.toPx() }
    val shouldAnimate = isResumed && estimatedVisible && !isContentMoving

    val bobOffsetYpx = if (shouldAnimate) {
        val infinite = rememberInfiniteTransition(label = "callout-bob-leaf")
        val bobPhase by infinite.animateFloat(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 1000, easing = EaseInOutSine),
                repeatMode = RepeatMode.Reverse
            ),
            label = "bob-phase"
        )
        bobPhase * bobAmplitudePx
    } else 0f

    // Center horizontally on anchor; align visible tip to anchor at the *topmost* point
    val leftPx = (localAnchor.x - (if (bubbleW == 0) 1 else bubbleW) / 2f)
    val topAtRestPx = localAnchor.y - (if (bubbleH == 0) 1 else bubbleH) + tipCompensationPx
    val animatedTopPx = topAtRestPx + bobOffsetYpx

    // If completely off-screen, skip composing the bubble entirely
    if (!estimatedVisible) return

    Box(
        modifier = Modifier
            .graphicsLayer {
                translationX = leftPx
                translationY = animatedTopPx
                transformOrigin = TransformOrigin(0.5f, 0.0f)
            }
            .zIndex(5f)
            .onGloballyPositioned { coords ->
                val nw = coords.size.width
                val nh = coords.size.height
                if (nw != bubbleW) bubbleW = nw
                if (nh != bubbleH) bubbleH = nh
            }
    ) {
        // Localized string resource (defaults to Persian text to preserve visuals).
        CalloutBubble(
            isDarkTheme = isDarkTheme,
            text = stringResource(R.string.stages_callout_start)
        )
    }
}
