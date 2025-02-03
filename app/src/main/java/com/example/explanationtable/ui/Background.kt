package com.example.explanationtable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.explanationtable.R

/**
 * A composable function that sets up the background for a screen.
 *
 * For home pages, it displays a background image (with a dark/light variant based on [isDarkTheme])
 * along with a semi-transparent overlay. For non-home pages, it simply applies the theme's background color.
 *
 * @param isHomePage If true, applies the background image and overlay.
 * @param isDarkTheme If true, uses the dark variant of the background image.
 * @param content The main content to be rendered on top of the background.
 */
@Composable
fun Background(
    isHomePage: Boolean = false,
    isDarkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    // Root container that fills the entire available screen space
    Box(modifier = Modifier.fillMaxSize()) {
        if (isHomePage) {
            // Choose the appropriate background image resource based on the theme
            val backgroundRes = if (isDarkTheme) {
                R.drawable.background_main_dark
            } else {
                R.drawable.background_main_light
            }

            // Render the background image with cropping to cover the full container
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null, // Image is decorative
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Apply a semi-transparent overlay to blend the image with the theme's background color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            )
        } else {
            // For non-home pages, simply fill the background with the theme's background color
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // Render the main content on top of the background layers
        content()
    }
}
