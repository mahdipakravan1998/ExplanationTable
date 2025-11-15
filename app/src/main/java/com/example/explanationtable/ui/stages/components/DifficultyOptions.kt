package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R

/**
 * A composable that displays difficulty options as selectable buttons.
 *
 * @param onOptionSelected Callback invoked with the selected difficulty label ("Easy", "Medium", "Hard").
 * @param loadingDifficultyValue The label of the option that is currently in a loading state, or `null` if none.
 * @param interactionEnabled When false, all options are effectively read-only and ignore taps.
 */
@Composable
fun DifficultyOptions(
    onOptionSelected: (String) -> Unit,
    loadingDifficultyValue: String? = null,
    interactionEnabled: Boolean = true
) {
    data class DifficultyOption(
        val labelResId: Int,
        val difficultyValue: String,
        val backgroundColor: androidx.compose.ui.graphics.Color,
        val shadowColor: androidx.compose.ui.graphics.Color,
        val textColor: androidx.compose.ui.graphics.Color
    )

    val difficultyOptions = listOf(
        DifficultyOption(
            labelResId = R.string.easy_step_label,
            difficultyValue = "Easy",
            backgroundColor = MaterialTheme.colorScheme.primary,
            shadowColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimary
        ),
        DifficultyOption(
            labelResId = R.string.medium_step_label,
            difficultyValue = "Medium",
            backgroundColor = MaterialTheme.colorScheme.secondary,
            shadowColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondary
        ),
        DifficultyOption(
            labelResId = R.string.difficult_step_label,
            difficultyValue = "Hard",
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            shadowColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiary
        )
    )

    val optionSpacing = 16.dp

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        difficultyOptions.forEachIndexed { index, option ->
            val isLoading = loadingDifficultyValue == option.difficultyValue
            val isOptionEnabled = interactionEnabled && !isLoading

            OptionCard(
                label = stringResource(id = option.labelResId),
                onClick = {
                    if (isOptionEnabled) {
                        onOptionSelected(option.difficultyValue)
                    }
                },
                backgroundColor = option.backgroundColor,
                shadowColor = option.shadowColor,
                textColor = option.textColor,
                isLoading = isLoading
            )
            if (index < difficultyOptions.lastIndex) {
                Spacer(modifier = Modifier.height(optionSpacing))
            }
        }
    }
}
