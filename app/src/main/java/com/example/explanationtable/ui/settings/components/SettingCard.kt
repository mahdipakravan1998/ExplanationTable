package com.example.explanationtable.ui.settings.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

/**
 * A customizable setting card component displaying an icon, a label, and a toggle switch.
 *
 * @param iconResId Resource ID for the icon to display.
 * @param label The descriptive text label.
 * @param isChecked Current state of the toggle switch.
 * @param onCheckedChange Callback invoked when the switch is toggled.
 * @param borderColor Color used for the card's border.
 * @param backgroundColor Background color of the card.
 */
@Composable
fun SettingCard(
    iconResId: Int,
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    borderColor: Color,
    backgroundColor: Color
) {
    // Outer container Row for the setting card.
    Row(
        modifier = Modifier
            .fillMaxWidth()              // Occupy the full width of the parent.
            .height(56.dp)               // Set fixed height (adjust as needed).
            .background(color = backgroundColor)  // Apply the custom background color.
            .border(
                width = 2.dp,
                color = borderColor,
                shape = RoundedCornerShape(8.dp)  // Rounded border corners.
            ),
        verticalAlignment = Alignment.CenterVertically  // Center items vertically.
    ) {
        // Icon on the left.
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,  // Accessibility description.
            tint = MaterialTheme.colorScheme.onSurface,  // Tint based on the theme.
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)  // Icon size.
        )

        // Label text in the center with weight to occupy available space.
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        // Toggle switch on the right.
        // The lambda ignores the incoming Boolean value and calls the provided callback.
        Switch(
            checked = isChecked,
            onCheckedChange = { _ -> onCheckedChange() },
            modifier = Modifier.padding(end = 16.dp)
        )
    }
}
