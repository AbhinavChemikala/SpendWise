package com.yourapp.spendwise.background

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.yourapp.spendwise.backup.DriveBackupWorker
import com.yourapp.spendwise.data.SettingsStore
import java.time.Duration
import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import java.util.concurrent.TimeUnit

object BackupScheduler {
    private const val UNIQUE_WORK_NAME = "drive_backup_daily"

    fun scheduleNext(
        context: Context,
        policy: ExistingWorkPolicy = ExistingWorkPolicy.REPLACE
    ) {
        val appContext = context.applicationContext
        val settingsStore = SettingsStore(appContext)
        val workManager = WorkManager.getInstance(appContext)

        if (!settingsStore.isDriveBackupAutoEnabled() || settingsStore.getDriveBackupAccount().isBlank()) {
            workManager.cancelUniqueWork(UNIQUE_WORK_NAME)
            return
        }

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = OneTimeWorkRequestBuilder<DriveBackupWorker>()
            .setInitialDelay(
                nextDelayMillis(
                    hour = settingsStore.getDriveBackupHour(),
                    minute = settingsStore.getDriveBackupMinute()
                ),
                TimeUnit.MILLISECONDS
            )
            .setConstraints(constraints)
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

    fun cancel(context: Context) {
        WorkManager.getInstance(context.applicationContext).cancelUniqueWork(UNIQUE_WORK_NAME)
    }

    private fun nextDelayMillis(hour: Int, minute: Int): Long {
        val zone = ZoneId.systemDefault()
        val now = ZonedDateTime.now(zone)
        val backupTime = LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        var nextRun = now.toLocalDate().atTime(backupTime).atZone(zone)

        if (!nextRun.isAfter(now)) {
            nextRun = nextRun.plusDays(1)
        }

        return Duration.between(now, nextRun).toMillis().coerceAtLeast(1_000L)
    }
}
