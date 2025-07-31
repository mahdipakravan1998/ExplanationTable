package com.example.explanationtable.ui.stages.util

import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.Density

/**
 * Compute the scroll offset to center the unlocked stage item in the viewport.
 *
 * @param unlockedStage the 1-based index of the last-unlocked stage
 * @param columnHeightPx the height of the scrolling Column in pixels
 * @param density the current Density for converting Dp to pixels
 * @param buttonContainerSize the height of each stage button container in Dp
 * @param buttonVerticalPadding the vertical padding around each button in Dp
 * @param listVerticalPadding the vertical padding for the entire list in Dp
 * @return the scroll offset in pixels to center the target item
 */
fun computeCenterOffset(
    unlockedStage: Int,
    columnHeightPx: Int,
    density: Density,
    buttonContainerSize: Dp,
    buttonVerticalPadding: Dp,
    listVerticalPadding: Dp
): Int {
    // Compute the pixel height of one list item (container + padding)
    val itemHeightPx = with(density) {
        (buttonContainerSize + buttonVerticalPadding * 2).toPx()
    }
    // Total top padding applied: inner list padding * 2 (accounting for parent and child)
    val totalTopPaddingPx = with(density) {
        listVerticalPadding.toPx() * 2
    }
    // Index of the target item (0-based)
    val targetIndex = unlockedStage - 1
    // Center position of target item relative to top of the Column
    val targetCenterPx = totalTopPaddingPx + (targetIndex * itemHeightPx) + (itemHeightPx / 2f)
    // Scroll offset to bring target center into the viewport center
    return (targetCenterPx - columnHeightPx / 2f).toInt()
}
