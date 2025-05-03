package com.example.explanationtable.ui.hint.dialog

import androidx.compose.runtime.*
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.ui.gameplay.table.CellPosition
import com.example.explanationtable.ui.hint.viewmodel.HintViewModel

@Composable
fun HintDialogHandler(
    showDialog: Boolean,
    isDarkTheme: Boolean,
    difficulty: Difficulty,
    originalTableState: EasyLevelTable?,
    currentTableState: MutableMap<CellPosition, List<String>>?,
    onDismiss: () -> Unit,
    onCellsRevealed: (List<CellPosition>) -> Unit
) {
    val viewModel: HintViewModel = viewModel()

    // When dialog opens, configure and load hint options
    LaunchedEffect(showDialog) {
        if (showDialog) {
            viewModel.setDifficulty(difficulty)
            viewModel.setOriginalTableState(originalTableState)
            viewModel.setCurrentTableState(currentTableState)
            viewModel.loadHintOptions()
        }
    }

    // Forward any reveal events back to the caller
    LaunchedEffect(viewModel.selectedCells) {
        viewModel.selectedCells.collect { cells ->
            onCellsRevealed(cells)
        }
    }

    val hintOptions by viewModel.hintOptions.collectAsState()

    HintDialog(
        showDialog = showDialog,
        onDismiss = onDismiss,
        isDarkTheme = isDarkTheme,
        difficulty = difficulty,
        hintOptions = hintOptions,
        onOptionSelected = { selectedOption ->
            // 1) Tell VM to perform the hint action
            viewModel.onOptionSelected(selectedOption)
            // 2) Immediately close the dialog, same as old behavior
            onDismiss()
        }
    )
}
