package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.gameplay.table.components.cells.utils.AutoResizingText
import com.example.explanationtable.ui.theme.Bee
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

/**
 * A 80×80 colored square that uses [AutoResizingText] with a two-line priority:
 *   - We attempt to place text on up to 2 lines.
 *   - If it exceeds those 2 lines in the available space, we reduce the font size.
 */
@Composable
fun ColoredSquare(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(color = Bee)
                .padding(8.dp),
            contentAlignment = Alignment.Center
        ) {
            AutoResizingText(
                text = text,
                modifier = Modifier.fillMaxSize(),
                color = Color(0xFF5E3700),
                fontWeight = FontWeight.Bold,
                maxLines = 2,        // Priority: fit into two lines
                minTextSize = 14.sp,
                maxTextSize = 16.sp,
                textAlign = TextAlign.Center,
                fontFamily = VazirmatnFontFamily
            )
        }
    }
}