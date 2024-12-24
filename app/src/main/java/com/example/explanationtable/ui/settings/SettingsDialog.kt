package com.example.explanationtable.ui.popup

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

/**
 * A reusable composable for showing the settings dialog.
 *
 * @param showDialog Whether the dialog should be shown.
 * @param onDismiss Callback when the dialog is dismissed.
 * @param currentTheme Whether the current theme is dark or not.
 * @param onToggleTheme Callback to toggle the theme.
 * @param isMuted Whether the sound is currently muted.
 * @param onToggleMute Callback to toggle mute/unmute.
 * @param onExit Callback to exit the app or perform cleanup.
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentTheme: Boolean,
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    if (showDialog) {
        AlertDialog(
            onDismissRequest = onDismiss,
            title = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .wrapContentSize(Alignment.TopEnd)
                ) {
                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .padding(4.dp)
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
                // Place your updated SettingsPopup here
                SettingsPopup(
                    onDismiss = onDismiss,
                    currentTheme = currentTheme,
                    onToggleTheme = onToggleTheme,
                    isMuted = isMuted,
                    onToggleMute = onToggleMute,
                    onExit = onExit
                )
            },
            confirmButton = {},
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface
        )
    }
}
