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
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
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

@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsStateWithLifecycle(initialValue = 0)

    val context = LocalContext.current
    val stageRepository = remember(context) { StageRepositoryImpl(DataStoreManager(context.applicationContext)) }
    val stageViewModel: StageViewModel = viewModel(factory = StageViewModelFactory(stageRepository))

    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsStateWithLifecycle(initialValue = emptyMap())
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsStateWithLifecycle(initialValue = false)
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsStateWithLifecycle(initialValue = false)

    var anchorFlip by rememberSaveable { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor, isStageAbove) { if (showScrollAnchor) anchorFlip = isStageAbove }

    var showSettingsDialog by rememberSaveable { mutableStateOf(false) }
    val activity = context as? Activity

    val scrollState = rememberScrollState()
    var targetOffset by remember { mutableStateOf(0) }
    var viewportHeight by remember { mutableStateOf(0) }
    val currentTargetOffset by rememberUpdatedState(targetOffset)
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }

    val itemPx = remember(density) {
        with(density) {
            (StageListDefaults.ButtonContainerHeight + StageListDefaults.ButtonVerticalPadding * 2).toPx()
        }.toInt()
    }

    val topPaddingDp =
        StageListDefaults.ListVerticalPadding + if (unlockedStage == 1) 36.dp else 0.dp
    val bottomPaddingDp = StageListDefaults.ListVerticalPadding
    val totalListPaddingPx = with(density) { (topPaddingDp + bottomPaddingDp).toPx() }.toInt()

    val anchorEnterSpec: FiniteAnimationSpec<Float> = remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val anchorExitSpec: FiniteAnimationSpec<Float> = remember { tween(durationMillis = 150, easing = EaseInOutCubic) }
    val scrollAnimSpec: AnimationSpec<Float> = remember { tween(durationMillis = 600, easing = EaseInOutCubic) }

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
