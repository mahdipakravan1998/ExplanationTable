package com.example.explanationtable.ui.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue // Required for 'by' delegation
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.ui.popup.SettingsPopup
import com.example.explanationtable.ui.main.MainViewModel
import androidx.compose.ui.Modifier

@Composable
fun SettingsPage(navController: NavController, viewModel: MainViewModel = viewModel()) {
    val isDarkTheme by viewModel.isDarkTheme.collectAsState()
    val isMuted by viewModel.isMuted.collectAsState()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        SettingsPopup(
            onDismiss = { navController.popBackStack() },
            currentTheme = isDarkTheme,
            onToggleTheme = { viewModel.toggleTheme() },
            isMuted = isMuted,
            onToggleMute = { viewModel.toggleMute() }
        )
    }
}
