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
 * A composable that displays the settings dialog with various options and an exit confirmation.
 *
 * @param showDialog Whether the settings dialog should be shown.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param isDarkTheme True if dark theme is active, false otherwise.
 * @param onToggleTheme Callback to toggle the theme.
 * @param isMuted True if the app is muted, false otherwise.
 * @param onToggleMute Callback to toggle the mute state.
 * @param onExit Callback invoked when the user confirms exiting the app.
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean, // True if Dark, false if Light
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    // State to control the visibility of the confirmation dialog
    var showExitConfirmation by remember { mutableStateOf(false) }

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
                SettingsOptions(
                    onDismiss = onDismiss,
                    currentTheme = isDarkTheme,
                    onToggleTheme = onToggleTheme,
                    isMuted = isMuted,
                    onToggleMute = onToggleMute,
                    onExit = {
                        // Show the confirmation dialog instead of exiting immediately
                        showExitConfirmation = true
                    }
                )
            },
            confirmButton = {},
            // Set the container color based on the theme
            containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            // Adjust shape and elevation to prevent theme overlays
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 0.dp
        )

        // Confirmation Dialog for Exit Action
        ConfirmationDialog(
            showDialog = showExitConfirmation,
            titleResId = R.string.confirm_exit_title,
            messageResId = R.string.confirm_exit_message,
            onConfirm = {
                showExitConfirmation = false
                onExit()
                onDismiss() // Optionally dismiss the SettingsDialog after exiting
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}
