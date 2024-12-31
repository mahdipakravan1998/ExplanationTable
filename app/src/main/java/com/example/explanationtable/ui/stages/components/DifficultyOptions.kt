package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.*

/**
 * A composable that displays difficulty options as selectable cards.
 *
 * @param onOptionSelected Callback invoked with the selected difficulty.
 */
@Composable
fun DifficultyOptions(onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Easy option
        OptionCard(
            label = stringResource(id = R.string.easy_step_label),
            onClick = { onOptionSelected("Easy") },
            backgroundColor = MaterialTheme.colorScheme.primary, // Core Brand Color
            shadowColor = MaterialTheme.colorScheme.primaryContainer, // Slightly darker
            textColor = MaterialTheme.colorScheme.onPrimary,
            imageResId = R.drawable.ic_emerald_stroked // Ensure this drawable exists
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Medium option
        OptionCard(
            label = stringResource(id = R.string.medium_step_label),
            onClick = { onOptionSelected("Medium") },
            backgroundColor = MaterialTheme.colorScheme.secondary, // Secondary Palette Color
            shadowColor = MaterialTheme.colorScheme.secondaryContainer, // Slightly darker
            textColor = MaterialTheme.colorScheme.onSecondary,
            imageResId = R.drawable.ic_crown_stroked
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Hard option
        OptionCard(
            label = stringResource(id = R.string.difficult_step_label),
            onClick = { onOptionSelected("Hard") },
            backgroundColor = MaterialTheme.colorScheme.tertiary, // Duo's Palette Color
            shadowColor = MaterialTheme.colorScheme.tertiaryContainer, // Slightly darker
            textColor = MaterialTheme.colorScheme.onTertiary,
            imageResId = R.drawable.ic_diamond_stroked
        )
    }
}
