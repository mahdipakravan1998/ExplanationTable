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
import kotlinx.coroutines.launch

/**
 * A composable that displays a bright green square with a centered letter
 * overlaid by an animated star icon.
 *
 * The star icon undergoes three simultaneous animations:
 * - Translation: Moves diagonally (up-left) with a fast start and smooth deceleration.
 * - Rotation: Rotates from 0° to 90°.
 * - Scaling: Quickly scales up from 0 to 1 (appearing) and then scales down to 0.
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
    val animationTotalDuration = 800             // Total animation duration (ms)
    val fastPhaseDuration = 200                  // Duration of the fast phase (first 25% of total time)
    val translationDistance = 6.dp               // Maximum translation distance for the star

    // Layout size constants
    val squareSize = 80.dp                       // Size of the bright green square
    val starSize = 15.dp                         // Size of the star icon

    // Animatable values for translation, rotation, and scale animations.
    val translationProgress = remember { Animatable(0f) }
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(0f) }

    // Launch animations concurrently when this composable is first composed.
    LaunchedEffect(Unit) {
        // Animate translation progress using keyframes:
        // - Reach 50% of the progress quickly (at fastPhaseDuration).
        // - Complete translation by the end of the total duration.
        launch {
            translationProgress.animateTo(
                targetValue = 1f,
                animationSpec = keyframes {
                    durationMillis = animationTotalDuration
                    0.5f at fastPhaseDuration using FastOutSlowInEasing
                    1f at animationTotalDuration using FastOutSlowInEasing
                }
            )
        }

        // Animate rotation from 0° to 90° over the total duration with a linear easing.
        launch {
            rotationAnim.animateTo(
                targetValue = 90f,
                animationSpec = tween(
                    durationMillis = animationTotalDuration,
                    easing = LinearEasing
                )
            )
        }

        // Animate scaling:
        // 1. Scale up quickly from 0 to 1 (appearing) during the fast phase.
        // 2. Scale down from 1 to 0 over the remainder of the animation.
        launch {
            // Scale up (star "appearing")
            scaleAnim.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = fastPhaseDuration,
                    easing = FastOutSlowInEasing
                )
            )
            // Scale down (star "disappearing")
            scaleAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(
                    durationMillis = animationTotalDuration - fastPhaseDuration,
                    easing = LinearEasing
                )
            )
        }
    }

    // Calculate the current offset for the star icon based on the translation animation progress.
    // Negative values shift the star upward and to the left.
    val currentOffset = translationDistance * translationProgress.value
    val offsetX = -currentOffset
    val offsetY = -currentOffset

    // Main container aligning both the green square and the animated star at the center.
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

        // Animated star overlay applying translation, rotation, and scaling.
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
                painter = painterResource(id = R.drawable.ic_star),
                contentDescription = "Sparkling Star",
                colorFilter = ColorFilter.tint(Polar)
            )
        }
    }
}
