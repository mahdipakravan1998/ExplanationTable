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
 * A bright green square with a letter in the center, accompanied by a star animation.
 *
 * This composable displays a square that animates translation, rotation, and scaling of a star icon.
 * The translation animation is divided into two phases using keyframes and tween, while rotation and scaling
 * animations run concurrently. This approach improves clarity and efficiency by consolidating the animation launches.
 */
@Composable
fun BrightGreenSquare(
    letter: String,
    modifier: Modifier = Modifier
) {
    // Animation parameters
    val animationDuration = 800    // Total animation duration in milliseconds
    val translationDistance = 6.dp // Maximum translation distance for the star icon

    // Animatables for translation, rotation, and scaling
    val translationProgress = remember { Animatable(0f) }
    val rotationAnim = remember { Animatable(0f) }
    val scaleAnim = remember { Animatable(1f) }

    // Launch all animations concurrently within a single LaunchedEffect scope
    LaunchedEffect(Unit) {
        // Launch translation animation in two phases
        launch {
            // Phase 1: Animate to 60% progress using keyframes
            translationProgress.animateTo(
                targetValue = 0.6f,
                animationSpec = keyframes {
                    durationMillis = 440
                    0.5f at 120 using FastOutSlowInEasing // Fast phase: 0% to 50% progress in 120ms
                    0.6f at 440 using FastOutSlowInEasing // Smooth phase: 50% to 60% progress by 440ms
                }
            )
            // Phase 2: Continue to full progress using a tween animation
            translationProgress.animateTo(
                targetValue = 1f,
                animationSpec = tween(
                    durationMillis = animationDuration - 440,
                    easing = FastOutSlowInEasing
                )
            )
        }
        // Launch continuous rotation animation from 0° to 90°
        launch {
            rotationAnim.animateTo(
                targetValue = 90f,
                animationSpec = tween(durationMillis = animationDuration, easing = LinearEasing)
            )
        }
        // Launch scale animation with a delay until translation reaches 60%
        launch {
            delay(440L)
            scaleAnim.animateTo(
                targetValue = 0f,
                animationSpec = tween(durationMillis = animationDuration - 440, easing = LinearEasing)
            )
        }
    }

    // Calculate animated offsets based on the translation progress
    val animatedOffset = translationDistance * translationProgress.value
    val animatedXOffset = -animatedOffset
    val animatedYOffset = -animatedOffset

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // Main bright green square with the centered letter
        Box(
            modifier = Modifier
                .size(80.dp)
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
        // Star icon with animated translation, rotation, and scaling effects
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(start = 12.dp, top = 12.dp)
                .wrapContentSize(Alignment.TopStart)
                .offset(x = animatedXOffset, y = animatedYOffset)
                .graphicsLayer(
                    scaleX = scaleAnim.value,
                    scaleY = scaleAnim.value,
                    rotationZ = rotationAnim.value
                )
                .size(15.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Sparkling Star",
                colorFilter = ColorFilter.tint(Polar)
            )
        }
    }
}
