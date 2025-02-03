package com.example.explanationtable.ui.stages.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.stages.components.DifficultyOptions

/**
 * Displays a dialog that allows the user to select a difficulty option.
 *
 * @param showDialog Boolean flag indicating whether the dialog is visible.
 * @param onDismiss Callback triggered when the dialog is dismissed.
 * @param onOptionSelected Callback triggered when a difficulty option is selected.
 */
@Composable
fun DifficultyDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    // Do not render anything if the dialog should not be shown.
    if (!showDialog) return

    AlertDialog(
        // Called when the user dismisses the dialog (e.g., by clicking outside)
        onDismissRequest = onDismiss,
        // Title section containing a close button aligned at the top-end.
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth() // Occupies the full available width.
                    .wrapContentSize(Alignment.TopEnd) // Aligns its content (the close button) to the top end.
            ) {
                IconButton(
                    onClick = onDismiss, // Dismiss the dialog when clicked.
                    modifier = Modifier.padding(4.dp) // Adds padding around the button.
                ) {
                    Icon(
                        imageVector = Icons.Default.Close, // Uses the default close icon.
                        contentDescription = stringResource(id = R.string.close), // Accessibility description.
                        tint = MaterialTheme.colorScheme.onSurface // Sets the icon color based on the theme.
                    )
                }
            }
        },
        // Main content area displaying difficulty options.
        text = {
            DifficultyOptions(onOptionSelected = onOptionSelected)
        },
        // No explicit confirm button is needed for this dialog.
        confirmButton = {},
        // Apply the current theme's color scheme to the dialog.
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
