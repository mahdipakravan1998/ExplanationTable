package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update

/**
 * ViewModel that tracks scroll/layout parameters and exposes:
 *  - showScrollAnchor: whether the unlocked stage is entirely out of view
 *  - isStageAbove: whether the unlocked stage is scrolled past above the viewport
 */
class ScrollAnchorVisibilityViewModel : ViewModel() {

    // Bundle all inputs into one data class to simplify combine logic
    private data class ScrollParams(
        val scrollOffset: Int = 0,
        val viewportHeight: Int = 0,
        val itemHeight: Int = 0,
        val totalTopPadding: Int = 0,
        val unlockedStage: Int = 1
    )

    // Backing StateFlow holding the latest scroll parameters
    private val paramsFlow = MutableStateFlow(ScrollParams())

    /**
     * Emits `true` when the unlocked stage item is completely outside the visible window,
     * so the scroll-anchor should be shown.
     */
    val showScrollAnchor: StateFlow<Boolean> =
        paramsFlow
            // Compute visibility for each update
            .map { p ->
                // Calculate top/bottom y-positions of the unlocked item
                val itemTop = p.totalTopPadding + (p.unlockedStage - 1) * p.itemHeight
                val itemBottom = itemTop + p.itemHeight

                // Check if any part of the item intersects the viewport
                val isVisible = p.scrollOffset <= itemBottom &&
                        p.scrollOffset + p.viewportHeight >= itemTop

                // Anchor is shown when the item is not visible
                !isVisible
            }
            // Keep the latest boolean in a StateFlow tied to the VM’s scope
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

    /**
     * Emits `true` when the unlocked stage item is scrolled completely above the viewport,
     * indicating the “down” arrow should be used.
     */
    val isStageAbove: StateFlow<Boolean> =
        paramsFlow
            .map { p ->
                // Compare scrollOffset to the bottom edge of the unlocked item
                p.scrollOffset > (p.totalTopPadding + p.unlockedStage * p.itemHeight)
            }
            .stateIn(viewModelScope, SharingStarted.Eagerly, initialValue = false)

    /**
     * Update all scroll and layout parameters at once.
     *
     * @param scrollOffset   Current scroll offset in pixels.
     * @param viewportHeight Height of the viewport in pixels.
     * @param itemHeight     Height of one item (including padding) in pixels.
     * @param totalTopPadding Combined top padding of the list in pixels.
     * @param unlockedStage  1-based index of the unlocked stage.
     */
    fun updateParams(
        scrollOffset: Int,
        viewportHeight: Int,
        itemHeight: Int,
        totalTopPadding: Int,
        unlockedStage: Int
    ) {
        // Atomically update only the changed fields
        paramsFlow.update { current ->
            current.copy(
                scrollOffset = scrollOffset,
                viewportHeight = viewportHeight,
                itemHeight = itemHeight,
                totalTopPadding = totalTopPadding,
                unlockedStage = unlockedStage
            )
        }
    }
}
