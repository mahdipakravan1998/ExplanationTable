package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager

/**
 * Repository responsible for managing rewards data.
 */
class RewardsRepository(
    private val dataStore: DataStoreManager
) {
    suspend fun addDiamonds(amount: Int) {
        dataStore.addDiamonds(amount)
    }

    suspend fun spendDiamonds(amount: Int) {
        dataStore.spendDiamonds(amount)
    }
}
