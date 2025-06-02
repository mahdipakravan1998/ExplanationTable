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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.gameplay.table.components.utils.AutoResizingText
import com.example.explanationtable.ui.theme.Bee
import com.example.explanationtable.ui.theme.DarkBrown
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

// Constants defining the dimensions and text sizing parameters for the square.
private val SquareSize = 80.dp
private val CornerRadius = 16.dp
private val SquarePadding = 8.dp
private val MinTextSize = 12.sp
private val MaxTextSize = 16.sp

/**
 * A composable that displays a colored square with auto-resizing text.
 *
 * The square is rendered with:
 * - A fixed size ([SquareSize]) and rounded corners ([CornerRadius]).
 * - A background color ([Bee]) from the theme.
 * - Internal padding ([SquarePadding]) to prevent text from touching the edges.
 *
 * The [AutoResizingText] inside the square ensures that:
 * - The provided [text] fits within up to 2 lines.
 * - The font size dynamically adjusts between [MinTextSize] and [MaxTextSize] if necessary.
 *
 * @param text The content to display within the square.
 * @param modifier An optional [Modifier] for further customization.
 */
@Composable
fun ColoredSquare(
    text: String,
    modifier: Modifier = Modifier
) {
    // Define the square layout using a Box with specified size, shape, background, and padding.
    Box(
        modifier = modifier
            .size(SquareSize) // Set fixed dimensions for the square.
            .clip(RoundedCornerShape(CornerRadius)) // Apply rounded corners.
            .background(Bee) // Set the background color.
            .padding(SquarePadding), // Add internal padding.
        contentAlignment = Alignment.Center // Center the content inside the Box.
    ) {
        // Display auto-resizing text that adapts its size to fit within the box.
        AutoResizingText(
            text = text,
            modifier = Modifier.fillMaxSize(), // Allow the text to occupy the full available space.
            color = DarkBrown, // Set the text color to a dark brown tone.
            fontWeight = FontWeight.Bold, // Render text in bold.
            maxLines = 2, // Limit text to a maximum of 2 lines.
            minTextSize = MinTextSize, // Define the minimum font size for auto-resizing.
            maxTextSize = MaxTextSize, // Define the maximum font size for auto-resizing.
            textAlign = TextAlign.Center, // Center-align the text.
            fontFamily = VazirmatnFontFamily // Apply the custom font family.
        )
    }
}
