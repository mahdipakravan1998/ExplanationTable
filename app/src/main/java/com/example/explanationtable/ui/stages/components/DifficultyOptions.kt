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
    // Data class encapsulating the properties for each difficulty option.
    data class DifficultyOption(
        val labelResId: Int,         // Resource ID for the label text.
        val difficultyValue: String, // The difficulty string passed to the callback.
        val backgroundColor: androidx.compose.ui.graphics.Color,
        val shadowColor: androidx.compose.ui.graphics.Color,
        val textColor: androidx.compose.ui.graphics.Color,
        val imageResId: Int          // Drawable resource ID for the icon.
    )

    // List of all difficulty options.
    val difficultyOptions = listOf(
        DifficultyOption(
            labelResId = R.string.easy_step_label,
            difficultyValue = "Easy",
            backgroundColor = MaterialTheme.colorScheme.primary,
            shadowColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimary,
            imageResId = R.drawable.ic_emerald_stroked
        ),
        DifficultyOption(
            labelResId = R.string.medium_step_label,
            difficultyValue = "Medium",
            backgroundColor = MaterialTheme.colorScheme.secondary,
            shadowColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondary,
            imageResId = R.drawable.ic_crown_stroked
        ),
        DifficultyOption(
            labelResId = R.string.difficult_step_label,
            difficultyValue = "Hard",
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            shadowColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiary,
            imageResId = R.drawable.ic_diamond_stroked
        )
    )

    // Constant spacing between options.
    val optionSpacing = 10.dp

    // Container column for all the option cards.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Iterate through each difficulty option and display its corresponding card.
        difficultyOptions.forEachIndexed { index, option ->
            OptionCard(
                label = stringResource(id = option.labelResId),
                onClick = { onOptionSelected(option.difficultyValue) },
                backgroundColor = option.backgroundColor,
                shadowColor = option.shadowColor,
                textColor = option.textColor,
                imageResId = option.imageResId
            )
            // Add spacing below the card except after the last option.
            if (index < difficultyOptions.lastIndex) {
                Spacer(modifier = Modifier.height(optionSpacing))
            }
        }
    }
}
