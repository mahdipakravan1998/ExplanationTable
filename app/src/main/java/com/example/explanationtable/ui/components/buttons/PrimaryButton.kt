package com.example.explanationtable.ui.components.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Wrapper that reproduces the EXACT visuals of your existing PrimaryButton.
 * Use this anywhere you previously used the old button.
 *
 * Implementation note:
 * - Uses a shared, top-level style constant to avoid per-composition allocations.
 */
private val LEGACY_STYLE = PrimaryButtonStyle(
    height = 56.dp,
    cornerRadius = 18.dp,
    horizontalPadding = 16.dp,
    shadowOffset = 4.dp,
    fontSize = 16.sp
)

@Composable
fun PrimaryButton(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    text: String,
    modifier: Modifier = Modifier
) {
    PrimaryButtonBase(
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        text = text,
        style = LEGACY_STYLE,
        modifier = modifier
    )
}
