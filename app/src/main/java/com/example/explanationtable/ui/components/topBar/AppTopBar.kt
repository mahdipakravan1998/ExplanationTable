package com.example.explanationtable.ui.components.topBar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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

/**
 * A top app bar component that adapts its appearance based on the page type, theme, and difficulty.
 *
 * @param isHomePage Determines if the current page is the home page.
 * @param isDarkTheme Specifies whether the dark theme is enabled.
 * @param title Optional title text to display in the center.
 * @param gems Optional gem count to display on non-home pages.
 * @param difficulty Optional difficulty parameter; must be non-null on non-home pages.
 * @param onSettingsClick Callback invoked when the settings icon is clicked.
 * @param iconTint Default icon tint (unused in this implementation but kept for API compatibility).
 * @param onHelpClick Optional callback for the help icon; if provided, the help icon is shown.
 */
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
    // Fixed height for the top bar.
    val topBarHeight = 80.dp

    // Compute difficulty-based colors only if not on the home page.
    // On non-home pages, 'difficulty' is guaranteed to be non-null.
    val difficultyColorValues = if (!isHomePage) {
        difficultyColors(difficulty!!)
    } else {
        null
    }

    // Determine the container (background) color.
    // Use transparent on the home page or a difficulty-based background on other pages.
    val containerColor = if (isHomePage) {
        Color.Transparent
    } else {
        difficultyColorValues!!.backgroundColor
    }

    // Determine the divider color: transparent on home page or based on difficulty otherwise.
    val dividerColor = if (isHomePage) {
        Color.Transparent
    } else {
        difficultyColorValues!!.dividerColor
    }

    // Set the background color for the settings button:
    // On the home page, use theme-dependent button backgrounds; otherwise, transparent.
    val settingsButtonBackgroundColor = if (isHomePage) {
        if (isDarkTheme) ButtonBackgroundLight else ButtonBackgroundDark
    } else {
        Color.Transparent
    }

    // Determine the icon tint for settings (and help) buttons:
    // Use theme-dependent colors on the home page; otherwise, use difficulty-based text color.
    val settingsButtonIconTint = if (isHomePage) {
        if (isDarkTheme) ButtonIconDark else ButtonIconLight
    } else {
        difficultyColorValues!!.textColor
    }

    // Main container Box for the top bar.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(containerColor)
    ) {
        // LEFT SECTION: Display gem count (for non-home pages) or a spacer for alignment.
        if (!isHomePage) {
            gems?.let { gemCount ->
                GemGroup(
                    gems = gemCount,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 28.dp) // Extra left padding for gem display.
                )
            }
        } else {
            // A spacer to preserve layout consistency on the home page.
            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
            )
        }

        // CENTER SECTION: Display the title text if provided.
        title?.let { titleText ->
            Text(
                text = titleText,
                fontSize = 18.sp,
                // Use difficulty text color on non-home pages; otherwise, default to onSurface color.
                color = if (!isHomePage) difficultyColorValues!!.textColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // RIGHT SECTION: Row containing optional Help and mandatory Settings icons.
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp) // Right padding for icon alignment.
        ) {
            // Conditionally display the Help icon if an onHelpClick callback is provided.
            onHelpClick?.let { onHelp ->
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onHelp() },
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

            // Display the Settings icon.
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

        // BOTTOM DIVIDER: For non-home pages, display a horizontal divider.
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
