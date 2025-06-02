package com.example.explanationtable.ui.gameplay.table.components.utils

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
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
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.VazirmatnFontFamily
import com.example.explanationtable.utils.toPersianDigits

/**
 * A helper composable that automatically resizes text to fit within its constraints,
 * trying first to render the text in up to [maxLines] lines. If it still overflows,
 * the font size is reduced until it fits.
 *
 * Additionally, if the input [text] contains any Western digits (0–9), each maximal
 * digit‐sequence (e.g. “1234”) is converted to its Persian equivalent (e.g. “۱۲۳۴”)
 * using the existing Int.toPersianDigits() extension.
 *
 * By default, [maxLines] is set to two so that text is split into two lines before
 * reducing the font size.
 *
 * Note: Ellipses are not used; overflow is set to [TextOverflow.Clip].
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
    // 1) First, convert any Western- digit‐sequences to Persian digits
    //    We use a regex (\d+) to capture each consecutive run of digits,
    //    then attempt to parse it as Int; if successful, call toPersianDigits().
    //    If parsing fails (e.g. extremely large number), we leave it unchanged.
    val persianifiedText = text.replace(Regex("\\d+")) { matchResult ->
        val digitSequence = matchResult.value
        digitSequence.toIntOrNull()?.toPersianDigits() ?: digitSequence
    }

    // 2) State holding the current text size, starting at the maximum allowed size.
    var currentTextSize by remember { mutableStateOf(maxTextSize) }

    // 3) Remember a text measurer to measure text dimensions without composing UI.
    val textMeasurer = rememberTextMeasurer()

    BoxWithConstraints(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        // 4) Obtain the current layout constraints (available width and height).
        val constraints = this.constraints

        // 5) Attempt to adjust the text size until it fits within the constraints.
        //    We limit the number of attempts to prevent an infinite loop.
        var attempts = 0
        while (attempts < 20 && currentTextSize >= minTextSize) {
            // Measure the “Persian‐converted” text with the current text style.
            val measuredText = textMeasurer.measure(
                text = AnnotatedString(persianifiedText),
                style = TextStyle(
                    fontSize = currentTextSize,
                    color = color,
                    fontWeight = fontWeight,
                    textAlign = textAlign,
                    fontFamily = fontFamily
                ),
                maxLines = maxLines
            )

            // Check if the measured text fits within the available width and height.
            if (measuredText.size.width <= constraints.maxWidth &&
                measuredText.size.height <= constraints.maxHeight
            ) {
                // Text now fits; exit the loop.
                break
            } else {
                // Text still overflows; shrink font size by 1 sp and retry.
                currentTextSize = (currentTextSize.value - 1).sp
            }
            attempts++
        }

        // 6) Finally, render the “Persian‐converted” text with the adjusted font size.
        Text(
            text = persianifiedText,
            style = TextStyle(
                fontSize = currentTextSize,
                color = color,
                fontWeight = fontWeight,
                textAlign = textAlign,
                fontFamily = fontFamily
            ),
            maxLines = maxLines,
            overflow = TextOverflow.Clip
        )
    }
}
