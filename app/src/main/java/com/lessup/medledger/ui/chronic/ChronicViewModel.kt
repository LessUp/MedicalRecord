package com.lessup.medledger.ui.chronic

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.model.ConditionOverview
import com.lessup.medledger.notifications.ReminderScheduler
import com.lessup.medledger.repository.ChronicRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

class ChronicViewModel(
    private val repository: ChronicRepository,
    private val context: Context
) : ViewModel() {

    private val _isSaving = MutableStateFlow(false)
    val isSaving: StateFlow<Boolean> = _isSaving.asStateFlow()

    val uiState: StateFlow<ChronicUiState> = repository.observeConditions()
        .onEach { scheduleReminders(it) }
        .map { ChronicUiState(conditions = it) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5_000),
            initialValue = ChronicUiState()
        )

    fun createCondition(input: AddChronicInput) {
        if (_isSaving.value) return
        viewModelScope.launch {
            _isSaving.value = true
            try {
                val zone = ZoneId.systemDefault()
                val startDateMillis = input.startDate.atStartOfDay(zone).toInstant().toEpochMilli()
                val diagnosedAtMillis = input.diagnosedAt?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
                repository.createConditionWithPlan(
                    name = input.name.trim(),
                    diagnosedAt = diagnosedAtMillis,
                    note = input.note?.takeIf { it.isNotBlank() },
                    planItems = input.planItems?.takeIf { it.isNotBlank() },
                    intervalMonths = input.intervalMonths,
                    startDate = startDateMillis,
                    remindDaysBefore = input.remindDaysBefore
                )
            } finally {
                _isSaving.value = false
            }
        }
    }

    fun deleteCondition(conditionId: Long) {
        viewModelScope.launch {
            repository.deleteCondition(conditionId)
        }
    }

    private fun scheduleReminders(conditions: List<ConditionOverview>) {
        val nowMillis = System.currentTimeMillis()
        val zone = ZoneId.systemDefault()
        conditions.forEach { overview ->
            val conditionName = overview.condition.name
            overview.plans.forEach { plan ->
                val nextCheckMillis = plan.nextCheckDate ?: return@forEach
                val nextCheckDate = Instant.ofEpochMilli(nextCheckMillis)
                    .atZone(zone)
                    .toLocalDate()
                val remindAtMillis = (plan.remindDate ?: nextCheckMillis).let { remindAt ->
                    if (remindAt <= nowMillis) nowMillis + TimeUnit.MINUTES.toMillis(5) else remindAt
                }
                ReminderScheduler.schedulePlanReminder(
                    context = context,
                    planId = plan.plan.localId,
                    conditionName = conditionName,
                    planItems = plan.plan.items,
                    nextCheckDate = nextCheckDate,
                    remindAtMillis = remindAtMillis
                )
            }
        }
    }
}

data class ChronicUiState(
    val conditions: List<ConditionOverview> = emptyList()
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
