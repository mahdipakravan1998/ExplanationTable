package com.example.explanationtable.ui

import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.accompanist.navigation.animation.AnimatedNavHost
import com.google.accompanist.navigation.animation.composable
import com.google.accompanist.navigation.animation.rememberAnimatedNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.explanationtable.ui.main.pages.MainPage
import com.example.explanationtable.ui.stages.pages.StagesListPage
import com.example.explanationtable.ui.gameplay.pages.GameplayPage
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.rewards.pages.GameResultScreen
import androidx.navigation.NavHostController
import com.example.explanationtable.ui.rewards.viewmodel.RewardsViewModel

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: NavHostController = rememberAnimatedNavController(),
    isDarkTheme: Boolean
) {
    val mainViewModel: MainViewModel = viewModel()
    val rewardsViewModel: RewardsViewModel = viewModel()

    // AnimatedNavHost adds the slide animations during navigation transitions.
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        popExitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        }
    ) {
        // Main route: renders the MainPage composable.
        composable(Routes.MAIN) {
            MainPage(
                navController = navController,
                viewModel = mainViewModel,
                isDarkTheme = isDarkTheme
            )
        }

        // StagesListPage route: expects a "difficulty" argument.
        composable(
            route = Routes.STAGES_LIST_WITH_ARG,
            arguments = listOf(
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val difficulty = parseDifficulty(backStackEntry.arguments?.getString("difficulty"))
            StagesListPage(
                navController = navController,
                difficulty = difficulty,
                isDarkTheme = isDarkTheme
            )
        }

        // GameplayPage route: expects both "stageNumber" and "difficulty" arguments.
        composable(
            route = Routes.GAMEPLAY_WITH_ARGS,
            arguments = listOf(
                navArgument("stageNumber") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stageNumber = backStackEntry.arguments?.getInt("stageNumber") ?: 1
            val difficulty = parseDifficulty(backStackEntry.arguments?.getString("difficulty"))
            GameplayPage(
                navController = navController, // Pass navController here.
                isDarkTheme = isDarkTheme,
                stageNumber = stageNumber,
                difficulty = difficulty
            )
        }

        // Game rewards/results page route: expects minMoves, playerMoves, and elapsedTime as arguments.
        composable(
            route = Routes.GAME_REWARDS_WITH_ARGS,
            arguments = listOf(
                navArgument("playerMoves") { type = NavType.IntType },
                navArgument("elapsedTime") { type = NavType.LongType },
                navArgument("difficulty") { type = NavType.StringType },
                navArgument("stageNumber") { type = NavType.IntType },
                navArgument("optimalMoves") { type = NavType.IntType }, // Added optimalMoves argument
                navArgument("userAccuracy") { type = NavType.IntType }  // Added userAccuracy argument
            )
        ) { backStackEntry ->
            val playerMoves = backStackEntry.arguments?.getInt("playerMoves") ?: 0
            val elapsedTime = backStackEntry.arguments?.getLong("elapsedTime") ?: 0L
            val difficulty = parseDifficulty(backStackEntry.arguments?.getString("difficulty"))
            val stageNumber = backStackEntry.arguments?.getInt("stageNumber") ?: 1
            val optimalMoves = backStackEntry.arguments?.getInt("optimalMoves") ?: 0  // Retrieve optimalMoves
            val userAccuracy = backStackEntry.arguments?.getInt("userAccuracy") ?: 0  // Retrieve userAccuracy

            // Pass optimalMoves and userAccuracy to GameResultScreen
            GameResultScreen(
                isDarkTheme = isDarkTheme,
                optimalMoves = optimalMoves,  // Pass optimalMoves
                userAccuracy = userAccuracy,  // Pass userAccuracy
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

private fun parseDifficulty(difficultyArg: String?): Difficulty {
    return when (difficultyArg?.lowercase()) {
        "easy" -> Difficulty.EASY
        "medium" -> Difficulty.MEDIUM
        "hard" -> Difficulty.HARD
        else -> Difficulty.EASY
    }
}
