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
 * Displays a stage review table based on the provided difficulty and stage number.
 */
@Composable
fun StageReviewTable(
    difficulty: Difficulty,
    stageNumber: Int,
    isDarkTheme: Boolean,
    viewModel: StageReviewViewModel = viewModel()
) {
    // Reload whenever difficulty or stageNumber changes
    LaunchedEffect(difficulty, stageNumber) {
        viewModel.loadStageData(difficulty, stageNumber)
    }

    val uiState by viewModel.uiState.collectAsState()

    uiState.errorMessage?.let { error ->
        Text(text = error, color = if (isDarkTheme) TextColorLight else TextColorDark)
        return
    }

    if (uiState.isLoading || uiState.rows.isEmpty()) {
        // Optionally show a loading spinner
        return
    }

    // Styling constants
    val lineThickness    = 2.dp
    val headerHeight     = 48.dp
    val headerBgColor    = if (isDarkTheme) HeaderBackgroundDark else HeaderBackgroundLight
    val borderColor      = if (isDarkTheme) BorderColorDark else BorderColorLight
    val textColor        = if (isDarkTheme) TextColorDark else TextColorLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(width = lineThickness, color = borderColor, shape = RoundedCornerShape(16.dp))
    ) {
        // Header row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBgColor)
                .height(headerHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text      = uiState.headerLeft,
                modifier  = Modifier.weight(1f).padding(horizontal = 4.dp),
                style     = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                color     = textColor
            )
            Box(
                modifier = Modifier
                    .width(lineThickness)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            Text(
                text      = uiState.headerRight,
                modifier  = Modifier.weight(1f).padding(horizontal = 4.dp),
                style     = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                color     = textColor
            )
        }
        HorizontalDivider(color = borderColor, thickness = lineThickness)

        // Data rows
        uiState.rows.forEachIndexed { i, rowData ->
            Row(
                modifier          = Modifier.fillMaxWidth().height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text      = rowData.leftText,
                    modifier  = Modifier.weight(1f).padding(8.dp),
                    style     = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color     = textColor
                )
                Box(
                    modifier = Modifier
                        .width(lineThickness)
                        .fillMaxHeight()
                        .background(borderColor)
                )
                Text(
                    text      = rowData.rightText,
                    modifier  = Modifier.weight(1f).padding(8.dp),
                    style     = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color     = textColor
                )
            }
            if (i < uiState.rows.lastIndex) {
                HorizontalDivider(color = borderColor, thickness = lineThickness)
            }
        }
    }
}
