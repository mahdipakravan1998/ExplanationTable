package com.example.explanationtable.ui.components.topBar

import android.content.Context
import android.content.res.Configuration
import android.graphics.drawable.Drawable
import android.widget.ImageView
import androidx.appcompat.content.res.AppCompatResources
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
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
 *
 * Visual parity with the original is preserved:
 *  - The background behind the status bar and the bar itself is one continuous plane.
 *  - Home path uses a themed platform Drawable via AndroidView (as before).
 *  - Non-home path uses difficulty palette for title/icons/divider.
 *
 * Internally:
 *  - Theme reads are hoisted to the composable and passed as plain values.
 *  - A memoized [TopBarPalette] collects all derived values (stable inputs → stable outputs).
 *  - Callbacks use rememberUpdatedState to avoid stale captures.
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
    // Paint status bar area and the visible bar as one continuous background.
    val statusTop = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val totalHeight = barHeight + statusTop

    // Read from MaterialTheme *inside* the composable, pass through as raw Color.
    val onSurface = MaterialTheme.colorScheme.onSurface

    val palette = remember(isHomePage, isDarkTheme, difficulty, onSurface) {
        createTopBarPalette(
            isHomePage = isHomePage,
            isDarkTheme = isDarkTheme,
            difficulty = difficulty,
            homeOnSurface = onSurface
        )
    }

    // Keep callbacks current without causing click lambdas to change identity unnecessarily.
    val currentOnSettingsClick = rememberUpdatedState(onSettingsClick)
    val currentOnHelpClick = rememberUpdatedState(onHelpClick)

    val settingsContentDesc = stringResource(id = R.string.settings)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(totalHeight)
            .background(palette.containerColor)
    ) {
        // Bar content sits at the bottom edge of the painted background.
        Box(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .height(barHeight)
        ) {
            // LEFT: gems (non-home) or spacer (home) to keep layout aligned.
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

            // CENTER: title (optional)
            title?.let { titleText ->
                Text(
                    text = titleText,
                    fontSize = 18.sp,
                    color = palette.titleTextColor,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.align(Alignment.Center)
                )
            }

            // RIGHT: help (optional) + settings (always)
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .align(Alignment.CenterEnd)
                    .padding(end = 28.dp)
            ) {
                currentOnHelpClick.value?.let { onHelp ->
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .semantics { role = Role.Button }
                            .clickable { onHelp() },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_lightbulb),
                            contentDescription = stringResource(id = R.string.hint),
                            tint = palette.helpIconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }

                // SETTINGS
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .semantics {
                            contentDescription = settingsContentDesc
                            role = Role.Button
                        }
                        .clickable { currentOnSettingsClick.value.invoke() },
                    contentAlignment = Alignment.Center
                ) {
                    if (isHomePage) {
                        val context = LocalContext.current
                        // Memoize the themed drawable by (res, darkMode)
                        val themedDrawable = remember(palette.settingsIconRes, isDarkTheme) {
                            loadThemedDrawable(context, palette.settingsIconRes, isDarkTheme)
                        }
                        AndroidView(
                            factory = { ctx ->
                                ImageView(ctx).apply {
                                    adjustViewBounds = true
                                    scaleType = ImageView.ScaleType.FIT_CENTER
                                    setImageDrawable(themedDrawable)
                                }
                            },
                            update = { view ->
                                // Avoid redundant work during recomposition.
                                if (view.drawable !== themedDrawable) {
                                    view.setImageDrawable(themedDrawable)
                                }
                            },
                            modifier = Modifier.size(palette.settingsIconSize)
                        )
                    } else {
                        Icon(
                            painter = painterResource(id = palette.settingsIconRes),
                            contentDescription = null, // semantics provided on parent
                            tint = palette.textColor, // matches difficulty text
                            modifier = Modifier.size(palette.settingsIconSize)
                        )
                    }
                }
            }

            if (!isHomePage) {
                HorizontalDivider(
                    color = palette.dividerColor,
                    thickness = 2.dp,
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .fillMaxWidth()
                )
            }
        }
    }
}

@Immutable
private data class TopBarPalette(
    val containerColor: Color,
    val dividerColor: Color,
    val textColor: Color,
    val titleTextColor: Color,
    val helpIconTint: Color,
    val settingsIconRes: Int,
    val settingsIconSize: Dp
)

/**
 * Builds a stable palette for the top bar visuals.
 *
 * This function is intentionally non-composable. It derives all constants/Colors/Dp
 * from plain inputs to remain cheap and safe to call from `remember { ... }`.
 *
 * @param isHomePage True when rendering the home top bar variant.
 * @param isDarkTheme Current dark theme state to theme the home settings drawable.
 * @param difficulty Optional difficulty for the non-home palette selection.
 * @param homeOnSurface The themed onSurface color read from MaterialTheme in the caller.
 */
private fun createTopBarPalette(
    isHomePage: Boolean,
    isDarkTheme: Boolean,
    difficulty: Difficulty?,
    homeOnSurface: Color
): TopBarPalette {
    return if (isHomePage) {
        TopBarPalette(
            containerColor = Color.Transparent,
            dividerColor = Color.Transparent,
            textColor = homeOnSurface, // used only on the non-home path; harmless for home
            titleTextColor = homeOnSurface,
            helpIconTint = if (isDarkTheme) BackgroundLight else BackgroundDark,
            settingsIconRes = R.drawable.ic_settings_home,
            settingsIconSize = 28.dp
        )
    } else {
        val dc = difficultyColors(difficulty ?: Difficulty.EASY)
        TopBarPalette(
            containerColor = dc.backgroundColor,
            dividerColor = dc.dividerColor,
            textColor = dc.textColor,
            titleTextColor = dc.textColor,
            helpIconTint = dc.textColor,
            settingsIconRes = R.drawable.ic_settings,
            settingsIconSize = 24.dp
        )
    }
}

/**
 * Loads a platform Drawable themed for light/dark without Compose tinting.
 *
 * We mirror previous behavior while avoiding repeated Configuration churn:
 *  - The caller wraps this in `remember(resId, isDarkTheme) { ... }`.
 *  - Any system tint list is cleared to keep vector colors authored in the asset.
 */
private fun loadThemedDrawable(
    context: Context,
    resId: Int,
    isDarkTheme: Boolean
): Drawable? {
    val cfg = Configuration(context.resources.configuration).apply {
        @Suppress("DEPRECATION")
        uiMode =
            (uiMode and Configuration.UI_MODE_NIGHT_MASK.inv()) or
                    (if (isDarkTheme) Configuration.UI_MODE_NIGHT_YES else Configuration.UI_MODE_NIGHT_NO)
    }
    val themedCtx = context.createConfigurationContext(cfg)
    val d = AppCompatResources.getDrawable(themedCtx, resId)?.mutate()
    if (d != null) {
        DrawableCompat.setTintList(d, null) // preserve intrinsic asset colors
    }
    return d
}
