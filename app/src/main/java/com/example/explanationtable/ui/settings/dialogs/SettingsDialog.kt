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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.R
import com.example.explanationtable.ui.settings.components.ConfirmationDialog
import com.example.explanationtable.ui.settings.options.SettingsOptions
import com.example.explanationtable.ui.settings.viewmodel.SettingsViewModel
import com.example.explanationtable.ui.system.ImmersiveForDialog
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.ui.theme.Eel

/**
 * Settings dialog backed by SettingsViewModel.
 *
 * Immersive behavior is applied to the dialog window via [ImmersiveForDialog],
 * preventing system-bar flicker and any content reflow underneath.
 *
 * @param showDialog Controls whether to display the settings dialog.
 * @param onDismiss To hide the settings dialog.
 * @param onExit    Called (from MainPage) to finish the Activity.
 */
@Composable
fun SettingsDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    onExit: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    var showExitConfirmation by remember { mutableStateOf(false) }

    if (!showDialog) return

    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isMuted     by viewModel.isMuted.collectAsState()
    val isMusicEnabled by viewModel.isMusicEnabled.collectAsState()

    val textColor = if (isDarkTheme) TextDarkMode else Eel

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            // Bind immersive behavior to this dialog's Window.
            ImmersiveForDialog()

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(id = R.string.Settings_dialog_title),
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = textColor
                )
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
            SettingsOptions(
                onDismiss      = onDismiss,
                isDarkTheme    = isDarkTheme,
                onToggleTheme  = { viewModel.toggleTheme() },
                isMuted        = isMuted,
                onToggleMute   = { viewModel.toggleMute() },
                isMusicEnabled = isMusicEnabled,
                onToggleMusic  = { viewModel.toggleMusic() },
                onExit         = { showExitConfirmation = true }
            )
        },
        confirmButton     = { /* none */ },
        containerColor    = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor  = MaterialTheme.colorScheme.onSurface,
        shape             = MaterialTheme.shapes.medium,
        tonalElevation    = 0.dp
    )

    // Exit confirmation (likely another AlertDialog). Ensure it also uses ImmersiveForDialog inside.
    ConfirmationDialog(
        showDialog     = showExitConfirmation,
        titleResId     = R.string.confirm_exit_title,
        messageResId   = R.string.confirm_exit_message,
        onConfirm      = {
            showExitConfirmation = false
            onExit()
            onDismiss()
        },
        onDismiss      = { showExitConfirmation = false }
    )
}
