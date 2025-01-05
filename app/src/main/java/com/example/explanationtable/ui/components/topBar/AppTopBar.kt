package com.example.explanationtable.ui.components.topBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyColors
import com.example.explanationtable.ui.theme.ButtonBackgroundDark
import com.example.explanationtable.ui.theme.ButtonBackgroundLight
import com.example.explanationtable.ui.theme.ButtonIconDark
import com.example.explanationtable.ui.theme.ButtonIconLight

@Composable
fun AppTopBar(
    isHomePage: Boolean,
    isDarkTheme: Boolean,
    title: String? = null,
    diamonds: Int? = null,
    difficulty: Difficulty? = null,
    onSettingsClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onHelpClick: (() -> Unit)? = null
) {
    val topBarHeight = 80.dp

    // Define container color
    val containerColor = if (isHomePage) {
        Color.Transparent
    } else {
        // For non-home pages, get color based on difficulty
        difficultyColors(difficulty!!).backgroundColor
    }

    // Define divider color for non-home pages
    val dividerColor = if (isHomePage) {
        Color.Transparent
    } else {
        difficultyColors(difficulty!!).dividerColor
    }

    // Define settings button background color based on isDarkTheme and isHomePage
    val settingsButtonBackgroundColor = if (isHomePage) {
        if (isDarkTheme) ButtonBackgroundLight else ButtonBackgroundDark
    } else {
        Color.Transparent
    }

    // Define settings button icon tint based on isDarkTheme and isHomePage
    val settingsButtonIconTint = if (isHomePage) {
        if (isDarkTheme) ButtonIconDark else ButtonIconLight
    } else {
        difficultyColors(difficulty!!).textColor
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(containerColor)
    ) {
        // Left Side: DiamondGroup or Spacer
        if (!isHomePage) {
            diamonds?.let { diamondCount ->
                DiamondGroup(
                    diamonds = diamondCount,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 16.dp)
                )
            }
        } else {
            // Spacer to match DiamondGroup width
            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
            )
        }

        // Title: Always Centered
        if (title != null) {
            Text(
                text = title,
                fontSize = 18.sp,
                color = if (!isHomePage && difficulty != null) {
                    difficultyColors(difficulty).textColor
                } else {
                    MaterialTheme.colorScheme.onSurface
                },
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // Right Side: Help Icon (optional) and Settings Icon
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 16.dp)
        ) {
            // HELP ICON (only if a callback is provided)
            if (onHelpClick != null) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onHelpClick() },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_lightbulb),
                        contentDescription = stringResource(id = R.string.hint),
                        tint = settingsButtonIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // SETTINGS ICON
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = settingsButtonBackgroundColor,
                        shape = CircleShape
                    )
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings),
                    tint = settingsButtonIconTint,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom divider for non-home pages
        if (!isHomePage) {
            HorizontalDivider(
                color = dividerColor,
                thickness = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}
