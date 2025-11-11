package com.example.explanationtable.ui.components.buttons

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.components.buttons.internal.rememberPressGesture

/**
 * Secondary button with a gradient ring + inner surface and a gradient "shadow" skirt.
 *
 * Visuals, sizes, animation timing, and behavior are preserved exactly.
 * Internal changes:
 * - Brush and color objects are remembered to minimize allocations.
 * - Press/gesture logic is centralized via [rememberPressGesture].
 */
@Composable
fun SecondaryButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    // ---- Constants (unchanged visually) ----
    val buttonHeight = 56.dp
    val shadowOffset = 3.dp
    val cornerRadius = 18.dp
    val borderWidth = 2.dp
    val animationDuration = 30 // ms
    val clickDelayMs = 120 // ms

    // ---- Colors/Brushes (remembered) ----
    // Gradient colors (same values as original).
    val gradientColors = remember {
        listOf(Color(0xFF77F4CA), Color(0xFF63BDFF))
    }
    // Single gradient brush reused by both the top border and the bottom skirt.
    val gradientBrush = remember {
        Brush.linearGradient(
            colors = gradientColors,
            start = Offset(100f, 0f),
            end = Offset(0f, 100f)
        )
    }
    // Text color is theme-dependent (unchanged logic).
    val textColor = if (isDarkTheme) Color(0xFF43D1BB) else Color(0xFF2292A9)
    val pageBackgroundColor = MaterialTheme.colorScheme.background

    // ---- Press interaction (shared helper) ----
    val press = rememberPressGesture(
        shadowOffset = shadowOffset,
        animationDurationMillis = animationDuration,
        clickDelayMillis = clickDelayMs,
        onClick = onClick
    )

    // ---- Layout Composition (unchanged structure) ----
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(buttonHeight)
            .semantics { this.role = Role.Button } // a11y without visual changes
            .then(press.modifier),
        contentAlignment = Alignment.TopCenter
    ) {
        // Background Shadow Layer (gradient skirt).
        Box(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = shadowOffset)
                .clip(RoundedCornerShape(cornerRadius))
                .background(brush = gradientBrush)
        )
        // Top Layer: gradient border + inner surface.
        Box {
            // Gradient Border Layer (outer).
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .offset(y = press.pressOffset)
                    .clip(RoundedCornerShape(cornerRadius))
                    .background(brush = gradientBrush)
            )
            // Inner Layer: inset with page background color.
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(borderWidth)
                    .offset(y = press.pressOffset)
                    .clip(RoundedCornerShape(cornerRadius - borderWidth))
                    .background(pageBackgroundColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = text,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Black,
                        fontSize = 16.sp
                    ),
                    color = textColor
                )
            }
        }
    }
}
