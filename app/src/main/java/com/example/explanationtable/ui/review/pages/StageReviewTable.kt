package com.example.explanationtable.ui.review.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.review.viewmodel.StageReviewViewModel
import com.example.explanationtable.ui.theme.BorderColorDark
import com.example.explanationtable.ui.theme.BorderColorLight
import com.example.explanationtable.ui.theme.HeaderBackgroundDark
import com.example.explanationtable.ui.theme.HeaderBackgroundLight
import com.example.explanationtable.ui.theme.TextColorDark
import com.example.explanationtable.ui.theme.TextColorLight

/**
 * Renders a two-column review table for a given stage and difficulty.
 *
 * Public API and visual output are preserved. Internally delegates to a pure-UI composable
 * to reduce recompositions and allow easier testing. Rows are rendered in a LazyColumn to
 * avoid eagerly composing large lists.
 *
 * @param difficulty The difficulty whose stage data should be shown.
 * @param stageNumber The stage number to load.
 * @param isDarkTheme Whether dark palette should be used (colors come from app theme).
 * @param viewModel The ViewModel providing state; defaults to a local ViewModel().
 */
@Composable
fun StageReviewTable(
    difficulty: Difficulty,
    stageNumber: Int,
    isDarkTheme: Boolean,
    viewModel: StageReviewViewModel = viewModel()
) {
    // Kick off (re)load when the inputs change.
    LaunchedEffect(difficulty, stageNumber) {
        viewModel.loadStageData(difficulty, stageNumber)
    }

    // Single source of truth for the screen.
    val uiState by viewModel.uiState.collectAsState()

    // Render inline error as before (keeps visuals identical).
    uiState.errorMessage?.let { error ->
        Text(
            text = error,
            color = if (isDarkTheme) TextColorLight else TextColorDark,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    // Preserve existing behavior: render nothing while loading/empty.
    if (uiState.isLoading || uiState.rows.isEmpty()) return

    // Map rows into a stable list of pairs to decouple UI from the data model shape.
    val rows: List<Pair<String, String>> = remember(uiState.rows) {
        uiState.rows.map { it.leftText to it.rightText }
    }

    StageReviewTableContent(
        headerLeft = uiState.headerLeft,
        headerRight = uiState.headerRight,
        rows = rows,
        isDarkTheme = isDarkTheme
    )
}

/**
 * Stateless, pure renderer of the stage review table.
 * Accepts plain data and does not perform IO or state collection.
 */
@Composable
private fun StageReviewTableContent(
    headerLeft: String,
    headerRight: String,
    rows: List<Pair<String, String>>,
    isDarkTheme: Boolean
) {
    // ---- Layout constants ----
    val borderWidth: Dp = 2.dp
    val headerHeight: Dp = 48.dp
    val cornerRadius: Dp = 16.dp

    val shape = remember(cornerRadius) { RoundedCornerShape(cornerRadius) }

    val palette = remember(isDarkTheme) {
        Palette(
            headerBg = if (isDarkTheme) HeaderBackgroundDark else HeaderBackgroundLight,
            border = if (isDarkTheme) BorderColorDark else BorderColorLight,
            text = if (isDarkTheme) TextColorDark else TextColorLight
        )
    }

    // Read typography in-composition, then memoize the derived style only.
    val typography = MaterialTheme.typography
    val headerTextStyle: TextStyle = remember(typography.titleLarge) {
        typography.titleLarge.copy(fontSize = 16.sp)
    }
    // bodyLarge is stable; no need to remember it.
    val cellTextStyle: TextStyle = typography.bodyLarge

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(shape)
            .border(borderWidth, palette.border, shape)
    ) {
        TableHeader(
            leftText = headerLeft,
            rightText = rightTextOrFallback(headerRight),
            height = headerHeight,
            background = palette.headerBg,
            textStyle = headerTextStyle,
            textColor = palette.text,
            dividerWidth = borderWidth,
            dividerColor = palette.border
        )

        HorizontalDivider(color = palette.border, thickness = borderWidth)

        LazyColumn {
            itemsIndexed(
                items = rows,
                key = { index, item ->
                    // Stable key across identical content; cheap to compute.
                    item.first.hashCode() * 31 + item.second.hashCode() + index
                }
            ) { index, (left, right) ->
                TableRow(
                    leftText = left,
                    rightText = right,
                    textStyle = cellTextStyle,
                    textColor = palette.text,
                    dividerWidth = borderWidth,
                    dividerColor = palette.border
                )
                if (index < rows.lastIndex) {
                    HorizontalDivider(color = palette.border, thickness = borderWidth)
                }
            }
        }
    }
}

/**
 * Draws the header row with a vertical divider using drawBehind to reduce node count.
 */
@Composable
private fun TableHeader(
    leftText: String,
    rightText: String,
    height: Dp,
    background: Color,
    textStyle: TextStyle,
    textColor: Color,
    dividerWidth: Dp,
    dividerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .height(height)
            .drawBehind {
                // Draw a vertical divider at the midline between the two weighted cells.
                val x = size.width / 2f
                // Use a rect to match a 2.dp-thick divider for pixel-snapped results.
                drawRect(
                    color = dividerColor,
                    topLeft = androidx.compose.ui.geometry.Offset(x - dividerWidth.toPx() / 2f, 0f),
                    size = androidx.compose.ui.geometry.Size(dividerWidth.toPx(), size.height)
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leftText,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .semantics { heading() },
            style = textStyle,
            textAlign = TextAlign.Center,
            color = textColor
        )
        // No Box divider child → fewer layout nodes.
        Text(
            text = rightText,
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp)
                .semantics { heading() },
            style = textStyle,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

/**
 * Draws a content row with the same visual structure as the header:
 * two equal-weight text cells and a center vertical divider drawn behind.
 */
@Composable
private fun TableRow(
    leftText: String,
    rightText: String,
    textStyle: TextStyle,
    textColor: Color,
    dividerWidth: Dp,
    dividerColor: Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .drawBehind {
                val x = size.width / 2f
                drawRect(
                    color = dividerColor,
                    topLeft = androidx.compose.ui.geometry.Offset(x - dividerWidth.toPx() / 2f, 0f),
                    size = androidx.compose.ui.geometry.Size(dividerWidth.toPx(), size.height)
                )
            },
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = leftText,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            style = textStyle,
            textAlign = TextAlign.Center,
            color = textColor
        )
        Text(
            text = rightText,
            modifier = Modifier
                .weight(1f)
                .padding(8.dp),
            style = textStyle,
            textAlign = TextAlign.Center,
            color = textColor
        )
    }
}

/** Small defensive helper to avoid an empty-looking heading cell if callers pass blank. */
private fun rightTextOrFallback(value: String): String =
    if (value.isEmpty()) " " else value

@Immutable
private data class Palette(
    val headerBg: Color,
    val border: Color,
    val text: Color
)
