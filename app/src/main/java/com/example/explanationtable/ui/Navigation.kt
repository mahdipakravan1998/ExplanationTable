package com.example.explanationtable.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.ui.main.MainPage
import com.example.explanationtable.ui.settings.SettingsPage
import com.example.explanationtable.ui.stages.StagesListPage

object Routes {
    const val MAIN = "main"
    const val SETTINGS = "settings"
    const val STAGES_LIST = "stages_list"
}

@Composable
fun AppNavHost(navController: NavHostController = rememberNavController()) {
    Background {
        NavHost(navController = navController, startDestination = Routes.MAIN) {
            composable(Routes.MAIN) { MainPage(navController) }
            composable(Routes.SETTINGS) { SettingsPage(navController) }
            composable(Routes.STAGES_LIST) { StagesListPage(navController) }
        }
    }
}
