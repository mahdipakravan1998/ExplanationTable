package com.example.explanationtable.ui.hint

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import com.example.explanationtable.R
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.model.Difficulty

/**
 * Composable that handles the logic for displaying the hint dialog and managing user selection.
 *
 * @param showDialog Boolean indicating if the hint dialog should be visible.
 * @param isDarkTheme Boolean indicating if dark theme is enabled.
 * @param difficulty The difficulty level of the current stage.
 * @param originalTableState The original table state to help with hint calculation.
 * @param currentTableState The current table state to help with hint calculation.
 * @param onDismiss Function to call when the dialog is dismissed.
 * @param onOptionSelected Callback function for handling user-selected options.
 */
@Composable
fun HintDialogHandler(
    showDialog: Boolean,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    originalTableState: EasyLevelTable?,
    currentTableState: MutableMap<CellPosition, List<String>>?,
    onDismiss: () -> Unit,
    onOptionSelected: (List<CellPosition>) -> Unit
) {
    val context = LocalContext.current

    // Pass showDialog correctly to HintDialog
    HintDialog(
        showDialog = showDialog,
        onDismiss = { onDismiss() },
        isDarkTheme = isDarkTheme,
        difficulty = difficulty,
        onOptionSelected = { selectedOption ->
            when (selectedOption.displayText) {
                // Handle hint for single word
                context.getString(R.string.hint_single_word) -> {
                    originalTableState?.let { origData ->
                        currentTableState?.let { currData ->
                            revealRandomCategory(
                                currentTableData = currData,
                                originalTableData = origData,
                                onCellsCorrectlyPlaced = { correctPositions ->
                                    // Notify about correct positions
                                    onOptionSelected(correctPositions)
                                }
                            )
                        }
                    }
                }
                // Handle hint for single letter
                context.getString(R.string.hint_single_letter) -> {
                    originalTableState?.let { origData ->
                        currentTableState?.let { currData ->
                            revealRandomCell(
                                currentTableData = currData,
                                originalTableData = origData,
                                onCellCorrectlyPlaced = { correctPositions ->
                                    // Notify about correct positions
                                    onOptionSelected(correctPositions)
                                }
                            )
                        }
                    }
                }
                // Handle complete stage hint
                context.getString(R.string.hint_complete_stage) -> {
                    onOptionSelected(emptyList())  // No cells to notify for complete stage
                }
            }
            onDismiss()
        }
    )
}
