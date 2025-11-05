package com.example.explanationtable.ui.system

import android.app.Activity
import android.os.Build
import android.view.ViewTreeObserver
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * App-wide immersive-mode effect.
 *
 * Call this ONCE near the top of your app (e.g., in MainActivity's setContent {}).
 * - Hides status & navigation bars for the whole app.
 * - Draws into the display cutout.
 * - CONSUMES system-bar insets (including ignoring-visibility) so no empty space is left.
 * - Re-applies immersive on ON_RESUME and when the window regains focus.
 * - Restores defaults when leaving composition (e.g., activity finishes).
 */
@Composable
fun AppImmersiveSystemBars() {
    val context = LocalContext.current
    val activity = context as? Activity
    val lifecycleOwner = LocalLifecycleOwner.current

    DisposableEffect(activity, lifecycleOwner) {
        val window = activity?.window ?: return@DisposableEffect onDispose { /* nothing */ }
        val controller = WindowInsetsControllerCompat(window, window.decorView)

        fun applyImmersive() {
            // Edge-to-edge content
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Allow content under the notch / cutout
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            // Hide both status & navigation bars; swipe to reveal transiently
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            controller.hide(WindowInsetsCompat.Type.systemBars())

            // CONSUME insets so Compose doesn't reserve space even when bars are hidden.
            // Use "ignoring visibility" to handle OEMs that keep sizes non-zero.
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
                val types = WindowInsetsCompat.Type.statusBars() or
                        WindowInsetsCompat.Type.navigationBars() or
                        WindowInsetsCompat.Type.displayCutout()

                val builder = WindowInsetsCompat.Builder(insets)
                builder.setInsets(types, Insets.of(0, 0, 0, 0))
                builder.setInsetsIgnoringVisibility(types, Insets.of(0, 0, 0, 0))
                builder.build()
            }
            window.decorView.requestApplyInsets()
        }

        fun clearImmersive() {
            // Restore default inset dispatch and visible bars
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)
            controller.show(WindowInsetsCompat.Type.systemBars())
            WindowCompat.setDecorFitsSystemWindows(window, true)
            window.decorView.requestApplyInsets()
        }

        // Re-apply immersive when this window regains focus
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) applyImmersive()
        }

        // Re-apply immersive on resume; do NOT clear on pause to avoid residual gaps
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> applyImmersive()
                else -> Unit
            }
        }

        // Initial state
        applyImmersive()

        window.decorView.viewTreeObserver.addOnWindowFocusChangeListener(focusListener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            window.decorView.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            clearImmersive()
        }
    }
}
