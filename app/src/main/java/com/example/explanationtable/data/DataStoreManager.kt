package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.*
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.dataStore by preferencesDataStore(name = "settings")

class DataStoreManager(private val context: Context) {

    companion object {
        val IS_MUTED = booleanPreferencesKey("is_muted")
        val IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
    }

    val isMuted: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_MUTED] ?: false
        }

    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME]
        }

    suspend fun toggleMute() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_MUTED] ?: false
            preferences[IS_MUTED] = !current
        }
    }

    suspend fun toggleTheme() {
        context.dataStore.edit { preferences ->
            val current = preferences[IS_DARK_THEME] ?: false
            preferences[IS_DARK_THEME] = !current
        }
    }
}
