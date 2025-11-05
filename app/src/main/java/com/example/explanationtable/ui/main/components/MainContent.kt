package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.components.PrimaryButton
import com.example.explanationtable.ui.components.SecondaryButton

/**
 * Bottom-anchored actions without scrim.
 * Secondary (“Stages list”) sits above Primary (“Start game”).
 * Tuned to clear the floor band while staying thumb-reachable.
 */
@Composable
fun MainContent(
    isDarkTheme: Boolean,
    onListClicked: () -> Unit,
    onStartGameClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Spacing
    val sidePadding = 24.dp
    val betweenButtons = 12.dp
    val baseBottom = 12.dp // plus safe area (applied below)

    // Small, screen-aware lift so buttons aren't on the very edge.
    val h = LocalConfiguration.current.screenHeightDp
    val liftDp = when {
        h <= 640 -> 40.dp
        h <= 760 -> 48.dp
        h <= 880 -> 56.dp
        else     -> 60.dp
    }

    val startLabel = stringResource(id = R.string.start_game)
    val stagesLabel = stringResource(id = R.string.stages_list)

    Box(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = sidePadding)
            .windowInsetsPadding(WindowInsets.safeDrawing.only(WindowInsetsSides.Bottom))
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = baseBottom + liftDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(betweenButtons)
        ) {
            SecondaryButton(
                isDarkTheme = isDarkTheme,
                onClick = onListClicked,
                text = stagesLabel,
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButton(
                isDarkTheme = isDarkTheme,
                onClick = onStartGameClicked,
                text = startLabel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
