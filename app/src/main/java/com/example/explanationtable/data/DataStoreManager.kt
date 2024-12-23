package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

// Extension property to create DataStore
val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val IS_MUTED = booleanPreferencesKey("is_muted")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    // Flow to observe mute state
    val isMuted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_MUTED] ?: false
        }

    // Nullable Flow to observe theme state; null indicates no user preference
    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME]
        }

    // Toggles the mute state
    suspend fun toggleMute() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_MUTED] ?: false
            preferences[IS_MUTED] = !current
        }
    }

    // Sets the theme explicitly
    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME] = isDark
        }
    }
}
