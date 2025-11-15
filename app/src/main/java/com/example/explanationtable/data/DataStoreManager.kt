package com.example.explanationtable.data

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.explanationtable.model.Difficulty
import java.io.IOException
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

// -----------------------------------------------------------------------------
// Extension property to lazily initialize a single DataStore instance per Context
// -----------------------------------------------------------------------------
private val Context.dataStore by preferencesDataStore(name = "settings")

/**
 * Central access point for user/game preferences backed by Preferences DataStore.
 *
 * ## Error handling
 * All read flows are derived from [dataFlow], which catches [IOException] and
 * emits [emptyPreferences] to provide safe defaults instead of crashing.
 *
 * ## Performance & recomposition
 * - Outward-facing flows apply [distinctUntilChanged] to avoid redundant emissions.
 * - Helpers keep mapping/allocation minimal and consistent.
 *
 * ## Threading
 * - DataStore manages I/O safely, but this class additionally wraps all **suspend**
 *   one-shot reads and writes in [withContext] using [ioDispatcher] (default: [Dispatchers.IO])
 *   to guarantee non-blocking behavior for callers (e.g., ViewModels on the main thread).
 *
 * The public API and behavior remain identical to the original implementation.
 */
class DataStoreManager(
    private val context: Context,
    private val ioDispatcher: CoroutineDispatcher = Dispatchers.IO
) {

    companion object {
        // Preference Keys
        private val KEY_IS_MUTED = booleanPreferencesKey("is_muted")
        private val KEY_IS_DARK_THEME = booleanPreferencesKey("is_dark_theme")
        private val KEY_DIAMONDS = intPreferencesKey("diamonds")

        private val KEY_LAST_UNLOCKED_EASY = intPreferencesKey("last_unlocked_easy")
        private val KEY_LAST_UNLOCKED_MEDIUM = intPreferencesKey("last_unlocked_medium")
        private val KEY_LAST_UNLOCKED_HARD = intPreferencesKey("last_unlocked_hard")

        // Last played per difficulty (0 means "none yet")
        private val KEY_LAST_PLAYED_EASY = intPreferencesKey("last_played_easy")
        private val KEY_LAST_PLAYED_MEDIUM = intPreferencesKey("last_played_medium")
        private val KEY_LAST_PLAYED_HARD = intPreferencesKey("last_played_hard")

        // Claimed chest sets (stage numbers as strings)
        private val KEY_CLAIMED_CHESTS_EASY = stringSetPreferencesKey("claimed_chests_easy")
        private val KEY_CLAIMED_CHESTS_MEDIUM = stringSetPreferencesKey("claimed_chests_medium")
        private val KEY_CLAIMED_CHESTS_HARD = stringSetPreferencesKey("claimed_chests_hard")

        // Default Values
        private const val DEFAULT_DIAMONDS = 200
        private const val DEFAULT_LAST_UNLOCKED = 1
        private const val DEFAULT_LAST_PLAYED = 0
    }

    // -------------------------------------------------------------------------
    // Internal shared data flow with IO-safety.
    // DataStore performs its own threading; we still switch context for
    // suspend one-shot operations to keep call sites main-safe.
    // -------------------------------------------------------------------------
    private val dataFlow: Flow<Preferences> =
        context.dataStore.data
            .catch { e ->
                // Surface defaults on IO errors rather than crashing the app.
                if (e is IOException) emit(emptyPreferences()) else throw e
            }

    // -------------------------------------------------------------------------
    // Utilities
    // -------------------------------------------------------------------------
    private fun claimedKeyFor(difficulty: Difficulty): Preferences.Key<Set<String>> =
        when (difficulty) {
            Difficulty.EASY -> KEY_CLAIMED_CHESTS_EASY
            Difficulty.MEDIUM -> KEY_CLAIMED_CHESTS_MEDIUM
            Difficulty.HARD -> KEY_CLAIMED_CHESTS_HARD
        }

    private fun lastUnlockedKeyFor(difficulty: Difficulty): Preferences.Key<Int> =
        when (difficulty) {
            Difficulty.EASY -> KEY_LAST_UNLOCKED_EASY
            Difficulty.MEDIUM -> KEY_LAST_UNLOCKED_MEDIUM
            Difficulty.HARD -> KEY_LAST_UNLOCKED_HARD
        }

    private fun lastPlayedKeyFor(difficulty: Difficulty): Preferences.Key<Int> =
        when (difficulty) {
            Difficulty.EASY -> KEY_LAST_PLAYED_EASY
            Difficulty.MEDIUM -> KEY_LAST_PLAYED_MEDIUM
            Difficulty.HARD -> KEY_LAST_PLAYED_HARD
        }

    // Small helpers to consistently apply defaults & distinctness
    private inline fun <reified T> Flow<Preferences>.read(
        key: Preferences.Key<T>,
        default: T
    ): Flow<T> = map { prefs -> prefs[key] ?: default }.distinctUntilChanged()

    private fun Flow<Preferences>.readNullableBool(
        key: Preferences.Key<Boolean>
    ): Flow<Boolean?> = map { prefs -> prefs[key] }.distinctUntilChanged()

    // -------------------------------------------------------------------------
    // Public Flows
    // -------------------------------------------------------------------------

    /** Emits the current mute state. Defaults to false if not set. */
    val isMuted: Flow<Boolean> = dataFlow.read(KEY_IS_MUTED, default = false)

    /**
     * Emits the current theme preference:
     *  • true  = dark theme enabled
     *  • false = light theme enabled
     *  • null  = user hasn’t set a preference
     */
    val isDarkTheme: Flow<Boolean?> = dataFlow.readNullableBool(KEY_IS_DARK_THEME)

    /** Emits the current diamond count. Defaults to [DEFAULT_DIAMONDS] if not set. */
    val diamonds: Flow<Int> = dataFlow.read(KEY_DIAMONDS, default = DEFAULT_DIAMONDS)

    /** Emits the highest stage unlocked for [difficulty]. Defaults to 1. */
    fun getLastUnlockedStage(difficulty: Difficulty): Flow<Int> =
        dataFlow.map { prefs -> prefs[lastUnlockedKeyFor(difficulty)] ?: DEFAULT_LAST_UNLOCKED }
            .distinctUntilChanged()

    /** Emits set of claimed gold chest stage numbers for a difficulty. */
    fun claimedChests(difficulty: Difficulty): Flow<Set<Int>> =
        dataFlow.map { prefs ->
            val raw = prefs[claimedKeyFor(difficulty)] ?: emptySet()
            // Convert lazily and drop malformed entries defensively.
            raw.mapNotNull { it.toIntOrNull() }.toSet()
        }.distinctUntilChanged()

    // -------------------------------------------------------------------------
    // Suspend Functions to Modify / Read Preferences (once)
    // -------------------------------------------------------------------------

    /** Toggles the mute state. Main-safe. */
    suspend fun toggleMute() = withContext(ioDispatcher) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_IS_MUTED] ?: false
            prefs[KEY_IS_MUTED] = !current
        }
    }

    /** Sets the theme preference explicitly. Main-safe. */
    suspend fun setTheme(isDark: Boolean) = withContext(ioDispatcher) {
        context.dataStore.edit { prefs ->
            prefs[KEY_IS_DARK_THEME] = isDark
        }
    }

    /** Retrieves the current diamond count once. Main-safe. */
    suspend fun getDiamondCount(): Int = withContext(ioDispatcher) {
        dataFlow.map { prefs -> prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS }.first()
    }

    /**
     * Adds the given [amount] of diamonds.
     * Note: preserves previous behavior (allows negative amounts if callers use it),
     * but clamps on overflow to [Int.MAX_VALUE]. Main-safe.
     */
    suspend fun addDiamonds(amount: Int) = withContext(ioDispatcher) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            val next = (current.toLong() + amount.toLong())
                .coerceAtMost(Int.MAX_VALUE.toLong())
                .toInt()
            prefs[KEY_DIAMONDS] = next
        }
    }

    /**
     * Atomically spends diamonds if balance is sufficient.
     * Returns true if spend succeeded, false otherwise (no write performed). Main-safe.
     */
    suspend fun trySpendDiamonds(amount: Int): Boolean = withContext(ioDispatcher) {
        var success = false
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            if (current >= amount) {
                prefs[KEY_DIAMONDS] = current - amount
                success = true
            }
        }
        success
    }

    /** Legacy non-atomic spend (kept only for backward compatibility). Prefer [trySpendDiamonds]. Main-safe. */
    @Deprecated("Use trySpendDiamonds(amount) for atomic spend with success/failure.")
    suspend fun spendDiamonds(amount: Int) = withContext(ioDispatcher) {
        context.dataStore.edit { prefs ->
            val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
            prefs[KEY_DIAMONDS] = (current - amount).coerceAtLeast(0)
        }
    }

    // “Once” getter — unlocked. Main-safe.
    suspend fun getLastUnlockedStageOnce(difficulty: Difficulty): Int = withContext(ioDispatcher) {
        dataFlow.map { prefs -> prefs[lastUnlockedKeyFor(difficulty)] ?: DEFAULT_LAST_UNLOCKED }
            .first()
    }

    // Setter — unlocked (clamped to >= 1). Main-safe.
    suspend fun setLastUnlockedStage(difficulty: Difficulty, stage: Int) = withContext(ioDispatcher) {
        val safe = stage.coerceAtLeast(1)
        context.dataStore.edit { prefs ->
            prefs[lastUnlockedKeyFor(difficulty)] = safe
        }
    }

    // “Once” getter — last played (0 = none). Main-safe.
    suspend fun getLastPlayedStageOnce(difficulty: Difficulty): Int = withContext(ioDispatcher) {
        dataFlow.map { prefs -> prefs[lastPlayedKeyFor(difficulty)] ?: DEFAULT_LAST_PLAYED }
            .first()
    }

    /**
     * Setter — last played (monotonic non-decreasing to avoid regressions).
     * Negative inputs are clamped to 0; 0 means "none". Main-safe.
     */
    suspend fun setLastPlayedStage(difficulty: Difficulty, stage: Int) = withContext(ioDispatcher) {
        val safe = stage.coerceAtLeast(0)
        context.dataStore.edit { prefs ->
            val key = lastPlayedKeyFor(difficulty)
            val current = prefs[key] ?: DEFAULT_LAST_PLAYED
            // Ensure we never move backwards.
            prefs[key] = maxOf(current, safe)
        }
    }

    /**
     * Atomically awards a chest if eligible (not previously claimed and stage is unlocked).
     * Updates both the claimed set and diamond count in a single transaction.
     *
     * @return true if diamonds were awarded (first claim), false otherwise. Main-safe.
     */
    suspend fun awardChestIfEligible(
        difficulty: Difficulty,
        stageNumber: Int,
        diamondsAward: Int
    ): Boolean = withContext(ioDispatcher) {
        var awarded = false
        // Clamp negative / zero inputs defensively; there is no stage 0.
        val safeStage = stageNumber.coerceAtLeast(1)

        context.dataStore.edit { prefs ->
            val claimedKey = claimedKeyFor(difficulty)
            val lastKey = lastUnlockedKeyFor(difficulty)

            val claimed = prefs[claimedKey] ?: emptySet()
            val lastUnlocked = prefs[lastKey] ?: DEFAULT_LAST_UNLOCKED

            val stageStr = safeStage.toString()
            val alreadyClaimed = stageStr in claimed
            val isUnlocked = safeStage <= lastUnlocked

            if (!alreadyClaimed && isUnlocked) {
                // mark claimed
                prefs[claimedKey] = claimed + stageStr
                // add diamonds with overflow protection
                val current = prefs[KEY_DIAMONDS] ?: DEFAULT_DIAMONDS
                val next = (current.toLong() + diamondsAward.toLong())
                    .coerceAtMost(Int.MAX_VALUE.toLong())
                    .toInt()
                prefs[KEY_DIAMONDS] = next
                awarded = true
            }
        }
        awarded
    }
}
