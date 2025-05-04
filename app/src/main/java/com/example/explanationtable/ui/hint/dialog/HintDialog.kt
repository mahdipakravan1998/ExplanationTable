package com.example.explanationtable.ui.hint.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.ui.hint.component.HintOptionItem
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

// Spacing and sizing constants for consistent layout
private val OptionSpacing = 10.dp
private val TitleHorizontalPadding = 8.dp
private val ContentPadding = 16.dp
private val DialogCornerRadius = 8.dp
private val DialogElevation = 0.dp

/**
 * A modal dialog displaying a list of hint options.
 *
 * @param showDialog       Controls whether the dialog is visible.
 * @param onDismiss        Callback invoked when the user dismisses the dialog.
 * @param isDarkTheme      True if dark theme is active, false otherwise.
 * @param difficulty       Current difficulty level, passed to each hint item.
 * @param hintOptions      List of hint options to display.
 * @param onOptionSelected Callback when the user selects a hint option.
 */
@Composable
fun HintDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    hintOptions: List<HintOption>,
    onOptionSelected: (HintOption) -> Unit
) {
    // Only render when requested
    if (!showDialog) return

    // Pick colors based on the current theme
    val containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight
    val contentTextColor = if (isDarkTheme) TextDarkMode else Eel

    // Localized strings
    val titleText = stringResource(R.string.hint_dialog_title)
    val closeDesc = stringResource(R.string.close)

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(DialogCornerRadius),
        tonalElevation = DialogElevation,

        // Dialog title with centered text and a close button in the top-right corner
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = TitleHorizontalPadding),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = titleText,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = contentTextColor
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeDesc,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },

        // Body content: a column of HintOptionItems separated by spacers
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(ContentPadding),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                hintOptions.forEachIndexed { index, option ->
                    // Individual hint option row
                    HintOptionItem(
                        modifier = Modifier.fillMaxWidth(),
                        hintOption = option,
                        difficulty = difficulty,
                        isDarkTheme = isDarkTheme,
                        backgroundColor = containerColor,
                        onClick = { onOptionSelected(option) }
                    )
                    // Add spacing between items, but not after the last one
                    if (index < hintOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(OptionSpacing))
                    }
                }
            }
        },

        // No default confirm button; actions are handled via the list items and close icon
        confirmButton = {}
    )
}
