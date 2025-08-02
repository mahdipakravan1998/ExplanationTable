package com.example.explanationtable.ui.stages.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density

/**
 * Calculates the vertical scroll offset (in pixels) needed to bring the unlocked stage
 * button to the vertical center of a scrolling Column.
 *
 * @param unlockedStage 1-based index of the last-unlocked stage in the list.
 * @param viewportHeightPx Height of the scrolling Column viewport, in pixels.
 * @param density Current [Density] for Dp ↔ px conversions.
 * @param buttonHeightDp Height of each stage button container, in Dp.
 * @param buttonPaddingDp Vertical padding around each button, in Dp.
 * @param listPaddingDp Vertical padding applied to the top and bottom of the list, in Dp.
 * @return Scroll offset in pixels; positive values scroll downward.
 */
fun computeCenterOffset(
    unlockedStage: Int,
    viewportHeightPx: Int,
    density: Density,
    buttonHeightDp: Dp,
    buttonPaddingDp: Dp,
    listPaddingDp: Dp
): Int = with(density) {
    // 1. Compute total item height (button + its top & bottom padding) in pixels
    val itemTotalHeightPx = (buttonHeightDp + buttonPaddingDp * 2).toPx()

    // 2. Compute total vertical padding for the entire list (top + bottom) in pixels
    val totalListPaddingPx = listPaddingDp.toPx() * 2

    // 3. Convert unlockedStage to a zero-based index
    val targetIndex = (unlockedStage - 1).coerceAtLeast(0)

    // 4. Determine the center Y-position of the target item relative to the top of the list
    val targetCenterPx = totalListPaddingPx +
            targetIndex * itemTotalHeightPx +
            itemTotalHeightPx / 2f

    // 5. Offset so that this center aligns with the center of the viewport
    val viewportCenterPx = viewportHeightPx / 2f

    // 6. Final scroll offset: positive = scroll down, negative = scroll up
    (targetCenterPx - viewportCenterPx).toInt()
}
