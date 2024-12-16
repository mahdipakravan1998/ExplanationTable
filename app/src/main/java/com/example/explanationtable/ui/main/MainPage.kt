package com.example.explanationtable.ui.main

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.components.AppTopBar
import com.example.explanationtable.ui.main.components.MainContent
import com.example.explanationtable.ui.popup.PopupOptions

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController) {
    var showDialog by remember { mutableStateOf(false) }
    var selectedOption by remember { mutableStateOf<String?>(null) }

    Background {
        Scaffold(
            topBar = {
                AppTopBar(
                    isHomePage = true,
                    onSettingsClick = { navController.navigate(Routes.SETTINGS) }
                )
            },
            containerColor = Color.Transparent,
            content = { paddingValues ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                ) {
                    // 'onListClicked' triggers showing the popup
                    MainContent(onListClicked = { showDialog = true })
                }

                // AlertDialog for choosing difficulty
                if (showDialog) {
                    AlertDialog(
                        onDismissRequest = { showDialog = false },
                        title = {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .wrapContentSize(align = Alignment.TopEnd)
                            ) {
                                IconButton(
                                    onClick = { showDialog = false },
                                    modifier = Modifier
                                        .size(24.dp)
                                        .padding(4.dp)
                                        .align(Alignment.TopEnd)
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
                                    // Navigate with the chosen difficulty in the route
                                    navController.navigate("${Routes.STAGES_LIST}/$option")
                                }
                            )
                        },
                        confirmButton = {},
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
