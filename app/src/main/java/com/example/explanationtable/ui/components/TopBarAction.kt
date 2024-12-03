package com.example.explanationtable.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.Alignment
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource

@Composable
fun TopBarAction(iconId: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 32.dp, end = 40.dp)
            .size(56.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(56.dp)
        )
    }
}
