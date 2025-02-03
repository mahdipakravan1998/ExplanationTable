package com.example.explanationtable.ui.settings.options

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.settings.components.SettingCard
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.DialogBackgroundDark
import com.example.explanationtable.ui.theme.DialogBackgroundLight

/**
 * Displays the settings options panel containing controls for sound, theme toggle, and exit.
 *
 * @param onDismiss Callback invoked when a setting option is dismissed.
 *                  Note: This parameter is currently not used in the implementation.
 * @param isDarkTheme Boolean flag representing whether the dark theme is active.
 * @param onToggleTheme Callback to toggle between dark and light themes.
 * @param isMuted Boolean flag representing whether the sound is muted.
 * @param onToggleMute Callback to toggle the mute state.
 * @param onExit Callback invoked when the exit option is selected.
 */
@Composable
fun SettingsOptions(
    onDismiss: () -> Unit,
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    // Determine the appropriate icons based on the current sound and theme states.
    val soundIcon = if (isMuted) R.drawable.ic_volume_down else R.drawable.ic_volume_up
    val themeIcon = if (isDarkTheme) R.drawable.ic_moon else R.drawable.ic_sun

    // Select border and background colors based on the active theme.
    val borderColor = if (isDarkTheme) BorderDark else BorderLight
    val backgroundColor = if (isDarkTheme) DialogBackgroundDark else DialogBackgroundLight

    // Main vertical layout container for the settings options.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Sound Setting Card:
        // Displays a toggle for the sound state. The switch is "checked" when sound is enabled.
        SettingCard(
            iconResId = soundIcon,
            label = stringResource(id = R.string.soundLabel),
            isChecked = !isMuted, // Not muted means sound is enabled.
            onCheckedChange = { onToggleMute() },
            borderColor = borderColor,
            backgroundColor = backgroundColor
        )

        // Spacer for visual separation between setting cards.
        Spacer(modifier = Modifier.height(8.dp))

        // Theme Setting Card:
        // Displays a toggle for switching between dark and light themes.
        SettingCard(
            iconResId = themeIcon,
            label = stringResource(id = R.string.themeLabel),
            isChecked = isDarkTheme, // Checked if dark theme is active.
            onCheckedChange = { onToggleTheme() },
            borderColor = borderColor,
            backgroundColor = backgroundColor
        )

        // Spacer before the exit button to add extra separation.
        Spacer(modifier = Modifier.height(16.dp))

        // Exit Button:
        // Triggers the exit action when clicked, styled with an error color scheme.
        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            // Localized text label for the exit button.
            Text(
                text = stringResource(id = R.string.exitLabel),
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}
