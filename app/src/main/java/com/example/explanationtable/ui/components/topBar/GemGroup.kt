package com.example.explanationtable.ui.components.topBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.sp
import com.example.explanationtable.utils.toPersianDigits
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.White

/**
 * A composable representing a colored rectangle with a gem icon and a count.
 */
@Composable
fun GemGroup(
    gems: Int,
    modifier: Modifier = Modifier
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
    ) {
        // Load the SVG image
        Image(
            painter = painterResource(id = R.drawable.ic_gem),
            contentDescription = "Gem Icon",
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(6.dp))

        // Format the gems count
        val formattedGems = if (gems > 999) {
            "+${999.toPersianDigits()}"
        } else {
            gems.toPersianDigits().toString()
        }

        Text(
            text = formattedGems,
            style = AppTypography.titleLarge.copy(
                color = White,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        )
    }
}
