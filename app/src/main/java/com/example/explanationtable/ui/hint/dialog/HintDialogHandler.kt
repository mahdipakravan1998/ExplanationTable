package com.example.explanationtable.ui.hint.dialog

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.CellPosition
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.LevelTable
import com.example.explanationtable.ui.hint.viewmodel.HintViewModel

/**
 * Hosts the hint‐dialog workflow: configures the ViewModel, observes its outputs,
 * and renders the dialog UI when requested.
 *
 * @param showDialog       Whether the dialog should be visible.
 * @param isDarkTheme      Whether the app is in dark‐theme mode.
 * @param difficulty       Difficulty level for generating hints.
 * @param originalTableState  Immutable snapshot of the table for hint logic.
 * @param currentTableState   Mutable map of current cell contents.
 * @param onDismiss         Callback invoked when the dialog is dismissed.
 * @param onCellsRevealed   Callback invoked with the positions of revealed cells.
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
    // Obtain (or create) the ViewModel scoped to this composable
    val viewModel: HintViewModel = viewModel()

    // Whenever the dialog opens, configure the ViewModel and load new hint options.
    LaunchedEffect(showDialog) {
        if (showDialog) {
            viewModel.run {
                setDifficulty(difficulty)
                setOriginalTableState(originalTableState)
                setCurrentTableState(currentTableState)
                loadHintOptions()
            }
        }
    }

    // Continuously observe the ViewModel's selected‐cells flow and forward updates.
    LaunchedEffect(viewModel.selectedCells) {
        viewModel.selectedCells.collect { cells ->
            onCellsRevealed(cells)
        }
    }

    // Collect hint options and diamond balance from the ViewModel as Compose state.
    val hintOptions by viewModel.hintOptions.collectAsState()
    val diamondBalance by viewModel.diamondBalance.collectAsState()

    // Render the dialog UI, passing through all necessary state and event handlers.
    HintDialog(
        showDialog = showDialog,
        onDismiss = onDismiss,
        isDarkTheme = isDarkTheme,
        difficulty = difficulty,
        hintOptions = hintOptions,
        balance = diamondBalance,
        onOptionSelected = { option ->
            // Delegate the selected option back to the ViewModel...
            viewModel.onOptionSelected(option)
            // …then close the dialog.
            onDismiss()
        }
    )
}
