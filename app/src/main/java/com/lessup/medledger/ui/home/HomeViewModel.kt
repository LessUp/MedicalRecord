package com.lessup.medledger.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.data.entity.Visit
import com.lessup.medledger.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val visitRepository: VisitRepository
) : ViewModel() {
    val visits: StateFlow<List<Visit>> = visitRepository
        .getVisits()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun delete(visit: Visit) {
        viewModelScope.launch {
            visitRepository.delete(visit)
        }
    }
}
