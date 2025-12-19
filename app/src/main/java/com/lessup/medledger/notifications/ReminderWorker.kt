package com.lessup.medledger.notifications

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        val title = inputData.getString(KEY_TITLE) ?: "复查提醒"
        val content = inputData.getString(KEY_CONTENT) ?: "请关注您的复查计划"
        NotificationHelper.showReminder(
            applicationContext,
            title = title,
            content = content
        )
        return Result.success()
    }

    companion object {
        const val KEY_TITLE = "reminder_title"
        const val KEY_CONTENT = "reminder_content"
    }
}
