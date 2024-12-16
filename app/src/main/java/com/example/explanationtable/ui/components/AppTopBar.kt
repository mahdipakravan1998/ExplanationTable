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
import com.example.explanationtable.ui.theme.ColorHighlightDark
import com.example.explanationtable.ui.theme.ColorPrimaryTextLight

/**
 * A unified top bar composable that handles both the home page and other pages.
 *
 * @param isHomePage Indicates whether the current page is the home page.
 * @param title The title to display in the center (only for non-home pages).
 * @param diamonds The number of user diamonds to display on the left (only for non-home pages).
 * @param onSettingsClick Callback when the settings button is clicked.
 */
@Composable
fun AppTopBar(
    isHomePage: Boolean,
    title: String? = null,
    diamonds: Int? = null,
    onSettingsClick: () -> Unit
) {
    // Determine if the current theme is dark
    val isDarkTheme = isSystemInDarkTheme()

    // Select background color for the settings button based on theme
    val settingsButtonBackgroundColor = if (isDarkTheme) {
        ColorHighlightDark // From Color.kt for Night Mode
    } else {
        ColorPrimaryTextLight // From Color.kt for Day Mode
    }

    // Define the height of the top bar based on the page type
    val topBarHeight = 80.dp

    // Define the container color for non-home pages
    val containerColor = if (isHomePage) {
        Color.Transparent
    } else {
        // You can customize this further based on page requirements
        MaterialTheme.colorScheme.primary
    }

    // Define the divider color for non-home pages
    val dividerColor = if (isHomePage) {
        Color.Transparent
    } else {
        // Adjust the divider color as needed
        MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.12f)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(containerColor)
            .height(topBarHeight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = if (isHomePage) Arrangement.End else Arrangement.SpaceBetween
        ) {
            if (!isHomePage) {
                // Left side: Diamond count
                DiamondGroup(diamonds = diamonds ?: 0)
            } else {
                // To balance the layout, add a spacer if it's the home page
                Spacer(modifier = Modifier.width(48.dp))
            }

            if (!isHomePage && title != null) {
                // Centered title
                Text(
                    text = title,
                    fontSize = 18.sp,
                    color = MaterialTheme.colorScheme.onPrimary,
                    fontWeight = FontWeight.SemiBold,
                )
            } else {
                // Spacer for the home page to keep the settings button aligned to the right
                Spacer(modifier = Modifier.weight(1f))
            }

            // Right side: Settings icon with circular background
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
                    tint = Color.White,
                    modifier = Modifier.size(24.dp)
                )
            }
        }

        // Bottom divider for non-home pages
        if (!isHomePage) {
            Divider(
                color = dividerColor,
                thickness = 2.dp,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .fillMaxWidth()
            )
        }
    }
}