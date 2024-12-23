package com.example.explanationtable.ui.popup

import android.app.Activity
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.popup.components.OptionCard

@Composable
fun SettingsPopup(
    onDismiss: () -> Unit,
    currentTheme: Boolean,  // True if Dark, false if Light
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit
) {
    val context = LocalContext.current
    val activity = context as? Activity

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // 1) Mute/Unmute
        OptionCard(
            label = if (isMuted) {
                stringResource(id = R.string.unmute_sound)
            } else {
                stringResource(id = R.string.mute_sound)
            },
            onClick = onToggleMute,
            backgroundColor = MaterialTheme.colorScheme.primary,
            shadowColor = MaterialTheme.colorScheme.primaryContainer,
            textColor = MaterialTheme.colorScheme.onPrimary,
            imageResId = if (isMuted) {
                R.drawable.ic_volume_up
            } else {
                R.drawable.ic_volume_down
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 2) Day/Night Mode
        OptionCard(
            label = if (currentTheme) {
                stringResource(id = R.string.day_mode)
            } else {
                stringResource(id = R.string.night_mode)
            },
            onClick = onToggleTheme,
            backgroundColor = MaterialTheme.colorScheme.secondary,
            shadowColor = MaterialTheme.colorScheme.secondaryContainer,
            textColor = MaterialTheme.colorScheme.onSecondary,
            imageResId = if (currentTheme) {
                R.drawable.ic_sun
            } else {
                R.drawable.ic_moon
            }
        )
        Spacer(modifier = Modifier.height(8.dp))

        // 3) Exit App
        OptionCard(
            label = stringResource(id = R.string.exit_app),
            onClick = {
                activity?.finishAndRemoveTask()
            },
            backgroundColor = MaterialTheme.colorScheme.tertiary,
            shadowColor = MaterialTheme.colorScheme.tertiaryContainer,
            textColor = MaterialTheme.colorScheme.onTertiary,
            imageResId = R.drawable.ic_logout
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Close Button
        Button(
            onClick = onDismiss,
            modifier = Modifier.align(Alignment.End)
        ) {
            Text(text = stringResource(id = R.string.close))
        }
    }
}
