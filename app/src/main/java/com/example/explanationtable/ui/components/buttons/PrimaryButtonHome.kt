package com.example.explanationtable.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Home-page variant.
 * Colors and animation duration are identical to the legacy version.
 *
 * Implementation note:
 * - Uses a shared, top-level style constant to avoid per-composition allocations.
 */
private val HOME_STYLE = PrimaryButtonStyle(
    height = 60.dp,
    cornerRadius = 20.dp,
    horizontalPadding = 20.dp,
    shadowOffset = 6.dp,
    fontSize = 18.sp
)

/**
 * Small motion buffer so the press/release animation completes and
 * navigation transitions don’t visually “fight” with the button.
 *
 * Tuned to feel instant while guaranteeing the 30ms release animation
 * finishes comfortably.
 */
private const val HOME_NAVIGATION_MOTION_BUFFER_MS: Long = 120L

@Composable
fun PrimaryButtonHome(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    PrimaryButtonBase(
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        text = text,
        style = HOME_STYLE,
        modifier = modifier,
        postReleaseDelayMs = HOME_NAVIGATION_MOTION_BUFFER_MS
    )
}
