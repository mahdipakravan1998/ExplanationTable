package com.example.explanationtable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.ui.modifiers.innerShadowAllSides
import androidx.compose.material3.Text
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Shadow

/**
 * A composable representing a colored rectangle with a diamond icon and a count.
 */
@Composable
fun DiamondGroup(
    diamonds: Int,
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = RoundedCornerShape(6.dp)
) {
    val rectangleColor = Color(0xFF0096DC)
    val bottomBorderColor = Color(0xFFA5E9FF)
    val horizontalPadding = 6.dp
    val verticalPadding = 1.dp
    val borderOffset = 2.dp
    val density = LocalDensity.current

    Box(modifier = modifier) {
        // Bottom rectangle (border)
        Box(
            modifier = Modifier
                .offset(y = borderOffset)
                .clip(shape)
                .background(bottomBorderColor)
                .matchParentSize()
        )

        // Main rectangle with inner shadow
        Box(
            modifier = Modifier
                .clip(shape)
                .background(rectangleColor)
                .innerShadowAllSides(
                    color = Color.Black.copy(alpha = 0.23f),
                    blur = 7.dp,
                    spread = 4.dp,
                    shape = shape
                )
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .padding(
                        start = horizontalPadding,
                        end   = horizontalPadding,
                        top   = verticalPadding,
                        bottom= verticalPadding
                    )
            ) {
                ShadowedImage(
                    painter = painterResource(id = R.drawable.ic_diamond),
                    contentDescription = "Diamond Icon",
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))

                // Text drop shadow properties
                val shadowColor = Color.Black.copy(alpha = 0.75f)
                val shadowOffset = with(density) { Offset(x = 0f, y = 2.dp.toPx()) }
                val shadowBlurRadius = with(density) { 4.dp.toPx() }

                Text(
                    text = diamonds.toString(),
                    fontSize = 14.sp,
                    color = Color.White,
                    fontWeight = FontWeight.Normal,
                    style = TextStyle(
                        shadow = Shadow(
                            color = shadowColor,
                            offset = shadowOffset,
                            blurRadius = shadowBlurRadius
                        ),
                        textAlign = TextAlign.Center
                    )
                )
            }
        }
    }
}
