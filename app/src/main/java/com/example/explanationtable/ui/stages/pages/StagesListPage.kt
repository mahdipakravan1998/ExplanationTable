package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
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
import com.example.explanationtable.ui.stages.content.BUTTON_CONTAINER_SIZE
import com.example.explanationtable.ui.stages.content.BUTTON_VERTICAL_PADDING
import com.example.explanationtable.ui.stages.content.LIST_VERTICAL_PADDING
import com.example.explanationtable.ui.stages.content.StagesListContent
import com.example.explanationtable.ui.stages.viewmodel.ScrollAnchorVisibilityViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageProgressViewModel
import com.example.explanationtable.ui.stages.viewmodel.StageViewModel
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch

/**
 * Composable that sets up and renders the stage list screen with conditional,
 * animated appearance and orientation of the ScrollAnchor.
 */
@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    // App-wide state
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsState()

    // Stage data
    val stageViewModel: StageViewModel = viewModel()
    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedStage = unlockedMap[difficulty] ?: 1

    // Visibility logic
    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsState()
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsState()

    // Freeze arrow flip at the moment of showing, so exit animation keeps correct orientation
    val anchorFlip = remember { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor) {
        if (showScrollAnchor) {
            anchorFlip.value = isStageAbove
        }
    }

    // UI state
    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    // Scroll and measurements
    val scrollState = rememberScrollState()
    var targetOffset by remember { mutableStateOf(0) }
    var viewportHeight by remember { mutableStateOf(0) }
    val coroutineScope = rememberCoroutineScope()
    val density = LocalDensity.current

    // Back handling
    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }
// Load stages on difficulty change
    LaunchedEffect(difficulty) {
        stageViewModel.fetchStagesCount(difficulty)
    }

    // Feed scroll and size into visibility ViewModel
    LaunchedEffect(scrollState, viewportHeight, unlockedStage) {
        snapshotFlow { Triple(scrollState.value, viewportHeight, unlockedStage) }
            .distinctUntilChanged()
            .collect { (offset, height, stage) ->
                val itemHeightPx = with(density) { (BUTTON_CONTAINER_SIZE + BUTTON_VERTICAL_PADDING * 2).toPx() }.toInt()
                val totalTopPaddingPx = with(density) { LIST_VERTICAL_PADDING.toPx() * 2 }.toInt()
                visibilityViewModel.updateParams(
                    scrollOffset = offset,
                    viewportHeight = height,
                    itemHeight = itemHeightPx,
                    totalTopPadding = totalTopPaddingPx,
                    unlockedStage = stage
                )
            }
    }

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        Box(Modifier.fillMaxSize()) {
            Column(Modifier.fillMaxSize()) {
                AppTopBar(
                    isHomePage = false,
                    isDarkTheme = isDarkTheme,
                    title = stringResource(id = R.string.stages_list),
                    gems = diamonds,
                    difficulty = difficulty,
                    onSettingsClick = { showSettingsDialog = true },
                    iconTint = MaterialTheme.colorScheme.onSurface
                )

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

            // Animated appearance/hide with scale and fixed flipVertical
            AnimatedVisibility(
                visible = showScrollAnchor,
                enter = scaleIn(initialScale = 0f, animationSpec = tween(300, easing = EaseInOutCubic)),
                exit = scaleOut(targetScale = 0f, animationSpec = tween(300, easing = EaseInOutCubic)),
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 16.dp)
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