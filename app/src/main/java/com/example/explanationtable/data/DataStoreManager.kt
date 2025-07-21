package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

// -----------------------------------------------------------------------------
// Extension property to lazily initialize a single DataStore instance per Context
// -----------------------------------------------------------------------------
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * DataStoreManager
 *
 * Provides reactive Flows and suspend functions for reading/updating:
 *  • Mute state
 *  • Theme preference
 *  • Diamond count
 *
 * @param context Android Context used to access the DataStore.
 */
class DataStoreManager(private val context: Context) {

    companion object {
        // Preference Keys
        private val KEY_IS_MUTED      = booleanPreferencesKey("is_muted")
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_DIAMONDS      = intPreferencesKey("diamonds")
        private val KEY_LAST_UNLOCKED_EASY   = intPreferencesKey("last_unlocked_easy")
        private val KEY_LAST_UNLOCKED_MEDIUM = intPreferencesKey("last_unlocked_medium")
        private val KEY_LAST_UNLOCKED_HARD   = intPreferencesKey("last_unlocked_hard")

        // Default Values
        private const val DEFAULT_DIAMONDS = 200
    }

    // -----------------------------------------------------------------------------
    // Public Flows
    // -----------------------------------------------------------------------------

    /**
     * Emits the current mute state.
     * Defaults to false if not set.
     */
    val isMuted: Flow<Boolean> = context.dataStore.data
        .map { prefs -> prefs[KEY_IS_MUTED] ?: false }

    /**
     * Emits the current theme preference:
     *  • true  = dark theme enabled
     *  • false = light theme enabled
     *  • null  = user hasn’t set a preference
     */
    val isDarkTheme: Flow<Boolean?> = context.dataStore.data
        .map { prefs -> prefs[KEY_IS_DARK_THEME] }

    /**
     * Emits the current diamond count.
     * Defaults to [DEFAULT_DIAMONDS] if not set.
     */
    val diamonds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS }

    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> =
        context.dataStore.data
            .map { prefs ->
                val key = when (difficulty) {
                    Difficulty.EASY   -> KEY_LAST_UNLOCKED_EASY
                    Difficulty.MEDIUM -> KEY_LAST_UNLOCKED_MEDIUM
                    Difficulty.HARD   -> KEY_LAST_UNLOCKED_HARD
                }
                prefs[key] ?: 1
            }

    // -----------------------------------------------------------------------------
    // Suspend Functions to Modify Preferences
    // -----------------------------------------------------------------------------

    /**
     * Toggles the mute state.
     *
     * Reads current value and writes the opposite.
     */
    suspend fun toggleMute() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_IS_MUTED] ?: false
            prefs[KEY_IS_MUTED] = !current
        }
    }

    /**
     * Sets the theme preference explicitly.
     *
     * @param isDark true to enable dark theme, false for light theme
     */
    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DARK_THEME] = isDark
        }
    }

    /**
     * Retrieves the current diamond count once.
     *
     * @return current diamonds, or [DEFAULT_DIAMONDS] if unset
     */
    suspend fun getDiamondCount(): Int =
        context.dataStore.data
            .map { prefs -> prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS }
            .first()

    /**
     * Adds the given [amount] of diamonds.
     *
     * @param amount Number to add (can be zero or negative).
     */
    suspend fun addDiamonds(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            prefs[KEY_DIAMONDS] = current + amount
        }
    }

    /**
     * Spends the given [amount] of diamonds, never dropping below zero.
     *
     * @param amount Number to deduct
     */
    suspend fun spendDiamonds(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            prefs[KEY_DIAMONDS] = (current - amount).coerceAtLeast(0)
        }
    }

    // “Once” getter for repository logic
    suspend fun getLastUnlockedStageOnce(difficulty: Difficulty): Int =
        context.dataStore.data
            .map { prefs ->
                val key = when (difficulty) {
                    Difficulty.EASY   -> KEY_LAST_UNLOCKED_EASY
                    Difficulty.MEDIUM -> KEY_LAST_UNLOCKED_MEDIUM
                    Difficulty.HARD   -> KEY_LAST_UNLOCKED_HARD
                }
                prefs[key] ?: 1
            }
            .first()

    // Setter
    suspend fun setLastUnlockedStage(difficulty: Difficulty, stage: Int) {
        context.dataStore.edit { prefs ->
            val key = when (difficulty) {
                Difficulty.EASY   -> KEY_LAST_UNLOCKED_EASY
                Difficulty.MEDIUM -> KEY_LAST_UNLOCKED_MEDIUM
                Difficulty.HARD   -> KEY_LAST_UNLOCKED_HARD
            }
            prefs[key] = stage
        }
    }
}
