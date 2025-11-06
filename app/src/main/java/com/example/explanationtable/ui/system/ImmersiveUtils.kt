package com.example.explanationtable.ui.system

import android.os.Build
import android.view.Window
import android.view.WindowManager
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * Reusable immersive-mode utilities for both Activity and Dialog windows.
 *
 * Use [applyImmersiveToWindow] to:
 * - Draw content edge-to-edge (including display cutout).
 * - Hide status & navigation bars with transient reveal by swipe.
 * - **Consume** system-bar insets (including ignoring-visibility) so no layout space is reserved.
 * - Leave IME insets intact so keyboards continue to work properly.
 *
 * Use [clearImmersiveFromWindow] when tearing down an Activity root window to restore defaults.
 * (For dialogs, simply dismissing the dialog is enough; do not force-show bars.)
 */
object ImmersiveUtils {

    // Precompute constants to avoid per-dispatch allocations in the insets listener.
    private val ZERO_INSETS: Insets = Insets.of(0, 0, 0, 0)
    private val BARS_TYPES: Int by lazy {
        WindowInsetsCompat.Type.statusBars() or
                WindowInsetsCompat.Type.navigationBars() or
                WindowInsetsCompat.Type.displayCutout()
    }

    /**
     * Apply immersive behavior to [window].
     *
     * Notes:
     * - Only status/navigation/cutout insets are consumed (set to zero). IME remains intact.
     * - Transient bars can still be revealed by user swipe; content will not reflow.
     */
    fun applyImmersiveToWindow(window: Window) {
        // Edge-to-edge.
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // Allow content under the notch / cutout.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.attributes = window.attributes.apply {
                layoutInDisplayCutoutMode =
                    WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
            }
        }

        // Hide status & nav bars; allow transient reveal.
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        controller.hide(WindowInsetsCompat.Type.systemBars())

        // Consume ONLY system bars + cutout; IME remains untouched.
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            WindowInsetsCompat.Builder(insets)
                .setInsets(BARS_TYPES, ZERO_INSETS)
                .setInsetsIgnoringVisibility(BARS_TYPES, ZERO_INSETS)
                .build()
        }

        // Dispatch immediately so changes take effect without extra frames.
        window.decorView.requestApplyInsets()
    }

    /**
     * Restore default inset dispatch & visible bars on [window].
     *
     * Intended for Activity windows when leaving composition or finishing.
     * Avoid calling this for dialog windows (just dismiss the dialog instead).
     */
    fun clearImmersiveFromWindow(window: Window) {
        // Remove our insets interception.
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)

        // Show system bars and return to default layout.
        val controller = WindowInsetsControllerCompat(window, window.decorView)
        controller.show(WindowInsetsCompat.Type.systemBars())
        WindowCompat.setDecorFitsSystemWindows(window, true)

        window.decorView.requestApplyInsets()
    }
}
