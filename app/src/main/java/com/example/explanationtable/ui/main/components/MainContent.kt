package com.example.explanationtable.ui.main.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import com.example.explanationtable.R
import com.example.explanationtable.ui.components.buttons.SecondaryButtonHome
import com.example.explanationtable.ui.components.buttons.PrimaryButtonHome

/**
 * Bottom-anchored actions.
 *
 * Secondary (“Stages list”) sits above Primary (“Start game”).
 * Tuned to clear the floor band while staying thumb-reachable.
 *
 * BEHAVIOR GUARANTEE:
 * - UI output and behavior are identical to the original implementation.
 *
 * DESIGN NOTES:
 * - Uses stable bottom padding (no dynamic WindowInsets) so content never "jumps"
 *   when transient system bars are revealed/hidden.
 * - Spacing and lift policy are centralized in [MainContentDefaults].
 * - Memoization reduces unnecessary recomputation and child recomposition.
 */
@Composable
fun MainContent(
    isDarkTheme: Boolean,
    onListClicked: () -> Unit,
    onStartGameClicked: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Screen height in dp (drives the small "lift" off the bottom edge).
    val configuration = LocalConfiguration.current
    val screenHeightDp = configuration.screenHeightDp

    // Recompute lift ONLY when height changes; identical thresholds as before.
    val liftDp by remember(screenHeightDp) {
        derivedStateOf { MainContentDefaults.liftForHeight(screenHeightDp) }
    }

    // Stable references for click lambdas; prevents unnecessary child recompositions if identities change.
    val onListClick by rememberUpdatedState(newValue = onListClicked)
    val onStartClick by rememberUpdatedState(newValue = onStartGameClicked)

    // Labels via string resources (i18n).
    val startLabel = stringResource(id = R.string.start_game)
    val stagesLabel = stringResource(id = R.string.stages_list)

    Box(
        modifier = modifier
            .fillMaxSize()
            // Keep side gutters stable; matches original 24.dp
            .padding(horizontal = MainContentDefaults.SidePadding)
    ) {
        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                // Stable bottom padding; no windowInsetsPadding → zero reflow on transient bars.
                .padding(bottom = MainContentDefaults.BaseBottomPadding + liftDp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(MainContentDefaults.BetweenButtons)
        ) {
            SecondaryButtonHome(
                isDarkTheme = isDarkTheme,
                onClick = onListClick,
                text = stagesLabel,
                modifier = Modifier.fillMaxWidth()
            )
            PrimaryButtonHome(
                isDarkTheme = isDarkTheme,
                onClick = onStartClick,
                text = startLabel,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}
