package com.example.explanationtable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.painterResource
import com.example.explanationtable.R
import kotlin.math.abs

/**
 * Background composable for your screens.
 *
 * - Uses 8 background assets: 4 aspect-ratio buckets for light, and 4 for dark.
 * - Picks the closest bucket based on the current screen aspect ratio (no BoxWithConstraints).
 * - If the device ratio matches one of your 4 buckets (within tolerance), shows the image without cropping.
 * - If it doesn't match, fills and crops to avoid letterboxing.
 *
 * Expected drawable names:
 *   Light: bg_home_16_9, bg_home_19_5_9, bg_home_20_9, bg_home_21_9
 *   Dark : bg_home_dark_16_9, bg_home_dark_19_5_9, bg_home_dark_20_9, bg_home_dark_21_9
 */
@Composable
fun Background(
    isHomePage: Boolean = false,
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        if (isHomePage) {
            val configuration = LocalConfiguration.current
            val screenWidthDp = configuration.screenWidthDp.coerceAtLeast(1)
            val screenHeightDp = configuration.screenHeightDp.coerceAtLeast(1)

            data class Selection(val resId: Int, val matched: Boolean)

            val selection = remember(screenWidthDp, screenHeightDp, isDarkTheme) {
                // Orientation-agnostic aspect ratio (treat as portrait): longer/shorter
                val aspect = maxOf(screenWidthDp, screenHeightDp).toFloat() /
                        minOf(screenWidthDp, screenHeightDp).toFloat()

                data class Bucket(val ratio: Float, val lightRes: Int, val darkRes: Int)
                val buckets = listOf(
                    Bucket(16f / 9f,   R.drawable.bg_home_16_9,     R.drawable.bg_home_dark_16_9),
                    Bucket(19.5f / 9f, R.drawable.bg_home_19_5_9,   R.drawable.bg_home_dark_19_5_9),
                    Bucket(20f / 9f,   R.drawable.bg_home_20_9,     R.drawable.bg_home_dark_20_9),
                    Bucket(21f / 9f,   R.drawable.bg_home_21_9,     R.drawable.bg_home_dark_21_9),
                )

                val best = buckets.minByOrNull { b -> abs(aspect - b.ratio) }!!
                val tolerance = 0.02f
                val matched = abs(aspect - best.ratio) <= tolerance
                val resId = if (isDarkTheme) best.darkRes else best.lightRes
                Selection(resId, matched)
            }

            val contentScale = if (selection.matched) {
                // When the aspect ratio matches a bucket, fill the bounds without cropping.
                ContentScale.FillBounds
            } else {
                // Otherwise, crop to avoid letterboxing.
                ContentScale.Crop
            }

            Image(
                painter = painterResource(id = selection.resId),
                contentDescription = null, // decorative background
                contentScale = contentScale,
                alignment = Alignment.Center,
                modifier = Modifier.fillMaxSize()
            )
        } else {
            // Non-home pages: plain themed background color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // Foreground content
        content()
    }
}
