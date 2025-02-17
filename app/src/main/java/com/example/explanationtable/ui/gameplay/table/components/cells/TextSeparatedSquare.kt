package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.gameplay.table.components.cells.utils.AutoResizingText
import com.example.explanationtable.ui.theme.Bee
import com.example.explanationtable.ui.theme.DarkBrown
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

//-------------------------------------------------------------------------------------
// Constants for cell text styling.
// These values help maintain consistency and ease future style adjustments.
//-------------------------------------------------------------------------------------
private val cellTextColor = DarkBrown
private val cellFontWeight = FontWeight.Bold
private val cellMinTextSize = 10.sp
private val cellMaxTextSize = 16.sp
private const val cellMaxLines = 2

/**
 * Helper composable to render auto-resizing text with consistent styling.
 *
 * This function centralizes the text styling parameters so that both the top and bottom
 * text areas share the same configuration.
 *
 * @param text The text content to display.
 */
@Composable
private fun CellText(text: String) {
    AutoResizingText(
        text = text,
        modifier = Modifier.fillMaxSize(),
        color = cellTextColor,
        fontWeight = cellFontWeight,
        maxLines = cellMaxLines,
        minTextSize = cellMinTextSize,
        maxTextSize = cellMaxTextSize,
        textAlign = TextAlign.Center,
        fontFamily = VazirmatnFontFamily
    )
}

/**
 * A composable representing an 80×80 square cell with two auto-resizing text areas
 * (top and bottom) separated by a horizontal divider.
 *
 * Each text area attempts to fit its content within two lines by adjusting its font size.
 *
 * @param topText The text to display in the top section.
 * @param bottomText The text to display in the bottom section.
 * @param modifier A [Modifier] for external styling or layout adjustments.
 */
@Composable
fun TextSeparatedSquare(
    topText: String,
    bottomText: String,
    modifier: Modifier = Modifier
) {
    // Outer container: sets a fixed size (80x80 dp) and centers its content.
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        // Inner container: applies rounded corners, a background color, and padding.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(Bee)
                .padding(8.dp)
        ) {
            // Column layout: vertically arranges the top text, divider, and bottom text.
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top text section: displays auto-resizing text centered within its area.
                Box(
                    modifier = Modifier.weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    CellText(topText)
                }

                // Divider: a horizontal line that separates the two text sections.
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp),
                    color = cellTextColor,
                    thickness = 1.dp
                )

                // Bottom text section: displays auto-resizing text centered within its area.
                Box(
                    modifier = Modifier.weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    CellText(bottomText)
                }
            }
        }
    }
}
