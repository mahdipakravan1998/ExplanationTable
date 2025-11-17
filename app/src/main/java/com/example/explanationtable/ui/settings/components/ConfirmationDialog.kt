package com.example.explanationtable.ui.settings.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.example.explanationtable.R
import com.example.explanationtable.ui.sfx.LocalUiSoundManager

/**
 * A customizable confirmation dialog with a title, message, and action buttons.
 *
 * @param showDialog Whether the dialog should be displayed.
 * @param titleResId Resource ID for the dialog title.
 * @param messageResId Resource ID for the dialog message.
 * @param confirmTextResId Resource ID for the confirm button text (default is "Yes").
 * @param dismissTextResId Resource ID for the dismiss button text (default is "No").
 * @param onConfirm Callback invoked when the confirm button is clicked.
 * @param onDismiss Callback invoked when the dismiss button is clicked or the dialog is dismissed.
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
    // Only show the dialog if the flag is true.
    if (showDialog) {
        val uiSoundManager = LocalUiSoundManager.current

        Dialog(onDismissRequest = onDismiss) {
            // Surface defines the background shape, color, and elevation.
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surface,
                tonalElevation = 8.dp
            ) {
                // Column to arrange the title, message, and buttons vertically.
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                ) {
                    // Dialog title text.
                    Text(
                        text = stringResource(id = titleResId),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Spacer between title and message.
                    Spacer(modifier = Modifier.height(8.dp))

                    // Dialog message text.
                    Text(
                        text = stringResource(id = messageResId),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurface,
                        textAlign = TextAlign.End,
                        modifier = Modifier.fillMaxWidth()
                    )

                    // Spacer before the action buttons.
                    Spacer(modifier = Modifier.height(24.dp))

                    // Row to arrange action buttons horizontally.
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.Start,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Confirm action button.
                        TextButton(
                            onClick = {
                                uiSoundManager.playClick()
                                onConfirm()
                            }
                        ) {
                            Text(
                                text = stringResource(id = confirmTextResId),
                                color = MaterialTheme.colorScheme.error
                            )
                        }

                        // Spacer between the confirm and dismiss buttons.
                        Spacer(modifier = Modifier.width(8.dp))

                        // Dismiss action button.
                        TextButton(
                            onClick = {
                                uiSoundManager.playClick()
                                onDismiss()
                            }
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
