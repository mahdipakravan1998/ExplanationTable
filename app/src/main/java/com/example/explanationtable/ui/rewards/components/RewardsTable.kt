package com.example.explanationtable.ui.rewards.components

import android.annotation.SuppressLint
import android.graphics.Paint
import android.graphics.Rect
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.core.content.res.ResourcesCompat
import com.example.explanationtable.R
import com.example.explanationtable.ui.rewards.viewmodel.RewardsViewModel
import com.example.explanationtable.ui.theme.BorderDark
import com.example.explanationtable.ui.theme.BorderLight
import com.example.explanationtable.ui.theme.Eel
import com.example.explanationtable.ui.theme.Hare
import com.example.explanationtable.ui.theme.NeutralColorDark
import com.example.explanationtable.ui.theme.ShabnamFontFamily
import com.example.explanationtable.ui.theme.SpecialFillGlowColor
import com.example.explanationtable.ui.theme.TitleTextColorDark
import com.example.explanationtable.ui.theme.SpecialFillColor
import com.example.explanationtable.ui.theme.StrokeColor
import kotlin.math.roundToInt

// Data class holding layout parameters for the progress bar.
data class ProgressBarLayout(
    val totalWidthPx: Float,
    val totalHeightPx: Float,
    val filledWidthPx: Float,
    val filledLeftPx: Float,
    val textLeftPx: Float,
    val textRightPx: Float,
    val glowWidth: Dp,
    val glowHeight: Dp,
    val isCovered: Boolean
)

/**
 * Computes the layout parameters needed for the custom progress bar.
 *
 * @param progress Current progress as a fraction (0f to 1f)
 * @param maxWidth Maximum width of the progress bar (Dp)
 * @param maxHeight Maximum height of the progress bar (Dp)
 * @param textWidthPx Measured width of the score text in pixels
 * @param density Current screen density for conversion between Dp and pixels
 * @param glowPadding Padding around the glow effect (Dp)
 * @param glowHeightFraction Fraction of maxHeight used for the glow effect's height
 * @return A [ProgressBarLayout] containing calculated dimensions and flags.
 */
@Composable
private fun calculateProgressBarLayout(
    progress: Float,
    maxWidth: Dp,
    maxHeight: Dp,
    textWidthPx: Float,
    density: Density,
    glowPadding: Dp,
    glowHeightFraction: Float
): ProgressBarLayout {
    // Convert dimensions from Dp to pixels
    val totalWidthPx = with(density) { maxWidth.toPx() }
    val totalHeightPx = with(density) { maxHeight.toPx() }

    // Calculate filled section dimensions based on progress
    val filledWidthPx = totalWidthPx * progress
    val filledLeftPx = totalWidthPx - filledWidthPx

    // Center the score text horizontally
    val textLeftPx = (totalWidthPx - textWidthPx) / 2f
    val textRightPx = textLeftPx + textWidthPx

    // Determine if the filled section overlaps the text region
    val isCovered = filledLeftPx < textRightPx

    // Calculate glow effect dimensions if progress is at least 25%
    val glowWidth = if (progress >= 0.25f) {
        val calculatedWidth = (maxWidth * progress) - (glowPadding * 2f)
        if (calculatedWidth > 0.dp) calculatedWidth else 0.dp
    } else {
        0.dp
    }
    val glowHeight = if (progress >= 0.25f && glowWidth > 0.dp) {
        maxHeight * glowHeightFraction
    } else {
        0.dp
    }

    return ProgressBarLayout(
        totalWidthPx = totalWidthPx,
        totalHeightPx = totalHeightPx,
        filledWidthPx = filledWidthPx,
        filledLeftPx = filledLeftPx,
        textLeftPx = textLeftPx,
        textRightPx = textRightPx,
        glowWidth = glowWidth,
        glowHeight = glowHeight,
        isCovered = isCovered
    )
}

/**
 * A custom progress bar that includes a glow effect when progress exceeds 25%.
 * It overlays a score text centered on top of the progress bar, with the text color
 * changing based on whether it overlaps the filled region.
 *
 * @param progress Progress value between 0f and 1f.
 * @param borderColor Color used for the border/background of the progress bar.
 * @param score Score to display at the center of the bar.
 * @param isDarkTheme Boolean flag to determine color schemes for dark/light themes.
 * @param modifier Modifier for layout customization.
 * @param glowPadding Padding around the glow effect (default 20.dp).
 * @param glowHeightFraction Fraction of bar height for the glow effect (default 0.27f).
 * @param glowVerticalOffset Vertical offset for the glow effect (default 6.dp).
 */
@SuppressLint("UnusedBoxWithConstraintsScope")
@Composable
fun ProgressBarComponent(
    progress: Float,
    borderColor: Color,
    score: Int,
    isDarkTheme: Boolean,
    modifier: Modifier = Modifier,
    glowPadding: Dp = 20.dp,
    glowHeightFraction: Float = 0.27f,
    glowVerticalOffset: Dp = 6.dp
) {
    // Define the text style for the score
    val textStyle = TextStyle(
        fontSize = 18.sp,
        fontFamily = ShabnamFontFamily,
        fontWeight = FontWeight.Bold
    )

    // Measure the width of the score text for precise layout positioning
    val textMeasurer = rememberTextMeasurer()
    val textLayoutResult = remember(score, textStyle) {
        textMeasurer.measure(
            text = AnnotatedString(score.toString()),
            style = textStyle
        )
    }
    val textWidthPx = textLayoutResult.size.width.toFloat()

    // Define color palette based on theme and design specifications
    val neutralColor = if (isDarkTheme) NeutralColorDark else Hare
    val strokeWidth = 12f

    // Select the appropriate font resource based on the text style
    val fontResource = when (textStyle.fontFamily) {
        ShabnamFontFamily -> when (textStyle.fontWeight) {
            FontWeight.Thin -> R.font.shabnam_thin_fd_wol
            FontWeight.Light -> R.font.shabnam_light_fd_wol
            FontWeight.Normal -> R.font.shabnam_fd_wol
            FontWeight.Medium -> R.font.shabnam_medium_fd_wol
            FontWeight.Bold -> R.font.shabnam_bold_fd_wol
            else -> R.font.shabnam_fd_wol
        }
        else -> R.font.shabnam_fd_wol
    }
    val context = LocalContext.current
    val typeface = remember(fontResource) { ResourcesCompat.getFont(context, fontResource) }

    val density = LocalDensity.current

    // BoxWithConstraints provides access to parent constraints for responsive layout
    BoxWithConstraints(modifier = modifier) {
        // Calculate dynamic layout parameters for the progress bar
        val layout = calculateProgressBarLayout(
            progress = progress,
            maxWidth = maxWidth,
            maxHeight = maxHeight,
            textWidthPx = textWidthPx,
            density = density,
            glowPadding = glowPadding,
            glowHeightFraction = glowHeightFraction
        )

        // Define the shape of the progress bar with rounded ends on the right side
        val progressBarShape = RoundedCornerShape(
            topStart = 0.dp,
            topEnd = 12.dp,
            bottomStart = 0.dp,
            bottomEnd = 12.dp
        )

        // Draw the background layer of the progress bar using the border color
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(borderColor, shape = progressBarShape)
        )

        // Draw the progress fill layer on the right side
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.CenterEnd
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(progress)
                    .fillMaxHeight()
                    .background(
                        color = StrokeColor,
                        shape = progressBarShape
                    )
            ) {
                // Render the glow effect if applicable
                if (layout.glowWidth > 0.dp && layout.glowHeight > 0.dp) {
                    Box(
                        modifier = Modifier
                            .offset(x = glowPadding, y = glowVerticalOffset)
                            .width(layout.glowWidth)
                            .height(layout.glowHeight)
                            .background(
                                color = SpecialFillGlowColor,
                                shape = RoundedCornerShape(12.dp)
                            )
                    )
                }
            }
        }

        // Overlay the score text, splitting the drawing into unfilled and filled regions
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                // Prepare the Paint object for drawing text
                val paint = Paint().apply {
                    isAntiAlias = true
                    textSize = textStyle.fontSize.toPx()
                    this.typeface = typeface
                }

                // Determine text position so it is centered
                val bounds = Rect()
                val scoreString = "$score / 10"
                paint.getTextBounds(scoreString, 0, scoreString.length, bounds)
                val textWidth = bounds.width().toFloat()
                val x = (size.width - textWidth) / 2f - bounds.left.toFloat()
                val y = size.height / 2f - (bounds.top + bounds.bottom) / 2f

                val filledLeftPx = layout.filledLeftPx

                // Draw text in the unfilled region with a neutral color
                drawContext.canvas.save()
                drawContext.canvas.clipRect(0f, 0f, filledLeftPx, size.height)
                paint.color = neutralColor.toArgb()
                paint.style = Paint.Style.FILL
                drawContext.canvas.nativeCanvas.drawText(scoreString, x, y, paint)
                drawContext.canvas.restore()

                // Draw text in the filled region using a stroke and fill for a highlighted effect
                drawContext.canvas.save()
                drawContext.canvas.clipRect(filledLeftPx, 0f, size.width, size.height)
                // Draw stroke
                paint.color = StrokeColor.toArgb()
                paint.style = Paint.Style.STROKE
                paint.strokeWidth = strokeWidth
                drawContext.canvas.nativeCanvas.drawText(scoreString, x, y, paint)
                // Draw fill on top of the stroke
                paint.color = SpecialFillColor.toArgb()
                paint.style = Paint.Style.FILL
                drawContext.canvas.nativeCanvas.drawText(scoreString, x, y, paint)
                drawContext.canvas.restore()
            }
        }
    }
}

/**
 * Represents a single row in the rewards table.
 * Each row includes a title, a custom progress bar with score, and icons.
 *
 * @param isDarkTheme Flag indicating if the dark theme is active.
 * @param title The title text for the row.
 * @param progress Progress value for the progress bar (0f to 1f).
 * @param score The score to display within the progress bar.
 * @param borderColor Color used for the progress bar border/background.
 * @param titleColor Color of the title text.
 * @param rowIcon Icon displayed on the right of the row.
 * @param awardIcon Icon displayed on the left alongside the progress bar.
 */
@Composable
fun TableRowItem(
    isDarkTheme: Boolean,
    title: String,
    progress: Float,
    score: Int,
    borderColor: Color,
    titleColor: Color,
    rowIcon: Painter,
    awardIcon: Painter
) {
    // Constant for the size of the award icon
    val awardIconSize = 48.dp

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 20.dp, end = 4.dp, top = 4.dp, bottom = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left section containing title, progress bar, and award icon
        ConstraintLayout(modifier = Modifier.weight(1f)) {
            val (titleRef, progressRef, awardIconRef) = createRefs()

            // Title text at the top right of the left section
            Text(
                text = title,
                color = titleColor,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Black
                ),
                modifier = Modifier.constrainAs(titleRef) {
                    top.linkTo(parent.top)
                    end.linkTo(parent.end)
                    width = Dimension.wrapContent
                }
            )

            // Custom progress bar placed below the title, slightly overlapping the award icon
            ProgressBarComponent(
                progress = progress,
                borderColor = borderColor,
                score = score,
                isDarkTheme = isDarkTheme,
                modifier = Modifier
                    .height(20.dp)
                    .constrainAs(progressRef) {
                        top.linkTo(titleRef.bottom, margin = 12.dp)
                        start.linkTo(awardIconRef.end, margin = (-8).dp)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )

            // Award icon on the left side
            Icon(
                painter = awardIcon,
                contentDescription = null,
                tint = Color.Unspecified,
                modifier = Modifier
                    .offset(y = 8.dp)
                    .size(awardIconSize)
                    .constrainAs(awardIconRef) {
                        start.linkTo(parent.start)
                        bottom.linkTo(progressRef.bottom)
                    }
            )
        }

        // Right section: a separate row icon
        Icon(
            painter = rowIcon,
            contentDescription = null,
            tint = Color.Unspecified,
            modifier = Modifier
                .padding(20.dp)
                .size(52.dp)
        )
    }
}

/**
 * Displays a rewards table containing multiple rows with a border and dividers.
 * Each row shows a title, a progress bar with a score, and associated icons.
 *
 * @param isDarkTheme Flag to toggle between dark and light theme styling.
 * @param modifier Modifier for overall table styling.
 */
@Composable
fun RewardsTable(
    isDarkTheme: Boolean,
    optimalMoves: Int,
    userAccuracy: Int,
    playerMoves: Int,
    elapsedTime: Long,
    modifier: Modifier = Modifier,
    viewModel: RewardsViewModel
) {
    val tableBorderColor = if (isDarkTheme) BorderDark else BorderLight
    val titleTextColor = if (isDarkTheme) TitleTextColorDark else Eel
    val rowIcon1 = painterResource(id = R.drawable.ic_step_complete)
    val rowIcon2 = painterResource(id = R.drawable.ic_accuracy)
    val rowIcon3 = painterResource(id = R.drawable.ic_speed)
    val awardIconBarrel = if (isDarkTheme) {
        painterResource(id = R.drawable.ic_diamond_barrel_dark)
    } else {
        painterResource(id = R.drawable.ic_diamond_barrel_light)
    }
    val rowIconBook = painterResource(id = R.drawable.ic_book)
    val awardIconFund = if (isDarkTheme) {
        painterResource(id = R.drawable.ic_diamond_fund_dark)
    } else {
        painterResource(id = R.drawable.ic_diamond_fund_light)
    }
    val tableShape = RoundedCornerShape(24.dp)
    val borderWidth = 2.dp

    // Determine accuracyScore:
    // Method 1: if optimalMoves is available (non-zero), use optimalMoves/playerMoves.
    // Otherwise, use the fallback userAccuracy value.
    val accuracyScore = if (optimalMoves > 0) {
        (optimalMoves.toFloat() / playerMoves * 10).coerceAtMost(10f)
    } else {
        userAccuracy.toFloat()
    }

    val precisionScore = 10f
    val elapsedSec = elapsedTime / 1000f
    val targetTime = 120f
    val maxTime = 300f
    val speedScore = when {
        elapsedSec <= targetTime -> 10f
        elapsedSec >= maxTime -> 0f
        else -> 10f * ((maxTime - elapsedSec) / (maxTime - targetTime))
    }
    val stageScore = (accuracyScore + precisionScore + speedScore) / 3f

    // Update diamonds based on the score
    val stageDiamonds = stageScore.roundToInt()
    LaunchedEffect(stageDiamonds) {
        viewModel.addDiamonds(stageDiamonds) // Add diamonds when stage is completed
    }

    Column {
        Box(
            modifier = Modifier
                .border(width = borderWidth, color = tableBorderColor, shape = tableShape)
                .clip(tableShape)
        ) {
            Column {
                TableRowItem(
                    isDarkTheme = isDarkTheme,
                    title = stringResource(id = R.string.score_precision),
                    progress = precisionScore / 10,
                    score = precisionScore.roundToInt(),
                    borderColor = tableBorderColor,
                    titleColor = titleTextColor,
                    rowIcon = rowIcon1,
                    awardIcon = awardIconBarrel
                )
                HorizontalDivider(color = tableBorderColor, thickness = borderWidth)
                TableRowItem(
                    isDarkTheme = isDarkTheme,
                    title = stringResource(id = R.string.score_accuracy),
                    progress = accuracyScore / 10,
                    score = accuracyScore.roundToInt(),
                    borderColor = tableBorderColor,
                    titleColor = titleTextColor,
                    rowIcon = rowIcon2,
                    awardIcon = awardIconBarrel
                )
                HorizontalDivider(color = tableBorderColor, thickness = borderWidth)
                TableRowItem(
                    isDarkTheme = isDarkTheme,
                    title = stringResource(id = R.string.score_speed),
                    progress = speedScore / 10,
                    score = speedScore.roundToInt(),
                    borderColor = tableBorderColor,
                    titleColor = titleTextColor,
                    rowIcon = rowIcon3,
                    awardIcon = awardIconBarrel
                )
            }
        }
        Spacer(modifier = Modifier.height(12.dp))
        Box(
            modifier = Modifier
                .border(width = borderWidth, color = tableBorderColor, shape = tableShape)
                .clip(tableShape)
        ) {
            TableRowItem(
                isDarkTheme = isDarkTheme,
                title = stringResource(id = R.string.score_stage),
                progress = stageScore / 10,
                score = stageScore.roundToInt(),
                borderColor = tableBorderColor,
                titleColor = titleTextColor,
                rowIcon = rowIconBook,
                awardIcon = awardIconFund
            )
        }
    }
}
