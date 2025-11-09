package com.example.explanationtable.ui.system

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/** Declarative “what should the system bottom area look like?” */
sealed class BottomBarAppearance {
    /** Don’t draw anything over the nav-bar area. */
    object None : BottomBarAppearance()

    /** Draw a subtle gradient scrim over the nav-bar area. */
    object Scrim : BottomBarAppearance()

    /** Fill the nav-bar area with a solid [color] (optional extra padding above it). */
    data class Solid(val color: Color, val extraPadding: Dp = 0.dp) : BottomBarAppearance()
}
