package com.example.explanationtable.ui.components.topBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * Renders an image with a subtle drop shadow beneath it for visual depth.
 */
@Composable
fun ShadowedImage(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    shadowOffsetY: Dp = 4.dp,
    shadowBlur: Dp = 4.dp,
    shadowAlpha: Float = 0.33f
) {
    Box {
        // Shadow layer
        Image(
            painter = painter,
            contentDescription = null,
            modifier = modifier
                .size(24.dp)
                .offset(y = shadowOffsetY)
                .blur(shadowBlur)
                .alpha(shadowAlpha),
            colorFilter = ColorFilter.tint(Color.Black)
        )

        // Main image layer
        Image(
            painter = painter,
            contentDescription = contentDescription,
            modifier = modifier.size(24.dp)
        )
    }
}
