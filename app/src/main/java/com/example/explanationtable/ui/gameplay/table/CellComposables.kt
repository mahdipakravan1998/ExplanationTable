package com.example.explanationtable.ui.gameplay.table

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.theme.*
import androidx.compose.ui.tooling.preview.Preview

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

/**
 * A 80×80 square that shows [topText], a divider, and [bottomText], each
 * trying to fit on two lines first, then resizing if necessary.
 */
@Composable
fun TextSeparatedSquare(
    topText: String,
    bottomText: String,
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
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top text
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizingText(
                        text = topText,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF5E3700),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,  // first try 2 lines
                        minTextSize = 10.sp,
                        maxTextSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp),
                    color = Color(0xFF5E3700),
                    thickness = 1.dp
                )

                // Bottom text
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizingText(
                        text = bottomText,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF5E3700),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2, // first try 2 lines
                        minTextSize = 10.sp,
                        maxTextSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
}

/**
 * A three-layered stacked square (3D-ish effect) with a letter in the center.
 */
@Composable
fun StackedSquare3D(
    letter: String,
    modifier: Modifier = Modifier
) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val frontColor = if (isDarkTheme) BackgroundDark else BackgroundLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // 1) Track a pressed state + animate
    val offsetY = 2.dp  // This is the amount you want to move down
    var isPressed by remember { mutableStateOf(false) }
    val pressOffsetY by animateFloatAsState(
        targetValue = if (isPressed) with(LocalDensity.current) { 2.dp.toPx() } else 0f,
        animationSpec = tween(durationMillis = 30), // smooth transition
        label = "" // no label needed here
    )

    // Convert to dp for the UI
    val density = LocalDensity.current
    val pressOffsetDp = with(density) { pressOffsetY.toDp() }

    // 2) Handle pointer input (press detection)
    val gestureModifier = Modifier.pointerInput(Unit) {
        awaitEachGesture {
            awaitFirstDown(requireUnconsumed = false)
            isPressed = true

            // Wait for finger up or cancel => pressed = false
            val upOrCancel = waitForUpOrCancellation()
            isPressed = false

            // If the user actually lifted (not canceled), it's a click
            if (upOrCancel != null) {
                // Optional: Handle click if needed
            }
        }
    }

    Box(
        modifier = modifier
            .width(80.dp)
            .height(82.dp)
            .then(gestureModifier), // Add gesture modifier to detect clicks
        contentAlignment = Alignment.TopCenter
    ) {
        Box {
            // 3rd square (back)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = offsetY)
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            // 2nd square (middle)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp) // Apply the animated offset here
                    .size(80.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(borderColor)
            )

            // 1st square (front)
            Box(
                modifier = Modifier
                    .align(Alignment.Center)
                    .offset(y = pressOffsetDp) // Apply the same animated offset here
                    .size(75.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(frontColor),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = letter,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = textColor,
                    fontFamily = VazirmatnFontFamily
                )
            }
        }
    }
}

/**
 * A bright green square with a letter in the center.
 */
@Composable
fun BrightGreenSquare(
    letter: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .size(80.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(color = FeatherGreen),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = letter,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                maxLines = 2,
                textAlign = TextAlign.Center,
                fontFamily = VazirmatnFontFamily,
                // We don't want ellipses
                overflow = TextOverflow.Clip
            )
        }
    }
}

/**
 * Directional arrow sign composables (pure drawing, no text).
 */
@Composable
fun DirectionalSign0_1(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = signColor,
            start = Offset(x = size.width, y = size.height / 2),
            end = Offset(x = size.width - 9.dp.toPx(), y = size.height / 2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrowStartX = size.width - 9.dp.toPx()
        val arrowStartY = size.height / 2
        val arrowEndY = arrowStartY + 10.dp.toPx()

        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing down
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX - 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX + 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_0(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(10.dp)) {
        val stroke = 2.dp.toPx()
        val arrowHeight = size.height
        val arrowWidth = size.width

        // Draw vertical line
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2, y = 0f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 - 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 + 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_2(modifier: Modifier = Modifier) {
    // Reuse DirectionalSign1_0
    DirectionalSign1_0(modifier)
}

@Composable
fun DirectionalSign3_2(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = signColor,
            start = Offset(x = size.width / 2, y = size.height),
            end = Offset(x = size.width / 2, y = size.height - 9.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrowStartX = size.width / 2
        val arrowStartY = size.height - 9.dp.toPx()
        val arrowEndX = arrowStartX - 10.dp.toPx()

        // Horizontal line
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing left
        drawLine(
            color = signColor,
            start = Offset(x = arrowEndX + 3.dp.toPx(), y = arrowStartY - 3.dp.toPx()),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
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
fun PreviewColoredSquare() {
    ColoredSquare(text = "AB")
}

@Preview(showBackground = true)
@Composable
fun PreviewTextSeparatedSquare() {
    TextSeparatedSquare(topText = "Hello World", bottomText = "This is a test")
}

@Preview(showBackground = true)
@Composable
fun PreviewStackedSquare3DLight() {
    // Simulate light theme
    ExplanationTableTheme(darkTheme = false) {
        StackedSquare3D(letter = "A")
    }
}

@Preview(showBackground = true, uiMode = Configuration.UI_MODE_NIGHT_YES)
@Composable
fun PreviewStackedSquare3DDark() {
    // Simulate dark theme
    ExplanationTableTheme(darkTheme = true) {
        StackedSquare3D(letter = "A")
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewBrightGreenSquare() {
    BrightGreenSquare(letter = "B")
}
