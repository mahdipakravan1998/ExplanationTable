package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import com.example.explanationtable.R

/**
 * The main content composable that displays primary action buttons.
 *
 * @param onListClicked Callback invoked when the "List of Steps" button is clicked.
 * @param onStartGameClicked Callback invoked when the "Start Game" button is clicked.
 * @param modifier Modifier to be applied to the Column layout.
 */
@Composable
fun MainContent(
    onListClicked: () -> Unit,
    onStartGameClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth(0.8f) // Adjust the width to maintain button ratio
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
                onClick = onStartGameClicked // Navigate to StartGame screen
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}
