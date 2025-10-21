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
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.explanationtable.R
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
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Screen showing a scrollable list of stages for a given [difficulty].
 * Displays a floating “scroll to unlocked stage” anchor and a settings dialog.
 * (CalloutBubble is now fully handled inside StagesListContent.)
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    //---- Shared ViewModels & State ----
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsState()

    val stageViewModel: StageViewModel = viewModel()
    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsState()
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsState()

    // Remembered flip flag so the arrow orientation “freezes” during exit animation
    val anchorFlip = remember { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor) {
        if (showScrollAnchor) anchorFlip.value = isStageAbove
    }

    //---- UI State ----
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
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

    // Feed scroll position, viewport size, and unlocked stage into visibility logic
    LaunchedEffect(scrollState, viewportHeight, unlockedStage) {
        snapshotFlow { Triple(scrollState.value, viewportHeight, unlockedStage) }
            .distinctUntilChanged()
            .collect { (offset, height, stage) ->
                val itemPx = with(density) {
                    (ButtonContainerHeight + ButtonVerticalPadding * 2).toPx()
                }.toInt()
                val paddingPx = with(density) { (ListVerticalPadding * 2).toPx() }.toInt()

                visibilityViewModel.updateParams(
                    scrollOffset = offset,
                    viewportHeight = height,
                    itemHeight = itemPx,
                    totalTopPadding = paddingPx,
                    unlockedStage = stage
                )
            }
    }

    //---- Main UI ----
    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.fillMaxSize()) {
                // Top bar with title, gem count, difficulty selector, and settings icon
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

                // Settings pop-up dialog
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
