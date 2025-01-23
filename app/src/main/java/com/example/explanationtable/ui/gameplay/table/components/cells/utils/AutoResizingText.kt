package com.example.explanationtable.ui.gameplay.table.components.cells.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextOverflow.Companion
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

/**
 * A helper composable that automatically resizes text to fit within its constraints
 * while trying to keep it in up to [maxLines]. The priority is:
 *  1. Try to render the text in up to [maxLines] lines.
 *  2. If it still overflows, reduce the font size.
 *
 * By default, we set [maxLines] to 2 in the calling composables so it first
 * splits into two lines if needed, then shrinks if it doesn't fit within 2 lines.
 *
 * No ellipses are used (overflow is set to [TextOverflow.Clip]).
 */
@Suppress("UnusedBoxWithConstraintsScope")
@Composable
fun AutoResizingText(
    text: String,
    modifier: Modifier = Modifier,
    minTextSize: TextUnit = 10.sp,
    maxTextSize: TextUnit = 12.sp,
    color: Color = Color.Black,
    fontWeight: FontWeight? = FontWeight.Bold,
    textAlign: TextAlign = TextAlign.Center,
    maxLines: Int = Int.MAX_VALUE,
    fontFamily: FontFamily = VazirmatnFontFamily
) {
    var textSize by remember { mutableStateOf(maxTextSize) }
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val c = constraints
        var fits = false
        var attempts = 0

        // Attempt to reduce text size until it fits in the box up to [maxLines].
        while (!fits && attempts < 20 && textSize >= minTextSize) {
            val measureResult = textMeasurer.measure(
                text = AnnotatedString(text),
                style = TextStyle(
                    fontSize = textSize,
                    color = color,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    fontFamily = fontFamily
                ),
                maxLines = maxLines
            )

            if (measureResult.size.width <= c.maxWidth &&
                measureResult.size.height <= c.maxHeight
            ) {
                fits = true
            } else {
                textSize = (textSize.value - 1).sp
            }
            attempts++
        }

        Text(
            text = text,
            style = TextStyle(
                fontSize = textSize,
                color = color,
                fontWeight = fontWeight,
                textAlign = textAlign,
                fontFamily = fontFamily
            ),
            maxLines = maxLines,
            // Use Clip to avoid ellipses. If text doesn't fit, the logic above reduces size.
            overflow = TextOverflow.Clip
        )
    }
}