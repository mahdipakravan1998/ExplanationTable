package com.example.explanationtable.ui.system

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import android.view.View
import android.view.ViewParent
import android.view.ViewTreeObserver
import android.view.Window
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.window.DialogWindowProvider
import androidx.core.view.ViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver

/**
 * App-wide edge-to-edge system bar effect.
 *
 * Apply this ONCE near the top of your app (e.g., in MainActivity's setContent {}),
 * after you know the current theme so icons can be set for contrast.
 *
 * Behavior:
 * - Status & navigation bars remain VISIBLE.
 * - Their backgrounds are FULLY TRANSPARENT so the app background shows through.
 * - Content receives real system-bar insets (not consumed).
 * - Draws into the display cutout.
 * - Re-applies on ON_RESUME and window focus gain.
 * - Restores defaults when leaving composition (activity root case).
 *
 * This is a side-effect only; it renders no UI.
 *
 * Implementation notes:
 * - Listeners are registered once and kept stable (DisposableEffect on activity/lifecycleOwner).
 * - Theme flips are handled by LaunchedEffect + rememberUpdatedState to avoid listener churn.
 */
@Composable
fun AppEdgeToEdgeSystemBars(isDarkTheme: Boolean) {
    val context = LocalContext.current
    val activity = context.findActivity() ?: return
    val lifecycleOwner = LocalLifecycleOwner.current

    // Keep the latest theme value without re-registering listeners.
    val latestIsDarkTheme = rememberUpdatedState(isDarkTheme)
    val window: Window = activity.window

    // Apply immediately and whenever the theme changes, without re-attaching listeners.
    LaunchedEffect(window, latestIsDarkTheme.value) {
        ImmersiveUtils.applyEdgeToEdgeToWindow(
            window = window,
            isDarkTheme = latestIsDarkTheme.value
        )
    }

    // Register listeners once per window/lifecycleOwner; they always read latestIsDarkTheme.
    DisposableEffect(window, lifecycleOwner) {
        fun applyLatest() = ImmersiveUtils.applyEdgeToEdgeToWindow(
            window = window,
            isDarkTheme = latestIsDarkTheme.value
        )

        // Initial application (defensive: ensure first frame is correct even if LaunchedEffect is delayed).
        applyLatest()

        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) applyLatest()
        }

        val lifecycleObserver = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) applyLatest()
        }

        val decorView = window.decorView
        val vto = decorView.viewTreeObserver
        vto.addOnWindowFocusChangeListener(focusListener)
        lifecycleOwner.lifecycle.addObserver(lifecycleObserver)

        onDispose {
            // Remove hooks first to avoid late calls during teardown.
            vto.removeOnWindowFocusChangeListener(focusListener)
            lifecycleOwner.lifecycle.removeObserver(lifecycleObserver)

            // Restore defaults as we're leaving the Activity's composition.
            ImmersiveUtils.clearEdgeToEdgeFromWindow(window)
        }
    }
}

/**
 * Apply the same edge-to-edge (transparent bars, visible) behavior to a Compose Dialog/AlertDialog window.
 *
 * Usage:
 * ```
 * AlertDialog(
 *   onDismissRequest = onDismiss,
 *   title = {
 *     ImmersiveForDialog() // place early inside any slot to ensure early application
 *     Text("Title")
 *   },
 * )
 * ```
 *
 * Notes:
 * - Finds the dialog Window via [DialogWindowProvider].
 * - Applies edge-to-edge with icon contrast based on the current system night mode.
 * - On dispose: removes listeners and clears only our insets listener.
 * - Listeners remain stable; theme flips re-apply visual state without re-registering.
 */
@Composable
fun ImmersiveForDialog() {
    val localView = LocalView.current
    val context = LocalContext.current

    // Infer dark theme for icon contrast from system night mode (good compromise for dialogs).
    val derivedIsDarkTheme =
        (context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES
    val latestIsDarkTheme = rememberUpdatedState(derivedIsDarkTheme)

    // Re-apply when theme changes (window may appear slightly after composition, so we re-find it).
    LaunchedEffect(localView, latestIsDarkTheme.value) {
        findDialogWindowFromView(localView)?.let { dlgWindow ->
            ImmersiveUtils.applyEdgeToEdgeToWindow(
                window = dlgWindow,
                isDarkTheme = latestIsDarkTheme.value
            )
        }
    }

    DisposableEffect(localView) {
        // Discover the Dialog's Window by walking up the view-parent chain.
        val dialogWindow = findDialogWindowFromView(localView)
            ?: return@DisposableEffect onDispose { /* preview/non-dialog host: no-op */ }

        fun applyLatest() = ImmersiveUtils.applyEdgeToEdgeToWindow(
            window = dialogWindow,
            isDarkTheme = latestIsDarkTheme.value
        )

        // Apply immediately so the dialog appears edge-to-edge from first frame.
        applyLatest()

        // Re-apply when the dialog window regains focus.
        val focusListener = ViewTreeObserver.OnWindowFocusChangeListener { hasFocus ->
            if (hasFocus) applyLatest()
        }
        val decorView = dialogWindow.decorView
        val vto = decorView.viewTreeObserver
        vto.addOnWindowFocusChangeListener(focusListener)

        onDispose {
            vto.removeOnWindowFocusChangeListener(focusListener)
            // Clear only our insets listener to prevent leaks.
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
    (root.parent as? DialogWindowProvider)?.let { return it.window }

    var current: ViewParent? = root.parent
    while (current != null) {
        if (current is DialogWindowProvider) return current.window
        current = current.parent
    }
    return null
}
