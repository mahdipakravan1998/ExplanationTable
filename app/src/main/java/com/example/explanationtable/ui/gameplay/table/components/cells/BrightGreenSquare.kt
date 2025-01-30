package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import com.example.explanationtable.ui.theme.FeatherGreen
import com.example.explanationtable.ui.theme.VazirmatnFontFamily
import com.example.explanationtable.ui.theme.White
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.Polar

/**
 * A bright green square with a letter in the center.
 */
@Composable
fun BrightGreenSquare(
    letter: String,
    modifier: Modifier = Modifier
) {
    // States to control star animation (starting at 0dp size and 0 rotation)
    val star1ScaleAndRotation = remember { Animatable(0f) } // Large star

    // Start animation when the BrightGreenSquare is displayed
    LaunchedEffect(Unit) {
        // Slow growth: using FastOutSlowInEasing for a slow grow
        val growSpec = tween<Float>(durationMillis = 1200, easing = FastOutSlowInEasing)
        star1ScaleAndRotation.animateTo(1f, animationSpec = growSpec)

        // Fast shrink: using LinearEasing for fast shrink
        val shrinkSpec = tween<Float>(durationMillis = 300, easing = LinearEasing)
        star1ScaleAndRotation.animateTo(0f, animationSpec = shrinkSpec)
    }

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = FeatherGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = VazirmatnFontFamily,
            )
        }

        // Animate Stars: Large Star at top-left, Medium & Small Stars at bottom-right
        Box(
            modifier = Modifier
                .align(Alignment.TopStart)
                .padding(start = 8.dp, top = 8.dp)
                .graphicsLayer(
                    scaleX = star1ScaleAndRotation.value,
                    scaleY = star1ScaleAndRotation.value,
                    rotationZ = star1ScaleAndRotation.value * 360f
                )
                .size(15.dp)
        ) {
            Image(
                painter = painterResource(id = R.drawable.star),
                contentDescription = "Sparkling Star",
                colorFilter = ColorFilter.tint(Polar) // Gold Color
            )
        }
    }
}