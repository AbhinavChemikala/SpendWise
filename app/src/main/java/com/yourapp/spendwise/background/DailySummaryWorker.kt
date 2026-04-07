package com.yourapp.spendwise.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.sms.SpendWiseNotificationManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.ZoneId

class DailySummaryWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val database = AppDatabase.getInstance(applicationContext)
        val transactionDao = database.transactionDao()
        
        // Get start and end of today in ms
        val today = LocalDate.now()
        val startMs = today.atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli()
        val endMs = today.plusDays(1).atStartOfDay(ZoneId.systemDefault()).toInstant().toEpochMilli() - 1

        val summary = transactionDao.getMonthlySummary(startMs, endMs)
        val count = transactionDao.getTransactionCount(startMs, endMs).count

        SpendWiseNotificationManager.showDailySummary(
            context = applicationContext,
            totalSpent = summary.totalSpent,
            transactionCount = count
        )

        Result.success()
    }
}
