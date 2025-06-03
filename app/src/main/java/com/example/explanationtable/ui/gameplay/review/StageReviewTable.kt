package com.example.explanationtable.ui.gameplay.review

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.explanationtable.data.easy.easyLevelComponentsData
import com.example.explanationtable.data.easy.easyLevelTables
import com.example.explanationtable.model.LevelComponentsTable
import com.example.explanationtable.model.LevelTable
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.res.stringResource
import com.example.explanationtable.R
import com.example.explanationtable.ui.theme.HeaderBackgroundLight
import com.example.explanationtable.ui.theme.HeaderBackgroundDark
import com.example.explanationtable.ui.theme.BorderColorLight
import com.example.explanationtable.ui.theme.BorderColorDark
import com.example.explanationtable.ui.theme.TextColorLight
import com.example.explanationtable.ui.theme.TextColorDark

/**
 * Displays a stage review table based on the provided stage number and theme.
 *
 * The table displays two columns with headers and rows of data extracted from
 * easyLevelComponentsData and easyLevelTables. If the data for the given stage is missing,
 * a fallback message is shown.
 *
 * @param stageNumber the stage number to display data for (1-indexed)
 * @param isDarkTheme flag indicating whether the dark theme should be used
 */
@Composable
fun StageReviewTable(
    stageNumber: Int,
    isDarkTheme: Boolean
) {
    // Constants for styling
    val lineThickness = 2.dp
    val headerHeight = 48.dp

    // Retrieve data using stageNumber (adjusted for 0-indexing)
    val componentsData: LevelComponentsTable? = easyLevelComponentsData.getOrNull(stageNumber - 1)
    val tableData: LevelTable? = easyLevelTables.getOrNull(stageNumber - 1)

    // If data is missing, display an error message and exit early.
    if (componentsData == null || tableData == null) {
        Text(text = stringResource(id = R.string.error_no_data, stageNumber))
        return
    }

    // Define colors based on the current theme.
    val headerBackgroundColor = if (isDarkTheme) HeaderBackgroundDark else HeaderBackgroundLight
    val borderColor = if (isDarkTheme) BorderColorDark else BorderColorLight
    val textColor = if (isDarkTheme) TextColorDark else TextColorLight

    // Determine the number of rows to display.
    val rowCount = componentsData.components.size

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
            // Left header using string resource R.string.header_left
            Text(
                text = stringResource(id = R.string.header_left),
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
            // Right header using string resource R.string.header_right
            Text(
                text = stringResource(id = R.string.header_right),
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

        // Loop through each row of data
        for (i in 0 until rowCount) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Max),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Retrieve left column data; default to an empty string if not found.
                val leftColumnData = componentsData.components[i]?.firstOrNull() ?: ""

                // Retrieve right column data based on the row index using a when block.
                val rightColumnData = when (i) {
                    0 -> tableData.rows[0]?.get(0)?.firstOrNull() ?: ""
                    1 -> tableData.rows[0]?.get(2)?.getOrNull(0) ?: ""
                    2 -> tableData.rows[0]?.get(2)?.getOrNull(1) ?: ""
                    3 -> tableData.rows[4]?.get(2)?.firstOrNull() ?: ""
                    else -> ""
                }

                // Left column text display.
                Text(
                    text = leftColumnData,
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
                    text = rightColumnData,
                    modifier = Modifier
                        .weight(1f)
                        .padding(8.dp),
                    style = MaterialTheme.typography.bodyLarge,
                    textAlign = TextAlign.Center,
                    color = textColor
                )
            }
            // Draw a horizontal divider between rows, except after the final row.
            if (i < rowCount - 1) {
                HorizontalDivider(color = borderColor, thickness = lineThickness)
            }
        }
    }
}
