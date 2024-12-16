package com.example.explanationtable.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
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
import com.example.explanationtable.ui.theme.ColorHighlightDark
import com.example.explanationtable.ui.theme.ColorPrimaryTextLight

/**
 * A unified top bar composable that handles both the home page and other pages.
 *
 * @param isHomePage Indicates whether the current page is the home page.
 * @param title The title to display in the center (only for non-home pages).
 * @param diamonds The number of user diamonds to display on the left (only for non-home pages).
 * @param difficulty The difficulty level determining the top bar's background color (only for non-home pages).
 * @param onSettingsClick Callback when the settings button is clicked.
 */
@Composable
fun AppTopBar(
    isHomePage: Boolean,
    title: String? = null,
    diamonds: Int? = null,
    difficulty: Difficulty? = null,
    onSettingsClick: () -> Unit
) {
    // Define top bar height based on page type
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

    // Define settings button background color
    val settingsButtonBackgroundColor = if (isHomePage) {
        // For home page, set circular background based on theme
        if (isSystemInDarkTheme()) {
            ColorHighlightDark
        } else {
            ColorPrimaryTextLight
        }
    } else {
        // For other pages, no background
        Color.Transparent
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(containerColor),
        contentAlignment = Alignment.Center
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween  // Unified arrangement
        ) {
            // Left side
            if (!isHomePage) {
                diamonds?.let { DiamondGroup(diamonds = it) }
            } else {
                // Spacer to match DiamondGroup width on home page
                Spacer(modifier = Modifier.width(48.dp))
            }

            // Middle
            if (!isHomePage && title != null) {
                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = difficultyColors(difficulty!!).textColor,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                // Spacer to help push the settings button to the right
                Spacer(modifier = Modifier.weight(1f))
            }

            // Right side (Unified Settings Button)
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .background(
                        color = if (isHomePage) settingsButtonBackgroundColor else Color.Transparent,
                        shape = CircleShape
                    )
                    .clickable { onSettingsClick() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_settings),
                    contentDescription = stringResource(id = R.string.settings),
                    tint = if (isHomePage) Color.White else difficultyColors(difficulty!!).textColor,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom divider for non-home pages
        if (!isHomePage) {
            HorizontalDivider(  // Replacing deprecated Divider
                color = dividerColor,
                thickness = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }

}
