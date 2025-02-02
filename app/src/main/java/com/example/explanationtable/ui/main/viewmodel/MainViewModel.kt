package com.example.explanationtable.ui.main.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

/**
 * ViewModel for the main page that manages UI-related data and state.
 *
 * @param application The application context.
 */
class MainViewModel(application: Application) : AndroidViewModel(application) {

    // Instance of DataStoreManager to handle persistent settings (e.g., theme and mute state)
    private val dataStoreManager = DataStoreManager(application)

    // Determine whether the system is using dark mode based on the current configuration.
    private val systemDark: Boolean =
        (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /**
     * StateFlow representing the app's theme mode (dark or light).
     *
     * Observes the theme preference from DataStoreManager. If no preference is set,
     * it falls back to the system default (systemDark).
     *
     * Note: Changed from SharingStarted.WhileSubscribed to SharingStarted.Eagerly so that
     * the flow is always active. This ensures that changes to the theme are immediately
     * reflected even on pages that may not have an active subscription at all times.
     */
    val isDarkTheme: StateFlow<Boolean> = dataStoreManager.isDarkTheme
        .map { it ?: systemDark }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = systemDark
        )

    /**
     * StateFlow representing the app's mute state.
     *
     * Observes the mute preference from DataStoreManager with a default of 'false'.
     * (Using WhileSubscribed is acceptable here if immediate updates are not critical.)
     */
    val isMuted: StateFlow<Boolean> = dataStoreManager.isMuted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * Toggles the mute state by invoking the corresponding method in DataStoreManager.
     */
    fun toggleMute() {
        viewModelScope.launch {
            dataStoreManager.toggleMute()
        }
    }

    /**
     * Toggles the app theme between dark and light modes.
     *
     * Reads the current theme from isDarkTheme and sets the opposite value.
     */
    fun toggleTheme() {
        viewModelScope.launch {
            val currentTheme = isDarkTheme.value
            dataStoreManager.setTheme(!currentTheme)
        }
    }
}
