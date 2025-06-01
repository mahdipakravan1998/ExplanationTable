package com.example.explanationtable.ui.gameplay.table.components.directions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

/**
 * Renders a directional arrow sign with a horizontal component and a downward pointing arrowhead.
 *
 * The sign's color depends on whether it is on the correct square or if the dark theme is active.
 *
 * @param isDarkTheme Boolean flag to indicate if dark theme is enabled.
 * @param isOnCorrectSquare Optional flag to indicate if the sign is on the correct square.
 * @param modifier Modifier for styling and layout.
 */
@Composable
fun LeftDownArrow(
    isDarkTheme: Boolean,
    isOnCorrectSquare: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Choose the appropriate color based on theme and square selection.
    val signColor = if (isOnCorrectSquare || isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        // Define stroke width and frequently used dp conversions.
        val strokeWidth = 2.dp.toPx()
        val horizontalOffset = 9.dp.toPx()
        val arrowHeadOffset = 3.dp.toPx()
        val verticalArrowLength = 10.dp.toPx()

        // Draw the horizontal line from right to left.
        drawLine(
            color = signColor,
            start = Offset(x = size.width, y = size.height / 2),
            end = Offset(x = size.width - horizontalOffset, y = size.height / 2),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Calculate the starting position for the vertical arrow component.
        val arrowStartX = size.width - horizontalOffset
        val arrowStartY = size.height / 2
        val arrowEndY = arrowStartY + verticalArrowLength

        // Draw the vertical line of the arrow.
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the left side of the downward arrowhead.
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX - arrowHeadOffset, y = arrowEndY - arrowHeadOffset),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Draw the right side of the downward arrowhead.
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX + arrowHeadOffset, y = arrowEndY - arrowHeadOffset),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Renders a vertical directional arrow sign.
 *
 * The arrow comprises a vertical line with a downward pointing arrowhead.
 * The sign's color depends on whether it is on the correct square or if the dark theme is active.
 *
 * @param isDarkTheme Boolean flag to indicate if dark theme is enabled.
 * @param isOnCorrectSquare Optional flag to indicate if the sign is on the correct square.
 * @param modifier Modifier for styling and layout.
 */
@Composable
fun DownArrow(
    isDarkTheme: Boolean,
    isOnCorrectSquare: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Determine the color based on theme or selection.
    val signColor = if (isOnCorrectSquare || isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(10.dp)) {
        // Define stroke width and conversion for the arrowhead offset.
        val strokeWidth = 2.dp.toPx()
        val arrowheadOffset = 2.dp.toPx()
        val arrowWidth = size.width
        val arrowHeight = size.height

        // Draw the main vertical line of the arrow.
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2, y = 0f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight * 0.7f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Draw the left side of the arrowhead.
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 - arrowheadOffset, y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Draw the right side of the arrowhead.
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 + arrowheadOffset, y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

/**
 * Renders a directional arrow sign with a vertical component and a leftward pointing arrowhead.
 *
 * The sign's color depends on whether it is on the correct square or if the dark theme is active.
 *
 * @param isDarkTheme Boolean flag to indicate if dark theme is enabled.
 * @param isOnCorrectSquare Optional flag to indicate if the sign is on the correct square.
 * @param modifier Modifier for styling and layout.
 */
@Composable
fun UpLeftArrow(
    isDarkTheme: Boolean,
    isOnCorrectSquare: Boolean = false,
    modifier: Modifier = Modifier
) {
    // Select the sign color based on the state.
    val signColor = if (isOnCorrectSquare || isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        // Define stroke width and common dimension offsets.
        val strokeWidth = 2.dp.toPx()
        val verticalLineOffset = 9.dp.toPx()
        val horizontalArrowLength = 10.dp.toPx()
        val arrowheadOffset = 3.dp.toPx()

        // Draw the vertical line (from bottom upward).
        drawLine(
            color = signColor,
            start = Offset(x = size.width / 2, y = size.height),
            end = Offset(x = size.width / 2, y = size.height - verticalLineOffset),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Calculate starting point for the horizontal arrow line.
        val arrowStartX = size.width / 2
        val arrowStartY = size.height - verticalLineOffset
        val arrowEndX = arrowStartX - horizontalArrowLength

        // Draw the horizontal line of the arrow.
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )

        // Draw the upper part of the arrowhead pointing left.
        drawLine(
            color = signColor,
            start = Offset(x = arrowEndX + arrowheadOffset, y = arrowStartY - arrowheadOffset),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        // Draw the lower part of the arrowhead pointing left.
        drawLine(
            color = signColor,
            start = Offset(x = arrowEndX + arrowheadOffset, y = arrowStartY + arrowheadOffset),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}
