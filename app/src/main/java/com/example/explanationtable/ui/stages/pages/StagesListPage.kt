package com.example.explanationtable.ui.stages.pages

import android.app.Activity
import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.EaseInOutCubic
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.IntOffset
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
import com.example.explanationtable.ui.stages.components.CalloutBubble
import com.example.explanationtable.ui.stages.components.ScrollAnchor
import com.example.explanationtable.ui.stages.content.*
import com.example.explanationtable.ui.stages.content.StageListDefaults.ButtonContainerHeight
import com.example.explanationtable.ui.stages.content.StageListDefaults.ButtonVerticalPadding
import com.example.explanationtable.ui.stages.content.StageListDefaults.ListVerticalPadding
import com.example.explanationtable.ui.stages.viewmodel.*
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun StagesListPage(
    navController: NavController,
    difficulty: Difficulty = Difficulty.EASY,
    isDarkTheme: Boolean
) {
    val mainViewModel: MainViewModel = viewModel()
    val diamonds by mainViewModel.diamonds.collectAsState()

    val stageViewModel: StageViewModel = viewModel()
    val progressViewModel: StageProgressViewModel = viewModel()
    val unlockedMap by progressViewModel.lastUnlocked.collectAsState()
    val unlockedStage = unlockedMap[difficulty] ?: 1

    val visibilityViewModel: ScrollAnchorVisibilityViewModel = viewModel()
    val showScrollAnchor by visibilityViewModel.showScrollAnchor.collectAsState()
    val isStageAbove by visibilityViewModel.isStageAbove.collectAsState()

    val anchorFlip = remember { mutableStateOf(false) }
    LaunchedEffect(showScrollAnchor) {
        if (showScrollAnchor) anchorFlip.value = isStageAbove
    }

    var showSettingsDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current
    val activity = context as? Activity

    val scrollState = rememberScrollState()
    var targetOffset by remember { mutableStateOf(0) }
    var viewportHeight by remember { mutableStateOf(0) }
    var viewportWidth by remember { mutableStateOf(0) }
    val density = LocalDensity.current
    val coroutineScope = rememberCoroutineScope()

    BackHandler {
        navController.navigate(Routes.MAIN) {
            popUpTo(Routes.MAIN) { inclusive = true }
        }
    }

    LaunchedEffect(difficulty) { stageViewModel.fetchStagesCount(difficulty) }

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

    Background(isHomePage = false, isDarkTheme = isDarkTheme) {
        var rootTopLeftInWindow by remember { mutableStateOf(Offset.Zero) }
        // Measured anchor from the unlocked step: (centerX, TOP of visible ellipse) in window coords
        var stageAnchorInWindow by remember { mutableStateOf<Offset?>(null) }

        Box(
            modifier = Modifier
                .fillMaxSize()
                .onGloballyPositioned { coords ->
                    viewportWidth = coords.size.width
                    rootTopLeftInWindow = coords.boundsInWindow().topLeft
                }
        ) {
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

                StagesListContent(
                    navController = navController,
                    isDarkTheme = isDarkTheme,
                    difficulty = difficulty,
                    scrollState = scrollState,
                    onTargetOffsetChanged = { offset -> targetOffset = offset },
                    onViewportHeightChanged = { height -> viewportHeight = height },
                    onUnlockedStageAnchorInWindow = { cx, topY ->
                        stageAnchorInWindow = Offset(cx, topY)
                    },
                    stageViewModel = stageViewModel,
                    progressViewModel = progressViewModel
                )

                SettingsDialog(
                    showDialog = showSettingsDialog,
                    onDismiss = { showSettingsDialog = false },
                    onExit = { activity?.finishAndRemoveTask() }
                )
            }

            // Scroll-to anchor
            AnimatedVisibility(
                visible = showScrollAnchor,
                enter = scaleIn(initialScale = 0f, animationSpec = tween(150, easing = EaseInOutCubic)),
                exit = scaleOut(targetScale = 0f, animationSpec = tween(150, easing = EaseInOutCubic)),
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
                            scrollState.animateScrollTo(targetOffset, animationSpec = tween(600, easing = EaseInOutCubic))
                        }
                    }
                )
            }

            // ======== CALLOUT BUBBLE (tip tangential to button top at bob peak) ========
            val infinite = rememberInfiniteTransition(label = "callout-bob")
            val bobPhase by infinite.animateFloat(
                initialValue = -1f,
                targetValue = 1f,
                animationSpec = infiniteRepeatable(
                    animation = tween(durationMillis = 1100, easing = LinearEasing),
                    repeatMode = RepeatMode.Reverse
                ),
                label = "bob-phase"
            )
            val bobAmplitudePx = with(density) { 6.dp.toPx() } // A
            val bobOffsetYpx = bobPhase * bobAmplitudePx       // [-A, +A]

            var bubbleWidthPx by remember { mutableStateOf(0) }
            var bubbleHeightPx by remember { mutableStateOf(0) }

            val localAnchor = stageAnchorInWindow?.let { it - rootTopLeftInWindow }

            if (localAnchor != null && viewportHeight > 0 && viewportWidth > 0) {
                val wPx = if (bubbleWidthPx == 0) 1 else bubbleWidthPx
                val hPx = if (bubbleHeightPx == 0) 1 else bubbleHeightPx

                // Horizontally center bubble on measured centerX — no clamping to avoid lateral gap
                val placedLeftPx = localAnchor.x - wPx / 2f
                val triangleCenterBiasPx = localAnchor.x - (placedLeftPx + wPx / 2f)

                // Vertically: ensure bubble tip (bottom) touches the measured TOP at bob's highest point
                // topAtRest + (-A) + h = anchorTop  =>  topAtRest = anchorTop - h + A
                val topAtRestPx = localAnchor.y - hPx + bobAmplitudePx
                val animatedTopPx = topAtRestPx + bobOffsetYpx

                CalloutBubble(
                    isDarkTheme = isDarkTheme,
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .offset {
                            IntOffset(
                                x = placedLeftPx.roundToInt(),
                                y = animatedTopPx.roundToInt()
                            )
                        }
                        .zIndex(5f)
                        .onGloballyPositioned { coords ->
                            bubbleWidthPx = coords.size.width
                            bubbleHeightPx = coords.size.height
                        },
                    contentPaddingHorizontal = 16.dp,
                    contentPaddingVertical = 12.dp,
                    cornerRadius = 10.dp,
                    triangleBase = 12.dp,
                    triangleHeight = 10.dp,
                    triangleCenterBias = with(density) { triangleCenterBiasPx.toDp() }
                ) {
                    Text(text = "شروع", style = MaterialTheme.typography.titleMedium)
                }
            }
            // ======== END CALLOUT BUBBLE ========
        }
    }
}
