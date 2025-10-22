package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.FiniteAnimationSpec
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.repository.StageRepositoryImpl
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.components.ScrollAnchor
import com.example.explanationtable.ui.stages.content.StageListDefaults
import com.example.explanationtable.ui.stages.content.StagesListContent
import com.example.explanationtable.ui.stages.viewmodel.ScrollAnchorVisibilityViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModelFactory
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Screen that shows a scrollable list of stages for the given [difficulty].
 *
 * Responsibilities:
 * - Wire ViewModels and collect state (lifecycle-aware).
 * - Own transient UI state (settings dialog, scroll-anchor animation).
 * - Stream scroll/viewport metrics to the visibility VM at most once per frame.
 *
 * Behavior & visuals are preserved. Internals are adjusted for safer animation specs.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    // ---- Shared ViewModels & State ----
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsStateWithLifecycle(initialValue = 0)

    // Repository & VM via manual factory (no DI framework).
    val context = LocalContext.current
    val stageRepository = remember(context) {
        StageRepositoryImpl(
            dataStore = DataStoreManager(context.applicationContext)
        )
    }
    val stageViewModel: StageViewModel = viewModel(
        factory = StageViewModelFactory(stageRepository)
    )

    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsStateWithLifecycle(initialValue = false)
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsStateWithLifecycle(initialValue = false)

    // Remembered flip flag so the arrow orientation “freezes” during exit animation.
    var anchorFlip by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor, isStageAbove) {
        if (showScrollAnchor) anchorFlip = isStageAbove
    }

    // ---- UI State ----
    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val activity = context as? Activity

    // ---- Scroll & Layout Metrics ----
    val scrollState = rememberScrollState()
    var targetOffset by remember { mutableStateOf(0) }
    var viewportHeight by remember { mutableStateOf(0) }
    val currentTargetOffset by rememberUpdatedState(targetOffset)
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // ---- Back Handler ----
    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Reload available stages when difficulty changes.
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Precompute constants once (no per-frame work).
    val itemPx = remember(density) {
        with(density) {
            (StageListDefaults.ButtonContainerHeight + StageListDefaults.ButtonVerticalPadding * 2).toPx()
        }.toInt()
    }
    val paddingPx = remember(density) {
        with(density) { (StageListDefaults.ListVerticalPadding * 2).toPx() }.toInt()
    }

    // ---- Animation specs (explicit Float generics to avoid inference as Int) ----
    val anchorEnterSpec: FiniteAnimationSpec<Float> = remember {
        tween<Float>(durationMillis = 150, easing = EaseInOutCubic)
    }
    val anchorExitSpec: FiniteAnimationSpec<Float> = remember {
        tween<Float>(durationMillis = 150, easing = EaseInOutCubic)
    }
    val scrollAnimSpec: AnimationSpec<Float> = remember {
        // Animate scroll using a Float-based tween as required by ScrollState.animateScrollTo
        tween<Float>(durationMillis = 600, easing = EaseInOutCubic)
    }

    // Feed scroll position into visibility logic (at most one update per frame).
    LaunchedEffect(scrollState, viewportHeight, unlockedStage, itemPx, paddingPx) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .conflate() // drop intermediate values while processing
            .collectLatest { offset ->
                // Align to choreographer frame to avoid jitter
                withFrameNanos { /* sync to frame */ }
                visibilityViewModel.updateParams(
                    scrollOffset = offset,
                    viewportHeight = viewportHeight,
                    itemHeight = itemPx,
                    totalTopPadding = paddingPx,
                    unlockedStage = unlockedStage
                )
            }
    }

    // Also push a refresh when *non-scroll* params change while the user isn't scrolling.
    LaunchedEffect(viewportHeight, unlockedStage, itemPx, paddingPx) {
        visibilityViewModel.updateParams(
            scrollOffset = scrollState.value,
            viewportHeight = viewportHeight,
            itemHeight = itemPx,
            totalTopPadding = paddingPx,
            unlockedStage = unlockedStage
        )
    }

    // ---- Main UI ----
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = stringResource(R.string.stages_list),
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    iconTint = MaterialTheme.colorScheme.onSurface
                )

                // Scrollable list of stages (owns the CalloutBubble overlay internally)
                StagesListContent(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    difficulty = difficulty,
                    scrollState = scrollState,
                    onTargetOffsetChanged = { offset -> targetOffset = offset },
                    onViewportHeightChanged = { height -> viewportHeight = height },
                    stageViewModel = stageViewModel,
                    progressViewModel = progressViewModel
                )

                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    onExit = { activity?.finishAndRemoveTask() }
                )
            }

            // Floating scroll–to–stage anchor with scale animation
            AnimatedVisibility(
                visible = showScrollAnchor,
                enter = scaleIn(initialScale = 0f, animationSpec = anchorEnterSpec),
                exit = scaleOut(targetScale = 0f, animationSpec = anchorExitSpec),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .zIndex(4f)
            ) {
                ScrollAnchor(
                    isDarkTheme = isDarkTheme,
                    flipVertical = anchorFlip,
                    onClick = {
                        coroutineScope.launch {
                            // Use the latest target offset captured via rememberUpdatedState
                            scrollState.animateScrollTo(
                                currentTargetOffset,
                                animationSpec = scrollAnimSpec
                            )
                        }
                    }
                )
            }
        }
    }
}
