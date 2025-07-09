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
import com.example.explanationtable.ui.review.viewmodel.StageReviewViewModel
import com.example.explanationtable.ui.theme.BorderColorDark
import com.example.explanationtable.ui.theme.BorderColorLight
import com.example.explanationtable.ui.theme.HeaderBackgroundDark
import com.example.explanationtable.ui.theme.HeaderBackgroundLight
import com.example.explanationtable.ui.theme.TextColorDark
import com.example.explanationtable.ui.theme.TextColorLight

/**
 * Displays a stage review table based on the provided stage number and theme.
 *
 * This composable is the View in an MVVM architecture. It observes state from
 * [StageReviewViewModel] and renders the UI. It contains no business logic.
 *
 * @param stageNumber The stage number to display data for (1-indexed).
 * @param isDarkTheme Flag indicating whether the dark theme should be used.
 * @param viewModel The ViewModel that provides state for this composable.
 */
@Composable
fun StageReviewTable(
    stageNumber: Int,
    isDarkTheme: Boolean,
    viewModel: StageReviewViewModel = viewModel()
) {
    // Trigger data loading when the stageNumber changes
    LaunchedEffect(stageNumber) {
        viewModel.loadStageData(stageNumber)
    }

    val uiState by viewModel.uiState.collectAsState()

    // Display an error message if one exists
    uiState.errorMessage?.let { error ->
        Text(text = error)
        return
    }

    // Do not render the table if it's loading or has no data
    if (uiState.isLoading || uiState.rows.isEmpty()) {
        // You can optionally show a loading indicator here
        return
    }

    // Constants for styling
    val lineThickness = 2.dp
    val headerHeight = 48.dp

    // Define colors based on the current theme.
    val headerBackgroundColor = if (isDarkTheme) HeaderBackgroundDark else HeaderBackgroundLight
    val borderColor = if (isDarkTheme) BorderColorDark else BorderColorLight
    val textColor = if (isDarkTheme) TextColorDark else TextColorLight

    // Main container with padding, rounded corners, and a border.
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 32.dp)
            .clip(RoundedCornerShape(16.dp))
            .border(
                width = lineThickness,
                color = borderColor,
                shape = RoundedCornerShape(16.dp)
            )
    ) {
        // Table Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(headerBackgroundColor)
                .height(headerHeight),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Left header from uiState
            Text(
                text = uiState.headerLeft,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                color = textColor
            )
            // Vertical divider between headers
            Box(
                modifier = Modifier
                    .width(lineThickness)
                    .fillMaxHeight()
                    .background(borderColor)
            )
            // Right header from uiState
            Text(
                text = uiState.headerRight,
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 4.dp),
                style = MaterialTheme.typography.titleLarge.copy(fontSize = 16.sp),
                textAlign = TextAlign.Center,
                color = textColor
            )
        }
        // Horizontal divider below header
        HorizontalDivider(color = borderColor, thickness = lineThickness)

        // Loop through each row of data from uiState
        uiState.rows.forEachIndexed { i, rowData ->
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left column text display.
                Text(
                    text = rowData.leftText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
                // Vertical divider between columns.
                Box(
                    modifier = Modifier
                        .width(lineThickness)
                        .fillMaxHeight()
                        .background(borderColor)
                )
                // Right column text display.
                Text(
                    text = rowData.rightText,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }
            // Draw a horizontal divider between rows, except after the final row.
            if (i < uiState.rows.size - 1) {
                HorizontalDivider(color = borderColor, thickness = lineThickness)
            }
        }
    }
}