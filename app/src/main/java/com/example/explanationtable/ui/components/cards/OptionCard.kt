package com.example.explanationtable.ui.components.cards

import android.graphics.Color as AndroidColor
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.*
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    imageResId: Int
) {
    fun Color.lighten(factor: Float): Color {
        val hsv = FloatArray(3)
        AndroidColor.colorToHSV(this.toArgb(), hsv)
        hsv[2] = (hsv[2] + factor).coerceAtMost(1f)
        return Color(AndroidColor.HSVToColor(hsv))
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .drawBehind {
                        val glowRadius = size.minDimension / 2 + 64.dp.toPx()
                        val glowColor = shadowColor.lighten(0.8f)
                        val brush = Brush.radialGradient(
                            colors = listOf(
                                glowColor.copy(alpha = 1f),
                                glowColor.copy(alpha = 0f)
                            ),
                            center = center,
                            radius = glowRadius
                        )
                        drawCircle(
                            brush = brush,
                            radius = glowRadius,
                            center = center
                        )
                    },
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = imageResId),
                    contentDescription = null,
                    modifier = Modifier.size(40.dp)
                )
            }
            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = label,
                color = textColor,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                ),
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.End
            )
        }
    }
}
