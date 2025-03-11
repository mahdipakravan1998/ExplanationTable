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

    // Instance of DataStoreManager to handle persistent settings (e.g., theme, mute, and diamond state)
    private val dataStoreManager = DataStoreManager(application)

    // Determine whether the system is using dark mode based on the current configuration.
    private val systemDark: Boolean =
        (application.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) == Configuration.UI_MODE_NIGHT_YES

    /**
     * StateFlow representing the app's theme mode (dark or light).
     * Observes the theme preference from DataStoreManager. If no preference is set,
     * it falls back to the system default (systemDark).
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
     * Observes the mute preference from DataStoreManager with a default of 'false'.
     */
    val isMuted: StateFlow<Boolean> = dataStoreManager.isMuted
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = false
        )

    /**
     * StateFlow representing the current diamond (gem) count.
     * Defaults to 200 diamonds if no value is set.
     */
    val diamonds: StateFlow<Int> = dataStoreManager.diamonds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 200
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
     */
    fun toggleTheme() {
        viewModelScope.launch {
            val currentTheme = isDarkTheme.value
            dataStoreManager.setTheme(!currentTheme)
        }
    }

    /**
     * Adds a specified amount of diamonds to the current count.
     *
     * @param amount The number of diamonds to add.
     */
    fun addDiamonds(amount: Int) {
        viewModelScope.launch {
            dataStoreManager.addDiamonds(amount)
        }
    }

    /**
     * Spends a specified amount of diamonds from the current count.
     *
     * @param amount The number of diamonds to spend.
     */
    fun spendDiamonds(amount: Int) {
        viewModelScope.launch {
            dataStoreManager.spendDiamonds(amount)
        }
    }
}
