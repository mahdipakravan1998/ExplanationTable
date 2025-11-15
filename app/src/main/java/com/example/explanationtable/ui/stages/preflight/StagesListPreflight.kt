package com.example.explanationtable.ui.stages.preflight

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.zIndex
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepositoryImpl
import com.example.explanationtable.ui.stages.content.StagesListContent
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private const val PREFLIGHT_TIMEOUT_MS: Long = 2_000L
// We want the loading state to be visible for roughly the same budget as the timeout.
// If preflight finishes early, we "fill" the remainder with a smooth animation.
private const val MIN_LOADING_VISIBLE_MS: Long = PREFLIGHT_TIMEOUT_MS
private const val TAG_PREFLIGHT = "StagesListPreflight"

/**
 * Invisible, full-size preflight runner for the stages list.
 *
 * It builds the stages list off-screen and waits until the list is "minimally ready":
 *  - stage data loaded
 *  - viewport measured
 *  - target offset computed
 *  - initial snap (centering) settled
 *
 * When that happens (or when timeout is reached), it calls [onPrepared].
 *
 * UX constraints:
 *  - The dialog's LoadingDots should be able to animate while this runs.
 *  - Even if preflight finishes very quickly, keep the loading visible for a short,
 *    noticeable period (bounded by [PREFLIGHT_TIMEOUT_MS]).
 */
@Composable
fun StagesListPreflight(
    difficulty: Difficulty,
    isDarkTheme: Boolean,
    onPrepared: () -> Unit
) {
    val context = LocalContext.current
    val componentActivity = context.findActivity() as? ComponentActivity

    val stageRepository = remember(context) {
        StageRepositoryImpl(DataStoreManager(context.applicationContext))
    }

    val stageViewModel: StageViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity, factory = StageViewModelFactory(stageRepository))
        } else {
            viewModel(factory = StageViewModelFactory(stageRepository))
        }

    // PROGRESS VM: same Activity owner as in StagesListPage so we share
    // unlocked-stage data between preflight and the visible screen.
    val progressViewModel: StageProgressViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity)
        } else {
            viewModel()
        }

    val preflightViewModel: StagesPreflightViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity)
        } else {
            viewModel()
        }

    val dummyNavController = rememberNavController()

    val scrollState: ScrollState = rememberSaveable(saver = ScrollState.Saver) {
        ScrollState(0)
    }

    // Per-(difficulty, theme) tracker and one-shot onPrepared guard
    val readyTracker = remember(difficulty, isDarkTheme) { MutableReadyTracker() }
    var preparedFired by remember(difficulty, isDarkTheme) { mutableStateOf(false) }

    // Capture the time this preflight instance started so we can ensure a minimum
    // loading duration in the dialog.
    val startTimeMs = remember(difficulty, isDarkTheme) { SystemClock.uptimeMillis() }

    val currentOnPrepared by rememberUpdatedState(onPrepared)

    Log.d(
        TAG_PREFLIGHT,
        "Composing StagesListPreflight: difficulty=$difficulty, isDarkTheme=$isDarkTheme"
    )

    // Wait for minimally ready, with timeout.
    LaunchedEffect(readyTracker, difficulty, isDarkTheme, startTimeMs) {
        Log.d(
            TAG_PREFLIGHT,
            "Waiting for minimallyReady snapshot (timeout=${PREFLIGHT_TIMEOUT_MS}ms) difficulty=$difficulty"
        )

        val minimalSnapshot: ReadySnapshot? = withTimeoutOrNull(PREFLIGHT_TIMEOUT_MS) {
            readyTracker.snapshot.first { snapshot ->
                val minimal = snapshot.minimallyReady
                if (minimal) {
                    Log.d(TAG_PREFLIGHT, "minimallyReady reached: $snapshot")
                }
                minimal
            }
        }

        val finalSnapshot: ReadySnapshot = if (minimalSnapshot != null) {
            Log.d(TAG_PREFLIGHT, "Preflight reached minimal ready: $minimalSnapshot")
            minimalSnapshot
        } else {
            val last = readyTracker.snapshot.value
            Log.w(
                TAG_PREFLIGHT,
                "Preflight TIMEOUT for difficulty=$difficulty, lastSnapshot=$last"
            )
            Log.w(TAG_PREFLIGHT, "Calling forceMinimalReady() after timeout")
            // This will clamp values and mark everything as ready.
            readyTracker.forceMinimalReady()
            // Read back the forced snapshot so we can persist it.
            readyTracker.snapshot.value
        }

        // IMPORTANT: Always store the snapshot (normal or forced),
        // so StagesListPage sees hasPreflightSnapshot=true and uses
        // the precomputed target offset.
        preflightViewModel.updateSnapshot(difficulty, finalSnapshot)
        Log.d(
            TAG_PREFLIGHT,
            "Stored preflight snapshot for difficulty=$difficulty: $finalSnapshot"
        )

        // --- Enforce a minimum visible loading time on the dialog side ---
        // This is measured from the moment this preflight instance started
        // (i.e. when the user tapped the OptionCard and we mounted preflight).
        val now = SystemClock.uptimeMillis()
        val elapsed = now - startTimeMs
        val minVisible = MIN_LOADING_VISIBLE_MS.coerceAtLeast(0L)
        val remaining = minVisible - elapsed

        if (remaining > 0) {
            Log.d(
                TAG_PREFLIGHT,
                "Preflight ready early (elapsed=${elapsed}ms); delaying onPrepared by ${remaining}ms to keep loading visible"
            )
            // delay is non-blocking; the main thread is free to run animations.
            delay(remaining)
        }

        if (!preparedFired) {
            preparedFired = true
            Log.d(TAG_PREFLIGHT, "Invoking onPrepared() for difficulty=$difficulty")
            currentOnPrepared()
        }
    }

    val readinessHooks = remember(readyTracker, difficulty) {
        ReadinessHooks(
            onStageDataReady = {
                Log.d(TAG_PREFLIGHT, "onStageDataReady() for difficulty=$difficulty")
                readyTracker.markStageDataReady()
            },
            onViewportMeasured = { px ->
                Log.d(TAG_PREFLIGHT, "onViewportMeasured($px) for difficulty=$difficulty")
                readyTracker.setViewportHeight(px)
            },
            onTargetOffsetComputed = { px ->
                Log.d(TAG_PREFLIGHT, "onTargetOffsetComputed($px) for difficulty=$difficulty")
                readyTracker.setTargetOffset(px)
            },
            onInitialSnapSettled = {
                Log.d(TAG_PREFLIGHT, "onInitialSnapSettled() for difficulty=$difficulty")
                readyTracker.markInitialSnapSettled()
            },
            onBubbleCalibrated = {
                Log.d(TAG_PREFLIGHT, "onBubbleCalibrated() for difficulty=$difficulty")
                readyTracker.markBubbleCalibrated()
            },
            onVisualsSettled = {
                Log.d(TAG_PREFLIGHT, "onVisualsSettled() for difficulty=$difficulty")
                readyTracker.markVisualsSettled()
            }
        )
    }

    // --- Defer heavy off-screen composition by one frame ---
    // On the very first composition, we *don't* build StagesListContent yet.
    // We yield to the next frame so the MainPage can:
    //   1) Flip the OptionCard into its loading state, and
    //   2) Start the LoadingDots animation.
    //
    // Then, on the next frame, we turn on the preflight StagesListContent, which
    // will JIT-compile and compose off-screen. By that time, the loading
    // indicator is already on-screen and has an opportunity to tick.
    var shouldComposeContent by remember(difficulty, isDarkTheme) { mutableStateOf(false) }

    LaunchedEffect(difficulty, isDarkTheme) {
        // Wait for the next frame boundary.
        withFrameNanos { /* next frame */ }
        shouldComposeContent = true
    }

    if (shouldComposeContent) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer(alpha = 0f) // fully invisible
                .zIndex(-1f)               // always behind everything
        ) {
            // Off-screen composition of the full stages list
            StagesListContent(
                navController = dummyNavController,
                isDarkTheme = isDarkTheme,
                difficulty = difficulty,
                scrollState = scrollState,
                stageViewModel = stageViewModel,
                progressViewModel = progressViewModel,
                readinessHooks = readinessHooks,
                firstRenderInstantCenter = true,   // instant center off-screen
                enableProgrammaticCentering = true,
                allowBubbleCalibration = true
            )
        }
    }
}

/**
 * Resolves the nearest [Activity] from a [Context], unwrapping [ContextWrapper]s if needed.
 */
private tailrec fun Context.findActivity(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.findActivity()
    else -> null
}
