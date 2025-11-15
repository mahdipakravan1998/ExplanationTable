package com.example.explanationtable.ui.stages.preflight

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.explanationtable.model.Difficulty
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private const val TAG_PREFLIGHT_VM = "StagesPreflightVM"

/**
 * Shared ViewModel that stores the latest preflight readiness snapshot per difficulty.
 */
class StagesPreflightViewModel : ViewModel() {

    private val _snapshots =
        MutableStateFlow<Map<Difficulty, ReadySnapshot>>(emptyMap())

    val snapshots: StateFlow<Map<Difficulty, ReadySnapshot>> =
        _snapshots.asStateFlow()

    fun updateSnapshot(difficulty: Difficulty, snapshot: ReadySnapshot) {
        Log.d(TAG_PREFLIGHT_VM, "updateSnapshot: difficulty=$difficulty, snapshot=$snapshot")
        _snapshots.value = _snapshots.value + (difficulty to snapshot)
    }

    fun clear() {
        Log.d(TAG_PREFLIGHT_VM, "clear()")
        _snapshots.value = emptyMap()
    }
}
