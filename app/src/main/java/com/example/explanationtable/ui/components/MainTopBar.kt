package com.example.explanationtable.ui.components

import androidx.compose.foundation.layout.height
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.graphics.Color
import androidx.navigation.NavController
import androidx.compose.ui.res.stringResource
import com.example.explanationtable.R
import com.example.explanationtable.ui.Routes

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
