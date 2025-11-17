package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import android.os.SystemClock
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.EaseInOutSine
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepositoryImpl
import com.example.explanationtable.ui.components.BackAnchor
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.navigation.Routes
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.components.ScrollAnchor
import com.example.explanationtable.ui.stages.content.StageListDefaults
import com.example.explanationtable.ui.stages.content.StagesListContent
import com.example.explanationtable.ui.stages.content.StagesListPlaceholderZigZagBees
import com.example.explanationtable.ui.stages.preflight.MutableReadyTracker
import com.example.explanationtable.ui.stages.preflight.ReadinessHooks
import com.example.explanationtable.ui.stages.preflight.ReadySnapshot
import com.example.explanationtable.ui.stages.preflight.StagesPreflightViewModel
import com.example.explanationtable.ui.stages.viewmodel.ScrollAnchorVisibilityViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModelFactory
import com.example.explanationtable.ui.system.AppScreenScaffold
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val TAG_PAGE = "StagesListPage"

/**
 * How long we keep ONLY the placeholder visible after the first frame,
 * before we even start preflight. This avoids overlapping heavy work with
 * the nav animation and gives a clear pause on the placeholder.
 */
private const val PREFLIGHT_START_DELAY_MS: Long = 180L

/**
 * Minimal time (from the first frame on this page) that the placeholder
 * stays visible before we swap to the real content. This makes the
 * sequence readable: nav → placeholder → content.
 */
private const val PLACEHOLDER_MIN_VISIBLE_MS: Long = 320L

/**
 * Stages list screen; orchestrates:
 *  - top app bar + back anchor
 *  - zigzag-bee placeholder
 *  - hidden preflight StagesListContent behind the placeholder
 *  - final visible StagesListContent (same ScrollState, bubble, scroll anchor)
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsStateWithLifecycle(initialValue = 0)

    val context = LocalContext.current
    val activity = context as? Activity
    val componentActivity = context as? ComponentActivity

    // Repository + VM (shared with preflight)
    val stageRepository = remember(context) {
        StageRepositoryImpl(DataStoreManager(context.applicationContext))
    }

    val stageViewModel: StageViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity, factory = StageViewModelFactory(stageRepository))
        } else {
            viewModel(factory = StageViewModelFactory(stageRepository))
        }

    val progressViewModel: StageProgressViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity)
        } else {
            viewModel()
        }

    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(
        initialValue = emptyMap()
    )
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsStateWithLifecycle(
        initialValue = false
    )
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsStateWithLifecycle(
        initialValue = false
    )

    // Shared preflight VM – we still store the final snapshot here (optional reuse).
    val preflightViewModel: StagesPreflightViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity)
        } else {
            viewModel()
        }

    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val cameFromMain: Boolean = previousRoute == Routes.MAIN

    Log.d(
        TAG_PAGE,
        "Compose StagesListPage: difficulty=$difficulty, unlockedStage=$unlockedStage, " +
                "previousRoute=$previousRoute, cameFromMain=$cameFromMain"
    )

    // Scroll state is shared between preflight (invisible) and visible content.
    val scrollState: ScrollState = rememberSaveable(
        saver = ScrollState.Saver,
        inputs = arrayOf(difficulty)
    ) {
        Log.d(TAG_PAGE, "Creating ScrollState for difficulty=$difficulty (initial=0)")
        ScrollState(0)
    }

    // --- Readiness tracking (local tracker + hooks) ---
    val readyTracker = remember(difficulty) { MutableReadyTracker() }
    var lastSnapshot by remember { mutableStateOf(ReadySnapshot()) }

    var contentVisible by rememberSaveable(difficulty) { mutableStateOf(false) }
    val currentContentVisible by rememberUpdatedState(contentVisible)

    // First-frame timestamp for this page (used to enforce min placeholder time).
    var firstFrameTimeMs by remember(difficulty) { mutableStateOf(0L) }

    // Start preflight (hidden content) only after:
    //  1) we've drawn at least one frame with placeholder,
    //  2) a small delay to avoid overlapping with nav animation.
    var shouldStartContent by rememberSaveable(difficulty) { mutableStateOf(false) }

    LaunchedEffect(difficulty) {
        // Wait for at least one frame so the placeholder is definitely drawn.
        withFrameNanos { /* first frame boundary with placeholder */ }
        firstFrameTimeMs = SystemClock.uptimeMillis()
        Log.d(
            TAG_PAGE,
            "First frame on StagesListPage (difficulty=$difficulty) at $firstFrameTimeMs ms"
        )

        // Let the nav animation finish + placeholder sit alone for a bit.
        delay(PREFLIGHT_START_DELAY_MS)
        Log.d(
            TAG_PAGE,
            "Starting hidden content preflight for $difficulty " +
                    "after ${PREFLIGHT_START_DELAY_MS}ms delay"
        )
        shouldStartContent = true
    }

    // Observe readiness snapshot; when safeToShow, we:
    //  - store it in preflight VM
    //  - ensure placeholder has been visible for at least PLACEHOLDER_MIN_VISIBLE_MS
    //  - then flip contentVisible = true (which fades content in)
    LaunchedEffect(readyTracker, difficulty) {
        readyTracker.snapshot.collect { snapshot ->
            lastSnapshot = snapshot
            if (snapshot.safeToShow && !currentContentVisible) {
                Log.d(TAG_PAGE, "safeToShow reached for $difficulty: $snapshot")
                preflightViewModel.updateSnapshot(difficulty, snapshot)

                val now = SystemClock.uptimeMillis()
                val first = firstFrameTimeMs
                val elapsedSinceFirstFrame =
                    if (first > 0L) now - first else 0L
                val remaining =
                    (PLACEHOLDER_MIN_VISIBLE_MS - elapsedSinceFirstFrame).coerceAtLeast(0L)

                if (remaining > 0L) {
                    Log.d(
                        TAG_PAGE,
                        "Placeholder has been visible for ${elapsedSinceFirstFrame}ms; " +
                                "waiting extra ${remaining}ms before showing content"
                    )
                    delay(remaining)
                }

                contentVisible = true
            }
        }
    }

    val readinessHooks = remember(readyTracker, difficulty) {
        ReadinessHooks(
            onStageDataReady = {
                Log.d(TAG_PAGE, "onStageDataReady() for difficulty=$difficulty")
                readyTracker.markStageDataReady()
            },
            onViewportMeasured = { px ->
                Log.d(TAG_PAGE, "onViewportMeasured($px) for difficulty=$difficulty")
                readyTracker.setViewportHeight(px)
            },
            onTargetOffsetComputed = { px ->
                Log.d(TAG_PAGE, "onTargetOffsetComputed($px) for difficulty=$difficulty")
                readyTracker.setTargetOffset(px)
            },
            onInitialSnapSettled = {
                Log.d(TAG_PAGE, "onInitialSnapSettled() for difficulty=$difficulty")
                readyTracker.markInitialSnapSettled()
            },
            onBubbleCalibrated = {
                Log.d(TAG_PAGE, "onBubbleCalibrated() for difficulty=$difficulty")
                readyTracker.markBubbleCalibrated()
            },
            onVisualsSettled = {
                Log.d(TAG_PAGE, "onVisualsSettled() for difficulty=$difficulty")
                readyTracker.markVisualsSettled()
            }
        )
    }

    // Target offset for ScrollAnchor.
    var targetOffset by remember { mutableIntStateOf(0) }
    val currentTargetOffset by rememberUpdatedState(targetOffset)

    var viewportHeight by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Geometry used for the scroll-to-unlocked anchor.
    val itemPx = remember(density) {
        with(density) {
            (StageListDefaults.ButtonContainerHeight + StageListDefaults.ButtonVerticalPadding * 2).toPx()
        }.toInt()
    }

    val topPaddingDp by remember(unlockedStage) {
        mutableStateOf(
            StageListDefaults.ListVerticalPadding + if (unlockedStage == 1) 36.dp else 0.dp
        )
    }
    val bottomPaddingDp = StageListDefaults.ListVerticalPadding

    val totalListPaddingPx = remember(density, topPaddingDp) {
        with(density) { (topPaddingDp + bottomPaddingDp).toPx() }.toInt()
    }

    val anchorEnterSpec: FiniteAnimationSpec<Float> =
        remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val anchorExitSpec: FiniteAnimationSpec<Float> =
        remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val scrollAnimSpec: AnimationSpec<Float> =
        remember { tween(durationMillis = 600, easing = EaseInOutCubic) }

    // Latch the flip orientation for the ScrollAnchor.
    var anchorFlip by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor, isStageAbove) {
        if (showScrollAnchor) anchorFlip = isStageAbove
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    BackHandler {
        Log.d(TAG_PAGE, "BackHandler → navigate to MAIN")
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Ensure counts are loaded for this difficulty (harmless if also used elsewhere).
    LaunchedEffect(difficulty) {
        Log.d(TAG_PAGE, "fetchStagesCount($difficulty) from StagesListPage")
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Feed scroll geometry into ScrollAnchorVisibilityViewModel.
    LaunchedEffect(scrollState, viewportHeight, unlockedStage, itemPx, totalListPaddingPx) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .conflate()
            .collectLatest { offset ->
                visibilityViewModel.updateParams(
                    scrollOffset = offset,
                    viewportHeight = viewportHeight,
                    itemHeight = itemPx,
                    totalTopPadding = totalListPaddingPx,
                    unlockedStage = unlockedStage
                )
            }
    }

    LaunchedEffect(viewportHeight, unlockedStage, itemPx, totalListPaddingPx) {
        visibilityViewModel.updateParams(
            scrollOffset = scrollState.value,
            viewportHeight = viewportHeight,
            itemHeight = itemPx,
            totalTopPadding = totalListPaddingPx,
            unlockedStage = unlockedStage
        )
    }

    // Content alpha – this gives a visible "pause / reveal" when switching from placeholder.
    val contentAlpha by animateFloatAsState(
        targetValue = if (contentVisible) 1f else 0f,
        animationSpec = tween(durationMillis = 260, easing = EaseInOutSine)
    )

    AppScreenScaffold(
        isHomePage = false,
        isDarkTheme = isDarkTheme,
        topBar = {
            AppTopBar(
                isHomePage = false,
                isDarkTheme = isDarkTheme,
                title = stringResource(R.string.stages_list),
                gems = diamonds,
                difficulty = difficulty,
                onSettingsClick = { showSettingsDialog = true }
            )
        },
        contentTopSpacing = 0.dp,
        floatingStart = {
            BackAnchor(
                isDarkTheme = isDarkTheme,
                onClick = {
                    Log.d(TAG_PAGE, "BackAnchor clicked → MAIN")
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        },
        floatingEnd = {
            if (contentVisible) {
                AnimatedVisibility(
                    visible = showScrollAnchor,
                    enter = scaleIn(initialScale = 0f, animationSpec = anchorEnterSpec),
                    exit = scaleOut(targetScale = 0f, animationSpec = anchorExitSpec)
                ) {
                    ScrollAnchor(
                        isDarkTheme = isDarkTheme,
                        flipVertical = anchorFlip,
                        onClick = {
                            coroutineScope.launch {
                                if (scrollState.isScrollInProgress) return@launch
                                val target = currentTargetOffset
                                if (target == scrollState.value) return@launch

                                Log.d(
                                    TAG_PAGE,
                                    "ScrollAnchor clicked: from=${scrollState.value} to=$target"
                                )

                                scrollState.animateScrollTo(
                                    target,
                                    animationSpec = scrollAnimSpec
                                )
                            }
                        }
                    )
                }
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                // --- Placeholder layer (only while content not yet visible) ---
                if (!contentVisible) {
                    StagesListPlaceholderZigZagBees(
                        modifier = Modifier.fillMaxSize()
                    )
                }

                // --- Real content layer (shared ScrollState) ---
                if (shouldStartContent) {
                    // During preflight (contentVisible = false):
                    //  - firstRenderInstantCenter = true
                    //  - enableProgrammaticCentering = true
                    // So it snaps to center OFF-SCREEN (alpha=0).
                    // After safeToShow + min placeholder time:
                    //  - contentVisible flips to true,
                    //  - we fade content in (alpha anim),
                    //  - those flags effectively stop re-centering.
                    val enableProgrammaticCentering = !contentVisible
                    val firstRenderInstantCenter = !contentVisible

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .graphicsLayer(alpha = contentAlpha)
                    ) {
                        StagesListContent(
                            navController = navController,
                            isDarkTheme = isDarkTheme,
                            difficulty = difficulty,
                            scrollState = scrollState,
                            onTargetOffsetChanged = { offset -> targetOffset = offset },
                            onViewportHeightChanged = { height -> viewportHeight = height },
                            stageViewModel = stageViewModel,
                            progressViewModel = progressViewModel,
                            readinessHooks = readinessHooks,
                            firstRenderInstantCenter = firstRenderInstantCenter,
                            enableProgrammaticCentering = enableProgrammaticCentering,
                            allowBubbleCalibration = true
                        )
                    }
                }
            }

            SettingsDialog(
                showDialog = showSettingsDialog,
                onDismiss = { showSettingsDialog = false },
                onExit = { activity?.finishAndRemoveTask() }
            )
        }
    }
}
