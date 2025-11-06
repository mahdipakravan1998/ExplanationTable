// FILE: app/src/main/java/com/example/explanationtable/ui/stages/components/ScrollAnchor.kt
package com.example.explanationtable.ui.stages.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.explanationtable.ui.components.ActionTile
import com.example.explanationtable.ui.components.ArrowDirection
import com.example.explanationtable.ui.components.ArrowIcon

/**
 * A pressable anchor that indicates “scroll to main content.”
 *
 * API kept identical to your original so you don't have to touch call sites:
 * - [flipVertical] = false -> arrow points UP
 * - [flipVertical] = true  -> arrow points DOWN
 */
@Composable
fun ScrollAnchor(
    isDarkTheme: Boolean,
    flipVertical: Boolean = false,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val direction = if (flipVertical) ArrowDirection.Down else ArrowDirection.Up
    ActionTile(
        isDarkTheme = isDarkTheme,
        onClick = onClick,
        modifier = modifier
    ) {
        ArrowIcon(
            direction = direction,
            contentDescription = "Scroll to content"
        )
    }
}
