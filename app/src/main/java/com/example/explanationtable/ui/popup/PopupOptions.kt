package com.example.explanationtable.ui.popup

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.explanationtable.R
import com.example.explanationtable.ui.components.OptionCard

@Composable
fun PopupOptions(
    onOptionSelected: (String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
    ) {
        OptionCard(
            label = stringResource(id = R.string.easy_step_label),
            onClick = { onOptionSelected("Easy") },
            backgroundColor = colorResource(id = R.color.easy_option_background),
            shadowColor = colorResource(id = R.color.easy_option_shadow),
            textColor = colorResource(id = R.color.easy_option_text),
            imageResId = R.drawable.emerald
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionCard(
            label = stringResource(id = R.string.medium_step_label),
            onClick = { onOptionSelected("Medium") },
            backgroundColor = colorResource(id = R.color.medium_option_background),
            shadowColor = colorResource(id = R.color.medium_option_shadow),
            textColor = colorResource(id = R.color.medium_option_text),
            imageResId = R.drawable.crown
        )
        Spacer(modifier = Modifier.height(8.dp))
        OptionCard(
            label = stringResource(id = R.string.difficult_step_label),
            onClick = { onOptionSelected("Hard") },
            backgroundColor = colorResource(id = R.color.hard_option_background),
            shadowColor = colorResource(id = R.color.hard_option_shadow),
            textColor = colorResource(id = R.color.hard_option_text),
            imageResId = R.drawable.ruby_diamond
        )
    }
}
