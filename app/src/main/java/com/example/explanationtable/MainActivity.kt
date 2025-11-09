package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.navigation.AppNavHost
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.system.AppEdgeToEdgeSystemBars
import com.example.explanationtable.ui.theme.ExplanationTableTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            val mainViewModel: MainViewModel = viewModel()
            val isDarkTheme by mainViewModel.isDarkTheme.collectAsState(initial = false)

            ExplanationTableTheme(darkTheme = isDarkTheme) {
                // Transparent, visible system bars; content handles insets.
                AppEdgeToEdgeSystemBars(isDarkTheme = isDarkTheme)

                // Keep current visuals LTR.
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Ltr) {
                    AppNavHost(isDarkTheme = isDarkTheme)
                }
            }
        }
    }
}