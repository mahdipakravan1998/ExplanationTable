package com.example.explanationtable.ui.hint

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.data.getHintOptions
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

/**
 * Displays a dialog with a list of hint options based on the current difficulty.
 *
 * @param showDialog Controls whether the dialog is visible.
 * @param onDismiss Callback for dismissing the dialog.
 * @param isDarkTheme Flag indicating if dark theme is active.
 * @param difficulty Current difficulty level to customize hint rendering.
 * @param onOptionSelected Callback that is invoked when a hint option is chosen.
 */
@Composable
fun HintDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    onOptionSelected: (HintOption) -> Unit
) {
    // Do not render the dialog if it should not be visible.
    if (!showDialog) return

    // Retrieve the current context and cache the hint options to avoid recomputation.
    val context = LocalContext.current
    val hintOptions = remember(context) { getHintOptions(context) }

    // Choose colors based on the current theme.
    val containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel

    // Spacing constant between hint options.
    val optionSpacing = 10.dp

    // Retrieve localized strings to avoid repeated calls during recomposition.
    val dialogTitle = stringResource(id = R.string.hint_dialog_title)
    val closeDescription = stringResource(id = R.string.close)

    // Render the alert dialog with a title, a list of hint options, and customized appearance.
    AlertDialog(
        onDismissRequest = onDismiss,
        // Custom title section with the dialog title and a close button.
        title = {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                // Title text styled prominently.
                Text(
                    text = dialogTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = textColor
                )
                // Icon button for dismissing the dialog; aligned to the top-end corner.
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = closeDescription,
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        },
        // Content area displaying the list of hint options.
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Iterate over hint options and render each item.
                hintOptions.forEachIndexed { index, option ->
                    HintOptionItem(
                        modifier = Modifier.fillMaxWidth(),
                        hintOption = option,
                        difficulty = difficulty,
                        isDarkTheme = isDarkTheme,
                        backgroundColor = containerColor,
                        onClick = { onOptionSelected(option) }
                    )
                    // Add vertical spacing after each option except the last one.
                    if (index < hintOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(optionSpacing))
                    }
                }
            }
        },
        // No confirm button is required; leaving it empty.
        confirmButton = {},
        // Dialog appearance settings.
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = androidx.compose.foundation.shape.RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    )
}
