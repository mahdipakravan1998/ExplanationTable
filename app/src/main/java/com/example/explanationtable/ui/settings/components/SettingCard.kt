package com.example.explanationtable.ui.settings.components

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp

@Composable
fun SettingCard(
    iconResId: Int,
    label: String,
    isChecked: Boolean,
    onCheckedChange: () -> Unit,
    borderColor: Color,
    backgroundColor: Color // New parameter
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp) // Adjust as needed
            .background(color = backgroundColor) // Use the passed background color
            .border(width = 2.dp, color = borderColor, shape = RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Icon on the left
        Icon(
            painter = painterResource(id = iconResId),
            contentDescription = label,
            tint = MaterialTheme.colorScheme.onSurface, // Apply tint based on theme
            modifier = Modifier
                .padding(start = 16.dp)
                .size(24.dp)
        )

        // Fixed label in the middle
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface, // Ensure text color matches
            modifier = Modifier
                .weight(1f)
                .padding(start = 16.dp)
        )

        // Toggle Switch on the right
        Switch(
            checked = isChecked,
            onCheckedChange = { onCheckedChange() },
            modifier = Modifier
                .padding(end = 16.dp)
        )
    }
}
