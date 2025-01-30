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
    gems: Int? = null,
    difficulty: Difficulty? = null,
    onSettingsClick: () -> Unit,
    iconTint: Color = MaterialTheme.colorScheme.onSurface,
    onHelpClick: (() -> Unit)? = null
) {
    val topBarHeight = 80.dp

    // Container color based on the page type and difficulty
    val containerColor = if (isHomePage) Color.Transparent else difficultyColors(difficulty!!).backgroundColor

    // Divider color for non-home pages
    val dividerColor = if (isHomePage) Color.Transparent else difficultyColors(difficulty!!).dividerColor

    // Settings button styles for different themes and page types
    val settingsButtonBackgroundColor = if (isHomePage) {
        if (isDarkTheme) ButtonBackgroundLight else ButtonBackgroundDark
    } else {
        Color.Transparent
    }

    // Icon tint for the settings button
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
        // Left section: Display gem count or spacer
        if (!isHomePage) {
            gems?.let { gemCount ->
                // Increase the left padding here to push gem count further away from the left side
                GemGroup(
                    gems = gemCount,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 28.dp)  // Increased padding on the left
                )
            }
        } else {
            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
            )
        }

        // Title displayed in the center
        title?.let {
            Text(
                text = it,
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

        // Right section: Help and Settings icons with adjusted spacing
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp) // Slightly reduced padding on the right side
        ) {
            // Help icon (visible only if onHelpClick is provided)
            onHelpClick?.let {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { it() },
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

            // Settings icon
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

        // Divider for non-home pages
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
