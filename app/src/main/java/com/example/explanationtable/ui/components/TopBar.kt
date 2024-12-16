package com.example.explanationtable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyColors
import androidx.compose.foundation.Image
import androidx.compose.ui.graphics.ColorFilter

/**
 * A top bar composable that displays:
 * - Diamond count on the left
 * - A centered title
 * - A settings icon on the right
 *
 * Color scheme is derived from [difficulty].
 */
@Composable
fun TopBar(
    difficulty: Difficulty,
    title: String,
    diamonds: Int,
    onSettingsClick: () -> Unit
) {
    val colors = difficultyColors(difficulty)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(colors.backgroundColor)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(80.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Left side: Diamond count
            DiamondGroup(diamonds = diamonds)

            // Centered title
            Text(
                text = title,
                fontSize = 18.sp,
                color = colors.textColor,
                fontWeight = FontWeight.SemiBold,
            )

            // Right side: Settings icon
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable(onClick = onSettingsClick),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = "Settings",
                    modifier = Modifier.size(24.dp),
                    colorFilter = ColorFilter.tint(Color.White)
                )
            }
        }

        // Bottom divider
        HorizontalDivider(
            color = colors.dividerColor,
            thickness = 2.dp,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
        )
    }
}
