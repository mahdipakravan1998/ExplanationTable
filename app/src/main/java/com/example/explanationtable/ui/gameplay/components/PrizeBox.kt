package com.example.explanationtable.ui.gameplay.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringArrayResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.BackgroundDark
import com.example.explanationtable.ui.theme.CheckColorDark
import com.example.explanationtable.ui.theme.DarkGreenBackground
import com.example.explanationtable.ui.theme.DialogBackgroundLight
import com.example.explanationtable.ui.theme.IconCircleDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundDark
import com.example.explanationtable.ui.theme.PrizeButtonBackgroundLight
import com.example.explanationtable.ui.theme.SeaSponge
import com.example.explanationtable.ui.theme.TreeFrog

/**
 * A reusable composable for displaying title text with a predefined style.
 *
 * @param text The text to display.
 * @param modifier Optional [Modifier] for additional styling.
 * @param color The text color.
 * @param style The [TextStyle] applied to the text. Defaults to a large title style with
 *              bold weight and a font size of 20.sp.
 * @param textAlign Alignment of the text; defaults to [TextAlign.Start].
 */
@Composable
fun TitleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color,
    style: TextStyle = MaterialTheme.typography.titleLarge.copy(
        fontWeight = FontWeight.Black,
        fontSize = 20.sp
    ),
    textAlign: TextAlign = TextAlign.Start
) {
    // Render the text using the specified style, color, and alignment.
    Text(
        text = text,
        style = style,
        color = color,
        textAlign = textAlign,
        modifier = modifier
    )
}

/**
 * Displays a prize box containing a congratulatory message and an animated prize button.
 *
 * The component adapts its appearance based on the active theme.
 *
 * @param isDarkTheme Flag indicating whether the dark theme is active.
 * @param onPrizeButtonClick Callback invoked when the prize button is clicked.
 */
@Composable
fun PrizeBox(
    isDarkTheme: Boolean,
    onPrizeButtonClick: () -> Unit
) {
    // Retrieve an array of congratulatory texts from resources.
    val congratulatoryTexts = stringArrayResource(id = R.array.congratulatory_texts)
    // Select a random congratulatory text during the initial composition.
    val randomText = remember { congratulatoryTexts.random() }

    // Determine theme-specific colors.
    val boxBackgroundColor = if (isDarkTheme) DarkGreenBackground else SeaSponge
    val iconCircleColor = if (isDarkTheme) IconCircleDark else TreeFrog
    val checkColor = if (isDarkTheme) CheckColorDark else DialogBackgroundLight
    val messageTextColor = iconCircleColor

    val buttonBackgroundColor =
        if (isDarkTheme) PrizeButtonBackgroundDark else PrizeButtonBackgroundLight
    val buttonShadowColor = iconCircleColor
    val buttonTextColor = if (isDarkTheme) BackgroundDark else DialogBackgroundLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(boxBackgroundColor)
            .padding(vertical = 24.dp),
        horizontalAlignment = Alignment.End
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 32.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End
        ) {
            // Row containing the congratulatory text and an exclamation mark.
            Row(verticalAlignment = Alignment.CenterVertically) {
                TitleText(text = "!", color = messageTextColor)
                TitleText(text = randomText, color = messageTextColor)
            }
            Spacer(modifier = Modifier.width(12.dp))
            // Circular icon container displaying a check mark.
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .background(iconCircleColor, shape = CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_check),
                    contentDescription = "Check mark",
                    tint = checkColor,
                    modifier = Modifier.size(14.dp)
                )
            }
        }
        Spacer(modifier = Modifier.height(22.dp))
        // Display the animated prize button.
        AnimatedPrizeButton(
            onClick = onPrizeButtonClick,
            backgroundColor = buttonBackgroundColor,
            shadowColor = buttonShadowColor,
            textColor = buttonTextColor,
            modifier = Modifier.padding(horizontal = 32.dp)
        )
    }
}
