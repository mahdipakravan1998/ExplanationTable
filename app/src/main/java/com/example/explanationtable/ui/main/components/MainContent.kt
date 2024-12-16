package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import androidx.compose.ui.res.stringResource


/**
 * Main Content Composable with Buttons
 */
@Composable
fun MainContent(onListClicked: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First Button: List of Steps
            MainButton(
                iconId = R.drawable.ic_stages_list,
                label = stringResource(id = R.string.stages_list),
                onClick = onListClicked // Open Dialog
            )

            // Second Button: Start Game
            MainButton(
                iconId = R.drawable.ic_start_game,
                label = stringResource(id = R.string.start_game),
                onClick = {
                    // Temporarily do nothing until we implement the StartGame screen
                }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
