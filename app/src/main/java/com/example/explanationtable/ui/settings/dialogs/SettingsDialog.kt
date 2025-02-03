package com.example.explanationtable.ui.settings.dialogs

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.options.SettingsOptions
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight

/**
 * Displays the settings dialog with options and an exit confirmation prompt.
 *
 * @param showDialog Controls whether the settings dialog should be shown.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param isDarkTheme True if the dark theme is active.
 * @param onToggleTheme Callback to toggle between themes.
 * @param isMuted True if the app is muted.
 * @param onToggleMute Callback to toggle the mute state.
 * @param onExit Callback invoked when the user confirms exiting the app.
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    // Manage the state for displaying the exit confirmation dialog.
    var showExitConfirmation by remember { mutableStateOf(false) }

    // If the dialog should not be shown, exit early.
    if (!showDialog) return

    // Main settings dialog using an AlertDialog.
    AlertDialog(
        onDismissRequest = onDismiss,
        // Title contains a close button aligned to the top-right.
        title = {
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
        // Content displays the settings options.
        text = {
            SettingsOptions(
                onDismiss = onDismiss,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                isMuted = isMuted,
                onToggleMute = onToggleMute,
                onExit = {
                    // Instead of immediate exit, show the confirmation dialog.
                    showExitConfirmation = true
                }
            )
        },
        confirmButton = {}, // No explicit confirm button is used.
        // Set the dialog's container color based on the current theme.
        containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium, // Use a medium shape for consistency.
        tonalElevation = 0.dp             // Remove elevation to avoid unwanted overlays.
    )

    // Exit confirmation dialog that appears when the user opts to exit.
    ConfirmationDialog(
        showDialog = showExitConfirmation,
        titleResId = R.string.confirm_exit_title,
        messageResId = R.string.confirm_exit_message,
        onConfirm = {
            // On confirmation, hide the confirmation dialog, execute exit logic, and dismiss the settings dialog.
            showExitConfirmation = false
            onExit()
            onDismiss()
        },
        onDismiss = {
            // Simply dismiss the confirmation dialog if the user cancels.
            showExitConfirmation = false
        }
    )
}
