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

/**
 * MainActivity is the entry point of the ExplanationTable application.
 * It sets up the Compose UI and observes the user's theme preference via MainViewModel.
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the UI content using Jetpack Compose
        setContent {
            // Retrieve the MainViewModel instance to observe the user's theme preference.
            val mainViewModel: MainViewModel = viewModel()

            // Collect the current theme state from MainViewModel as a Compose state.
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            // Apply the custom ExplanationTableTheme based on the theme state.
            ExplanationTableTheme(darkTheme = isDarkTheme) {
                // Set up the navigation host and pass the theme state to it.
                AppNavHost(isDarkTheme = isDarkTheme)
            }
        }
    }
}
