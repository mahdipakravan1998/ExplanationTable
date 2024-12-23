package com.example.explanationtable.ui.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.components.AppTopBar
import com.example.explanationtable.ui.main.SharedViewModel

@Composable
fun StagesListPage(
    difficulty: Difficulty = Difficulty.EASY,
    diamonds: Int = 100,
    onSettingsClick: () -> Unit = {},
    isDarkTheme: Boolean // New parameter
) {
    // Use the same background composable, but isHomePage = false
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Column(modifier = Modifier.fillMaxSize()) {
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme, // Pass the theme state here
                title = stringResource(id = R.string.stages_list),
                diamonds = diamonds,
                difficulty = difficulty,
                onSettingsClick = onSettingsClick,
                iconTint = MaterialTheme.colorScheme.onSurface
            )

            Text(
                text = "List of Game Stages for $difficulty difficulty",
                modifier = Modifier.padding(16.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
        }
    }
}

