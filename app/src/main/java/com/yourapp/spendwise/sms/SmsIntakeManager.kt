package com.yourapp.spendwise.sms

import android.content.Context
import com.yourapp.spendwise.background.TransactionCategoryRefinementWorker
import com.yourapp.spendwise.data.LocationCache
import com.yourapp.spendwise.data.LocationHelper
import com.yourapp.spendwise.data.TransactionFactory
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.widget.WidgetUpdater
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

sealed class SmsIntakeOutcome {
    data class Confirmed(val notificationId: Int) : SmsIntakeOutcome()
    data class Pending(val pendingId: Long, val notificationId: Int) : SmsIntakeOutcome()
    data object Discarded : SmsIntakeOutcome()
}

object SmsIntakeManager {
    suspend fun ingestEmailCandidate(
        context: Context,
        sender: String,
        body: String,
        timestamp: Long,
        amount: Double,
        type: TransactionType,
        merchant: String,
        bank: String,
        eventSource: String = "EMAIL",
        emitNotifications: Boolean = false,
        emitPendingEvent: Boolean = true,
        latitude: Double? = null,
        longitude: Double? = null
    ): SmsIntakeOutcome = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val database = AppDatabase.getInstance(appContext)
        val transactionDao = database.transactionDao()
        val pendingSmsDao = database.pendingSmsDao()
        val reviewDao = database.smsReviewDao()
        val settingsStore = com.yourapp.spendwise.data.SettingsStore(appContext)
        val isAiReviewEnabled = settingsStore.isAiReviewEnabled()
        val resolvedMerchant = merchant.ifBlank { SmsPreFilter.fallbackMerchant(body) }
        val resolvedBank = bank.ifBlank { "Axis Bank" }
        val debugLog = buildString {
            appendLine("sender=$sender")
            appendLine("source=$eventSource")
            appendLine("prefilter=email_bypassed")
            appendLine("amount=$amount")
            appendLine("type=${type.name}")
            appendLine("merchant=$resolvedMerchant")
            append("bank=$resolvedBank")
        }

        if (transactionDao.exists(rawSms = body, timestamp = timestamp) || reviewDao.exists(body = body, receivedAt = timestamp)) {
            reviewDao.insert(
                SmsReviewEntity(
                    sender = sender,
                    body = body,
                    receivedAt = timestamp,
                    eventSource = eventSource,
                    prefilterDecision = "EMAIL_DUPLICATE",
                    previewAmount = amount,
                    previewType = type,
                    previewMerchant = resolvedMerchant,
                    previewBank = resolvedBank,
                    finalStatus = "DUPLICATE_SKIPPED",
                    debugLog = "$debugLog\nfinal=duplicate_skipped"
                )
            )
            LocationCache.evict(body, timestamp)
            return@withContext SmsIntakeOutcome.Discarded
        }

        if (!isAiReviewEnabled) {
            // Use coords supplied by the caller (snapshotted when the sync job started).
            // This covers the Spark-mail path where one location serves the whole email batch.
            val buildResult = TransactionFactory.build(
                context = appContext,
                amount = amount,
                type = type,
                merchant = resolvedMerchant,
                bank = resolvedBank,
                rawSms = body,
                sourceSender = sender,
                timestamp = timestamp,
                isVerifiedByAi = false,
                verificationSource = "Email Intake",
                latitude = latitude,
                longitude = longitude
            )
            val transaction = buildResult.transaction ?: run {
                reviewDao.insert(
                    SmsReviewEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        eventSource = eventSource,
                        prefilterDecision = "EMAIL_RULE_EXCLUDED",
                        previewAmount = amount,
                        previewType = type,
                        previewMerchant = resolvedMerchant,
                        previewBank = resolvedBank,
                        finalStatus = "RULE_SKIPPED",
                        aiReason = "Skipped by rule: ${buildResult.excludedByRule.ruleDisplayName()}",
                        debugLog = "$debugLog\nrule=${buildResult.excludedByRule.ruleDisplayName()}\nfinal=rule_skipped"
                    )
                )
                LocationCache.evict(body, timestamp)
                return@withContext SmsIntakeOutcome.Discarded
            }
            val insertedId = transactionDao.insert(transaction)
            if (insertedId != -1L) {
                TransactionCategoryRefinementWorker.enqueue(appContext, insertedId)
                WidgetUpdater.updateAll(appContext)
                reviewDao.insert(
                    SmsReviewEntity(
                        sender = sender,
                        body = body,
                        receivedAt = timestamp,
                        eventSource = eventSource,
                        prefilterDecision = "EMAIL_DIRECT",
                        previewAmount = amount,
                        previewType = type,
                        previewMerchant = resolvedMerchant,
                        previewBank = resolvedBank,
                        finalStatus = "AI_BYPASSED_CONFIRMED",
                        transactionId = insertedId,
                        debugLog = "$debugLog\nfinal=email_confirmed_no_ai"
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
            }
            return@withContext SmsIntakeOutcome.Discarded
        }

        if (pendingSmsDao.exists(sender = sender, body = body, receivedAt = timestamp)) {
            return@withContext SmsIntakeOutcome.Discarded
        }

        // Seed the location cache so SmsProcessor can pick it up when AI confirms.
        if (latitude != null && longitude != null) {
            LocationCache.put(body, timestamp, latitude to longitude)
        }

        val reviewEventId = reviewDao.insert(
            SmsReviewEntity(
                sender = sender,
                body = body,
                receivedAt = timestamp,
                eventSource = eventSource,
                prefilterDecision = "EMAIL_QUEUED",
                previewAmount = amount,
                previewType = type,
                previewMerchant = resolvedMerchant,
                previewBank = resolvedBank,
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
                SpendWiseNotificationManager.showEmailPendingReview(
                    context = appContext,
                    notificationId = notificationId,
                    preview = SmsDetectionPreview(
                        amount = amount,
                        type = type,
                        merchant = resolvedMerchant,
                        bank = resolvedBank
                    ),
                    senderLabel = "Axis Bank email"
                )
            }
            if (emitPendingEvent) {
                SmsPipelineEvents.notifyPendingQueued()
            }
            return@withContext SmsIntakeOutcome.Pending(pendingId = pendingId, notificationId = notificationId)
        }
        SmsIntakeOutcome.Discarded
    }

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

        // Duplicate: evict any cached location for this SMS
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
            LocationCache.evict(body, timestamp)
            return@withContext SmsIntakeOutcome.Discarded
        }

        when (val result = SmsPreFilter.evaluate(sender, body)) {
            is PreFilterResult.Confident -> {
                val (lat, lng) = LocationCache.pop(body, timestamp) ?: (null to null)
                val buildResult = TransactionFactory.build(
                    context = appContext,
                    amount = result.amount,
                    type = result.type,
                    merchant = result.merchant,
                    bank = result.bank,
                    rawSms = body,
                    sourceSender = sender,
                    timestamp = timestamp,
                    isVerifiedByAi = false,
                    verificationSource = "Prefilter",
                    latitude = lat,
                    longitude = lng
                )
                val transaction = buildResult.transaction ?: run {
                    reviewDao.insert(
                        SmsReviewEntity(
                            sender = sender,
                            body = body,
                            receivedAt = timestamp,
                            eventSource = eventSource,
                            prefilterDecision = "RULE_EXCLUDED",
                            previewAmount = inspection.amount ?: 0.0,
                            previewType = inspection.type,
                            previewMerchant = inspection.merchant,
                            previewBank = inspection.bank,
                            finalStatus = "RULE_SKIPPED",
                            aiReason = "Skipped by rule: ${buildResult.excludedByRule.ruleDisplayName()}",
                            debugLog = "$debugLog\nrule=${buildResult.excludedByRule.ruleDisplayName()}\nfinal=rule_skipped"
                        )
                    )
                    LocationCache.evict(body, timestamp)
                    return@withContext SmsIntakeOutcome.Discarded
                }
                val insertedId = transactionDao.insert(transaction)
                if (insertedId != -1L) {
                    TransactionCategoryRefinementWorker.enqueue(appContext, insertedId)
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
                    val (lat, lng) = LocationCache.pop(body, timestamp) ?: (null to null)
                    val buildResult = TransactionFactory.build(
                        context = appContext,
                        amount = inspection.amount ?: 0.0,
                        type = inspection.type ?: com.yourapp.spendwise.data.db.TransactionType.DEBIT,
                        merchant = inspection.merchant ?: "Unknown",
                        bank = inspection.bank ?: "Unknown",
                        rawSms = body,
                        sourceSender = sender,
                        timestamp = timestamp,
                        isVerifiedByAi = false,
                        verificationSource = "Prefilter-Forced",
                        latitude = lat,
                        longitude = lng
                    )
                    val transaction = buildResult.transaction ?: run {
                        reviewDao.insert(
                            SmsReviewEntity(
                                sender = sender,
                                body = body,
                                receivedAt = timestamp,
                                eventSource = eventSource,
                                prefilterDecision = "RULE_EXCLUDED",
                                previewAmount = inspection.amount ?: 0.0,
                                previewType = inspection.type,
                                previewMerchant = inspection.merchant,
                                previewBank = inspection.bank,
                                finalStatus = "RULE_SKIPPED",
                                aiReason = "Skipped by rule: ${buildResult.excludedByRule.ruleDisplayName()}",
                                debugLog = "$debugLog\nrule=${buildResult.excludedByRule.ruleDisplayName()}\nfinal=rule_skipped"
                            )
                        )
                        LocationCache.evict(body, timestamp)
                        return@withContext SmsIntakeOutcome.Discarded
                    }
                    val insertedId = transactionDao.insert(transaction)
                    if (insertedId != -1L) {
                        TransactionCategoryRefinementWorker.enqueue(appContext, insertedId)
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

    private fun com.yourapp.spendwise.data.TransactionRule?.ruleDisplayName(): String {
        return this?.name?.trim()?.takeIf { it.isNotBlank() } ?: "Untitled exclusion rule"
    }
}
