package com.example.explanationtable.ui.stages.preflight

import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.math.max

private const val TAG_READY = "StagesReady"

/**
 * Discrete readiness signals for the stages list preflight.
 */
enum class ReadySignal {
    StageDataReady,
    ViewportMeasured,
    TargetOffsetComputed,
    InitialSnapSettled,
    BubbleCalibrated,
    VisualsSettled
}

/**
 * Immutable snapshot of the stages list readiness state.
 *
 * Derived flags:
 * - [minimallyReady]: "good enough to show" – data + geometry + settled first center.
 * - [ready]: everything is fully calibrated (including bubble and visuals).
 */
data class ReadySnapshot(
    val stageDataReady: Boolean = false,
    val viewportMeasured: Boolean = false,
    val viewportHeightPx: Int = 0,
    val targetOffsetComputed: Boolean = false,
    val targetOffsetPx: Int = 0,
    val initialSnapSettled: Boolean = false,
    val bubbleCalibrated: Boolean = false,
    val visualsSettled: Boolean = false
) {
    /** Minimal readiness = data + geometry + first-centering done. */
    val minimallyReady: Boolean
        get() = stageDataReady &&
                viewportMeasured &&
                targetOffsetComputed &&
                initialSnapSettled

    /** Full readiness = minimal + bubble + visuals. */
    val ready: Boolean
        get() = minimallyReady &&
                bubbleCalibrated &&
                visualsSettled
}

/**
 * Readiness tracker for the stages list.
 */
interface ReadyTracker {

    val snapshot: StateFlow<ReadySnapshot>

    fun markStageDataReady()
    fun setViewportHeight(px: Int)
    fun setTargetOffset(px: Int)
    fun markInitialSnapSettled()
    fun markBubbleCalibrated()
    fun markVisualsSettled()
}

/**
 * Default, in-memory implementation of [ReadyTracker].
 */
class MutableReadyTracker(
    private val initialSnapshot: ReadySnapshot = ReadySnapshot()
) : ReadyTracker {

    private val _snapshot = MutableStateFlow(initialSnapshot)

    override val snapshot: StateFlow<ReadySnapshot> = _snapshot.asStateFlow()

    /** Convenience access to derived minimal-ready flag. */
    val isMinimallyReady: Boolean
        get() = snapshot.value.minimallyReady

    override fun markStageDataReady() {
        updateSnapshot { current ->
            if (current.stageDataReady) current else current.copy(stageDataReady = true)
        }
    }

    override fun setViewportHeight(px: Int) {
        updateSnapshot { current ->
            val measured = px > 0
            current.copy(
                viewportMeasured = measured,
                viewportHeightPx = px
            )
        }
    }

    override fun setTargetOffset(px: Int) {
        updateSnapshot { current ->
            // Clamp negative offsets to 0 and ALWAYS treat as "computed".
            // This fixes the MEDIUM case where target was -640 and
            // targetOffsetComputed stayed false, causing endless waiting.
            val clamped = max(px, 0)
            current.copy(
                targetOffsetComputed = true,
                targetOffsetPx = clamped
            )
        }
    }

    override fun markInitialSnapSettled() {
        updateSnapshot { current ->
            if (current.initialSnapSettled) current else current.copy(initialSnapSettled = true)
        }
    }

    override fun markBubbleCalibrated() {
        updateSnapshot { current ->
            if (current.bubbleCalibrated) current else current.copy(bubbleCalibrated = true)
        }
    }

    override fun markVisualsSettled() {
        updateSnapshot { current ->
            if (current.visualsSettled) current else current.copy(visualsSettled = true)
        }
    }

    fun reset() {
        Log.d(TAG_READY, "reset() → $initialSnapshot")
        _snapshot.value = initialSnapshot
    }

    fun forceMinimalReady() {
        updateSnapshot { current ->
            val safeViewport = max(current.viewportHeightPx, 0)
            val safeTarget = max(current.targetOffsetPx, 0)
            val forced = ReadySnapshot(
                stageDataReady = true,
                viewportMeasured = true,
                viewportHeightPx = safeViewport,
                targetOffsetComputed = true,
                targetOffsetPx = safeTarget,
                initialSnapSettled = true,
                bubbleCalibrated = true,
                visualsSettled = true
            )
            Log.w(TAG_READY, "forceMinimalReady() → $forced (was=$current)")
            forced
        }
    }

    private inline fun updateSnapshot(block: (ReadySnapshot) -> ReadySnapshot) {
        val current = _snapshot.value
        val updated = block(current)
        if (updated != current) {
            _snapshot.value = updated
            Log.d(TAG_READY, "Snapshot updated: $updated")
        }
    }
}

/**
 * Lightweight callbacks for UI → tracker wiring.
 */
data class ReadinessHooks(
    val onStageDataReady: (() -> Unit)? = null,
    val onViewportMeasured: ((Int) -> Unit)? = null,
    val onTargetOffsetComputed: ((Int) -> Unit)? = null,
    val onInitialSnapSettled: (() -> Unit)? = null,
    val onBubbleCalibrated: (() -> Unit)? = null,
    val onVisualsSettled: (() -> Unit)? = null
)
