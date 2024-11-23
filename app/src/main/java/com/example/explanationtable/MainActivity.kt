package com.example.explanationtable

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.example.explanationtable.ui.AppNavHost
import com.example.explanationtable.ui.theme.ExplanationTableTheme


class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ExplanationTableTheme {
                AppNavHost()
            }
        }
    }
}