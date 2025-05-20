package com.example.explanationtable.ui.rewards.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.data.DataStoreManager
import com.example.explanationtable.repository.RewardsRepository
import kotlinx.coroutines.launch

class RewardsViewModel(application: Application) : AndroidViewModel(application) {

    private val rewardsRepository = RewardsRepository(dataStore = DataStoreManager(application))

    fun addDiamonds(amount: Int) {
        viewModelScope.launch {
            rewardsRepository.addDiamonds(amount)
        }
    }
}
