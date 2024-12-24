package com.example.explanationtable.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.components.cards.SettingCard

@Composable
fun SettingsOptions(
    onDismiss: () -> Unit,
    currentTheme: Boolean,  // True if Dark, false if Light
    onToggleTheme: () -> Unit,
    isMuted: Boolean,
    onToggleMute: () -> Unit,
    onExit: () -> Unit
) {
    // Fixed labels (you can translate or rename as needed)
    val soundLabel = "صدا"
    val themeLabel = "حالت شب"
    val exitLabel = "خروج"

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
            label = soundLabel,
            // Switch is ON => isChecked = true => means NOT muted
            isChecked = !isMuted,
            onCheckedChange = { onToggleMute() }
        )

        // Divider between the first and second option
        Divider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            color = MaterialTheme.colorScheme.outlineVariant
        )

        // 2) Theme setting row
        SettingCard(
            iconResId = themeIcon,
            label = themeLabel,
            // Switch is ON => isChecked = true => means "Dark theme"
            isChecked = currentTheme,
            onCheckedChange = { onToggleTheme() }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // 3) Exit button (red, no shadow)
        Button(
            onClick = onExit,
            colors = ButtonDefaults.buttonColors(
                containerColor = MaterialTheme.colorScheme.error
            ),
            elevation = null, // No shadow
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = exitLabel,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

