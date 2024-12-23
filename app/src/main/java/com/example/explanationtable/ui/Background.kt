package com.example.explanationtable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.explanationtable.R
import androidx.compose.material3.MaterialTheme

@Composable
fun Background(
    isHomePage: Boolean = false,
    isDarkTheme: Boolean = false, // Add isDarkTheme parameter
    content: @Composable () -> Unit
) {
    // Select the appropriate background resource based on isDarkTheme
    val backgroundRes = if (isHomePage) {
        if (isDarkTheme) R.drawable.background_main_dark else R.drawable.background_main_light
    } else {
        0 // No background for non-home pages
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (isHomePage) {
            Image(
                painter = painterResource(id = backgroundRes),
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )

            // Optional overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
            )
        } else {
            // Use the theme's background color for non-home screens
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.background)
            )
        }

        // Main screen content
        content()
    }
}
