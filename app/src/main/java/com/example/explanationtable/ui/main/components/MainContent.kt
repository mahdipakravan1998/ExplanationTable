package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R

/**
 * Displays the primary action buttons ("List of Steps" and "Start Game") centered on the screen.
 *
 * @param onListClicked Callback invoked when the "List of Steps" button is clicked.
 * @param onStartGameClicked Callback invoked when the "Start Game" button is clicked.
 * @param modifier Modifier to be applied to the overall layout.
 */
@Composable
fun MainContent(
    onListClicked: () -> Unit,
    onStartGameClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Use a Column to arrange content vertically in the full available space.
    Column(
        modifier = modifier
            .fillMaxSize()        // Occupies the full size of its parent.
            .padding(bottom = 48.dp), // Adds bottom padding for spacing.
        horizontalAlignment = Alignment.CenterHorizontally, // Centers children horizontally.
        verticalArrangement = Arrangement.Center          // Centers children vertically.
    ) {
        // Top spacer to create balanced spacing above the button row.
        Spacer(modifier = Modifier.weight(1f))

        // Row containing the two primary buttons with a fixed space between them.
        Row(
            modifier = Modifier.fillMaxWidth(0.8f), // Restricts the row to 80% of the parent's width.
            horizontalArrangement = Arrangement.spacedBy(24.dp), // Sets 24.dp spacing between buttons.
            verticalAlignment = Alignment.CenterVertically        // Aligns buttons vertically centered.
        ) {
            // Button for displaying the list of steps.
            MainButton(
                iconId = R.drawable.ic_stages_list,
                label = stringResource(id = R.string.stages_list),
                onClick = onListClicked // Callback to open the list dialog.
            )

            // Button for starting the game.
            MainButton(
                iconId = R.drawable.ic_start_game,
                label = stringResource(id = R.string.start_game),
                onClick = onStartGameClicked // Callback to navigate to the game screen.
            )
        }

        // Bottom spacer to create balanced spacing below the button row.
        Spacer(modifier = Modifier.weight(1f))
    }
}
