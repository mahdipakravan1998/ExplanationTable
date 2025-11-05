package com.example.explanationtable.ui.components.topBar

import android.content.res.Configuration
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyColors
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight

/**
 * A top app bar component that adapts its appearance based on the page type, theme, and difficulty.
 *
 * @param isHomePage Determines if the current page is the home page.
 * @param isDarkTheme Specifies whether the dark theme is enabled (app theme flag).
 * @param title Optional title text to display in the center.
 * @param gems Optional gem count to display on non-home pages.
 * @param difficulty Optional difficulty parameter; must be non-null on non-home pages.
 * @param onSettingsClick Callback invoked when the settings icon is clicked.
 * @param iconTint Default icon tint (unused here, kept for API compatibility).
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
    val topBarHeight = 80.dp

    val difficultyColorValues = if (!isHomePage) {
        difficultyColors(difficulty!!)
    } else null

    val containerColor = if (isHomePage) Color.Transparent else difficultyColorValues!!.backgroundColor
    val dividerColor   = if (isHomePage) Color.Transparent else difficultyColorValues!!.dividerColor

    // Help icon tint (unchanged behavior).
    val helpButtonIconTint = if (isHomePage) {
        if (isDarkTheme) BackgroundLight else BackgroundDark
    } else {
        difficultyColorValues!!.textColor
    }

    // Choose icon resource by page.
    val settingsIconRes =
        if (isHomePage) R.drawable.ic_settings_home else R.drawable.ic_settings

    // Slightly larger icon on the home page; 48.dp touch target preserved.
    val settingsIconSize = if (isHomePage) 28.dp else 24.dp

    val settingsContentDesc = stringResource(id = R.string.settings)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(topBarHeight)
            .background(containerColor)
    ) {
        // LEFT: gems or spacer
        if (!isHomePage) {
            gems?.let { gemCount ->
                GemGroup(
                    gems = gemCount,
                    modifier = Modifier
                        .align(Alignment.CenterStart)
                        .padding(start = 28.dp)
                )
            }
        } else {
            Spacer(
                modifier = Modifier
                    .align(Alignment.CenterStart)
                    .size(48.dp)
            )
        }

        // CENTER: title
        title?.let { titleText ->
            Text(
                text = titleText,
                fontSize = 18.sp,
                color = if (!isHomePage) difficultyColorValues!!.textColor else MaterialTheme.colorScheme.onSurface,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.align(Alignment.Center)
            )
        }

        // RIGHT: help (optional) + settings
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = 28.dp)
        ) {
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
                        tint = helpButtonIconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            // SETTINGS ICON
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clickable { onSettingsClick() }
                    .semantics { contentDescription = settingsContentDesc },
                contentAlignment = Alignment.Center
            ) {
                if (isHomePage) {
                    // --- HOME PAGE: force-load the correct day/night variant and strip any XML tint. ---
                    val context = LocalContext.current

                    AndroidView(
                        factory = {
                            ImageView(context).apply {
                                // Build an override configuration so resources resolve to the *intended* mode.
                                val cfg = Configuration(context.resources.configuration).apply {
                                    @Suppress("DEPRECATION")
                                    uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                                            (if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES
                                            else Configuration.UI_MODE_NIGHT_NO)
                                }
                                val themedCtx = context.createConfigurationContext(cfg)

                                // Load drawable from that context, mutate, and clear any tint.
                                val d = AppCompatResources.getDrawable(themedCtx, settingsIconRes)?.mutate()
                                if (d != null) {
                                    DrawableCompat.setTintList(d, null) // remove root XML tint if present
                                    colorFilter = null
                                    imageTintList = null
                                    setImageDrawable(d)
                                } else {
                                    // Fallback (shouldn't happen)
                                    setImageResource(settingsIconRes)
                                    imageTintList = null
                                }

                                adjustViewBounds = true
                                scaleType = ImageView.ScaleType.FIT_CENTER
                            }
                        },
                        update = { view ->
                            val cfg = Configuration(context.resources.configuration).apply {
                                @Suppress("DEPRECATION")
                                uiMode = (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                                        (if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES
                                        else Configuration.UI_MODE_NIGHT_NO)
                            }
                            val themedCtx = context.createConfigurationContext(cfg)
                            val d = AppCompatResources.getDrawable(themedCtx, settingsIconRes)?.mutate()
                            if (d != null) {
                                DrawableCompat.setTintList(d, null)
                                view.colorFilter = null
                                view.imageTintList = null
                                view.setImageDrawable(d)
                            } else {
                                view.setImageResource(settingsIconRes)
                                view.imageTintList = null
                            }
                        },
                        modifier = Modifier.size(settingsIconSize)
                    )
                } else {
                    // --- OTHER PAGES: use difficulty-based tint as before. ---
                    Icon(
                        painter = painterResource(id = settingsIconRes),
                        contentDescription = null,
                        tint = difficultyColorValues!!.textColor,
                        modifier = Modifier.size(settingsIconSize)
                    )
                }
            }
        }

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
