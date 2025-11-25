package com.lessup.medledger.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.data.entity.Visit
import com.lessup.medledger.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class CalendarViewModel @Inject constructor(
    visitRepository: VisitRepository
) : ViewModel() {
    val visits: StateFlow<List<Visit>> = visitRepository
        .getVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
}
