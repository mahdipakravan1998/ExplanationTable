package com.example.explanationtable.ui

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import com.example.explanationtable.R

@Composable
fun Background(content: @Composable () -> Unit) {
    Box(modifier = Modifier.fillMaxSize()) {
        // Background image
        Image(
            painter = painterResource(id = R.drawable.background_main),
            contentDescription = null, // Background image doesn't need a description
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
        )

        // Page content
        content()
    }
}
