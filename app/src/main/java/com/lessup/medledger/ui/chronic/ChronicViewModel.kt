package com.lessup.medledger.ui.chronic

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.data.repository.ChronicRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

@HiltViewModel
class ChronicViewModel @Inject constructor(
    private val repository: ChronicRepository
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val uiState: StateFlow<ChronicUiState> = repository.observeConditions()
        .map { ChronicUiState(conditions = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChronicUiState()
        )

    init {
        viewModelScope.launch { repository.ensurePlanReminders() }
    }

    fun createCondition(input: AddChronicInput) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                repository.createConditionWithPlan(
                    name = input.name.trim(),
                    diagnosedAt = input.diagnosedAt,
                    note = input.note?.takeIf { it.isNotBlank() },
                    planItems = input.planItems?.takeIf { it.isNotBlank() },
                    intervalMonths = input.intervalMonths,
                    startDate = input.startDate,
                    remindDaysBefore = input.remindDaysBefore
                )
            } finally {
                _isSaving.value = false
            }
        }
    }
}

data class ChronicUiState(
    val conditions: List<ChronicRepository.ConditionOverview> = emptyList()
)

data class AddChronicInput(
    val name: String,
    val planItems: String?,
    val intervalMonths: Int,
    val startDate: LocalDate,
    val remindDaysBefore: Int?,
    val diagnosedAt: LocalDate?,
    val note: String?
)
