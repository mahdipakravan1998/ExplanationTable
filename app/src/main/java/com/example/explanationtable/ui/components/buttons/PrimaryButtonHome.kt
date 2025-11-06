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
        modifier = modifier
    )
}
