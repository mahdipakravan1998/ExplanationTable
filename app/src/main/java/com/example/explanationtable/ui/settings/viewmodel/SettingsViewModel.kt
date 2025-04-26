package com.example.explanationtable.ui.settings.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Settings dialog.
 * Delegates all data work to SettingsRepository.
 */
class SettingsViewModel(application: Application) : AndroidViewModel(application) {
    // Determine system default dark mode
    private val systemDarkDefault = (
            application.resources.configuration.uiMode
                    and Configuration.UI_MODE_NIGHT_MASK
            ) == Configuration.UI_MODE_NIGHT_YES

    // Build our repository
    private val settingsRepo = SettingsRepository(
        dataStore = DataStoreManager(application),
        systemDarkDefault = systemDarkDefault
    )

    /** Expose theme as StateFlow */
    val isDarkTheme: StateFlow<Boolean> = settingsRepo
        .isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = systemDarkDefault
        )

    /** Expose mute as StateFlow */
    val isMuted: StateFlow<Boolean> = settingsRepo
        .isMuted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /** Toggle theme */
    fun toggleTheme() {
        viewModelScope.launch {
            settingsRepo.setTheme(!isDarkTheme.value)
        }
    }

    /** Toggle mute */
    fun toggleMute() {
        viewModelScope.launch {
            settingsRepo.toggleMute()
        }
    }
}
