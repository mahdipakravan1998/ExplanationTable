package com.example.explanationtable.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// Set of Material typography styles to start with
val Typography = Typography(
    bodyLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.5.sp
    )
    /* Other default text styles to override
    titleLarge = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Normal,
        fontSize = 22.sp,
        lineHeight = 28.sp,
        letterSpacing = 0.sp
    ),
    labelSmall = TextStyle(
        fontFamily = FontFamily.Default,
        fontWeight = FontWeight.Medium,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
    */
)

// Add a custom text style for "bodyBoldLarge"
val Typography.bodyBoldLarge: TextStyle
    get() = TextStyle(
        fontFamily = FontFamily.Default,  // Keep default font family
        fontWeight = FontWeight.Bold,     // Bold weight for emphasis
        fontSize = 20.sp,                 // Larger font size
        lineHeight = 28.sp,               // Adjust line height for readability
        letterSpacing = 0.sp              // No additional spacing
    )