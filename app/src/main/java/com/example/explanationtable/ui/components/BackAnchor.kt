// FILE: app/src/main/java/com/example/explanationtable/ui/components/BackAnchor.kt
package com.example.explanationtable.ui.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

/**
 * A left-arrow "back" anchor that reuses [ActionTile] visuals/behavior.
 *
 * Visual output is identical; this is a thin wrapper for clarity and reuse.
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
