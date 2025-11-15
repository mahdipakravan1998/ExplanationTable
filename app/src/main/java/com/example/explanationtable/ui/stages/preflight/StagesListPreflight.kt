package com.example.explanationtable.ui.stages.preflight

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
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
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

private const val PREFLIGHT_TIMEOUT_MS: Long = 2_000L
private const val TAG_PREFLIGHT = "StagesListPreflight"

/**
 * Invisible, full-size preflight runner for the stages list.
 *
 * It builds the stages list off-screen and waits until the list is "minimally ready":
 *  - stage data loaded
 *  - viewport measured
 *  - target offset computed
 *  - initial snap (centering) settled
 *  - bubble calibrated
 *
 * When that happens (or when timeout is reached), it calls [onPrepared].
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

    val currentOnPrepared by rememberUpdatedState(onPrepared)

    Log.d(
        TAG_PREFLIGHT,
        "Composing StagesListPreflight: difficulty=$difficulty, isDarkTheme=$isDarkTheme"
    )

    // Wait for minimally ready, with timeout.
    LaunchedEffect(readyTracker, difficulty, isDarkTheme) {
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
            firstRenderInstantCenter = true,            // instant center off-screen
            enableProgrammaticCentering = true,         // animate/scroll off-screen
            allowBubbleCalibration = true               // full calibration for readiness
        )
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
