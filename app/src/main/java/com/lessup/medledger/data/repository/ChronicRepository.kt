package com.lessup.medledger.data.repository

import android.content.Context
import com.lessup.medledger.data.dao.ChronicConditionDao
import com.lessup.medledger.data.dao.CheckupPlanDao
import com.lessup.medledger.data.entity.CheckupPlan
import com.lessup.medledger.data.entity.ChronicCondition
import com.lessup.medledger.notifications.ReminderScheduler
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.concurrent.TimeUnit

@Singleton
class ChronicRepository @Inject constructor(
    private val conditionDao: ChronicConditionDao,
    private val planDao: CheckupPlanDao,
    @ApplicationContext private val context: Context
) {

    data class PlanOverview(
        val plan: CheckupPlan,
        val nextCheckDate: LocalDate?,
        val remindDate: LocalDate?
    )

    data class ConditionOverview(
        val condition: ChronicCondition,
        val plans: List<PlanOverview>
    )

    fun observeConditions(): Flow<List<ConditionOverview>> =
        combine(
            conditionDao.observeAll(),
            planDao.observeAll()
        ) { conditions, plans ->
            conditions.map { condition ->
                val relatedPlans = plans.filter { it.conditionId == condition.id }
                ConditionOverview(
                    condition = condition,
                    plans = relatedPlans.map { plan ->
                        val schedule = computeSchedule(plan)
                        PlanOverview(
                            plan = plan,
                            nextCheckDate = schedule?.nextCheckDate,
                            remindDate = schedule?.remindDate
                        )
                    }.sortedBy { it.nextCheckDate ?: LocalDate.MAX }
                )
            }
        }

    suspend fun createConditionWithPlan(
        name: String,
        diagnosedAt: LocalDate?,
        note: String?,
        planItems: String?,
        intervalMonths: Int,
        startDate: LocalDate,
        remindDaysBefore: Int?
    ) {
        val zone = ZoneId.systemDefault()
        val diagnosedAtMillis = diagnosedAt?.atStartOfDay(zone)?.toInstant()?.toEpochMilli()
        val conditionId = conditionDao.insert(
            ChronicCondition(
                name = name,
                diagnosedAt = diagnosedAtMillis,
                department = null,
                note = note
            )
        )
        val plan = CheckupPlan(
            conditionId = conditionId,
            items = planItems,
            intervalMonths = intervalMonths,
            startDate = startDate.atStartOfDay(zone).toInstant().toEpochMilli(),
            remindDaysBefore = remindDaysBefore
        )
        val planId = planDao.insert(plan)
        scheduleReminder(conditionName = name, plan = plan.copy(id = planId))
    }

    suspend fun ensurePlanReminders() {
        val conditions = conditionDao.getAll()
        val plans = planDao.getAll()
        plans.forEach { plan ->
            val conditionName = conditions.firstOrNull { it.id == plan.conditionId }?.name ?: "复查提醒"
            scheduleReminder(conditionName, plan)
        }
    }

    private fun scheduleReminder(conditionName: String, plan: CheckupPlan) {
        val schedule = computeSchedule(plan) ?: return
        val remindAtMillis = schedule.remindAtMillis
        val nextCheckDate = schedule.nextCheckDate
        ReminderScheduler.schedulePlanReminder(
            context = context,
            planId = plan.id,
            conditionName = conditionName,
            planItems = plan.items,
            nextCheckDate = nextCheckDate,
            remindAtMillis = remindAtMillis
        )
    }

    data class PlanSchedule(
        val nextCheckDate: LocalDate,
        val remindDate: LocalDate,
        val remindAtMillis: Long
    )

    private fun computeSchedule(plan: CheckupPlan, nowMillis: Long = System.currentTimeMillis()): PlanSchedule? {
        val startDateMillis = plan.startDate ?: return null
        if (plan.intervalMonths <= 0) return null
        val zone = ZoneId.systemDefault()
        val nowDate = Instant.ofEpochMilli(nowMillis).atZone(zone).toLocalDate()
        var checkDate = Instant.ofEpochMilli(startDateMillis).atZone(zone).toLocalDate()
        if (checkDate.isBefore(nowDate)) {
            val interval = plan.intervalMonths.toLong()
            do {
                checkDate = checkDate.plusMonths(interval)
            } while (checkDate.isBefore(nowDate))
        }
        val remindDate = plan.remindDaysBefore?.let { checkDate.minusDays(it.toLong()) } ?: checkDate
        var remindAt = remindDate.atStartOfDay(zone).toInstant().toEpochMilli()
        if (remindAt <= nowMillis) {
            remindAt = nowMillis + TimeUnit.MINUTES.toMillis(5)
        }
        return PlanSchedule(
            nextCheckDate = checkDate,
            remindDate = remindDate,
            remindAtMillis = remindAt
        )
    }
}
