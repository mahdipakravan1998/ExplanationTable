package com.example.explanationtable.ui.popup

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.material3.MaterialTheme
import com.example.explanationtable.R
import com.example.explanationtable.ui.popup.components.OptionCard

@Composable
fun PopupOptions(onOptionSelected: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        // Easy = colorScheme.primary
        OptionCard(
            label          = stringResource(id = R.string.easy_step_label),
            onClick        = { onOptionSelected("Easy") },
            backgroundColor= MaterialTheme.colorScheme.primary,          // #228B22 (Light) or #2E8B57 (Dark)
            shadowColor    = MaterialTheme.colorScheme.primaryContainer, // #3CB371 or #3CB371
            textColor      = MaterialTheme.colorScheme.onPrimary,        // #FFFFFF or #D3D3D3
            imageResId     = R.drawable.emerald
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Medium = colorScheme.secondary
        OptionCard(
            label          = stringResource(id = R.string.medium_step_label),
            onClick        = { onOptionSelected("Medium") },
            backgroundColor= MaterialTheme.colorScheme.secondary,
            shadowColor    = MaterialTheme.colorScheme.secondaryContainer,
            textColor      = MaterialTheme.colorScheme.onSecondary,
            imageResId     = R.drawable.crown
        )
        Spacer(modifier = Modifier.height(8.dp))

        // Hard = colorScheme.tertiary
        OptionCard(
            label          = stringResource(id = R.string.difficult_step_label),
            onClick        = { onOptionSelected("Hard") },
            backgroundColor= MaterialTheme.colorScheme.tertiary,
            shadowColor    = MaterialTheme.colorScheme.tertiaryContainer,
            textColor      = MaterialTheme.colorScheme.onTertiary,
            imageResId     = R.drawable.ruby_diamond
        )
    }
}
