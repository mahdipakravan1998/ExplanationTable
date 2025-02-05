package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.AppNavHost
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.theme.ExplanationTableTheme
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.runtime.CompositionLocalProvider

/**
 * MainActivity serves as the entry point of the ExplanationTable application.
 * It initializes the UI using Jetpack Compose and observes the theme preference from MainViewModel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content of the activity using Jetpack Compose.
        setContent {
            // Retrieve the MainViewModel instance to observe the user's theme preference.
            val mainViewModel: MainViewModel = viewModel()

            // Collect the current theme state as a Compose state to reactively apply the theme.
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            // Apply the custom theme based on the current theme state.
            ExplanationTableTheme(darkTheme = isDarkTheme) {
                // Ensure Left-To-Right (LTR) layout direction regardless of device language.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    // Set up the navigation host and pass the theme state to it.
                    AppNavHost(isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}
