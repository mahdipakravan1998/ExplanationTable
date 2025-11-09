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
import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
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
import androidx.compose.ui.Modifier
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
import com.example.explanationtable.ui.stages.viewmodel.ScrollAnchorVisibilityViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModelFactory
import com.example.explanationtable.ui.system.AppScreenScaffold
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Stages list screen; orchestrates top app bar, back/scroll anchors, and a vertically
 * scrollable list of stage buttons. External behavior and visuals are preserved.
 *
 * @param navController Navigation controller for routing.
 * @param difficulty Current difficulty whose stages are shown. Defaults to [Difficulty.EASY].
 * @param isDarkTheme Whether the app is currently in dark mode (affects theming/icons).
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    // ----- Global state (from other VMs) -----
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsStateWithLifecycle(initialValue = 0)

    val context = LocalContext.current

    // Repository creation is memoized per application context; no leaks.
    val stageRepository = remember(context) {
        StageRepositoryImpl(DataStoreManager(context.applicationContext))
    }
    val stageViewModel: StageViewModel = viewModel(factory = StageViewModelFactory(stageRepository))

    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsStateWithLifecycle(initialValue = false)
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsStateWithLifecycle(initialValue = false)

    // When the anchor appears, we "latch" its flip orientation to avoid visual popping
    // during the brief show/hide animation.
    var anchorFlip by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor, isStageAbove) {
        if (showScrollAnchor) anchorFlip = isStageAbove
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val activity = context as? Activity

    // ----- Scroll & geometry state -----

    // Preserve list position across process death; behavior is otherwise identical.
    val scrollState: ScrollState = rememberSaveable(saver = ScrollState.Saver) { ScrollState(0) }

    // Target scroll offset is provided by child content; we use rememberUpdatedState so
    // animations always read the latest value even if a coroutine outlives a recomposition.
    var targetOffset by remember { mutableIntStateOf(0) }
    val currentTargetOffset by rememberUpdatedState(targetOffset)

    var viewportHeight by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    // Back navigates to main, clearing this screen—preserve exact behavior.
    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Ensure stages metadata is loaded when difficulty changes.
    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }

    // Stable per-density item height in PX: (container + vertical padding x2)
    val itemPx = remember(density) {
        with(density) {
            (StageListDefaults.ButtonContainerHeight + StageListDefaults.ButtonVerticalPadding * 2).toPx()
        }.toInt()
    }

    // Top padding depends on whether first stage is unlocked; derive once per change.
    val topPaddingDp by remember(unlockedStage) {
        mutableStateOf(
            StageListDefaults.ListVerticalPadding + if (unlockedStage == 1) 36.dp else 0.dp
        )
    }
    val bottomPaddingDp = StageListDefaults.ListVerticalPadding

    // Convert total list padding to PX; recompute only when density or top padding changes.
    val totalListPaddingPx = remember(density, topPaddingDp) {
        with(density) { (topPaddingDp + bottomPaddingDp).toPx() }.toInt()
    }

    // Stable animation specs; created once.
    val anchorEnterSpec: FiniteAnimationSpec<Float> = remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val anchorExitSpec: FiniteAnimationSpec<Float> = remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val scrollAnimSpec: AnimationSpec<Float> = remember { tween(durationMillis = 600, easing = EaseInOutCubic) }

    // Stream scroll offset to VM with backpressure-aware flow. Effect restarts only when
    // geometry parameters materially change.
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

    // Push initial/geometry changes without waiting for a scroll event.
    LaunchedEffect(viewportHeight, unlockedStage, itemPx, totalListPaddingPx) {
        visibilityViewModel.updateParams(
            scrollOffset = scrollState.value,
            viewportHeight = viewportHeight,
            itemHeight = itemPx,
            totalTopPadding = totalListPaddingPx,
            unlockedStage = unlockedStage
        )
    }

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
                    navController.navigate(Routes.MAIN) {
                        popUpTo(Routes.MAIN) { inclusive = true }
                    }
                }
            )
        },
        floatingEnd = {
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
                            scrollState.animateScrollTo(currentTargetOffset, animationSpec = scrollAnimSpec)
                        }
                    }
                )
            }
        }
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
    }
}
