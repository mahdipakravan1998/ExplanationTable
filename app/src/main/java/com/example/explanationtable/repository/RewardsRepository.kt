package com.example.explanationtable.repository

import com.example.explanationtable.data.DataStoreManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Repository responsible for mutating user rewards (e.g., diamonds).
 *
 * All write operations are executed on the IO dispatcher to keep Main thread free.
 * Public behavior is unchanged; validation and policy are deferred to DataStoreManager.
 */
class RewardsRepository(
    private val dataStore: DataStoreManager
) {

    /**
     * Increases the user's diamond balance by [amount].
     *
     * This function is intentionally thin and delegates business rules (e.g., clamping or allowing
     * negative deltas) to [DataStoreManager]. Executed on [Dispatchers.IO].
     */
    suspend fun addDiamonds(amount: Int) = withContext(Dispatchers.IO) {
        // Keep behavior identical: no local validation to avoid altering existing semantics.
        dataStore.addDiamonds(amount)
    }
}
