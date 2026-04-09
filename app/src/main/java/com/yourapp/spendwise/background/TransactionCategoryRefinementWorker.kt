package com.yourapp.spendwise.background

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.yourapp.spendwise.data.TransactionRepository

class TransactionCategoryRefinementWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result {
        val transactionId = inputData.getLong(KEY_TRANSACTION_ID, -1L)
        if (transactionId <= 0L) {
            return Result.success()
        }
        TransactionRepository(applicationContext).refineTransactionCategory(transactionId)
        return Result.success()
    }

    companion object {
        private const val KEY_TRANSACTION_ID = "transaction_id"

        fun enqueue(context: Context, transactionId: Long) {
            if (transactionId <= 0L) return
            val request = OneTimeWorkRequestBuilder<TransactionCategoryRefinementWorker>()
                .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
                .setInputData(workDataOf(KEY_TRANSACTION_ID to transactionId))
                .build()
            WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
                "transaction_category_refine_$transactionId",
                ExistingWorkPolicy.REPLACE,
                request
            )
        }
    }
}
