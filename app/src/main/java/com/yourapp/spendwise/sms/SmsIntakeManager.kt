package com.yourapp.spendwise.sms

import android.content.Context
import com.yourapp.spendwise.data.TransactionFactory
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SmsIntakeOutcome {
    data class Confirmed(val notificationId: Int) : SmsIntakeOutcome()
    data class Pending(val pendingId: Long, val notificationId: Int) : SmsIntakeOutcome()
    data object Discarded : SmsIntakeOutcome()
}

object SmsIntakeManager {
    suspend fun ingest(
        context: Context,
        sender: String,
        body: String,
        timestamp: Long,
        eventSource: String = "LIVE",
        emitNotifications: Boolean = true,
        emitPendingEvent: Boolean = true
    ): SmsIntakeOutcome = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val database = AppDatabase.getInstance(appContext)
        val transactionDao = database.transactionDao()
        val pendingSmsDao = database.pendingSmsDao()
        val reviewDao = database.smsReviewDao()
        val inspection = SmsPreFilter.inspect(sender = sender, body = body)
        val preview = SmsPreFilter.preview(sender = sender, body = body)
        val debugLog = SmsPreFilter.buildDebugLog(sender = sender, body = body)
        
        val settingsStore = com.yourapp.spendwise.data.SettingsStore(context)
        val isAiReviewEnabled = settingsStore.isAiReviewEnabled()

        if (transactionDao.exists(rawSms = body, timestamp = timestamp) || reviewDao.exists(body = body, receivedAt = timestamp)) {
            reviewDao.insert(
                SmsReviewEntity(
                    sender = sender,
                    body = body,
                    receivedAt = timestamp,
                    eventSource = eventSource,
                    prefilterDecision = inspection.resultLabel,
                    previewAmount = inspection.amount ?: 0.0,
                    previewType = inspection.type,
                    previewMerchant = inspection.merchant,
                    previewBank = inspection.bank,
                    finalStatus = "DUPLICATE_SKIPPED",
                    debugLog = "$debugLog\nfinal=duplicate_skipped"
                )
            )
            return@withContext SmsIntakeOutcome.Discarded
        }

        when (val result = SmsPreFilter.evaluate(sender, body)) {
            is PreFilterResult.Confident -> {
                val transaction = TransactionFactory.create(
                    context = appContext,
                    amount = result.amount,
                    type = result.type,
                    merchant = result.merchant,
                    bank = result.bank,
                    rawSms = body,
                    sourceSender = sender,
                    timestamp = timestamp,
                    isVerifiedByAi = false,
                    verificationSource = "Prefilter"
                )
                val insertedId = transactionDao.insert(transaction)
                if (insertedId != -1L) {
                    WidgetUpdater.updateAll(appContext)
                    reviewDao.insert(
                        SmsReviewEntity(
                            sender = sender,
                            body = body,
                            receivedAt = timestamp,
                            eventSource = eventSource,
                            prefilterDecision = inspection.resultLabel,
                            previewAmount = inspection.amount ?: 0.0,
                            previewType = inspection.type,
                            previewMerchant = inspection.merchant,
                            previewBank = inspection.bank,
                            finalStatus = "DIRECT_CONFIRMED",
                            transactionId = insertedId,
                            debugLog = "$debugLog\nfinal=direct_confirmed"
                        )
                    )
                    val notificationId = SpendWiseNotificationManager.directNotificationId(
                        rawSms = body,
                        timestamp = timestamp
                    )
                    if (emitNotifications) {
                        SpendWiseNotificationManager.showConfirmedTransaction(
                            context = appContext,
                            notificationId = notificationId,
                            transaction = transaction.copy(id = insertedId),
                            isAiVerified = false
                        )
                    }
                    SmsIntakeOutcome.Confirmed(notificationId)
                } else {
                    SmsIntakeOutcome.Discarded
                }
            }

            PreFilterResult.NeedsAiReview -> {
                if (!isAiReviewEnabled) {
                    val transaction = TransactionFactory.create(
                        context = appContext,
                        amount = inspection.amount ?: 0.0,
                        type = inspection.type ?: com.yourapp.spendwise.data.db.TransactionType.DEBIT,
                        merchant = inspection.merchant ?: "Unknown",
                        bank = inspection.bank ?: "Unknown",
                        rawSms = body,
                        sourceSender = sender,
                        timestamp = timestamp,
                        isVerifiedByAi = false,
                        verificationSource = "Prefilter-Forced"
                    )
                    val insertedId = transactionDao.insert(transaction)
                    if (insertedId != -1L) {
                        WidgetUpdater.updateAll(appContext)
                        reviewDao.insert(
                            SmsReviewEntity(
                                sender = sender,
                                body = body,
                                receivedAt = timestamp,
                                eventSource = eventSource,
                                prefilterDecision = inspection.resultLabel,
                                previewAmount = inspection.amount ?: 0.0,
                                previewType = inspection.type,
                                previewMerchant = inspection.merchant,
                                previewBank = inspection.bank,
                                finalStatus = "AI_BYPASSED_CONFIRMED",
                                transactionId = insertedId,
                                debugLog = "$debugLog\nfinal=ai_bypassed_confirmed"
                            )
                        )
                        val notificationId = SpendWiseNotificationManager.directNotificationId(
                            rawSms = body,
                            timestamp = timestamp
                        )
                        if (emitNotifications) {
                            SpendWiseNotificationManager.showConfirmedTransaction(
                                context = appContext,
                                notificationId = notificationId,
                                transaction = transaction.copy(id = insertedId),
                                isAiVerified = false
                            )
                        }
                        return@withContext SmsIntakeOutcome.Confirmed(notificationId)
                    } else {
                        return@withContext SmsIntakeOutcome.Discarded
                    }
                }

                if (pendingSmsDao.exists(sender = sender, body = body, receivedAt = timestamp)) {
                    return@withContext SmsIntakeOutcome.Discarded
                }
                val reviewEventId = reviewDao.insert(
                    SmsReviewEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        eventSource = eventSource,
                        prefilterDecision = inspection.resultLabel,
                        previewAmount = inspection.amount ?: 0.0,
                        previewType = inspection.type,
                        previewMerchant = inspection.merchant,
                        previewBank = inspection.bank,
                        finalStatus = "QUEUED_FOR_AI",
                        debugLog = "$debugLog\nfinal=queued_for_ai"
                    )
                )
                val pendingId = pendingSmsDao.insert(
                    PendingSmsEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        reviewEventId = reviewEventId
                    )
                )
                if (pendingId != -1L) {
                    val notificationId = SpendWiseNotificationManager.pendingNotificationId(pendingId)
                    if (emitNotifications) {
                        SpendWiseNotificationManager.showPendingReview(
                            context = appContext,
                            notificationId = notificationId,
                            preview = preview
                        )
                    }
                    if (emitPendingEvent) {
                        SmsPipelineEvents.notifyPendingQueued()
                    }
                    SmsIntakeOutcome.Pending(pendingId = pendingId, notificationId = notificationId)
                } else {
                    SmsIntakeOutcome.Discarded
                }
            }

            PreFilterResult.Discard -> {
                reviewDao.insert(
                    SmsReviewEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        eventSource = eventSource,
                        prefilterDecision = inspection.resultLabel,
                        previewAmount = inspection.amount ?: 0.0,
                        previewType = inspection.type,
                        previewMerchant = inspection.merchant,
                        previewBank = inspection.bank,
                        finalStatus = if (inspection.resultLabel == "SPAM_DISCARDED") "SPAM_DISCARDED" else "DISCARDED",
                        debugLog = "$debugLog\nfinal=${if (inspection.resultLabel == "SPAM_DISCARDED") "spam_discarded" else "discarded"}"
                    )
                )
                SmsIntakeOutcome.Discarded
            }
        }
    }
}
