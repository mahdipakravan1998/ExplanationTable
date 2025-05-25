package com.example.explanationtable.ui.hint.dialog

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.ui.hint.viewmodel.HintViewModel

/**
 * Composable function to handle the Hint Dialog interaction.
 *
 * It manages the dialog's visibility, configuration, and communication with the ViewModel.
 *
 * @param showDialog Boolean flag to control dialog visibility.
 * @param isDarkTheme Boolean flag indicating whether the dark theme is active.
 * @param difficulty The selected difficulty level.
 * @param originalTableState The original state of the table, used for hint calculations.
 * @param currentTableState The mutable state of the current table.
 * @param onDismiss Lambda function called when the dialog is dismissed.
 * @param onCellsRevealed Lambda function that is triggered when a set of cells are revealed.
 */
@Composable
fun HintDialogHandler(
    showDialog: Boolean,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    originalTableState: LevelTable?,
    currentTableState: MutableMap<CellPosition, List<String>>?,
    onDismiss: () -> Unit,
    onCellsRevealed: (List<CellPosition>) -> Unit
) {
    // Get the HintViewModel to manage the state and actions related to hint options
    val viewModel: HintViewModel = viewModel()

    // Load and set the hint options whenever the dialog visibility changes
    LaunchedEffect(showDialog) {
        if (showDialog) {
            viewModel.apply {
                setDifficulty(difficulty)
                setOriginalTableState(originalTableState)
                setCurrentTableState(currentTableState)
                loadHintOptions()
            }
        }
    }

    // Forward selected cells to the caller when any selection occurs in the ViewModel
    LaunchedEffect(viewModel.selectedCells) {
        viewModel.selectedCells.collect { selectedCells ->
            onCellsRevealed(selectedCells)
        }
    }

    // Observe the hint options provided by the ViewModel
    val hintOptions by viewModel.hintOptions.collectAsState()

    val balance by viewModel.diamondBalance.collectAsState()

    // Render the Hint Dialog with the required parameters and actions
    HintDialog(
        showDialog = showDialog,
        onDismiss = onDismiss,
        isDarkTheme = isDarkTheme,
        difficulty = difficulty,
        hintOptions = hintOptions,
        balance = balance,
        onOptionSelected = { selectedOption ->
            // Perform the hint action by passing the selected option to the ViewModel
            viewModel.onOptionSelected(selectedOption)
            // Close the dialog after the option is selected
            onDismiss()
        }
    )
}
