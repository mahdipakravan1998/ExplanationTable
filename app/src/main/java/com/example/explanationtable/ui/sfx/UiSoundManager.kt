package com.example.explanationtable.ui.sfx

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.annotation.RawRes
import androidx.compose.runtime.staticCompositionLocalOf
import com.example.explanationtable.R

/**
 * Lightweight, global UI SFX controller backed by SoundPool.
 *
 * - Preloads all short UI sounds at construction.
 * - All playX() methods are non-suspending and cheap (no I/O, no allocations).
 * - Respects a volatile in-memory mute flag updated via [updateMute].
 *
 * Threading & lifecycle:
 * - Safe to call playX() from the main thread on every click.
 * - The owning scope (e.g., Activity root composition) is responsible for calling [release]
 *   exactly once when the app UI is torn down.
 */
class UiSoundManager(context: Context) {

    private val soundPool: SoundPool

    private val clickSoundId: Int
    private val rewardDiamondSoundId: Int
    private val stageCompleteSoundId: Int
    private val stageScoreSoundId: Int
    private val swapGridSoundId: Int
    private val swapSuccessSoundId: Int

    @Volatile
    private var isMutedInternal: Boolean = false

    init {
        val audioAttributes = AudioAttributes.Builder()
            .setUsage(AudioAttributes.USAGE_GAME)
            .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
            .build()

        soundPool = SoundPool.Builder()
            .setAudioAttributes(audioAttributes)
            .setMaxStreams(MAX_STREAMS)
            .build()

        // Preload all UI SFX from res/raw. These are tiny and load quickly.
        clickSoundId = load(context, R.raw.sfx_click_ui)
        rewardDiamondSoundId = load(context, R.raw.sfx_reward_diamond)
        stageCompleteSoundId = load(context, R.raw.sfx_stage_complete)
        stageScoreSoundId = load(context, R.raw.sfx_stage_score)
        swapGridSoundId = load(context, R.raw.sfx_swap_grid)
        swapSuccessSoundId = load(context, R.raw.sfx_swap_success)
    }

    /**
     * Mirror of the global mute state.
     *
     * Called from a single, lifecycle-aware place (e.g., Activity root composition)
     * whenever the DataStore-backed mute flag changes.
     */
    fun updateMute(isMuted: Boolean) {
        isMutedInternal = isMuted
    }

    /** Release underlying SoundPool resources. Must be called exactly once by the owner. */
    fun release() {
        soundPool.release()
    }

    // --- Public play helpers (all are O(1), non-suspending, and main-thread safe) ---

    fun playClick() {
        playInternal(clickSoundId)
    }

    fun playRewardDiamond() {
        playInternal(rewardDiamondSoundId)
    }

    fun playStageComplete() {
        playInternal(stageCompleteSoundId)
    }

    fun playStageScore() {
        playInternal(stageScoreSoundId)
    }

    fun playSwapGrid() {
        playInternal(swapGridSoundId)
    }

    fun playSwapSuccess() {
        playInternal(swapSuccessSoundId)
    }

    // --- Internals ---

    private fun load(context: Context, @RawRes resId: Int): Int {
        return soundPool.load(context, resId, /* priority */ 1)
    }

    private fun playInternal(soundId: Int) {
        if (isMutedInternal || soundId == 0) return
        soundPool.play(
            soundId,
            /* leftVolume = */ 1f,
            /* rightVolume = */ 1f,
            /* priority = */ 1,
            /* loop = */ 0,
            /* rate = */ 1f
        )
    }

    private companion object {
        private const val MAX_STREAMS: Int = 6
    }
}

/**
 * CompositionLocal for accessing the global [UiSoundManager] from Composables.
 *
 * Provided once at the app root; any child Composable can obtain it via:
 *
 *   val uiSfx = LocalUiSoundManager.current
 *   uiSfx.playClick()
 */
val LocalUiSoundManager = staticCompositionLocalOf<UiSoundManager> {
    error("UiSoundManager not provided")
}
