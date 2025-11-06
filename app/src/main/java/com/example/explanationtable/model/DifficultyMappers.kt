package com.example.explanationtable.model

import java.util.Locale

/**
 * Helpers for converting between [Difficulty] and user/route strings.
 *
 * Keep these in the model layer so both UI and nav can depend on a single source of truth.
 */

/**
 * Stable token used in navigation routes (lowercase English).
 * Examples: EASY -> "easy", MEDIUM -> "medium", HARD -> "hard"
 */
fun Difficulty.asRouteArg(): String = when (this) {
    Difficulty.EASY -> "easy"
    Difficulty.MEDIUM -> "medium"
    Difficulty.HARD -> "hard"
}

/**
 * Parses a route argument string into a [Difficulty].
 * Falls back to [Difficulty.EASY] for null/unknown values to preserve prior behavior.
 */
fun String?.toDifficultyFromRoute(): Difficulty = when (this?.lowercase(Locale.ROOT)) {
    "easy" -> Difficulty.EASY
    "medium" -> Difficulty.MEDIUM
    "hard" -> Difficulty.HARD
    else -> Difficulty.EASY
}

/**
 * Maps a user-visible label (e.g., dialog option) into a [Difficulty].
 *
 * - Accepts enum names in any case ("easy", "EASY", ...).
 * - Accepts common English labels ("easy", "medium", "hard").
 * - Safe fallback to [Difficulty.EASY].
 *
 * Extend the "when" branch if your UI shows localized labels.
 */
fun String?.toDifficultyFromLabel(): Difficulty {
    val raw = this?.trim().orEmpty()
    // Try enum name first (case-insensitive)
    runCatching { Difficulty.valueOf(raw.uppercase(Locale.ROOT)) }
        .getOrNull()
        ?.let { return it }

    return when (raw.lowercase(Locale.ROOT)) {
        "easy" -> Difficulty.EASY
        "medium" -> Difficulty.MEDIUM
        "hard" -> Difficulty.HARD

        // Example for localization (uncomment/extend as needed):
        // "آسان" -> Difficulty.EASY
        // "متوسط" -> Difficulty.MEDIUM
        // "سخت" -> Difficulty.HARD

        else -> Difficulty.EASY
    }
}
