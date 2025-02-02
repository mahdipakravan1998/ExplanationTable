package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A composable representing a main action button featuring an icon and a label.
 *
 * @param iconId Resource ID for the icon image.
 * @param label Text label displayed beneath the icon.
 * @param onClick Callback invoked when the button is clicked.
 */
@Composable
fun MainButton(
    iconId: Int,
    label: String,
    onClick: () -> Unit
) {
    // Column arranges children vertically and centers them horizontally.
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            // Make the entire column clickable.
            .clickable(onClick = onClick)
            // Add padding around the button for a better touch target.
            .padding(8.dp)
    ) {
        // Box constrains the icon to a fixed size.
        Box(
            modifier = Modifier
                .size(136.dp)
        ) {
            // Display the icon at full size with a bottom padding adjustment.
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = label, // Provides accessibility description.
                tint = Color.Unspecified,   // Uses the icon's original colors.
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp)
            )
        }
        // Display the label text centered beneath the icon.
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp) // Adds spacing between icon and text.
        )
    }
}
