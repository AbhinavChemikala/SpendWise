package com.yourapp.spendwise.background

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.yourapp.spendwise.data.SettingsStore
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object DailyReminderScheduler {
    private const val UNIQUE_WORK_NAME = "daily_summary"

    fun scheduleNext(
        context: Context,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        val appContext = context.applicationContext
        val settingsStore = SettingsStore(appContext)
        val workManager = WorkManager.getInstance(appContext)

        if (!settingsStore.isDailyReminderEnabled()) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val delayMillis = nextDelayMillis(
            hour = settingsStore.getDailyReminderHour(),
            minute = settingsStore.getDailyReminderMinute()
        )
        val request = OneTimeWorkRequestBuilder<DailySummaryWorker>()
            .setInitialDelay(delayMillis, TimeUnit.MILLISECONDS)
            .build()

        workManager.enqueueUniqueWork(
            UNIQUE_WORK_NAME,
            policy,
            request
        )
    }

    fun scheduleAfterRun(context: Context) {
        scheduleNext(
            context = context,
            policy = ExistingWorkPolicy.APPEND_OR_REPLACE
        )
    }

    private fun nextDelayMillis(hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val reminderTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        var nextRun = now.toLocalDate().atTime(reminderTime).atZone(zone)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        return Duration.between(now, nextRun).toMillis().coerceAtLeast(1_000L)
    }
}
