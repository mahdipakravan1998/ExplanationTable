package com.example.explanationtable.ui.stages.content

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R

object StagesPlaceholderDefaults {
    // Single tunable size for all three bees.
    val BeeImageSize: Dp = 140.dp

    // Extra bottom padding to push the whole group upward.
    // Increase/decrease this to move all bees higher/lower.
    val VerticalShiftUp: Dp = 64.dp
}

/**
 * Static, branded placeholder for the stages list.
 *
 * Shows three bees in a top-right / middle-left / bottom-right zigzag,
 * spaced to fill the viewport vertically.
 *
 * - Uses StageListDefaults.ListVerticalPadding as base vertical padding,
 *   with extra bottom padding to shift the group upward.
 * - Uses StageListDefaults.SideImageEdgePadding for horizontal padding.
 * - No animations or timers; purely static UI.
 */
@Composable
fun StagesListPlaceholderZigZagBees(
    modifier: Modifier = Modifier,
    imageSize: Dp = StagesPlaceholderDefaults.BeeImageSize
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(
                start = StageListDefaults.SideImageEdgePadding,
                end = StageListDefaults.SideImageEdgePadding,
                top = StageListDefaults.ListVerticalPadding,
                bottom = StageListDefaults.ListVerticalPadding + StagesPlaceholderDefaults.VerticalShiftUp
            ),
        verticalArrangement = Arrangement.SpaceEvenly
    ) {
        // Top-right: engineer bee
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Top
        ) {
            PlaceholderBee(
                resId = R.drawable.char_bee_engineer,
                contentDescription = "Engineer bee",
                size = imageSize
            )
        }

        // Middle-left: builder bee
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            PlaceholderBee(
                resId = R.drawable.char_bee_builder,
                contentDescription = "Builder bee",
                size = imageSize
            )
        }

        // Bottom-right: painter bee
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
            verticalAlignment = Alignment.Bottom
        ) {
            PlaceholderBee(
                resId = R.drawable.char_bee_painter,
                contentDescription = "Painter bee",
                size = imageSize
            )
        }
    }
}

@Composable
private fun PlaceholderBee(
    resId: Int,
    contentDescription: String,
    size: Dp
) {
    Image(
        painter = painterResource(id = resId),
        contentDescription = contentDescription,
        modifier = Modifier.size(size),
        contentScale = ContentScale.Fit
    )
}
