package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.*
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
 *  • Last unlocked stage per difficulty
 *  • Claimed gold chests per difficulty (as Set<Int> stage numbers)
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

        // Claimed chest sets (stage numbers as strings)
        private val KEY_CLAIMED_CHESTS_EASY   = stringSetPreferencesKey("claimed_chests_easy")
        private val KEY_CLAIMED_CHESTS_MEDIUM = stringSetPreferencesKey("claimed_chests_medium")
        private val KEY_CLAIMED_CHESTS_HARD   = stringSetPreferencesKey("claimed_chests_hard")

        // Default Values
        private const val DEFAULT_DIAMONDS = 200
    }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------
    private fun claimedKeyFor(difficulty: Difficulty): Preferences.Key<Set<String>> =
        when (difficulty) {
            Difficulty.EASY   -> KEY_CLAIMED_CHESTS_EASY
            Difficulty.MEDIUM -> KEY_CLAIMED_CHESTS_MEDIUM
            Difficulty.HARD   -> KEY_CLAIMED_CHESTS_HARD
        }

    private fun lastUnlockedKeyFor(difficulty: Difficulty): Preferences.Key<Int> =
        when (difficulty) {
            Difficulty.EASY   -> KEY_LAST_UNLOCKED_EASY
            Difficulty.MEDIUM -> KEY_LAST_UNLOCKED_MEDIUM
            Difficulty.HARD   -> KEY_LAST_UNLOCKED_HARD
        }

    // -------------------------------------------------------------------------
    // Public Flows
    // -------------------------------------------------------------------------

    /** Emits the current mute state. Defaults to false if not set. */
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

    /** Emits the current diamond count. Defaults to [DEFAULT_DIAMONDS] if not set. */
    val diamonds: Flow<Int> = context.dataStore.data
        .map { prefs -> prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS }

    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> =
        context.dataStore.data.map { prefs ->
            val key = lastUnlockedKeyFor(difficulty)
            prefs[key] ?: 1
        }

    /** Emits set of claimed gold chest stage numbers for a difficulty. */
    fun claimedChests(difficulty: Difficulty): Flow<Set<Int>> =
        context.dataStore.data.map { prefs ->
            val raw = prefs[claimedKeyFor(difficulty)] ?: emptySet()
            raw.mapNotNull { it.toIntOrNull() }.toSet()
        }

    // -------------------------------------------------------------------------
    // Suspend Functions to Modify Preferences
    // -------------------------------------------------------------------------

    /** Toggles the mute state. */
    suspend fun toggleMute() {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_IS_MUTED] ?: false
            prefs[KEY_IS_MUTED] = !current
        }
    }

    /** Sets the theme preference explicitly. */
    suspend fun setTheme(isDark: Boolean) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DARK_THEME] = isDark
        }
    }

    /** Retrieves the current diamond count once. */
    suspend fun getDiamondCount(): Int =
        context.dataStore.data
            .map { prefs -> prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS }
            .first()

    /** Adds the given [amount] of diamonds. */
    suspend fun addDiamonds(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            prefs[KEY_DIAMONDS] = current + amount
        }
    }

    /** Spends the given [amount] of diamonds, never dropping below zero. */
    suspend fun spendDiamonds(amount: Int) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            prefs[KEY_DIAMONDS] = (current - amount).coerceAtLeast(0)
        }
    }

    // “Once” getter for repository logic
    suspend fun getLastUnlockedStageOnce(difficulty: Difficulty): Int =
        context.dataStore.data
            .map { prefs -> prefs[lastUnlockedKeyFor(difficulty)] ?: 1 }
            .first()

    // Setter
    suspend fun setLastUnlockedStage(difficulty: Difficulty, stage: Int) {
        context.dataStore.edit { prefs ->
            prefs[lastUnlockedKeyFor(difficulty)] = stage
        }
    }

    /**
     * Atomically awards a chest if eligible (not previously claimed and stage is unlocked).
     * Updates both the claimed set and diamond count in a single transaction.
     *
     * @return true if diamonds were awarded (first claim), false otherwise.
     */
    suspend fun awardChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int,
        diamondsAward: Int
    ): Boolean {
        var awarded = false
        context.dataStore.edit { prefs ->
            val claimedKey = claimedKeyFor(difficulty)
            val lastKey = lastUnlockedKeyFor(difficulty)

            val claimed = prefs[claimedKey] ?: emptySet()
            val lastUnlocked = prefs[lastKey] ?: 1

            val alreadyClaimed = claimed.contains(stageNumber.toString())
            val isUnlocked = stageNumber <= lastUnlocked

            if (!alreadyClaimed && isUnlocked) {
                // mark claimed
                prefs[claimedKey] = claimed + stageNumber.toString()
                // add diamonds
                val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
                prefs[KEY_DIAMONDS] = current + diamondsAward
                awarded = true
            }
        }
        return awarded
    }
}
