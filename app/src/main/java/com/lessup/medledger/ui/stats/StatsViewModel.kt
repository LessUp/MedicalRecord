package com.lessup.medledger.ui.stats

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lessup.medledger.repository.VisitRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

data class StatsUiState(
    val selectedYear: Int = LocalDate.now().year,
    val yearlyTotal: Double = 0.0,
    val yearlyVisitCount: Int = 0,
    val avgCostPerVisit: Double = 0.0,
    val monthlyData: List<MonthlyStats> = emptyList(),
    val departmentData: List<DepartmentStats> = emptyList(),
    val hospitalData: List<HospitalStats> = emptyList(),
    val isLoading: Boolean = false
)

class StatsViewModel(
    private val visitRepository: VisitRepository
) : ViewModel() {
    
    private val _uiState = MutableStateFlow(StatsUiState())
    val uiState: StateFlow<StatsUiState> = _uiState.asStateFlow()
    
    init {
        loadStats()
    }
    
    fun setYear(year: Int) {
        _uiState.update { it.copy(selectedYear = year) }
        loadStats()
    }
    
    fun showYearPicker() {
        // TODO: 显示年份选择对话框
    }
    
    private fun loadStats() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            visitRepository.observeAll().collect { visits ->
                val year = _uiState.value.selectedYear
                val zone = ZoneId.systemDefault()
                
                // 筛选当年的就诊记录
                val yearVisits = visits.filter { visit ->
                    val date = Instant.ofEpochMilli(visit.date).atZone(zone).toLocalDate()
                    date.year == year
                }
                
                // 计算年度总费用
                val yearlyTotal = yearVisits.mapNotNull { it.cost }.sum()
                val yearlyVisitCount = yearVisits.size
                val avgCost = if (yearlyVisitCount > 0) yearlyTotal / yearlyVisitCount else 0.0
                
                // 按月统计
                val monthlyData = (1..12).map { month ->
                    val monthVisits = yearVisits.filter { visit ->
                        val date = Instant.ofEpochMilli(visit.date).atZone(zone).toLocalDate()
                        date.monthValue == month
                    }
                    MonthlyStats(
                        month = month,
                        cost = monthVisits.mapNotNull { it.cost }.sum(),
                        visitCount = monthVisits.size
                    )
                }
                
                // 按科室统计
                val departmentData = yearVisits
                    .filter { it.department != null }
                    .groupBy { it.department!! }
                    .map { (dept, deptVisits) ->
                        DepartmentStats(
                            department = dept,
                            cost = deptVisits.mapNotNull { it.cost }.sum(),
                            visitCount = deptVisits.size
                        )
                    }
                    .sortedByDescending { it.cost }
                
                // 按医院统计
                val hospitalData = yearVisits
                    .groupBy { it.hospital }
                    .map { (hospital, hospitalVisits) ->
                        HospitalStats(
                            hospital = hospital,
                            cost = hospitalVisits.mapNotNull { it.cost }.sum(),
                            visitCount = hospitalVisits.size
                        )
                    }
                    .sortedByDescending { it.cost }
                    .take(10)
                
                _uiState.update {
                    it.copy(
                        yearlyTotal = yearlyTotal,
                        yearlyVisitCount = yearlyVisitCount,
                        avgCostPerVisit = avgCost,
                        monthlyData = monthlyData,
                        departmentData = departmentData,
                        hospitalData = hospitalData,
                        isLoading = false
                    )
                }
            }
        }
    }
}
