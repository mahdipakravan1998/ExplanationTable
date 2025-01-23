package com.example.explanationtable.ui.gameplay.table.components.cells

import android.content.res.Configuration
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.waitForUpOrCancellation
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.ExplanationTableTheme
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

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

// ------- PREVIEWS (Optional) -------

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