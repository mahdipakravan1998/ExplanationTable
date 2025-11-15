package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import android.util.Log
import androidx.activity.ComponentActivity
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
import com.example.explanationtable.ui.stages.preflight.ReadySnapshot
import com.example.explanationtable.ui.stages.preflight.StagesPreflightViewModel
import com.example.explanationtable.ui.stages.viewmodel.ScrollAnchorVisibilityViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModelFactory
import com.example.explanationtable.ui.system.AppScreenScaffold
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

private const val TAG_PAGE = "StagesListPage"

/**
 * Stages list screen; orchestrates top app bar, back/scroll anchors, and the list.
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

    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsStateWithLifecycle(initialValue = false)
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsStateWithLifecycle(initialValue = false)

    // Preflight snapshots (produced off-screen)
    val preflightViewModel: StagesPreflightViewModel =
        if (componentActivity != null) {
            viewModel(componentActivity)
        } else {
            viewModel()
        }

    val preflightSnapshots: Map<Difficulty, ReadySnapshot> by preflightViewModel
        .snapshots
        .collectAsStateWithLifecycle(initialValue = emptyMap())

    val previousRoute = navController.previousBackStackEntry?.destination?.route
    val cameFromMain: Boolean = previousRoute == Routes.MAIN

    val preflightSnapshotForDifficulty: ReadySnapshot? =
        preflightSnapshots[difficulty]

    val initialScrollOffset: Int = preflightSnapshotForDifficulty?.targetOffsetPx ?: 0

    Log.d(
        TAG_PAGE,
        "Compose StagesListPage: difficulty=$difficulty, unlockedStage=$unlockedStage, " +
                "previousRoute=$previousRoute, cameFromMain=$cameFromMain, " +
                "hasPreflightSnapshot=${preflightSnapshotForDifficulty != null}, " +
                "preflightTarget=${preflightSnapshotForDifficulty?.targetOffsetPx}, " +
                "initialScrollOffset=$initialScrollOffset"
    )

    // Scroll state starts at preflight-computed offset (if any)
    val scrollState: ScrollState = rememberSaveable(
        saver = ScrollState.Saver,
        inputs = arrayOf(initialScrollOffset, difficulty)
    ) {
        Log.d(TAG_PAGE, "Creating ScrollState with initialScrollOffset=$initialScrollOffset")
        ScrollState(initialScrollOffset)
    }

    // Target offset is driven by StagesListContent; ScrollAnchor uses this.
    var targetOffset by remember { mutableIntStateOf(initialScrollOffset) }
    val currentTargetOffset by rememberUpdatedState(targetOffset)

    var viewportHeight by remember { mutableIntStateOf(0) }

    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        Log.d(TAG_PAGE, "BackHandler → navigate to MAIN")
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Make sure counts are loaded (also done inside content, but harmless)
    LaunchedEffect(difficulty) {
        Log.d(TAG_PAGE, "fetchStagesCount($difficulty) from StagesListPage")
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Geometry used for the scroll-to-unlocked anchor
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

    Log.d(
        TAG_PAGE,
        "Centering: scrollStateInitial=${scrollState.value}, initialScrollOffset=$initialScrollOffset"
    )

    // Latch the flip orientation for the ScrollAnchor
    var anchorFlip by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor, isStageAbove) {
        if (showScrollAnchor) anchorFlip = isStageAbove
    }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

    // Feed scroll geometry into ScrollAnchorVisibilityViewModel
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
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // IMPORTANT: We now rely on the original bubble behaviour of StagesListContent.
            // Preflight still works because it uses its own off-screen StagesListContent
            // with ReadinessHooks; here we just use the defaults.
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
