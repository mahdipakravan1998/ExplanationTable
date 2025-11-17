package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

/**
 * Centralized access point for app-wide settings.
 *
 * Emits stable, de-noised Flows suitable for Compose collection without triggering
 * unnecessary recompositions. All mutations are dispatched off the Main thread.
 *
 * @param dataStore Backing DataStore manager.
 * @param systemDarkDefault Fallback value for theme when user preference is not set.
 */
class SettingsRepository(
    private val dataStore: DataStoreManager,
    private val systemDarkDefault: Boolean
) {

    /**
     * Emits the user-selected dark theme preference; when unset, falls back to [systemDarkDefault].
     *
     * `distinctUntilChanged()` prevents redundant UI updates in Compose layers.
     */
    val isDarkTheme: Flow<Boolean> = dataStore
        .isDarkTheme
        .map { it ?: systemDarkDefault }
        .distinctUntilChanged()

    /**
     * Emits whether global sound is muted.
     *
     * Already stabilized via `distinctUntilChanged()` to reduce recompositions.
     */
    val isMuted: Flow<Boolean> = dataStore
        .isMuted
        .distinctUntilChanged()

    /**
     * Emits whether background music is enabled.
     *
     * Stabilized via `distinctUntilChanged()` for efficient UI collection.
     */
    val isMusicEnabled: Flow<Boolean> = dataStore
        .isMusicEnabled
        .distinctUntilChanged()

    /**
     * Emits current diamond count, stabilized for UI collection.
     */
    val diamonds: Flow<Int> = dataStore
        .diamonds
        .distinctUntilChanged()

    /**
     * Toggles mute state. Executed on IO to avoid blocking the Main thread.
     */
    suspend fun toggleMute() = withContext(Dispatchers.IO) {
        dataStore.toggleMute()
    }

    /**
     * Toggles background music enabled state. Executed on IO to avoid blocking the Main thread.
     */
    suspend fun toggleMusic() = withContext(Dispatchers.IO) {
        dataStore.toggleMusic()
    }

    /**
     * Persists theme preference. Executed on IO to avoid blocking the Main thread.
     */
    suspend fun setTheme(on: Boolean) = withContext(Dispatchers.IO) {
        dataStore.setTheme(on)
    }
}
