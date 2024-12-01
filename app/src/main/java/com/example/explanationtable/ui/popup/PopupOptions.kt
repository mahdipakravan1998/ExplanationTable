package com.example.explanationtable.ui.popup

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import androidx.compose.foundation.clickable
import androidx.compose.ui.unit.sp
import androidx.compose.ui.Alignment
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.colorResource

@Composable
fun PopupOptions(
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp) // Keep minimal horizontal padding
            .padding(top = 0.dp, bottom = 0.dp) // Reduce top and bottom padding
    ) {
        OptionCard(
            label = stringResource(id = R.string.easy_step_label),
            onClick = { onOptionSelected("Option 1") },
            cardColor = colorResource(id = R.color.easy_step_color),
            imageResourceId = R.drawable.emerald, // Image for the first option
            shadowColor = colorResource(id = R.color.shadow_color_1) // Green shadow for the first image
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionCard(
            label = stringResource(id = R.string.medium_step_label),
            onClick = { onOptionSelected("Option 2") },
            cardColor = colorResource(id = R.color.medium_step_color),
            imageResourceId = R.drawable.crown, // Image for the second option
            shadowColor = colorResource(id = R.color.shadow_color_2) // Yellow shadow for the second image
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionCard(
            label = stringResource(id = R.string.difficult_step_label),
            onClick = { onOptionSelected("Option 3") },
            cardColor = colorResource(id = R.color.difficult_step_color),
            imageResourceId = R.drawable.ruby_diamond, // Image for the third option
            shadowColor = colorResource(id = R.color.shadow_color_3) // Orange shadow for the third image
        )
    }
}

@Composable
fun OptionCard(
    label: String,
    onClick: () -> Unit,
    cardColor: Color,
    imageResourceId: Int,
    shadowColor: Color // Add shadow color parameter
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp) // Set height of the card
            .clickable { onClick() }
            .padding(2.dp), // Reduced padding here to make the card tighter
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(containerColor = cardColor), // Use dynamic card color
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth() // Fill the width of the card
                .height(72.dp) // Ensure the row takes up the full height of the card
                .padding(horizontal = 16.dp), // Symmetric horizontal padding
            verticalAlignment = Alignment.CenterVertically // Align items vertically in the center
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp) // Image size
                    .padding(4.dp) // Space between the image and the glow
                    .drawBehind {
                        // Create a glowing gradient circle behind the image
                        val glowRadius = size.minDimension / 2 + 80.dp.toPx() // Radius of the glow

                        // Create a gradient that fades from the shadow color to transparent
                        val brush = Brush.radialGradient(
                            colors = listOf(
                                shadowColor.copy(alpha = 0.8f), // Inner glowing shadow color
                                shadowColor.copy(alpha = 0f) // Fade to transparent
                            ),
                            center = center, // Gradient center is the image center
                            radius = glowRadius // Radius of the gradient
                        )

                        // Draw the gradient behind the image
                        drawCircle(
                            brush = brush,
                            radius = glowRadius,
                            center = center
                        )
                    }
            ) {
                // The image itself
                Image(
                    painter = painterResource(id = imageResourceId),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize() // Fill the Box with the image
                )
            }

            // Text on the right
            Text(
                text = label,
                style = MaterialTheme.typography.bodyLarge.copy(fontSize = 18.sp), // Make text a bit bigger
                modifier = Modifier.weight(1f), // Make the text take the available space
                textAlign = TextAlign.End // Align text to the end (right)
            )
        }
    }
}

