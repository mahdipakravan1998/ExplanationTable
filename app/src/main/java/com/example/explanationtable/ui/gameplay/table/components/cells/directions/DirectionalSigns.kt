package com.example.explanationtable.ui.gameplay.table.components.cells.directions

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.explanationtable.ui.main.viewmodel.MainViewModel
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.TextDarkMode

/**
 * Directional arrow sign composables (pure drawing, no text).
 */
@Composable
fun DirectionalSign0_1(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = signColor,
            start = Offset(x = size.width, y = size.height / 2),
            end = Offset(x = size.width - 9.dp.toPx(), y = size.height / 2),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrowStartX = size.width - 9.dp.toPx()
        val arrowStartY = size.height / 2
        val arrowEndY = arrowStartY + 10.dp.toPx()

        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing down
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX - 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX + 3.dp.toPx(), y = arrowEndY - 3.dp.toPx()),
            end = Offset(x = arrowStartX, y = arrowEndY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_0(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(10.dp)) {
        val stroke = 2.dp.toPx()
        val arrowHeight = size.height
        val arrowWidth = size.width

        // Draw vertical line
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2, y = 0f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight * 0.7f),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 - 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
            start = Offset(x = arrowWidth / 2 + 2.dp.toPx(), y = arrowHeight * 0.7f),
            end = Offset(x = arrowWidth / 2, y = arrowHeight),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}

@Composable
fun DirectionalSign1_2(modifier: Modifier = Modifier) {
    // Reuse DirectionalSign1_0
    DirectionalSign1_0(modifier)
}

@Composable
fun DirectionalSign3_2(modifier: Modifier = Modifier) {
    val mainViewModel: MainViewModel = viewModel()
    val isDarkTheme by mainViewModel.isDarkTheme.collectAsState()
    val signColor = if (isDarkTheme) TextDarkMode else Eel

    Canvas(modifier = modifier.size(20.dp)) {
        val stroke = 2.dp.toPx()
        drawLine(
            color = signColor,
            start = Offset(x = size.width / 2, y = size.height),
            end = Offset(x = size.width / 2, y = size.height - 9.dp.toPx()),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        val arrowStartX = size.width / 2
        val arrowStartY = size.height - 9.dp.toPx()
        val arrowEndX = arrowStartX - 10.dp.toPx()

        // Horizontal line
        drawLine(
            color = signColor,
            start = Offset(x = arrowStartX, y = arrowStartY),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        // Arrowhead pointing left
        drawLine(
            color = signColor,
            start = Offset(x = arrowEndX + 3.dp.toPx(), y = arrowStartY - 3.dp.toPx()),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
        drawLine(
            color = signColor,
            start = Offset(x = arrowEndX + 3.dp.toPx(), y = arrowStartY + 3.dp.toPx()),
            end = Offset(x = arrowEndX, y = arrowStartY),
            strokeWidth = stroke,
            cap = StrokeCap.Round
        )
    }
}