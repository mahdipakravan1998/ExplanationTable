package com.example.explanationtable.ui.main

import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.navigation.NavController
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.Background
import com.example.explanationtable.R // Make sure this is imported to access the drawable resources
import androidx.compose.ui.graphics.Color

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController) {
    Background { // Wrapping with the reusable background composable
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(text = "") }, // Empty text to hide the title
                    actions = {
                        Box(
                            modifier = Modifier
                                .size(64.dp) // Adjust size as necessary
                                .clickable { navController.navigate(Routes.SETTINGS) }
                        ) {
                            Icon(
                                painter = painterResource(id = R.drawable.ic_settings),
                                contentDescription = "Settings",
                                tint = Color.Unspecified, // Use original colors of the icon
                                modifier = Modifier.fillMaxSize()
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
            ) {
                MainContent(navController)
            }
        }
    }
}

@Composable
fun MainContent(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(bottom = 48.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Spacer(modifier = Modifier.weight(1f))

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { navController.navigate(Routes.STAGES_LIST) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_start_game),
                    contentDescription = "Start Game",
                    tint = Color.Unspecified, // Use original colors of the icon
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.width(8.dp)) // Reduced spacer for closer icons

            Box(
                modifier = Modifier
                    .size(120.dp)
                    .clickable { navController.navigate(Routes.STAGES_LIST) }
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_stages_list),
                    contentDescription = "Game Stages List",
                    tint = Color.Unspecified, // Use original colors of the icon
                    modifier = Modifier.fillMaxSize()
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}



@Preview
@Composable
fun MainPagePreview() {
    // If you're using a specific theme, wrap your MainPage composable in that theme.
    MaterialTheme {
        MainPage(navController = rememberNavController())
    }
}
