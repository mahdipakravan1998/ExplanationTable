package com.example.explanationtable.ui.settings

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
import com.example.explanationtable.ui.settings.components.CustomConfirmationDialog
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight

@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    currentTheme: Boolean, // True if Dark, false if Light
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
                SettingsOptions(
                    onDismiss = onDismiss,
                    currentTheme = currentTheme,
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
            containerColor = if (currentTheme) DialogBackgroundDark else DialogBackgroundLight,
            titleContentColor = MaterialTheme.colorScheme.onSurface,
            textContentColor = MaterialTheme.colorScheme.onSurface,
            // Adjust shape and elevation to prevent theme overlays
            shape = MaterialTheme.shapes.medium,
            tonalElevation = 0.dp
        )

        // Custom Confirmation Dialog for Exit Action
        CustomConfirmationDialog(
            showDialog = showExitConfirmation,
            title = R.string.confirm_exit_title,
            message = R.string.confirm_exit_message,
            onConfirm = {
                showExitConfirmation = false
                onExit()
                onDismiss() // Optionally dismiss the SettingsDialog after exiting
            },
            onDismiss = { showExitConfirmation = false }
        )
    }
}
