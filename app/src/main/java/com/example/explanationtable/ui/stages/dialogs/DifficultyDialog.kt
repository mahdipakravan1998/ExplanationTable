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
import com.example.explanationtable.ui.system.ImmersiveForDialog

/**
 * Displays a dialog that allows the user to select a difficulty option.
 *
 * Immersive behavior is applied to the dialog window via [ImmersiveForDialog],
 * so opening/closing this dialog never reveals system bars nor shifts content.
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
    if (!showDialog) return

    AlertDialog(
        onDismissRequest = onDismiss,
        // Put ImmersiveForDialog inside any slot to bind to this dialog's Window.
        title = {
            ImmersiveForDialog()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentSize(Alignment.TopEnd)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.padding(4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = stringResource(id = R.string.close),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        text = {
            DifficultyOptions(onOptionSelected = onOptionSelected)
        },
        confirmButton = {},
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
