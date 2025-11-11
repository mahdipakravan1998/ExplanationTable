package com.example.explanationtable.ui.navigation

import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.asRouteArg

/**
 * Central navigation routes and typed builders.
 *
 * - **Constants** (public) keep full compatibility with existing callers.
 * - **Arg keys** prevent typos across the graph.
 * - **Builder functions** help construct routes with type safety & consistent casing.
 */
object Routes {

    // -------------------------------------------------------------------------
    // Main Page Route
    // -------------------------------------------------------------------------
    /** Route for the main (home) page. */
    const val MAIN: String = "main"

    // -------------------------------------------------------------------------
    // Stages List Page Routes
    // -------------------------------------------------------------------------
    /** Static route for the stages list page. */
    const val STAGES_LIST: String = "stages_list"

    /** Parameterized route for the stages list page; accepts a 'difficulty' argument. */
    const val STAGES_LIST_WITH_ARG: String = "stages_list/{difficulty}"

    // -------------------------------------------------------------------------
    // Gameplay Page Routes
    // -------------------------------------------------------------------------
    /** Parameterized route for the gameplay page; accepts 'stageNumber' and 'difficulty'. */
    const val GAMEPLAY_WITH_ARGS: String = "gameplay/{stageNumber}/{difficulty}"

    // -------------------------------------------------------------------------
    // Game Rewards / Results Page Routes
    // -------------------------------------------------------------------------
    /** Parameterized route for the results page; argument order is fixed for compatibility. */
    const val GAME_REWARDS_WITH_ARGS: String =
        "game_rewards/{optimalMoves}/{userAccuracy}/{playerMoves}/{elapsedTime}/{difficulty}/{stageNumber}"

    // -------------------------------------------------------------------------
    // Argument Keys (single source of truth)
    // -------------------------------------------------------------------------
    /** Arg key: difficulty (String: easy|medium|hard). */
    const val ARG_DIFFICULTY: String = "difficulty"

    /** Arg key: stageNumber (Int). */
    const val ARG_STAGE_NUMBER: String = "stageNumber"

    /** Arg key: playerMoves (Int). */
    const val ARG_PLAYER_MOVES: String = "playerMoves"

    /** Arg key: elapsedTime (Long). */
    const val ARG_ELAPSED_TIME: String = "elapsedTime"

    /** Arg key: optimalMoves (Int). */
    const val ARG_OPTIMAL_MOVES: String = "optimalMoves"

    /** Arg key: userAccuracy (Int). */
    const val ARG_USER_ACCURACY: String = "userAccuracy"

    // -------------------------------------------------------------------------
    // Typed Route Builders (optional; additive)
    // -------------------------------------------------------------------------

    /**
     * Build a stages list route with typed [Difficulty].
     * Example: "stages_list/easy"
     */
    fun stagesList(difficulty: Difficulty): String =
        "stages_list/${difficulty.asRouteArg()}"

    /**
     * Build a gameplay route with typed args.
     * Example: "gameplay/3/medium"
     */
    fun gameplay(stageNumber: Int, difficulty: Difficulty): String =
        "gameplay/$stageNumber/${difficulty.asRouteArg()}"

    /**
     * Build the game rewards route with typed args.
     * Example: "game_rewards/12/94/15/123456/easy/3"
     */
    fun gameRewards(
        optimalMoves: Int,
        userAccuracy: Int,
        playerMoves: Int,
        elapsedTime: Long,
        difficulty: Difficulty,
        stageNumber: Int
    ): String =
        "game_rewards/$optimalMoves/$userAccuracy/$playerMoves/$elapsedTime/${difficulty.asRouteArg()}/$stageNumber"
}
