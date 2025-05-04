package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Single place for all app-wide settings data.
 */
class SettingsRepository(
    private val dataStore: DataStoreManager,
    private val systemDarkDefault: Boolean
) {
    /** Emits the user-chosen theme, or falls back to system default. */
    val isDarkTheme: Flow<Boolean> = dataStore
        .isDarkTheme
        .map { it ?: systemDarkDefault }

    /** Emits whether the app is muted. */
    val isMuted: Flow<Boolean> = dataStore.isMuted

    /** Emits the current diamond count. */
    val diamonds: Flow<Int> = dataStore.diamonds

    suspend fun toggleMute()   = dataStore.toggleMute()
    suspend fun setTheme(on: Boolean) = dataStore.setTheme(on)
}
