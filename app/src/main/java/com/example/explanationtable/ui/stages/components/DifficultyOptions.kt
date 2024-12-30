package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.explanationtable.R

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
            backgroundColor = MaterialTheme.colorScheme.primary,
            shadowColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimary,
            imageResId = R.drawable.ic_emerald_stroked // Ensure this drawable exists
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Medium option
        OptionCard(
            label = stringResource(id = R.string.medium_step_label),
            onClick = { onOptionSelected("Medium") },
            backgroundColor = MaterialTheme.colorScheme.secondary,
            shadowColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondary,
            imageResId = R.drawable.ic_crown_stroked
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Hard option
        OptionCard(
            label = stringResource(id = R.string.difficult_step_label),
            onClick = { onOptionSelected("Hard") },
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            shadowColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiary,
            imageResId = R.drawable.ic_diamond_stroked
        )
    }
}