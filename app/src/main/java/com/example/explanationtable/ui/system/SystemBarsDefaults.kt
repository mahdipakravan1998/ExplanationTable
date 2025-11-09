package com.example.explanationtable.ui.system

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.explanationtable.ui.theme.DarkGreenBackground
import com.example.explanationtable.ui.theme.SeaSponge

/**
 * Single source of truth for “chrome” sizing/colors used by the system-bar layer.
 */
object SystemBarsDefaults {
    // Visible height of our app top bar.
    val TopBarHeight = 72.dp

    /** PrizeBox overlay color used to paint the system bottom area during review. */
    @Composable
    fun prizeOverlayColor(isDarkTheme: Boolean): Color =
        if (isDarkTheme) DarkGreenBackground else SeaSponge
}
