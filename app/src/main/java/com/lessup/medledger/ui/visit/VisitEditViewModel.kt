package com.lessup.medledger.ui.visit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.data.entity.Visit
import com.lessup.medledger.data.repository.VisitRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.ZoneId

data class VisitEditUiState(
    val id: Long? = null,
    val dateText: String = LocalDate.now().toString(),
    val hospital: String = "",
    val department: String = "",
    val doctor: String = "",
    val items: String = "",
    val costText: String = "",
    val note: String = "",
    val error: String? = null,
    val saving: Boolean = false
)

@HiltViewModel
class VisitEditViewModel @Inject constructor(
    private val repo: VisitRepository
) : ViewModel() {

    private val _ui = MutableStateFlow(VisitEditUiState())
    val ui: StateFlow<VisitEditUiState> = _ui

    fun load(id: Long?) {
        if (id == null) return
        viewModelScope.launch {
            val v = repo.getById(id) ?: return@launch
            _ui.value = _ui.value.copy(
                id = v.id,
                dateText = LocalDate.ofEpochDay(v.date / 86_400_000L).toString(),
                hospital = v.hospital,
                department = v.department.orEmpty(),
                doctor = v.doctor.orEmpty(),
                items = v.items.orEmpty(),
                costText = v.cost?.toString().orEmpty(),
                note = v.note.orEmpty(),
                error = null
            )
        }
    }

    fun update(block: (VisitEditUiState) -> VisitEditUiState) {
        _ui.value = block(_ui.value)
    }

    fun save(onSaved: () -> Unit) {
        val s = _ui.value
        if (s.hospital.isBlank()) {
            _ui.value = s.copy(error = "请输入医院名称")
            return
        }
        viewModelScope.launch {
            _ui.value = s.copy(saving = true, error = null)
            val date = runCatching {
                LocalDate.parse(s.dateText).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
            }.getOrElse { System.currentTimeMillis() }
            val cost = s.costText.toDoubleOrNull()
            val entity = Visit(
                id = s.id ?: 0L,
                date = date,
                hospital = s.hospital.trim(),
                department = s.department.trim().ifEmpty { null },
                doctor = s.doctor.trim().ifEmpty { null },
                items = s.items.trim().ifEmpty { null },
                cost = cost,
                note = s.note.trim().ifEmpty { null }
            )
            repo.upsert(entity)
            onSaved()
        }
    }
}
