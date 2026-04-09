package com.yourapp.spendwise.mail

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class GmailAxisSyncWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val result = GmailAxisSyncManager.syncNow(applicationContext)
        return if (result.message.contains("Reconnect Gmail", ignoreCase = true)) {
            Result.retry()
        } else {
            Result.success()
        }
    }
}
