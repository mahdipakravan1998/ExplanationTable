package com.example.explanationtable.ui.hint.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.explanationtable.R
import com.example.explanationtable.model.HintOption
import com.example.explanationtable.model.Difficulty
import com.example.explanationtable.model.easy.EasyLevelTable
import com.example.explanationtable.repository.HintRepository
import com.example.explanationtable.ui.gameplay.table.CellPosition
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class HintViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = HintRepository(application)

    private val _hintOptions = MutableStateFlow<List<HintOption>>(emptyList())
    val hintOptions: StateFlow<List<HintOption>> = _hintOptions.asStateFlow()

    private val _selectedCells = MutableSharedFlow<List<CellPosition>>()
    val selectedCells: SharedFlow<List<CellPosition>> = _selectedCells.asSharedFlow()

    private var originalTable: EasyLevelTable? = null
    private var currentTable: MutableMap<CellPosition, List<String>>? = null

    private val _difficulty = MutableStateFlow(Difficulty.EASY)
    val difficulty: StateFlow<Difficulty> = _difficulty.asStateFlow()

    fun loadHintOptions() {
        viewModelScope.launch {
            _hintOptions.value = repository.getHintOptions()
        }
    }

    fun setOriginalTableState(table: EasyLevelTable?) {
        originalTable = table
    }

    fun setCurrentTableState(table: MutableMap<CellPosition, List<String>>?) {
        currentTable = table
    }

    fun setDifficulty(diff: Difficulty) {
        _difficulty.value = diff
    }

    fun onOptionSelected(option: HintOption) {
        viewModelScope.launch {
            val ctx = getApplication<Application>()
            val cells = when (option.displayText) {
                ctx.getString(R.string.hint_single_word) ->
                    originalTable?.let { orig ->
                        currentTable?.let { curr ->
                            repository.revealRandomCategory(curr, orig)
                        }
                    } ?: emptyList()
                ctx.getString(R.string.hint_single_letter) ->
                    originalTable?.let { orig ->
                        currentTable?.let { curr ->
                            repository.revealRandomCell(curr, orig)
                        }
                    } ?: emptyList()
                ctx.getString(R.string.hint_complete_stage) ->
                    emptyList()
                else -> emptyList()
            }
            _selectedCells.emit(cells)
        }
    }
}
