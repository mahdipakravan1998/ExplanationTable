package com.example.explanationtable.data

import android.content.Context
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.HintOption

/**
 * Creates a [HintOption] instance using a localized string resource and fees for different difficulty levels.
 *
 * @param context Android context to access resources.
 * @param displayTextResId Resource ID for the hint text.
 * @param easy Fee for [Difficulty.EASY].
 * @param medium Fee for [Difficulty.MEDIUM].
 * @param hard Fee for [Difficulty.HARD].
 * @return A configured [HintOption] object containing the display text and fee mappings.
 */
private fun createHintOption(
    context: Context,
    displayTextResId: Int,
    easy: Int,
    medium: Int,
    hard: Int
): HintOption = HintOption(
    displayText = context.getString(displayTextResId), // Load localized text from resources.
    feeMap = mapOf(
        Difficulty.EASY to easy,
        Difficulty.MEDIUM to medium,
        Difficulty.HARD to hard
    )
)

/**
 * Provides a list of preset [HintOption] objects with localized texts and corresponding fee values.
 *
 * @param context Android context to retrieve string resources.
 * @return A list of [HintOption] objects.
 */
fun getHintOptions(context: Context): List<HintOption> = listOf(
    createHintOption(context, R.string.hint_single_letter, easy = 15, medium = 30, hard = 40),
    createHintOption(context, R.string.hint_single_word, easy = 40, medium = 80, hard = 120),
    createHintOption(context, R.string.hint_complete_stage, easy = 135, medium = 270, hard = 400)
)
