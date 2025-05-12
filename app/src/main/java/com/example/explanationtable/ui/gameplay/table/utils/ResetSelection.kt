package com.example.explanationtable.ui.gameplay.table.utils

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import kotlinx.coroutines.delay

/**
 * When [isSelectionComplete] is true, this will reset the two selection state properties
 * after a short delay by invoking the passed callbacks.
 */
@Composable
fun ResetSelection(
    isSelectionComplete: Boolean,
    onReset: () -> Unit
) {
    if (isSelectionComplete) {
        LaunchedEffect(Unit) {
            delay(200)
            onReset()
        }
    }
}
