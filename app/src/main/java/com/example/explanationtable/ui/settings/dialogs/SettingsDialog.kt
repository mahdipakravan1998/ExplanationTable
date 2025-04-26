package com.example.explanationtable.ui.settings.dialogs

import android.app.Activity
import androidx.activity.compose.BackHandler
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
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.TextDarkMode
import com.example.explanationtable.ui.theme.Eel

/**
 * Settings dialog now backed by SettingsViewModel.
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
    // Local state for the exit‐confirmation overlay
    var showExitConfirmation by remember { mutableStateOf(false) }

    if (!showDialog) return

    // Collect flows from our ViewModel
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isMuted     by viewModel.isMuted.collectAsState()

    // Choose text color based on theme
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // Main settings AlertDialog
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
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
                onToggleTheme = { viewModel.toggleTheme() },
                isMuted        = isMuted,
                onToggleMute   = { viewModel.toggleMute() },
                onExit         = { showExitConfirmation = true }
            )
        },
        confirmButton    = { /* none */ },
        containerColor   = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor  = MaterialTheme.colorScheme.onSurface,
        shape            = MaterialTheme.shapes.medium,
        tonalElevation   = 0.dp
    )

    // Exit confirmation
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
