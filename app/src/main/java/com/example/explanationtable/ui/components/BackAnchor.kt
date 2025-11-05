package com.example.explanationtable.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * Same shape & behavior as ScrollAnchor, but with a LEFT arrow for "back/return".
 */
@Composable
fun BackAnchor(
    isDarkTheme: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ActionTile(
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        modifier = modifier
    ) {
        ArrowIcon(
            direction = ArrowDirection.Left,
            contentDescription = "Go back"
        )
    }
}
