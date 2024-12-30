package com.example.explanationtable.ui.main.viewmodel

import android.app.Application
import android.content.res.Configuration
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the MainPage, managing UI-related data and state.
 *
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val dataStoreManager = DataStoreManager(application)

    // Determine if the system is in Night Mode using the Configuration
    private val nightModeFlags = application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
    private val systemDark = (nightModeFlags == Configuration.UI_MODE_NIGHT_YES)

    // Map isDarkTheme Flow<Boolean?> to StateFlow<Boolean>, using systemDark as default
    val isDarkTheme: StateFlow<Boolean> = dataStoreManager.isDarkTheme
        .map { it ?: systemDark } // Use systemDark if no preference is set
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = systemDark // Initial value based on system theme
        )

    // Flow to observe mute state
    val isMuted: StateFlow<Boolean> = dataStoreManager.isMuted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    init {
        // Logging for debugging purposes
        viewModelScope.launch {
            isDarkTheme.collect { theme ->
                Log.d("MainViewModel", "Theme changed to: $theme")
            }
        }
    }

    /**
     * Toggles the mute state.
     */
    fun toggleMute() {
        viewModelScope.launch {
            dataStoreManager.toggleMute()
            Log.d("MainViewModel", "Mute state toggled")
        }
    }

    /**
     * Toggles the app theme between dark and light.
     */
    fun toggleTheme() {
        viewModelScope.launch {
            val currentTheme = isDarkTheme.value
            dataStoreManager.setTheme(!currentTheme)
            Log.d("MainViewModel", "Theme toggled to: ${!currentTheme}")
        }
    }
}
