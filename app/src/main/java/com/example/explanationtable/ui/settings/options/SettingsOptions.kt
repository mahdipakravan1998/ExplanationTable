package com.example.explanationtable.ui.settings.options

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
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
 * A composable that displays various settings options including sound, theme toggles, and exit.
 *
 * @param onDismiss Callback invoked when a setting option is dismissed.
 * @param currentTheme True if dark theme is active, false otherwise.
 * @param onToggleTheme Callback to toggle the theme.
 * @param isMuted True if the app is muted, false otherwise.
 * @param onToggleMute Callback to toggle the mute state.
 * @param onExit Callback invoked when the user opts to exit the app.
 */
@Composable
fun SettingsOptions(
    onDismiss: () -> Unit,
    currentTheme: Boolean, // True if Dark, false if Light
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    // Icons that change based on toggles
    val soundIcon = if (isMuted) R.drawable.ic_volume_down else R.drawable.ic_volume_up
    val themeIcon = if (currentTheme) R.drawable.ic_moon else R.drawable.ic_sun

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 1) Sound setting row
        SettingCard(
            iconResId = soundIcon,
            label = stringResource(id = R.string.soundLabel),
            // Switch is ON => isChecked = true => means NOT muted
            isChecked = !isMuted,
            onCheckedChange = { onToggleMute() },
            borderColor = if (currentTheme) BorderDark else BorderLight,
            backgroundColor = if (currentTheme) DialogBackgroundDark else DialogBackgroundLight
        )

        Spacer(modifier = Modifier.height(8.dp))

        // 2) Theme setting row
        SettingCard(
            iconResId = themeIcon,
            label = stringResource(id = R.string.themeLabel),
            // Switch is ON => isChecked = true => means "Dark theme"
            isChecked = currentTheme,
            onCheckedChange = { onToggleTheme() },
            borderColor = if (currentTheme) BorderDark else BorderLight,
            backgroundColor = if (currentTheme) DialogBackgroundDark else DialogBackgroundLight
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3) Exit button
        Button(
            onClick = onExit, // Trigger the confirmation dialog
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            elevation = ButtonDefaults.buttonElevation(0.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = stringResource(id = R.string.exitLabel),
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}
