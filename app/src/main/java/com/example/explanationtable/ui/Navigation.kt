package com.example.explanationtable.ui

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
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
import com.example.explanationtable.ui.main.MainViewModel

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
            MainPage(navController, viewModel)
        }
        composable(Routes.SETTINGS) {
            SettingsPage(navController, viewModel)
        }

        // Parameterized composable: "stages_list/{difficulty}"
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
                difficulty = difficultyEnum,
                isDarkTheme = isDarkTheme,
                onSettingsClick = { navController.navigate(Routes.SETTINGS) } // Navigate to Settings
            )
        }
    }
}
