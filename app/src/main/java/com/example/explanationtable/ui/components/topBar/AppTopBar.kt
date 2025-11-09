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
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.graphics.drawable.DrawableCompat
import com.example.explanationtable.R
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.difficultyColors
import com.example.explanationtable.ui.system.SystemBarsDefaults
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.BackgroundLight

/**
 * Single-color top bar that also paints the status-bar area (no seams).
 * [barHeight] = visible bar height.
 */
@Composable
fun AppTopBar(
    isHomePage: Boolean,
    isDarkTheme: Boolean,
    title: String? = null,
    gems: Int? = null,
    difficulty: Difficulty? = null,
    onSettingsClick: () -> Unit,
    onHelpClick: (() -> Unit)? = null,
    barHeight: Dp = SystemBarsDefaults.TopBarHeight
) {
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalHeight = barHeight + statusTop

    val difficultyColorValues = if (!isHomePage) difficultyColors(difficulty!!) else null
    val containerColor = if (isHomePage) Color.Transparent else difficultyColorValues!!.backgroundColor
    val dividerColor   = if (isHomePage) Color.Transparent else difficultyColorValues!!.dividerColor
    val helpButtonIconTint = if (isHomePage) {
        if (isDarkTheme) BackgroundLight else BackgroundDark
    } else {
        difficultyColorValues!!.textColor
    }
    val settingsIconRes = if (isHomePage) R.drawable.ic_settings_home else R.drawable.ic_settings
    val settingsIconSize = if (isHomePage) 28.dp else 24.dp
    val settingsContentDesc = stringResource(id = R.string.settings)

    // Paint one continuous background for status + bar heights (prevents seam)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(containerColor)
    ) {
        // Actual bar content sits at the bottom of that background
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(barHeight)
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

                // SETTINGS
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clickable { onSettingsClick() }
                        .semantics { contentDescription = settingsContentDesc },
                    contentAlignment = Alignment.Center
                ) {
                    if (isHomePage) {
                        val context = LocalContext.current
                        AndroidView(
                            factory = {
                                ImageView(context).apply {
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
                                        colorFilter = null
                                        imageTintList = null
                                        setImageDrawable(d)
                                    } else {
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
}
