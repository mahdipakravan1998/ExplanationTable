package com.example.explanationtable.ui.gameplay.table

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.theme.Bee
import com.example.explanationtable.ui.theme.Eel

/**
 * A simple colored square with text in the center.
 */
@Composable
fun Type1Square(
    text: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Bee),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                color = Color(0xFF5E3700),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * A square with top and bottom text, separated by a horizontal divider.
 */
@Composable
fun Type2Square(
    topText: String,
    bottomText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(color = Bee),
            contentAlignment = Alignment.Center
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = topText,
                    color = Color(0xFF5E3700),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
                // A dividing line that doesn't touch the left/right edges
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp), // small spacing around the line
                    color = Color(0xFF5E3700),
                    thickness = 1.dp
                )
                Text(
                    text = bottomText,
                    color = Color(0xFF5E3700),
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

/**
 * A three-layered stacked square (3D-ish effect) with a letter in the center.
 */
@Composable
fun Type3Square(
    letter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .width(64.dp)
            .height(66.dp),
        contentAlignment = Alignment.TopCenter
    ) {
        Box { // Inner container wraps content
            // 3rd square (back)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = 2.dp)
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E5E5))
            )

            // 2nd square (middle)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(64.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFE5E5E5))
            )

            // 1st square (front)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .size(60.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(Color(0xFFFFFDFD)),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Eel
                )
            }
        }
    }
}

/**
 * A bright green square (for a different styling) with a letter in the center.
 */
@Composable
fun Type4Square(
    letter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color(0xFF58cc02)),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

/**
 * Directional arrow sign composables for overlaying on top of squares.
 */
@Composable
fun DirectionalSign0_1(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        // Draw horizontal line from right to left
        drawLine(
            color = Eel,
            start = Offset(x = size.width, y = size.height / 2),
            end = Offset(x = size.width - 9.dp.toPx(), y = size.height / 2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Draw vertical arrow pointing down from the end of the horizontal line
        val arrowStartX = size.width - 9.dp.toPx()
        val arrowStartY = size.height / 2
        val arrowEndY = arrowStartY + 10.dp.toPx()

        // Vertical line
        drawLine(
            color = Eel,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing down
        drawLine(
            color = Eel,
            start = Offset(x = arrowStartX - 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Eel,
            start = Offset(x = arrowStartX + 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_0(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(10.dp)) {
        val stroke = 2.dp.toPx()
        val arrowHeight = size.height
        val arrowWidth = size.width

        // Draw vertical line
        drawLine(
            color = Eel,
            start = Offset(x = arrowWidth / 2, y = 0f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Draw arrowhead
        drawLine(
            color = Eel,
            start = Offset(x = arrowWidth / 2 - 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Eel,
            start = Offset(x = arrowWidth / 2 + 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_2(modifier: Modifier = Modifier) {
    // Reuse the same composable as DirectionalSign1_0
    DirectionalSign1_0(modifier = modifier)
}

@Composable
fun DirectionalSign3_2(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        // Draw vertical line from bottom to top
        drawLine(
            color = Eel,
            start = Offset(x = size.width / 2, y = size.height),
            end = Offset(x = size.width / 2, y = size.height - 9.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Draw horizontal arrow pointing left from the end of the vertical line
        val arrowStartX = size.width / 2
        val arrowStartY = size.height - 9.dp.toPx()
        val arrowEndX = arrowStartX - 10.dp.toPx()

        // Horizontal line
        drawLine(
            color = Eel,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing left
        drawLine(
            color = Eel,
            start = Offset(x = arrowEndX + 3.dp.toPx(), y = arrowStartY - 3.dp.toPx()),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = Eel,
            start = Offset(x = arrowEndX + 3.dp.toPx(), y = arrowStartY + 3.dp.toPx()),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

// ------- PREVIEWS (Optional) -------

@Preview(showBackground = true)
@Composable
fun PreviewType1Square() {
    Type1Square(text = "AB")
}

@Preview(showBackground = true)
@Composable
fun PreviewType2Square() {
    Type2Square(topText = "hello", bottomText = "world")
}

@Preview(showBackground = true)
@Composable
fun PreviewType3Square() {
    Type3Square(letter = "A")
}

@Preview(showBackground = true)
@Composable
fun PreviewType4Square() {
    Type4Square(letter = "B")
}
