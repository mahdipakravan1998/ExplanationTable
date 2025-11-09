package com.example.explanationtable.ui.system

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.windowInsetsBottomHeight
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

@Composable
fun BottomNavAreaScrim(
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier
) {
    val endAlpha   = if (isDarkTheme) 0.18f else 0.12f
    val startAlpha = 0f

    Box(
        modifier = modifier
            .fillMaxWidth()
            .windowInsetsBottomHeight(WindowInsets.navigationBars)
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color.Black.copy(alpha = startAlpha),
                        Color.Black.copy(alpha = endAlpha)
                    )
                )
            )
    )
}
