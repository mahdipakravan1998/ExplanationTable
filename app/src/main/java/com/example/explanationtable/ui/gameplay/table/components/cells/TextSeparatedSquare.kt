package com.example.explanationtable.ui.gameplay.table.components.cells

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.ui.gameplay.table.components.cells.utils.AutoResizingText
import com.example.explanationtable.ui.theme.Bee
import com.example.explanationtable.ui.theme.VazirmatnFontFamily

/**
 * A 80×80 square that shows [topText], a divider, and [bottomText], each
 * trying to fit on two lines first, then resizing if necessary.
 */
@Composable
fun TextSeparatedSquare(
    topText: String,
    bottomText: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier.size(80.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(16.dp))
                .background(color = Bee)
                .padding(8.dp)
        ) {
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Top text
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizingText(
                        text = topText,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF5E3700),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2,  // first try 2 lines
                        minTextSize = 10.sp,
                        maxTextSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirmatnFontFamily
                    )
                }

                // Divider
                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .padding(vertical = 4.dp),
                    color = Color(0xFF5E3700),
                    thickness = 1.dp
                )

                // Bottom text
                Box(
                    modifier = Modifier
                        .weight(1f, fill = true),
                    contentAlignment = Alignment.Center
                ) {
                    AutoResizingText(
                        text = bottomText,
                        modifier = Modifier.fillMaxSize(),
                        color = Color(0xFF5E3700),
                        fontWeight = FontWeight.Bold,
                        maxLines = 2, // first try 2 lines
                        minTextSize = 10.sp,
                        maxTextSize = 16.sp,
                        textAlign = TextAlign.Center,
                        fontFamily = VazirmatnFontFamily
                    )
                }
            }
        }
    }
}