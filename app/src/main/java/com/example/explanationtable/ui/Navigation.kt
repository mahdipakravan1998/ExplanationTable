package com.example.explanationtable.ui

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.NavType
import androidx.navigation.navArgument
import com.example.explanationtable.ui.main.MainPage
import com.example.explanationtable.ui.settings.SettingsPage
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.stages.StagesListPage

/**
 * Top-level NavHost for the application.
 *
 * We define a parameterized route for the StagesListPage
 * so that 'difficulty' is passed in the route: "stages_list/{difficulty}".
 */
@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    Background {
        NavHost(
            navController = navController,
            startDestination = Routes.MAIN
        ) {
            composable(Routes.MAIN) {
                MainPage(navController)
            }
            composable(Routes.SETTINGS) {
                SettingsPage(navController)
            }

            // Parameterized composable: "stages_list/{difficulty}"
            composable(
                route = Routes.STAGES_LIST_WITH_ARG,
                arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
            ) { backStackEntry ->
                // Parse the 'difficulty' argument and map it to the enum
                val difficultyArg = backStackEntry.arguments?.getString("difficulty") ?: "Easy"
                val difficultyEnum = when (difficultyArg.lowercase()) {
                    "easy"   -> Difficulty.EASY
                    "medium" -> Difficulty.MEDIUM
                    "hard"   -> Difficulty.HARD
                    else     -> Difficulty.EASY  // Fallback
                }
                StagesListPage(difficulty = difficultyEnum)
            }
        }
    }
}
