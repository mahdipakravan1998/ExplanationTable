package com.example.explanationtable.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.ui.Background
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.*
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.components.MainTopBar
import com.example.explanationtable.ui.popup.PopupOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Background {
        Scaffold(
            topBar = { MainTopBar(navController) }, // Extracted top bar
            containerColor = Color.Transparent, // Ensure transparent scaffold background
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    MainContent(navController, onListClicked = { showDialog = true })
                }

                // AlertDialog to show options (Centered)
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(align = Alignment.TopEnd) // Prevent Box from taking too much space
                            ) {
                                IconButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier
                                        .size(24.dp) // Control the size of the icon
                                        .padding(4.dp) // Use minimal padding to control spacing
                                        .align(Alignment.TopEnd) // Align to top-right corner
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Close,
                                        contentDescription = stringResource(id = R.string.close)
                                    )
                                }
                            }
                        },
                        text = {
                            PopupOptions(
                                onOptionSelected = { option ->
                                    selectedOption = option
                                    showDialog = false
                                }
                            )
                        },
                        confirmButton = { /* Remove the confirm button entirely */ },
                        containerColor = MaterialTheme.colorScheme.surface,
                        titleContentColor = MaterialTheme.colorScheme.onSurface,
                        textContentColor = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        )
    }
}

@Preview
@Composable
fun MainPagePreview() {
    MaterialTheme {
        MainPage(navController = rememberNavController())
    }
}