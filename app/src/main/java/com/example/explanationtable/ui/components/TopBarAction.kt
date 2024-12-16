package com.example.explanationtable.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R

@Composable
fun TopBarAction(
    iconId: Int,
    contentDescription: String,
    onClick: () -> Unit
) {
    val iconColor = MaterialTheme.colorScheme.onSurface

    Box(
        modifier = Modifier
            .padding(top = 16.dp, end = 16.dp)
            .size(48.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = contentDescription,
            tint = iconColor,
            modifier = Modifier.size(24.dp)
        )
    }
}

@Preview(showBackground = true)
@Composable
fun PreviewTopBarAction() {
    MaterialTheme {
        TopBarAction(
            iconId = R.drawable.ic_settings,
            contentDescription = "Settings"
        ) {}
    }
}
