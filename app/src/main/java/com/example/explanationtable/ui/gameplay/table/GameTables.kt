package com.example.explanationtable.ui.gameplay.table

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.explanationtable.model.Difficulty

/**
 * Top-level composable that decides which table layout to show
 * based on the current [difficulty].
 */
@Composable
fun GameTable(
    difficulty: Difficulty,
    modifier: Modifier = Modifier
) {
    when (difficulty) {
        Difficulty.EASY -> EasyThreeByFiveTable(modifier)
        Difficulty.MEDIUM -> MediumTablePlaceholder(modifier)
        Difficulty.HARD -> HardTablePlaceholder(modifier)
    }
}

/**
 * Easy level table layout (3 columns by 5 rows).
 * This is the old "ThreeByFiveTable".
 */
@Composable
fun EasyThreeByFiveTable(modifier: Modifier = Modifier) {
    // Sample letters for Type3 squares
    val type3Letters = listOf(
        "A", "B", "C", "D", "E",
        "F", "G", "H", "I", "J",
        "K", "L"
    )
    val type3Iterator = type3Letters.iterator()

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        for (rowIndex in 0 until 5) { // 5 rows
            Row(
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.wrapContentWidth(),
            ) {
                for (colIndex in 0 until 3) { // 3 columns
                    Box {
                        when (Pair(rowIndex, colIndex)) {
                            Pair(0, 0) -> {
                                Type1Square(text = "1")
                            }
                            Pair(0, 2) -> {
                                Type2Square(topText = "Top", bottomText = "Right")
                            }
                            Pair(4, 2) -> {
                                Type1Square(text = "1")
                            }
                            Pair(0, 1) -> {
                                // Square (0,1) with DirectionalSign0_1
                                Box {
                                    Type3Square(
                                        letter = if (type3Iterator.hasNext()) type3Iterator.next() else "?",
                                    )
                                    DirectionalSign0_1(
                                        modifier = Modifier
                                            .align(Alignment.TopEnd)
                                            .padding(end = 4.dp, top = 4.dp)
                                    )
                                }
                            }
                            Pair(1, 0) -> {
                                // Square (1,0) with DirectionalSign1_0
                                Box {
                                    Type3Square(
                                        letter = if (type3Iterator.hasNext()) type3Iterator.next() else "?",
                                    )
                                    DirectionalSign1_0(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                            Pair(1, 2) -> {
                                // Square (1,2) with DirectionalSign1_2
                                Box {
                                    Type3Square(
                                        letter = if (type3Iterator.hasNext()) type3Iterator.next() else "?",
                                    )
                                    DirectionalSign1_2(
                                        modifier = Modifier
                                            .align(Alignment.TopCenter)
                                            .padding(top = 4.dp)
                                    )
                                }
                            }
                            Pair(3, 2) -> {
                                // Square (3,2) with DirectionalSign3_2
                                Box {
                                    Type3Square(
                                        letter = if (type3Iterator.hasNext()) type3Iterator.next() else "?",
                                    )
                                    DirectionalSign3_2(
                                        modifier = Modifier
                                            .align(Alignment.BottomCenter)
                                            .padding(bottom = 5.dp)
                                    )
                                }
                            }
                            else -> {
                                // All other cells: Type3Square
                                Type3Square(
                                    letter = if (type3Iterator.hasNext()) type3Iterator.next() else "?",
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Placeholder for a future Medium table layout.
 * Only the order/arrangement of cells will differ.
 */
@Composable
fun MediumTablePlaceholder(modifier: Modifier = Modifier) {
    // TODO: Implement medium table arrangement here
    Box(modifier = modifier) {
        // Could be 3x5, 4x5, etc. with different arrangement
        // Just a placeholder to show how you can expand in the future
    }
}

/**
 * Placeholder for a future Hard table layout.
 * Only the order/arrangement of cells will differ.
 */
@Composable
fun HardTablePlaceholder(modifier: Modifier = Modifier) {
    // TODO: Implement hard table arrangement here
    Box(modifier = modifier) {
        // Could be 5x5 or something else
    }
}

/**
 * Preview of the Easy 3x5 table.
 */
@Preview(showBackground = true)
@Composable
fun EasyThreeByFiveTablePreview() {
    EasyThreeByFiveTable()
}

/**
 * Preview of the delegating composable for the EASY difficulty.
 */
@Preview(showBackground = true)
@Composable
fun GameTablePreview() {
    GameTable(difficulty = Difficulty.EASY)
}
