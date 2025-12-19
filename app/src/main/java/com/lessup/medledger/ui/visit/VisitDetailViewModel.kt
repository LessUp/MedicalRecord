package com.lessup.medledger.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.model.Document
import com.lessup.medledger.model.Visit
import com.lessup.medledger.repository.DocumentRepository
import com.lessup.medledger.repository.VisitRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow as KStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class VisitDetailViewModel(
    private val visitRepo: VisitRepository,
    private val docRepo: DocumentRepository
) : ViewModel() {

    private val _visit = MutableStateFlow<Visit?>(null)
    val visit: StateFlow<Visit?> = _visit.asStateFlow()

    private var _docs: KStateFlow<List<Document>>? = null
    val docs: KStateFlow<List<Document>>?
        get() = _docs

    fun load(visitId: Long) {
        viewModelScope.launch {
            _visit.value = visitRepo.getById(visitId)
        }
        _docs = docRepo.observeByVisit(visitId).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())
    }
}
