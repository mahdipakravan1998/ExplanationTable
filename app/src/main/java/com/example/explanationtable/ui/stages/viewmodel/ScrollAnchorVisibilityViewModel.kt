package com.example.explanationtable.ui.stages.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.*

class ScrollAnchorVisibilityViewModel : ViewModel() {
    private val scrollOffsetFlow = MutableStateFlow(0)
    private val viewportHeightFlow = MutableStateFlow(0)
    private val itemHeightFlow = MutableStateFlow(0)
    private val totalTopPaddingFlow = MutableStateFlow(0)
    private val unlockedStageFlow = MutableStateFlow(1)

    /**
     * Emits `true` when the unlocked stage item is outside the visible window,
     * i.e., when the ScrollAnchor should be shown.
     */
    val showScrollAnchor: StateFlow<Boolean> = combine(
        scrollOffsetFlow,
        viewportHeightFlow,
        itemHeightFlow,
        totalTopPaddingFlow,
        unlockedStageFlow
    ) { scrollOffset, viewportHeight, itemHeight, totalTopPadding, unlockedStage ->
        val itemTop = totalTopPadding + (unlockedStage - 1) * itemHeight
        val itemBottom = itemTop + itemHeight
        val visible = (scrollOffset <= itemBottom) && (scrollOffset + viewportHeight >= itemTop)
        !visible
    }.stateIn(viewModelScope, SharingStarted.Eagerly, false)

    /**
     * Update scroll and layout parameters for visibility calculation.
     *
     * @param scrollOffset current scroll offset in px
     * @param viewportHeight height of the viewport in px
     * @param itemHeight height of one item (container + padding) in px
     * @param totalTopPadding combined top padding of the list in px
     * @param unlockedStage 1-based index of the unlocked stage
     */
    fun updateParams(
        scrollOffset: Int,
        viewportHeight: Int,
        itemHeight: Int,
        totalTopPadding: Int,
        unlockedStage: Int
    ) {
        scrollOffsetFlow.value = scrollOffset
        viewportHeightFlow.value = viewportHeight
        itemHeightFlow.value = itemHeight
        totalTopPaddingFlow.value = totalTopPadding
        unlockedStageFlow.value = unlockedStage
    }
}
