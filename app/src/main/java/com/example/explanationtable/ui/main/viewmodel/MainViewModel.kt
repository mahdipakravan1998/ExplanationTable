package com.example.explanationtable.ui.main.viewmodel

import android.app.Application
import android.content.res.Configuration
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.repository.SettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class MainViewModel(application: Application) : AndroidViewModel(application) {
    // 1) Build your repo
    private val systemDarkDefault: Boolean =
        (application.resources.configuration.uiMode
                and Configuration.UI_MODE_NIGHT_MASK) ==
                Configuration.UI_MODE_NIGHT_YES

    private val settingsRepo = SettingsRepository(
        dataStore = DataStoreManager(application),
        systemDarkDefault = systemDarkDefault
    )

    // 2) Expose StateFlows from the repo
    val isDarkTheme: StateFlow<Boolean> = settingsRepo
        .isDarkTheme
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = systemDarkDefault
        )

    val diamonds: StateFlow<Int> = settingsRepo
        .diamonds
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.Eagerly,
            initialValue = 200
        )
}
