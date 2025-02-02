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

/**
 * Sets up the application's navigation host with defined routes and their corresponding UI pages.
 *
 * @param navController the navigation controller managing app navigation; defaults to a remembered controller.
 * @param isDarkTheme flag indicating whether the dark theme is active.
 */
@Composable
fun AppNavHost(
    navController: NavHostController = rememberNavController(),
    isDarkTheme: Boolean
) {
    // Obtain the MainViewModel instance for use within the composable destinations.
    val viewModel: MainViewModel = viewModel()

    // Define the navigation graph with a starting destination.
    NavHost(
        navController = navController,
        startDestination = Routes.MAIN
    ) {
        // Main application route
        composable(Routes.MAIN) {
            MainPage(
                navController = navController,
                viewModel = viewModel,
                isDarkTheme = isDarkTheme
            )
        }

        // Route for the StagesListPage which takes a "difficulty" argument.
        composable(
            route = Routes.STAGES_LIST_WITH_ARG,
            arguments = listOf(navArgument("difficulty") { type = NavType.StringType })
        ) { backStackEntry ->
            // Parse the "difficulty" argument from the navigation back stack.
            val difficultyEnum = parseDifficulty(backStackEntry.arguments?.getString("difficulty"))
            StagesListPage(
                navController = navController,
                difficulty = difficultyEnum,
                isDarkTheme = isDarkTheme
            )
        }

        // Route for the GameplayPage which takes both "stageNumber" and "difficulty" arguments.
        composable(
            route = Routes.GAMEPLAY_WITH_ARGS,
            arguments = listOf(
                navArgument("stageNumber") { type = NavType.IntType },
                navArgument("difficulty") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            // Retrieve the stage number with a default of 1 if not provided.
            val stageNumber = backStackEntry.arguments?.getInt("stageNumber") ?: 1
            // Parse the "difficulty" argument from the navigation back stack.
            val difficultyEnum = parseDifficulty(backStackEntry.arguments?.getString("difficulty"))
            GameplayPage(
                isDarkTheme = isDarkTheme,
                stageNumber = stageNumber,
                difficulty = difficultyEnum
            )
        }
    }
}

/**
 * Converts a difficulty argument string into the corresponding Difficulty enum.
 *
 * @param difficultyArg the difficulty parameter as a nullable String.
 * @return the matching Difficulty enum; defaults to Difficulty.EASY for null or unrecognized values.
 */
private fun parseDifficulty(difficultyArg: String?): Difficulty {
    return when (difficultyArg?.lowercase()) {
        "easy"   -> Difficulty.EASY
        "medium" -> Difficulty.MEDIUM
        "hard"   -> Difficulty.HARD
        else     -> Difficulty.EASY
    }
}
