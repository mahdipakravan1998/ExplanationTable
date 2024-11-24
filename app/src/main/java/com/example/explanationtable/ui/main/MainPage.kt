package com.example.explanationtable.ui.main

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.explanationtable.R
import com.example.explanationtable.ui.Routes
import com.example.explanationtable.ui.Background
import com.example.explanationtable.ui.theme.bodyBoldLarge
import androidx.compose.ui.tooling.preview.Preview

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainPage(navController: NavController) {
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
                    MainContent(navController)
                }
            }
        )
    }
}

/**
 * Reusable Top Bar Composable
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainTopBar(navController: NavController) {
    TopAppBar(
        modifier = Modifier.height(112.dp),
        title = { Text(text = "") }, // Empty title
        actions = {
            TopBarAction(
                iconId = R.drawable.ic_settings,
                contentDescription = stringResource(id = R.string.settings),
                onClick = { navController.navigate(Routes.SETTINGS) }
            )
        },
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = Color.Transparent
        )
    )
}

/**
 * Reusable Top Bar Action Button
 */
@Composable
fun TopBarAction(iconId: Int, contentDescription: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .padding(top = 32.dp, end = 40.dp)
            .size(56.dp)
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Icon(
            painter = painterResource(id = iconId),
            contentDescription = contentDescription,
            tint = Color.Unspecified,
            modifier = Modifier.size(56.dp)
        )
    }
}

/**
 * Main Content Composable with Buttons
 */
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
            horizontalArrangement = Arrangement.spacedBy(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // First Button: List of Steps
            MainButton(
                iconId = R.drawable.ic_stages_list,
                label = stringResource(id = R.string.list_of_steps),
                onClick = { navController.navigate(Routes.STAGES_LIST) }
            )

            // Second Button: Start Game
            MainButton(
                iconId = R.drawable.ic_start_game,
                label = stringResource(id = R.string.start_game),
                onClick = { navController.navigate(Routes.STAGES_LIST) }
            )
        }

        Spacer(modifier = Modifier.weight(1f))
    }
}

/**
 * Reusable Button with Icon and Label
 */
@Composable
fun MainButton(iconId: Int, label: String, onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable { onClick() }
    ) {
        Box(
            modifier = Modifier.size(136.dp)
        ) {
            Icon(
                painter = painterResource(id = iconId),
                contentDescription = label,
                tint = Color.Unspecified,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(bottom = 20.dp)
            )
        }
        Text(
            text = label,
            style = MaterialTheme.typography.bodyBoldLarge,
            textAlign = TextAlign.Center
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
