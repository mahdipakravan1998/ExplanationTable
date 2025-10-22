package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.explanationtable.repository.StageRepository

/**
 * Factory for creating [StageViewModel] instances without a DI framework.
 *
 * This factory is intentionally narrow: it avoids reflection and heavy dependencies,
 * keeps construction cost minimal, and preserves current behavior exactly.
 *
 * Usage:
 * ```
 * val factory = StageViewModelFactory(repository)
 * val vm = ViewModelProvider(owner, factory)[StageViewModel::class.java]
 * ```
 */
class StageViewModelFactory(
    /** Repository dependency provided to the ViewModel. */
    private val repository: StageRepository
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(StageViewModel::class.java)) {
            // Narrow, reflection-free creation; preserves existing behavior.
            return StageViewModel(repository) as T
        }
        // Clearer, more actionable error for misconfiguration.
        throw IllegalArgumentException(
            "StageViewModelFactory can only create ${StageViewModel::class.java.name}; requested ${modelClass.name}"
        )
    }
}
