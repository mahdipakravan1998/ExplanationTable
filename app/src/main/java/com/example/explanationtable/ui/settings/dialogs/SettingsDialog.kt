package com.example.explanationtable.ui.settings.dialogs

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.options.SettingsOptions
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

/**
 * Displays the settings dialog with various options and an exit confirmation prompt.
 *
 * @param showDialog Controls the visibility of the settings dialog.
 * @param onDismiss Callback invoked when the dialog is dismissed.
 * @param isDarkTheme Indicates if the dark theme is active.
 * @param onToggleTheme Callback to toggle between dark and light themes.
 * @param isMuted Indicates if the app is in mute mode.
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
    // State to control the visibility of the exit confirmation dialog.
    var showExitConfirmation by remember { mutableStateOf(false) }

    // Exit early if the settings dialog should not be displayed.
    if (!showDialog) return

    // Choose text color based on the current theme.
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // Main settings dialog using AlertDialog.
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Container for the dialog title and the close button.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Centered title text with bold styling.
                Text(
                    text = stringResource(id = R.string.Settings_dialog_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = textColor
                )
                // Close button aligned to the top-end corner.
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
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
            // Display the various settings options.
            SettingsOptions(
                onDismiss = onDismiss,
                isDarkTheme = isDarkTheme,
                onToggleTheme = onToggleTheme,
                isMuted = isMuted,
                onToggleMute = onToggleMute,
                onExit = {
                    // Trigger the exit confirmation dialog rather than immediate exit.
                    showExitConfirmation = true
                }
            )
        },
        confirmButton = {}, // No explicit confirm button for the main dialog.
        // Choose background color based on the theme.
        containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = MaterialTheme.shapes.medium, // Consistent medium shape style.
        tonalElevation = 0.dp               // Remove elevation to avoid undesired shadows.
    )

    // Exit confirmation dialog that appears when the user opts to exit.
    ConfirmationDialog(
        showDialog = showExitConfirmation,
        titleResId = R.string.confirm_exit_title,
        messageResId = R.string.confirm_exit_message,
        onConfirm = {
            // On confirmation: hide the exit dialog, execute exit logic, and dismiss the settings dialog.
            showExitConfirmation = false
            onExit()
            onDismiss()
        },
        onDismiss = {
            // Dismiss the exit confirmation if the user cancels.
            showExitConfirmation = false
        }
    )
}
