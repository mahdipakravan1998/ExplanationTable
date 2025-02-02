package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.FeatherGreen
import com.example.explanationtable.ui.theme.Polar
import com.example.explanationtable.ui.theme.VazirmatnFontFamily
import com.example.explanationtable.ui.theme.White
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * A composable that displays a bright green square with a centered letter,
 * overlaid with an animated star icon.
 *
 * The star icon undergoes a compound animation including:
 * - Translation: Moves diagonally in two phases.
 * - Rotation: Rotates from 0° to 90°.
 * - Scaling: Scales down to 0 after a delay.
 *
 * @param letter The character displayed at the center of the square.
 * @param modifier Optional [Modifier] for additional styling.
 */
@Composable
fun BrightGreenSquare(
    letter: String,
    modifier: Modifier = Modifier
) {
    // Animation configuration constants
    val animationTotalDuration = 800            // Total duration in milliseconds
    val phaseOneDuration = 440                  // Duration for the first phase of translation (milliseconds)
    val translationDistance = 6.dp              // Maximum translation distance for the star

    // Layout size constants
    val squareSize = 80.dp                      // Size of the green square
    val starSize = 15.dp                        // Size of the star icon

    // Animatables to control translation progress, rotation, and scaling
    val translationProgress = remember { Animatable(0f) }
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    // Launch concurrent animations when the composable is first composed
    LaunchedEffect(Unit) {
        // Animate translation in two phases:
        // Phase 1: Keyframe animation to reach 60% progress with non-linear pacing.
        launch {
            translationProgress.animateTo(
                targetValue = 0.6f,
                animationSpec = keyframes {
                    durationMillis = phaseOneDuration
                    0.5f at 120 using FastOutSlowInEasing    // 50% progress at 120ms
                    0.6f at phaseOneDuration using FastOutSlowInEasing  // 60% progress by end of phase one
                }
            )
            // Phase 2: Tween animation to complete translation from 60% to 100%.
            translationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationTotalDuration - phaseOneDuration,
                    easing = FastOutSlowInEasing
                )
            )
        }

        // Animate rotation from 0° to 90° over the entire duration.
        launch {
            rotationAnim.animateTo(
                targetValue = 90f,
                animationSpec = tween(
                    durationMillis = animationTotalDuration,
                    easing = LinearEasing
                )
            )
        }

        // Animate scaling: wait for phase one to complete, then scale down to 0.
        launch {
            delay(phaseOneDuration.toLong())
            scaleAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationTotalDuration - phaseOneDuration,
                    easing = LinearEasing
                )
            )
        }
    }

    // Calculate the current translation offsets based on animation progress.
    // Negative values move the star icon upward and to the left.
    val currentOffset = translationDistance * translationProgress.value
    val offsetX = -currentOffset
    val offsetY = -currentOffset

    // Main container with centered alignment for both the square and the star overlay.
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Bright green square with a centered letter.
        Box(
            modifier = Modifier
                .size(squareSize)
                .clip(RoundedCornerShape(16.dp))
                .background(FeatherGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirmatnFontFamily
            )
        }

        // Animated star overlay that applies translation, rotation, and scaling.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 12.dp)
                .wrapContentSize(Alignment.TopStart)
                .offset(x = offsetX, y = offsetY)
                .graphicsLayer(
                    scaleX = scaleAnim.value,
                    scaleY = scaleAnim.value,
                    rotationZ = rotationAnim.value
                )
                .size(starSize)
        ) {
            Image(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Sparkling Star",
                colorFilter = ColorFilter.tint(Polar)
            )
        }
    }
}
