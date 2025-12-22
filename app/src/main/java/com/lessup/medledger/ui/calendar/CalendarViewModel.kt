package com.lessup.medledger.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.model.Visit
import com.lessup.medledger.repository.VisitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class CalendarViewModel(
    visitRepository: VisitRepository
) : ViewModel() {
    val visits: StateFlow<List<Visit>> = visitRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
