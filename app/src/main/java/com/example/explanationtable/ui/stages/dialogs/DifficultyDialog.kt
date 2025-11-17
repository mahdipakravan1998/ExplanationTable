package com.example.explanationtable.ui.stages.dialogs

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.sfx.LocalUiSoundManager
import com.example.explanationtable.ui.stages.components.DifficultyOptions
import com.example.explanationtable.ui.system.ImmersiveForDialog
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

/**
 * Displays a dialog that allows the user to select a difficulty option.
 *
 * Immersive behavior is applied to the dialog window via [ImmersiveForDialog],
 * so opening/closing this dialog never reveals system bars nor shifts content.
 *
 * @param isDarkTheme Whether dark theme is active (for title color parity with Settings).
 * @param showDialog Boolean flag indicating whether the dialog is visible.
 * @param isInteractionLocked When true, the dialog cannot be dismissed and options behave
 * as read-only (used while navigation to the stages list is in progress).
 * @param onDismiss Callback triggered when the dialog is dismissed.
 * @param onOptionSelected Callback triggered when a difficulty option is selected.
 */
@Composable
fun DifficultyDialog(
    isDarkTheme: Boolean,
    showDialog: Boolean,
    isInteractionLocked: Boolean = false,
    onDismiss: () -> Unit,
    onOptionSelected: (String) -> Unit
) {
    if (!showDialog) return

    val textColor = if (isDarkTheme) TextDarkMode else Eel
    val uiSoundManager = LocalUiSoundManager.current

    AlertDialog(
        onDismissRequest = {
            if (!isInteractionLocked) {
                onDismiss()
            }
        },
        // Keep ImmersiveForDialog inside the title slot so it binds to the dialog's Window.
        title = {
            ImmersiveForDialog()

            // Mirror SettingsDialog: centered title, close at TopEnd, same horizontal padding.
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = stringResource(R.string.table_types_title),
                    modifier = Modifier
                        .semantics { heading() },
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = textColor,
                    textAlign = TextAlign.Center
                )

                IconButton(
                    onClick = {
                        if (!isInteractionLocked) {
                            uiSoundManager.playClick()
                            onDismiss()
                        }
                    },
                    enabled = !isInteractionLocked,
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
            // Small buffer so the options don't crowd the title (kept from your previous layout).
            Box(modifier = Modifier.padding(top = 8.dp)) {
                DifficultyOptions(
                    onOptionSelected = onOptionSelected,
                    interactionEnabled = !isInteractionLocked
                )
            }
        },
        confirmButton = {},
        // Match SettingsDialog corner shape for visual parity
        shape = MaterialTheme.shapes.medium,
        containerColor = MaterialTheme.colorScheme.surface,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface
    )
}
