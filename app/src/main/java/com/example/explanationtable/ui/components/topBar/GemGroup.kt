package com.example.explanationtable.ui.components.topBar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.AppTypography
import com.example.explanationtable.ui.theme.White
import com.example.explanationtable.utils.toPersianDigits

// Constant representing the maximum gem count to display before using a shorthand.
private const val MAX_DISPLAYABLE_GEMS = 999

// Constants for icon dimensions and spacing.
private val GEM_ICON_SIZE = 24.dp
private val GEM_ICON_SPACING = 6.dp

/**
 * A composable that displays a gem icon alongside its count.
 *
 * If the provided gem count exceeds [MAX_DISPLAYABLE_GEMS], the count is formatted in a shorthand style,
 * displaying a "+" sign followed by the maximum count (converted to Persian digits).
 * Otherwise, the actual gem count is displayed in Persian digits.
 *
 * @param gems The current gem count.
 * @param modifier Modifier for styling and layout adjustments.
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
        // Display the gem icon using a painter resource and the defined size.
        Image(
            painter = painterResource(id = R.drawable.ic_gem),
            contentDescription = "Gem Icon",
            modifier = Modifier.size(GEM_ICON_SIZE)
        )

        // Add horizontal spacing between the icon and the gem count text.
        Spacer(modifier = Modifier.width(GEM_ICON_SPACING))

        // Format the gem count:
        // If the gem count exceeds the maximum, display a shorthand format with a "+" sign.
        // Otherwise, display the actual count converted to Persian digits.
        val displayGems = if (gems > MAX_DISPLAYABLE_GEMS) {
            "+${MAX_DISPLAYABLE_GEMS.toPersianDigits()}"
        } else {
            gems.toPersianDigits().toString()
        }

        // Render the formatted gem count using a predefined typography style.
        Text(
            text = displayGems,
            style = AppTypography.titleLarge.copy(
                color = White,
                textAlign = TextAlign.Center,
                fontSize = 18.sp
            )
        )
    }
}
