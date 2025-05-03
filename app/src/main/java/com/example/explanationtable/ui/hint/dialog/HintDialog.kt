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

@Composable
fun HintDialog(
    showDialog: Boolean,
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    hintOptions: List<HintOption>,
    onOptionSelected: (HintOption) -> Unit
) {
    if (!showDialog) return

    val containerColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight
    val textColor = if (isDarkTheme) TextDarkMode else Eel
    val optionSpacing = 10.dp
    val dialogTitle = stringResource(id = R.string.hint_dialog_title)
    val closeDescription = stringResource(id = R.string.close)

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
                    text = dialogTitle,
                    style = MaterialTheme.typography.headlineSmall.copy(fontWeight = FontWeight.Black),
                    color = textColor
                )
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
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                hintOptions.forEachIndexed { index, option ->
                    HintOptionItem(
                        modifier = Modifier.fillMaxWidth(),
                        hintOption = option,
                        difficulty = difficulty,
                        isDarkTheme = isDarkTheme,
                        backgroundColor = containerColor,
                        onClick = { onOptionSelected(option) }
                    )
                    if (index < hintOptions.lastIndex) {
                        Spacer(modifier = Modifier.height(optionSpacing))
                    }
                }
            }
        },
        confirmButton = {},
        containerColor = containerColor,
        titleContentColor = MaterialTheme.colorScheme.onSurface,
        textContentColor = MaterialTheme.colorScheme.onSurface,
        shape = RoundedCornerShape(8.dp),
        tonalElevation = 0.dp
    )
}
