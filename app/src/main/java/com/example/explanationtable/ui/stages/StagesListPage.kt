package com.example.explanationtable.ui.stages

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.layout.padding
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.components.AppTopBar

/**
 * A page that displays a top bar (with a difficulty-based theme)
 * and a textual placeholder for listing stages.
 */
@Composable
fun StagesListPage(
    difficulty: Difficulty = Difficulty.EASY,
    diamonds: Int = 100,
    onSettingsClick: () -> Unit = {}
) {
    Column(modifier = Modifier.fillMaxSize()) {
        AppTopBar(
            isHomePage = false,
            title = stringResource(id = R.string.stages_list), // Extracted to strings.xml for localization
            diamonds = diamonds,
            difficulty = difficulty,
            onSettingsClick = onSettingsClick
        )

        // Page content
        Text(
            text = "List of Game Stages for $difficulty difficulty",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFFFFFFFF)
@Composable
fun PreviewStagesListPage() {
    StagesListPage(
        difficulty = Difficulty.EASY,
        diamonds = 200,
        onSettingsClick = {}
    )
}
