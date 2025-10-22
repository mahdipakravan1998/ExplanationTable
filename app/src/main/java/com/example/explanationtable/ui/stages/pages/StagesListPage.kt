package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
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
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.topBar.AppTopBar
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.settings.dialogs.SettingsDialog
import com.example.explanationtable.ui.stages.components.ScrollAnchor
import com.example.explanationtable.ui.stages.content.*
import com.example.explanationtable.ui.stages.content.StageListDefaults.ButtonContainerHeight
import com.example.explanationtable.ui.stages.content.StageListDefaults.ButtonVerticalPadding
import com.example.explanationtable.ui.stages.content.StageListDefaults.ListVerticalPadding
import com.example.explanationtable.ui.stages.viewmodel.*
import com.example.explanationtable.repository.StageRepositoryImpl
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Screen showing a scrollable list of stages for a given [difficulty].
 * CalloutBubble is owned & gated inside StagesListContent.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    //---- Shared ViewModels & State ----
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsStateWithLifecycle(initialValue = 0)

    // Build repository & VM via manual factory (no DI)
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

    // Remembered flip flag so the arrow orientation “freezes” during exit animation
    val anchorFlip = remember { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor) {
        if (showScrollAnchor) anchorFlip.value = isStageAbove
    }

    //---- UI State ----
    var showSettingsDialog by remember { mutableStateOf(false) }
    val activity = context as? Activity

    //---- Scroll & Layout Metrics ----
    val scrollState = rememberScrollState()
    var targetOffset by remember { mutableStateOf(0) }
    var viewportHeight by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    //---- Back Handler ----
    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    // Reload available stages when difficulty changes
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Precompute constants once (no per-frame work)
    val itemPx = remember(density) {
        with(density) { (ButtonContainerHeight + ButtonVerticalPadding * 2).toPx() }.toInt()
    }
    val paddingPx = remember(density) {
        with(density) { (ListVerticalPadding * 2).toPx() }.toInt()
    }

    // Feed scroll position, viewport size, and unlocked stage into visibility logic
    // Use conflate + withFrameNanos: at most one update per frame, no preview APIs, no timers.
    LaunchedEffect(scrollState, viewportHeight, unlockedStage, itemPx, paddingPx) {
        snapshotFlow { scrollState.value }
            .distinctUntilChanged()
            .conflate() // drop intermediate scroll values while we’re processing
            .collectLatest { offset ->
                // Align update to the choreographer frame to avoid jitter
                withFrameNanos { /* just sync to frame */ }
                visibilityViewModel.updateParams(
                    scrollOffset = offset,
                    viewportHeight = viewportHeight,
                    itemHeight = itemPx,
                    totalTopPadding = paddingPx,
                    unlockedStage = unlockedStage
                )
            }
    }

    //---- Main UI ----
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
                enter = scaleIn(
                    initialScale = 0f,
                    animationSpec = tween(150, easing = EaseInOutCubic)
                ),
                exit = scaleOut(
                    targetScale = 0f,
                    animationSpec = tween(150, easing = EaseInOutCubic)
                ),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
                    .zIndex(4f)
            ) {
                ScrollAnchor(
                    isDarkTheme = isDarkTheme,
                    flipVertical = anchorFlip.value,
                    onClick = {
                        coroutineScope.launch {
                            scrollState.animateScrollTo(
                                targetOffset,
                                animationSpec = tween(600, easing = EaseInOutCubic)
                            )
                        }
                    }
                )
            }
        }
    }
}
