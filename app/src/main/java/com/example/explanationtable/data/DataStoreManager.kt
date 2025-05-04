package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to initialize DataStore with the name "settings".
// This allows for a singleton-like access to DataStore on any Context instance.
val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * DataStoreManager provides an interface for reading and updating application settings,
 * such as mute, theme, and diamond preferences using Android's DataStore.
 *
 * @property context The Android context used to access the DataStore.
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // Keys used to store and retrieve preference values in the DataStore.
        val IS_MUTED = booleanPreferencesKey("is_muted")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val DIAMONDS_KEY = intPreferencesKey("diamonds")
    }

    /**
     * Flow that emits the current mute state.
     * Defaults to false if no value is set.
     */
    val isMuted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_MUTED] ?: false
        }

    /**
     * Flow that emits the current theme preference.
     * A null value indicates that no explicit theme preference has been set by the user.
     */
    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME]
        }

    /**
     * Flow that emits the current diamond count.
     * Defaults to 200 diamonds if no value is set.
     */
    val diamonds: Flow<Int> = context.dataStore.data
        .map { preferences ->
            preferences[DIAMONDS_KEY] ?: 200
        }

    /**
     * Toggles the mute state in the DataStore.
     * Reads the current mute state and then updates it to the opposite value.
     */
    suspend fun toggleMute() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_MUTED] ?: false
            preferences[IS_MUTED] = !current
        }
    }

    /**
     * Explicitly sets the theme preference in the DataStore.
     *
     * @param isDark A Boolean flag indicating whether dark theme should be enabled.
     */
    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }

    /**
     * Adds a specified amount of diamonds to the current count.
     *
     * @param amount The number of diamonds to add.
     */
    suspend fun addDiamonds(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[DIAMONDS_KEY] ?: 200
            preferences[DIAMONDS_KEY] = current + amount
        }
    }

    /**
     * Spends a specified amount of diamonds from the current count.
     *
     * @param amount The number of diamonds to subtract.
     */
    suspend fun spendDiamonds(amount: Int) {
        context.dataStore.edit { preferences ->
            val current = preferences[DIAMONDS_KEY] ?: 200
            preferences[DIAMONDS_KEY] = current - amount
        }
    }
}
