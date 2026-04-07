package com.yourapp.spendwise.sms

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.yourapp.spendwise.R
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.absoluteValue

object SpendWiseNotificationManager {
    private const val CHANNEL_ID = "spendwise_transactions"
    private const val CHANNEL_NAME = "SpendWise alerts"
    private const val CHANNEL_DESCRIPTION = "Transaction detection and AI review updates"

    const val AI_PROCESSING_CHANNEL_ID = "spendwise_ai_processing"
    private const val AI_PROCESSING_CHANNEL_NAME = "AI Processing"
    const val AI_PROCESSING_NOTIFICATION_ID = 1

    fun ensureChannel(context: Context) {
        val manager = context.getSystemService(NotificationManager::class.java)
        if (manager.getNotificationChannel(CHANNEL_ID) != null) {
            return
        }

        manager.createNotificationChannel(
            NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = CHANNEL_DESCRIPTION
            }
        )

        if (manager.getNotificationChannel(AI_PROCESSING_CHANNEL_ID) == null) {
            manager.createNotificationChannel(
                NotificationChannel(
                    AI_PROCESSING_CHANNEL_ID,
                    AI_PROCESSING_CHANNEL_NAME,
                    NotificationManager.IMPORTANCE_LOW  // Silent — no sound/vibration
                ).apply {
                    description = "Shows while SpendWise AI is processing messages in the background"
                    setShowBadge(false)
                }
            )
        }
    }

    fun buildAiProcessingNotification(
        context: Context,
        processed: Int,
        total: Int
    ) = NotificationCompat.Builder(context, AI_PROCESSING_CHANNEL_ID)
        .setSmallIcon(R.drawable.ic_stat_spendwise)
        .setContentTitle("SpendWise AI")
        .setContentText(
            if (total > 0) "Analyzing message $processed of $total..."
            else "Preparing to analyze messages..."
        )
        .setProgress(total, processed, total == 0)
        .setOngoing(true)
        .setOnlyAlertOnce(true)
        .setSilent(true)
        .setPriority(NotificationCompat.PRIORITY_LOW)
        .build()

    fun showPendingReview(
        context: Context,
        notificationId: Int,
        preview: SmsDetectionPreview
    ) {
        if (!canNotify(context)) return

        val title = when (preview.type) {
            TransactionType.CREDIT -> "Possible income detected"
            TransactionType.DEBIT -> "Possible expense detected"
            TransactionType.UNKNOWN -> "Possible transaction detected"
        }

        val detail = buildString {
            preview.amount?.let {
                append(formatRupees(it))
                append(" detected")
            } ?: append("Transaction-like SMS detected")
            append(". SpendWise will verify it with on-device AI when the app is active.")
        }

        notify(
            context = context,
            notificationId = notificationId,
            builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_spendwise)
                .setContentTitle(title)
                .setContentText(detail)
                .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        )
    }

    fun showConfirmedTransaction(
        context: Context,
        notificationId: Int,
        transaction: TransactionEntity,
        isAiVerified: Boolean
    ) {
        if (!canNotify(context)) return

        val title = when (transaction.type) {
            TransactionType.CREDIT -> "Income detected"
            TransactionType.DEBIT -> "Expense detected"
            TransactionType.UNKNOWN -> "Transaction detected"
        }
        val detail = buildString {
            append(formatRupees(transaction.amount))
            append(
                when (transaction.type) {
                    TransactionType.CREDIT -> " received"
                    TransactionType.DEBIT -> " spent"
                    TransactionType.UNKNOWN -> " detected"
                }
            )
            append(" at ")
            append(transaction.merchant)
            if (isAiVerified) {
                append(". Verified with on-device AI.")
            }
        }

        notify(
            context = context,
            notificationId = notificationId,
            builder = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_stat_spendwise)
                .setContentTitle(title)
                .setContentText(detail)
                .setStyle(NotificationCompat.BigTextStyle().bigText(detail))
                .setAutoCancel(true)
                .setOngoing(false)
                .setOnlyAlertOnce(true)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
        )
    }

    fun dismiss(context: Context, notificationId: Int) {
        NotificationManagerCompat.from(context).cancel(notificationId)
    }

    fun pendingNotificationId(pendingId: Long): Int = 20_000 + (pendingId % 10_000).toInt()

    fun directNotificationId(rawSms: String, timestamp: Long): Int {
        return 10_000 + ("$rawSms-$timestamp").hashCode().absoluteValue % 10_000
    }

    private fun notify(
        context: Context,
        notificationId: Int,
        builder: NotificationCompat.Builder
    ) {
        NotificationManagerCompat.from(context).notify(notificationId, builder.build())
    }

    private fun canNotify(context: Context): Boolean {
        ensureChannel(context)
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.POST_NOTIFICATIONS
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun formatRupees(amount: Double): String {
        return NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)
    }
}
