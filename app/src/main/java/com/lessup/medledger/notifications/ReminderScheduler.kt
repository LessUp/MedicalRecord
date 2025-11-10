package com.lessup.medledger.notifications

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.concurrent.TimeUnit
import kotlin.math.max

object ReminderScheduler {
    private const val WORK_PREFIX = "checkup_plan_"
    private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

    fun schedulePlanReminder(
        context: Context,
        planId: Long,
        conditionName: String,
        planItems: String?,
        nextCheckDate: LocalDate,
        remindAtMillis: Long
    ) {
        val now = System.currentTimeMillis()
        val delay = max(0L, remindAtMillis - now)
        val content = buildContent(conditionName, planItems, nextCheckDate)
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(
                workDataOf(
                    ReminderWorker.KEY_TITLE to "$conditionName 复查提醒",
                    ReminderWorker.KEY_CONTENT to content
                )
            )
            .addTag(WORK_PREFIX + planId)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            WORK_PREFIX + planId,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun buildContent(conditionName: String, planItems: String?, checkDate: LocalDate): String {
        val date = checkDate.format(dateFormatter)
        val detail = planItems?.takeIf { it.isNotBlank() }?.let { "复查项目：$it" }
        return listOfNotNull("$conditionName 下一次复查日期：$date", detail).joinToString("\n")
    }
}
