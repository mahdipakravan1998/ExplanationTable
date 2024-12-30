package com.example.explanationtable.ui.settings.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.explanationtable.R

/**
 * A customizable confirmation dialog with a title, message, and action buttons.
 *
 * @param showDialog Whether the dialog should be shown.
 * @param titleResId Resource ID for the dialog title.
 * @param messageResId Resource ID for the dialog message.
 * @param confirmTextResId Resource ID for the confirm button text.
 * @param dismissTextResId Resource ID for the dismiss button text.
 * @param onConfirm Callback invoked when the confirm button is clicked.
 * @param onDismiss Callback invoked when the dismiss button is clicked or dialog is dismissed.
 */
@Composable
fun ConfirmationDialog(
    showDialog: Boolean,
    titleResId: Int,
    messageResId: Int,
    confirmTextResId: Int = R.string.yes,
    dismissTextResId: Int = R.string.no,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    if (showDialog) {
        Dialog(onDismissRequest = onDismiss) {
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Title
                    Text(
                        text = stringResource(id = titleResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    // Message
                    Text(
                        text = stringResource(id = messageResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(24.dp))

                    // Buttons
                    Row(
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        // Confirm Button
                        TextButton(
                            onClick = onConfirm
                        ) {
                            Text(
                                text = stringResource(id = confirmTextResId),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        // Dismiss Button
                        TextButton(
                            onClick = onDismiss
                        ) {
                            Text(
                                text = stringResource(id = dismissTextResId),
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }
            }
        }
    }
}
