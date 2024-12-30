package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.clickable
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

/**
 * A composable that represents a main action button with an icon and a label.
 *
 * @param iconId Resource ID for the icon.
 * @param label The text label for the button.
 * @param onClick Lambda function to handle click events.
 */
@Composable
fun MainButton(
    iconId: Int,
    label: String,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .clickable { onClick() }
            .padding(8.dp) // Added padding for better touch target
    ) {
        Box(
            modifier = Modifier
                .size(136.dp) // Consistent size
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = label, // Ensure this is meaningful
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onBackground,
            modifier = Modifier.padding(top = 8.dp) // Added top padding for spacing
        )
    }
}
