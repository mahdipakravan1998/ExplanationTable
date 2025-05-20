package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first

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
        val IS_MUTED      = booleanPreferencesKey("is_muted")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        val DIAMONDS_KEY  = intPreferencesKey("diamonds")
        private const val DEFAULT_DIAMONDS = 200
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

    /** Flow that emits the current diamond count (default 200). */
    val diamonds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[DIAMONDS_KEY] ?: DEFAULT_DIAMONDS }

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

    /** Returns the current diamond count, suspending until retrieved */
    suspend fun getDiamondCount(): Int =
        context.dataStore.data
            .map { prefs -> prefs[DIAMONDS_KEY] ?: DEFAULT_DIAMONDS }
            .first()

    /**
     * Adds a specified amount of diamonds to the current count.
     *
     * @param amount The number of diamonds to add.
     */
    suspend fun addDiamonds(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[DIAMONDS_KEY] ?: DEFAULT_DIAMONDS
            prefs[DIAMONDS_KEY] = current + amount
        }
    }

    /**
     * Deducts the given amount of diamonds, never going below zero.
     */
    suspend fun spendDiamonds(amount: Int) {
        val current = getDiamondCount()
        context.dataStore.edit { prefs ->
            prefs[DIAMONDS_KEY] = (current - amount).coerceAtLeast(0)
        }
    }
}
