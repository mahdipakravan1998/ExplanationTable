package com.example.explanationtable.ui.review.pages

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.ui.review.viewmodel.StageReviewViewModel
import com.example.explanationtable.ui.theme.*

/**
 * Renders a two-column review table.
 *
 * IMPORTANT: Removed navigation-bar insets padding so it doesn't create a band under PrizeBox.
 * The bottom system area color is now controlled by GameplayPage's scaffold (to match PrizeBox).
 */
@Composable
fun StageReviewTable(
    difficulty: Difficulty,
    stageNumber: Int,
    isDarkTheme: Boolean,
    viewModel: StageReviewViewModel = viewModel()
) {
    LaunchedEffect(difficulty, stageNumber) {
        viewModel.loadStageData(difficulty, stageNumber)
    }

    val uiState by viewModel.uiState.collectAsState()

    uiState.errorMessage?.let { error ->
        Text(
            text = error,
            color = if (isDarkTheme) TextColorLight else TextColorDark,
            modifier = Modifier.padding(16.dp)
        )
        return
    }

    if (uiState.isLoading || uiState.rows.isEmpty()) return

    val borderWidth = 2.dp
    val headerHeight = 48.dp
    val cornerRadius = 16.dp

    val headerBg = if (isDarkTheme) HeaderBackgroundDark else HeaderBackgroundLight
    val borderClr = if (isDarkTheme) BorderColorDark else BorderColorLight
    val textClr   = if (isDarkTheme) TextColorDark else TextColorLight

    val headerTextStyle = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp)
    val cellTextStyle   = MaterialTheme.typography.bodyLarge

    val columnDivider = Modifier
        .width(borderWidth)
        .fillMaxHeight()
        .background(borderClr)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(cornerRadius))
            .border(borderWidth, borderClr, RoundedCornerShape(cornerRadius))
    ) {
        TableHeader(
            leftText      = uiState.headerLeft,
            rightText     = uiState.headerRight,
            height        = headerHeight,
            background    = headerBg,
            divider       = columnDivider,
            textStyle     = headerTextStyle,
            textColor     = textClr
        )

        HorizontalDivider(color = borderClr, thickness = borderWidth)

        uiState.rows.forEachIndexed { index, row ->
            TableRow(
                leftText    = row.leftText,
                rightText   = row.rightText,
                divider     = columnDivider,
                textStyle   = cellTextStyle,
                textColor   = textClr
            )
            if (index < uiState.rows.lastIndex) {
                HorizontalDivider(color = borderClr, thickness = borderWidth)
            }
        }
    }
}

@Composable
private fun TableHeader(
    leftText: String,
    rightText: String,
    height: Dp,
    background: androidx.compose.ui.graphics.Color,
    divider: Modifier,
    textStyle: androidx.compose.ui.text.TextStyle,
    textColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(background)
            .height(height),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text      = leftText,
            modifier  = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            style     = textStyle,
            textAlign = TextAlign.Center,
            color     = textColor
        )
        Box(modifier = divider)
        Text(
            text      = rightText,
            modifier  = Modifier
                .weight(1f)
                .padding(horizontal = 4.dp),
            style     = textStyle,
            textAlign = TextAlign.Center,
            color     = textColor
        )
    }
}

@Composable
private fun TableRow(
    leftText: String,
    rightText: String,
    divider: Modifier,
    textStyle: androidx.compose.ui.text.TextStyle,
    textColor: androidx.compose.ui.graphics.Color
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Max),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text      = leftText,
            modifier  = Modifier
                .weight(1f)
                .padding(8.dp),
            style     = textStyle,
            textAlign = TextAlign.Center,
            color     = textColor
        )
        Box(modifier = divider)
        Text(
            text      = rightText,
            modifier  = Modifier
                .weight(1f)
                .padding(8.dp),
            style     = textStyle,
            textAlign = TextAlign.Center,
            color     = textColor
        )
    }
}
