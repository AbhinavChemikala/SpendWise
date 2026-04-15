package com.yourapp.spendwise.backup

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourapp.spendwise.background.BackupScheduler

class DriveBackupWorker(
    context: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(context, workerParams) {
    override suspend fun doWork(): Result {
        SpendWiseBackupManager(applicationContext).pushToDrive(trigger = BackupTrigger.AUTO)
        BackupScheduler.scheduleAfterRun(applicationContext)
        return Result.success()
    }
}
