package com.lessup.medledger.repository

import com.lessup.medledger.db.MedLedgerDatabase
import com.lessup.medledger.model.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.datetime.*

class ChronicRepository(
    private val database: MedLedgerDatabase
) {
    private val conditionQueries get() = database.chronicConditionQueries
    private val planQueries get() = database.checkupPlanQueries
    
    fun observeConditions(): Flow<List<ConditionOverview>> {
        val conditionsFlow = conditionQueries.selectAll().asFlow().mapToList()
        val plansFlow = planQueries.selectAll().asFlow().mapToList()
        
        return combine(conditionsFlow, plansFlow) { conditions, plans ->
            conditions.map { condition ->
                val relatedPlans = plans.filter { it.conditionId == condition.localId }
                ConditionOverview(
                    condition = condition.toModel(),
                    plans = relatedPlans.map { plan ->
                        val schedule = computeSchedule(plan.toModel())
                        PlanOverview(
                            plan = plan.toModel(),
                            nextCheckDate = schedule?.nextCheckDate,
                            remindDate = schedule?.remindDate
                        )
                    }.sortedBy { it.nextCheckDate ?: Long.MAX_VALUE }
                )
            }
        }
    }
    
    suspend fun getConditionById(id: Long): ChronicCondition? {
        return conditionQueries.selectById(id).executeAsOneOrNull()?.toModel()
    }
    
    suspend fun createConditionWithPlan(
        name: String,
        diagnosedAt: Long?,
        note: String?,
        planItems: String?,
        intervalMonths: Int,
        startDate: Long,
        remindDaysBefore: Int?
    ): Long {
        val now = currentTimeMillis()
        
        // 创建慢病记录
        conditionQueries.insert(
            remoteId = null,
            userId = null,
            memberId = null,
            name = name,
            diagnosedAt = diagnosedAt,
            department = null,
            note = note,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
            version = 1
        )
        val conditionId = conditionQueries.getLastInsertId().executeAsOne()
        
        // 创建复查计划
        planQueries.insert(
            remoteId = null,
            userId = null,
            conditionId = conditionId,
            items = planItems,
            intervalMonths = intervalMonths.toLong(),
            startDate = startDate,
            remindDaysBefore = remindDaysBefore?.toLong(),
            lastRemindAt = null,
            createdAt = now,
            updatedAt = now,
            syncStatus = SyncStatus.PENDING.name,
            version = 1
        )
        
        return conditionId
    }
    
    suspend fun deleteCondition(conditionId: Long) {
        val now = currentTimeMillis()
        // 先删除关联的复查计划
        planQueries.deleteByCondition(conditionId)
        // 再软删除慢病记录
        conditionQueries.softDelete(deletedAt = now, updatedAt = now, localId = conditionId)
    }
    
    private data class PlanSchedule(
        val nextCheckDate: Long,
        val remindDate: Long
    )
    
    private fun computeSchedule(plan: CheckupPlan): PlanSchedule? {
        val startDate = plan.startDate ?: return null
        if (plan.intervalMonths <= 0) return null
        
        val now = currentTimeMillis()
        val tz = TimeZone.currentSystemDefault()
        val nowDate = Instant.fromEpochMilliseconds(now).toLocalDateTime(tz).date
        var checkDate = Instant.fromEpochMilliseconds(startDate).toLocalDateTime(tz).date
        
        // 找到下一个复查日期
        while (checkDate < nowDate) {
            checkDate = checkDate.plus(plan.intervalMonths, DateTimeUnit.MONTH)
        }
        
        // 计算提醒日期
        val remindDate = plan.remindDaysBefore?.let {
            checkDate.minus(it, DateTimeUnit.DAY)
        } ?: checkDate
        
        return PlanSchedule(
            nextCheckDate = checkDate.atStartOfDayIn(tz).toEpochMilliseconds(),
            remindDate = remindDate.atStartOfDayIn(tz).toEpochMilliseconds()
        )
    }
    
    private fun com.lessup.medledger.db.ChronicCondition.toModel() = ChronicCondition(
        localId = localId,
        remoteId = remoteId,
        userId = userId,
        memberId = memberId,
        name = name,
        diagnosedAt = diagnosedAt,
        department = department,
        note = note,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        version = version
    )
    
    private fun com.lessup.medledger.db.CheckupPlan.toModel() = CheckupPlan(
        localId = localId,
        remoteId = remoteId,
        userId = userId,
        conditionId = conditionId,
        items = items,
        intervalMonths = intervalMonths.toInt(),
        startDate = startDate,
        remindDaysBefore = remindDaysBefore?.toInt(),
        lastRemindAt = lastRemindAt,
        createdAt = createdAt,
        updatedAt = updatedAt,
        deletedAt = deletedAt,
        syncStatus = SyncStatus.valueOf(syncStatus),
        version = version
    )
}
