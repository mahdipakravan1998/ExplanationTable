package com.example.explanationtable.ui.main

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    // Determine if the system is in Night Mode using the Configuration
    private val nightModeFlags = application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    private val systemDark = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)

    val isMuted: StateFlow<Boolean> = dataStoreManager.isMuted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = false
        )

    // Start with `systemDark` to match the user's device theme at app launch
    val isDarkTheme: StateFlow<Boolean> = dataStoreManager.isDarkTheme
        .map { it ?: systemDark } // Use systemDark if no preference is set
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Lazily,
            initialValue = systemDark
        )

    fun toggleMute() {
        viewModelScope.launch {
            dataStoreManager.toggleMute()
        }
    }

    fun toggleTheme() {
        viewModelScope.launch {
            dataStoreManager.toggleTheme()
        }
    }
}
