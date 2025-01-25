package com.example.explanationtable.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.explanationtable.ui.main.pages.MainPage
import com.example.explanationtable.ui.stages.pages.StagesListPage
import com.example.explanationtable.ui.gameplay.pages.GameplayPage
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.model.Difficulty

@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean
) {
    val viewModel: MainViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        composable(Routes.MAIN) {
            MainPage(navController, viewModel, isDarkTheme)
        }

        // Parameterized composable for the StagesListPage
        composable(
            route = Routes.STAGES_LIST_WITH_ARG,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { backStackEntry ->
            val difficultyArg = backStackEntry.arguments?.getString("difficulty") ?: "Easy"
            val difficultyEnum = when (difficultyArg.lowercase()) {
                "easy"   -> Difficulty.EASY
                "medium" -> Difficulty.MEDIUM
                "hard"   -> Difficulty.HARD
                else     -> Difficulty.EASY
            }
            StagesListPage(
                navController = navController,
                difficulty = difficultyEnum,
                isDarkTheme = isDarkTheme
            )
        }

        // New route for the GameplayPage with both stageNumber and difficulty
        composable(
            route = Routes.GAMEPLAY_WITH_ARGS,
            arguments = listOf(
                navArgument("stageNumber") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val stageNumber = backStackEntry.arguments?.getInt("stageNumber") ?: 1
            val difficultyArg = backStackEntry.arguments?.getString("difficulty") ?: "Easy"
            val difficultyEnum = when (difficultyArg.lowercase()) {
                "easy"   -> Difficulty.EASY
                "medium" -> Difficulty.MEDIUM
                "hard"   -> Difficulty.HARD
                else     -> Difficulty.EASY
            }
            GameplayPage(
                isDarkTheme = isDarkTheme,
                stageNumber = stageNumber,
                difficulty = difficultyEnum
            )
        }
    }
}
