package com.example.explanationtable.ui.system

import android.app.Activity
import android.content.Context
import android.view.View
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * App-wide immersive-mode effect.
 *
 * Apply this ONCE near the top of your app (e.g., in MainActivity's setContent {}).
 *
 * Behavior (unchanged):
 * - Hides status & navigation bars and allows transient reveal by swipe.
 * - Draws into the display cutout.
 * - Consumes system-bar insets (including ignoring-visibility) so no layout gaps are reserved.
 * - Re-applies immersive on ON_RESUME and on window focus gain.
 * - Restores defaults when leaving composition (activity root case).
 *
 * This is a side-effect only; it renders no UI.
 */
@Composable
fun AppImmersiveSystemBars() {
    val context = LocalContext.current
    val activity = context.findActivity()
    val lifecycleOwner = LocalLifecycleOwner.current

    // If there's no Activity (e.g., preview), do nothing safely.
    if (activity == null) return

    DisposableEffect(activity, lifecycleOwner) {
        val window: Window = activity.window

        fun apply() = ImmersiveUtils.applyImmersiveToWindow(window)
        fun clear() = ImmersiveUtils.clearImmersiveFromWindow(window)

        // Re-apply immersive when the window regains focus (e.g., after system UI transient show).
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) apply()
        }

        // Re-apply immersive on resume. Do not clear on pause to avoid visual gaps/flicker.
        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) apply()
        }

        // Initial application.
        apply()

        val decorView = window.decorView
        val vto = decorView.viewTreeObserver
        vto.addOnWindowFocusChangeListener(focusListener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            // Remove hooks first to avoid late calls during teardown.
            decorView.viewTreeObserver.removeOnWindowFocusChangeListener(focusListener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)
            // Restore defaults as we're leaving the Activity's composition.
            clear()
        }
    }
}

/**
 * Apply the same immersive behavior to a Compose Dialog/AlertDialog window.
 *
 * Usage:
 * ```
 * AlertDialog(
 *   onDismissRequest = onDismiss,
 *   title = {
 *     ImmersiveForDialog() // place first inside any slot to ensure early application
 *     Text("Title")
 *   },
 * )
 * ```
 *
 * What it does (unchanged behavior):
 * - Finds the dialog Window via [DialogWindowProvider].
 * - Applies immersive settings immediately and re-applies on dialog window focus gain.
 * - On dispose: removes listeners and clears only the insets listener we attached
 *   (does NOT force-show bars to avoid flicker on dismiss).
 */
@Composable
fun ImmersiveForDialog() {
    val localView = LocalView.current

    DisposableEffect(localView) {
        // Robustly discover the Dialog's Window by walking up the view-parent chain.
        val dialogWindow = findDialogWindowFromView(localView)
            ?: return@DisposableEffect onDispose { /* no-op for non-dialog hosts (e.g., preview) */ }

        fun apply() = ImmersiveUtils.applyImmersiveToWindow(dialogWindow)

        // Apply immediately so the dialog appears immersive from first frame.
        apply()

        // Re-apply when the dialog window regains focus.
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) apply()
        }
        val decorView = dialogWindow.decorView
        val vto = decorView.viewTreeObserver
        vto.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            vto.removeOnWindowFocusChangeListener(focusListener)
            // Clear only our insets listener to prevent leaks without forcing bars to show.
            ViewCompat.setOnApplyWindowInsetsListener(decorView, null)
        }
    }
}

/**
 * Walks up a [Context] chain to find an [Activity], or null if none is present.
 * Safer than casting LocalContext.current to Activity.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is android.content.ContextWrapper -> baseContext.findActivity()
    else -> null
}

/**
 * Traverse up from a child [View] to find a [DialogWindowProvider] and return its [Window].
 * This is more robust than assuming the immediate parent is a [DialogWindowProvider].
 */
private fun findDialogWindowFromView(root: View): Window? {
    // Handle the immediate parent fast-path.
    (root.parent as? DialogWindowProvider)?.let { return it.window }

    // Fallback: walk the parent chain defensively.
    var current: ViewParent? = root.parent
    while (current != null) {
        if (current is DialogWindowProvider) return current.window
        current = current.parent
    }
    return null
}
