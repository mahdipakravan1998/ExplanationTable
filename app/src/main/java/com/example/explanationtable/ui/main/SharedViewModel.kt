package com.example.explanationtable.ui.main

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class SharedViewModel : ViewModel() {
    private val _showSettingsDialog = MutableStateFlow(false)
    val showSettingsDialog: StateFlow<Boolean> = _showSettingsDialog

    fun showSettings() {
        _showSettingsDialog.value = true
    }

    fun hideSettings() {
        _showSettingsDialog.value = false
    }
}
