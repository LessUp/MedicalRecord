package com.lessup.medledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.model.Visit
import com.lessup.medledger.repository.VisitRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class HomeViewModel(
    private val visitRepository: VisitRepository
) : ViewModel() {
    val visits: StateFlow<List<Visit>> = visitRepository
        .observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(visit: Visit) {
        viewModelScope.launch {
            visitRepository.delete(visit.localId)
        }
    }
}
