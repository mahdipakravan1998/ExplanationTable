package com.example.explanationtable.ui.stages.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    backgroundColor: Color,
    shadowColor: Color,
    textColor: Color,
    imageResId: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clickable { onClick() }
    ) {
        // Background Rectangle
        Card(
            modifier = Modifier
                .fillMaxSize()
                .offset(y = 4.dp), // Slightly offset downwards
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = shadowColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // No elevation
        ) {}

        // Foreground Card
        Card(
            modifier = Modifier
                .fillMaxSize(),
            shape = RoundedCornerShape(8.dp),
            colors = CardDefaults.cardColors(containerColor = backgroundColor),
            elevation = CardDefaults.cardElevation(defaultElevation = 0.dp) // Removed elevation
        ) {
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = imageResId),
                        contentDescription = null,
                        modifier = Modifier.size(40.dp)
                    )
                }
                Spacer(modifier = Modifier.width(16.dp))

                Text(
                    text = label,
                    color = textColor,
                    style = MaterialTheme.typography.bodyLarge.copy(
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
