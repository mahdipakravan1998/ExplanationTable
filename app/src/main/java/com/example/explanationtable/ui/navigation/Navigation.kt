package com.example.explanationtable.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.explanationtable.model.toDifficultyFromRoute
import com.example.explanationtable.ui.gameplay.pages.GameplayPage
import com.example.explanationtable.ui.main.pages.MainPage
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.rewards.pages.GameResultScreen
import com.example.explanationtable.ui.rewards.viewmodel.RewardsViewModel
import com.example.explanationtable.ui.stages.pages.StagesListPage

/**
 * Top-level NavHost for the app.
 *
 * - Behavior & visuals are IDENTICAL to prior version (same routes & 300ms slide animations).
 * - ViewModels are scoped to their NavBackStackEntry for better SavedStateHandle & lifecycle.
 *
 * @param navController external or remembered NavHostController
 * @param isDarkTheme whether the app is currently using the dark theme
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean
) {
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        // Main route
        composable(Routes.MAIN) { backStackEntry ->
            // Scope VM to the destination's back stack entry
            val mainViewModel: MainViewModel = viewModel(viewModelStoreOwner = backStackEntry)
            MainPage(
                navController = navController,
                viewModel = mainViewModel,
                isDarkTheme = isDarkTheme
            )
        }

        // Stages list route
        composable(
            route = Routes.STAGES_LIST_WITH_ARG,
            arguments = listOf(
                navArgument(Routes.ARG_DIFFICULTY) { type = NavType.StringType }
            ),
            enterTransition = { NavTransitions.defaultEnterTransition() },
            exitTransition = { NavTransitions.defaultExitTransition() },
            popEnterTransition = { NavTransitions.defaultPopEnterTransition() },
            popExitTransition = { NavTransitions.defaultPopExitTransition() }
        ) { backStackEntry ->
            val difficulty = backStackEntry
                .arguments
                ?.getString(Routes.ARG_DIFFICULTY)
                .toDifficultyFromRoute()

            StagesListPage(
                navController = navController,
                difficulty = difficulty,
                isDarkTheme = isDarkTheme
            )
        }

        // Gameplay route
        composable(
            route = Routes.GAMEPLAY_WITH_ARGS,
            arguments = listOf(
                navArgument(Routes.ARG_STAGE_NUMBER) { type = NavType.IntType },
                navArgument(Routes.ARG_DIFFICULTY) { type = NavType.StringType }
            ),
            enterTransition = { NavTransitions.defaultEnterTransition() },
            exitTransition = { NavTransitions.defaultExitTransition() },
            popEnterTransition = { NavTransitions.defaultPopEnterTransition() },
            popExitTransition = { NavTransitions.defaultPopExitTransition() }
        ) { backStackEntry ->
            val stageNumber = backStackEntry.arguments?.getInt(Routes.ARG_STAGE_NUMBER) ?: 1
            val difficulty = backStackEntry
                .arguments
                ?.getString(Routes.ARG_DIFFICULTY)
                .toDifficultyFromRoute()

            GameplayPage(
                navController = navController,
                isDarkTheme = isDarkTheme,
                stageNumber = stageNumber,
                difficulty = difficulty
            )
        }

        // Game rewards/results route
        composable(
            route = Routes.GAME_REWARDS_WITH_ARGS,
            arguments = listOf(
                navArgument(Routes.ARG_PLAYER_MOVES) { type = NavType.IntType },
                navArgument(Routes.ARG_ELAPSED_TIME) { type = NavType.LongType },
                navArgument(Routes.ARG_DIFFICULTY) { type = NavType.StringType },
                navArgument(Routes.ARG_STAGE_NUMBER) { type = NavType.IntType },
                navArgument(Routes.ARG_OPTIMAL_MOVES) { type = NavType.IntType },
                navArgument(Routes.ARG_USER_ACCURACY) { type = NavType.IntType }
            ),
            enterTransition = { NavTransitions.defaultEnterTransition() },
            exitTransition = { NavTransitions.defaultExitTransition() },
            popEnterTransition = { NavTransitions.defaultPopEnterTransition() },
            popExitTransition = { NavTransitions.defaultPopExitTransition() }
        ) { backStackEntry ->
            val playerMoves = backStackEntry.arguments?.getInt(Routes.ARG_PLAYER_MOVES) ?: 0
            val elapsedTime = backStackEntry.arguments?.getLong(Routes.ARG_ELAPSED_TIME) ?: 0L
            val difficulty = backStackEntry
                .arguments
                ?.getString(Routes.ARG_DIFFICULTY)
                .toDifficultyFromRoute()
            val stageNumber = backStackEntry.arguments?.getInt(Routes.ARG_STAGE_NUMBER) ?: 1
            val optimalMoves = backStackEntry.arguments?.getInt(Routes.ARG_OPTIMAL_MOVES) ?: 0
            val userAccuracy = backStackEntry.arguments?.getInt(Routes.ARG_USER_ACCURACY) ?: 0

            // Scope VM to this destination (restores better after process death)
            val rewardsViewModel: RewardsViewModel = viewModel(viewModelStoreOwner = backStackEntry)

            GameResultScreen(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,
                userAccuracy = userAccuracy,
                playerMoves = playerMoves,
                elapsedTime = elapsedTime,
                navController = navController,
                difficulty = difficulty,
                stageNumber = stageNumber,
                viewModel = rewardsViewModel
            )
        }
    }
}
