package com.example.explanationtable.ui

/**
 * Contains navigation route constants for the app.
 *
 * These constants are used by the navigation system to identify
 * screens and support parameterized routes where necessary.
 */
object Routes {

    // -------------------------------------------------------------------------
    // Main Page Route
    // -------------------------------------------------------------------------

    // Route for the main (home) page.
    const val MAIN = "main"

    // -------------------------------------------------------------------------
    // Stages List Page Routes
    // -------------------------------------------------------------------------

    // Static route for the stages list page.
    const val STAGES_LIST = "stages_list"
    // Parameterized route for the stages list page which accepts a 'difficulty' argument.
    const val STAGES_LIST_WITH_ARG = "stages_list/{difficulty}"

    // -------------------------------------------------------------------------
    // Gameplay Page Routes
    // -------------------------------------------------------------------------

    // Static route for the gameplay page.
    const val GAMEPLAY = "gameplay"
    // Parameterized route for the gameplay page that accepts both 'stageNumber' and 'difficulty' arguments.
    const val GAMEPLAY_WITH_ARGS = "gameplay/{stageNumber}/{difficulty}"

    // -------------------------------------------------------------------------
    // Game Rewards / Results Page Routes
    // -------------------------------------------------------------------------
    // New route that takes minMoves, playerMoves, and elapsedTime as parameters.
    const val GAME_REWARDS_WITH_ARGS = "game_rewards/{optimalMoves}/{userAccuracy}/{playerMoves}/{elapsedTime}/{difficulty}/{stageNumber}"}
