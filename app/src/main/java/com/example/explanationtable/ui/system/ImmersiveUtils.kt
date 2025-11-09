package com.example.explanationtable.ui.system

import android.graphics.Color
import android.os.Build
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat

/**
 * System bar utilities (edge-to-edge).
 *
 * Use [applyEdgeToEdgeToWindow] to:
 * - Keep status/navigation bars VISIBLE with FULLY TRANSPARENT backgrounds.
 * - Draw content edge-to-edge (including display cutout).
 * - Do NOT consume system-bar insets (content can pad/offset using insets).
 * - Set icon contrast (light/dark) based on theme.
 *
 * Use [clearEdgeToEdgeFromWindow] when tearing down an Activity root window to restore defaults.
 */
object ImmersiveUtils {

    private const val TAG: String = "ImmersiveUtils"

    /**
     * Apply edge-to-edge (transparent, visible bars) to [window].
     *
     * @param isDarkTheme If true, we render light system icons; if false, dark icons.
     */
    @JvmStatic
    fun applyEdgeToEdgeToWindow(window: Window, isDarkTheme: Boolean) {
        try {
            // Let our content draw behind the system bars.
            WindowCompat.setDecorFitsSystemWindows(window, false)

            // Allow content under the notch / cutout.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                window.attributes = window.attributes.apply {
                    layoutInDisplayCutoutMode =
                        WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
                }
            }

            // Make system bars fully transparent so the app background shows through.
            window.statusBarColor = Color.TRANSPARENT
            window.navigationBarColor = Color.TRANSPARENT

            // Disable the gray contrast scrim on some devices/versions for transparent nav bar.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                window.isNavigationBarContrastEnforced = false
            }

            val controller = WindowInsetsControllerCompat(window, window.decorView)

            // Ensure bars are visible (we're not hiding them).
            controller.show(WindowInsetsCompat.Type.systemBars())

            // Icon contrast: dark icons for light theme, light icons for dark theme.
            // (Appearance "light" means dark icons on a light background.)
            controller.isAppearanceLightStatusBars = !isDarkTheme
            controller.isAppearanceLightNavigationBars = !isDarkTheme

            // We no longer intercept/consume insets at the window level; let them flow to Compose.
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)

            // Dispatch immediately.
            window.decorView.requestApplyInsets()
        } catch (t: Throwable) {
            // Defensive: do not crash on rare OEM issues; log for diagnosis.
            Log.w(TAG, "applyEdgeToEdgeToWindow failed: ${t.message}", t)
        }
    }

    /**
     * Restore default inset dispatch & a non edge-to-edge layout on [window].
     *
     * Intended for Activity windows when leaving composition or finishing.
     */
    @JvmStatic
    fun clearEdgeToEdgeFromWindow(window: Window) {
        try {
            // Remove any insets interception we might have attached.
            ViewCompat.setOnApplyWindowInsetsListener(window.decorView, null)

            // Return to default layout behavior.
            val controller = WindowInsetsControllerCompat(window, window.decorView)
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
            controller.show(WindowInsetsCompat.Type.systemBars())

            WindowCompat.setDecorFitsSystemWindows(window, true)

            window.decorView.requestApplyInsets()
        } catch (t: Throwable) {
            // Defensive: do not crash on rare OEM issues; log for diagnosis.
            Log.w(TAG, "clearEdgeToEdgeFromWindow failed: ${t.message}", t)
        }
    }
}
