package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Required for 'by' delegation
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.AppNavHost
import com.example.explanationtable.ui.main.MainViewModel
import com.example.explanationtable.ui.theme.ExplanationTableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            // Get the MainViewModel to observe the user's theme preference
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()

            // Apply ExplanationTableTheme with the current theme state
            ExplanationTableTheme(darkTheme = isDarkTheme) {
                // Navigation host with isDarkTheme passed
                AppNavHost(isDarkTheme = isDarkTheme)
            }
        }
    }
}
