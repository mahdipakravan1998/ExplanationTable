package com.example.explanationtable.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Centralized navigation transitions.
 * Visuals are IDENTICAL to the previous inline definitions (300ms tween).
 */
object NavTransitions {
    const val PAGE_ANIM_DURATION = 300

    fun defaultEnterTransition(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = PAGE_ANIM_DURATION)
        )

    fun defaultExitTransition(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(durationMillis = PAGE_ANIM_DURATION)
        )

    fun defaultPopEnterTransition(): EnterTransition =
        slideInHorizontally(
            initialOffsetX = { fullWidth -> -fullWidth },
            animationSpec = tween(durationMillis = PAGE_ANIM_DURATION)
        )

    fun defaultPopExitTransition(): ExitTransition =
        slideOutHorizontally(
            targetOffsetX = { fullWidth -> fullWidth },
            animationSpec = tween(durationMillis = PAGE_ANIM_DURATION)
        )
}
