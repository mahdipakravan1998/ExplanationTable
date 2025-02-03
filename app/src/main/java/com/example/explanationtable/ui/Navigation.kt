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

@OptIn(ExperimentalAnimationApi::class)
@Composable
fun AppNavHost(
    navController: androidx.navigation.NavHostController = rememberAnimatedNavController(),
    isDarkTheme: Boolean
) {
    val viewModel: MainViewModel = viewModel()

    // AnimatedNavHost adds the slide animations during navigation transitions.
    AnimatedNavHost(
        navController = navController,
        startDestination = Routes.MAIN,
        // Slide in from the right when navigating forward.
        enterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        // Slide out to the left when navigating forward.
        exitTransition = {
            slideOutHorizontally(
                targetOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        // Slide in from the left when navigating back.
        popEnterTransition = {
            slideInHorizontally(
                initialOffsetX = { fullWidth -> -fullWidth },
                animationSpec = tween(durationMillis = 300)
            )
        },
        // Slide out to the right when navigating back.
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
                viewModel = viewModel,
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
                isDarkTheme = isDarkTheme,
                stageNumber = stageNumber,
                difficulty = difficulty
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
