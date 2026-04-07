package com.yourapp.spendwise.sms

import android.content.Context
import android.util.Log
import com.yourapp.spendwise.data.SettingsStore
import com.yourapp.spendwise.data.TransactionFactory
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.TransactionType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext

class SmsProcessor(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val transactionDao = database.transactionDao()
    private val pendingSmsDao = database.pendingSmsDao()
    private val reviewDao = database.smsReviewDao()
    private val settingsStore = SettingsStore(appContext)

    companion object {
        private const val TAG = "SmsProcessor"
        // Singleton-level mutex — shared across ALL SmsProcessor instances so
        // two concurrent callers (e.g. foreground + pipeline event) never race.
        private val drainMutex = Mutex()
    }

    suspend fun drainPendingQueue(
        onProgress: (processed: Int, total: Int, currentSms: PendingSmsEntity?) -> Unit
    ) {
        // If the lock is already held, do nothing — another drain is active.
        if (drainMutex.isLocked) return

        drainMutex.withLock {
            val pendingSms = withContext(Dispatchers.IO) {
                pendingSmsDao.getAll()
            }

            if (pendingSms.isEmpty()) return

            pendingSms.forEachIndexed { index, pending ->
                // Report that we're starting work on this item
                onProgress(index, pendingSms.size, pending)

                val analysis = analyzeWithFallback(pending.sender, pending.body)
                val aiResult = analysis?.result

                // --- FIX: Never silently skip ---
                // If AI returned null (model error, malformed JSON, timeout),
                // mark the item as rejected with an error reason so it gets
                // cleared from the queue and never blocks future processing.
                if (analysis == null || aiResult == null) {
                    withContext(Dispatchers.IO) {
                        pendingSmsDao.deleteById(pending.id)
                        pending.reviewEventId?.let { eventId ->
                            reviewDao.updateOutcome(
                                eventId = eventId,
                                finalStatus = "AI_REJECTED",
                                transactionId = null,
                                aiJson = "",
                                aiReason = "AI model returned no response (timeout or model unavailable).",
                                aiEngine = "Unknown",
                                debugLog = "${SmsPreFilter.buildDebugLog(pending.sender, pending.body)}\nai=null\nfinal=ai_error_rejected"
                            )
                        }
                    }
                    SpendWiseNotificationManager.dismiss(
                        context = appContext,
                        notificationId = SpendWiseNotificationManager.pendingNotificationId(pending.id)
                    )
                    delay(500L)
                    return@forEachIndexed
                }

                if (!aiResult.isGenuine) {
                    withContext(Dispatchers.IO) {
                        pendingSmsDao.deleteById(pending.id)
                        pending.reviewEventId?.let { eventId ->
                            reviewDao.updateOutcome(
                                eventId = eventId,
                                finalStatus = "AI_REJECTED",
                                transactionId = null,
                                aiJson = analysis.rawResponse,
                                aiReason = aiResult.reason,
                                aiEngine = analysis.source,
                                debugLog = "${SmsPreFilter.buildDebugLog(pending.sender, pending.body)}\nai=${analysis.rawResponse}\nfinal=ai_rejected"
                            )
                        }
                    }
                    SpendWiseNotificationManager.dismiss(
                        context = appContext,
                        notificationId = SpendWiseNotificationManager.pendingNotificationId(pending.id)
                    )
                    delay(500L)
                    return@forEachIndexed
                }

                val type = aiResult.type.toTransactionType()
                if (type != TransactionType.UNKNOWN && aiResult.amount > 0.0) {
                    val transaction = pending.toTransactionEntity(
                        amount = aiResult.amount,
                        type = type,
                        merchant = aiResult.merchant.ifBlank {
                            SmsPreFilter.fallbackMerchant(pending.body)
                        },
                        bank = aiResult.bank.ifBlank {
                            SmsPreFilter.fallbackBank(pending.sender)
                        },
                        aiReason = aiResult.reason,
                        verificationSource = analysis.source,
                        aiCardLast4 = aiResult.cardLast4,
                        aiCardType  = aiResult.cardType
                    )
                    var insertedId = -1L
                    withContext(Dispatchers.IO) {
                        insertedId = transactionDao.insert(transaction)
                        pendingSmsDao.deleteById(pending.id)
                        pending.reviewEventId?.let { eventId ->
                            reviewDao.updateOutcome(
                                eventId = eventId,
                                finalStatus = "AI_CONFIRMED",
                                transactionId = insertedId.takeIf { it > 0L },
                                aiJson = analysis.rawResponse,
                                aiReason = aiResult.reason,
                                aiEngine = analysis.source,
                                debugLog = "${SmsPreFilter.buildDebugLog(pending.sender, pending.body)}\nai=${analysis.rawResponse}\nfinal=ai_confirmed"
                            )
                        }
                    }
                    SpendWiseNotificationManager.showConfirmedTransaction(
                        context = appContext,
                        notificationId = SpendWiseNotificationManager.pendingNotificationId(pending.id),
                        transaction = transaction.copy(id = insertedId.takeIf { it > 0 } ?: 0L),
                        isAiVerified = true
                    )
                } else {
                    // AI said genuine but gave unknown type or zero amount —
                    // reject cleanly so it doesn't clog the queue.
                    withContext(Dispatchers.IO) {
                        pendingSmsDao.deleteById(pending.id)
                        pending.reviewEventId?.let { eventId ->
                            reviewDao.updateOutcome(
                                eventId = eventId,
                                finalStatus = "AI_REJECTED",
                                transactionId = null,
                                aiJson = analysis.rawResponse,
                                aiReason = "AI marked genuine but could not extract type/amount.",
                                aiEngine = analysis.source,
                                debugLog = "${SmsPreFilter.buildDebugLog(pending.sender, pending.body)}\nai=${analysis.rawResponse}\nfinal=ai_ambiguous_rejected"
                            )
                        }
                    }
                    SpendWiseNotificationManager.dismiss(
                        context = appContext,
                        notificationId = SpendWiseNotificationManager.pendingNotificationId(pending.id)
                    )
                }

                delay(200L)
            }

            onProgress(pendingSms.size, pendingSms.size, null)
        }
    }

    /**
     * Tries Gemma 3 27B cloud first (if enabled and API key set).
     * Falls back to on-device Gemini Nano if cloud is disabled, offline, or fails.
     */
    private suspend fun analyzeWithFallback(
        sender: String,
        body: String
    ): AiAnalysisOutput? {
        if (settingsStore.isCloudAiEnabled()) {
            val apiKey = settingsStore.getCloudAiApiKey()
            if (apiKey.isNotBlank()) {
                val cloudResult = GemmaCloudAnalyzer.analyzeDetailed(
                    smsSender = sender,
                    smsBody = body,
                    apiKey = apiKey
                )
                if (cloudResult != null) {
                    Log.d(TAG, "Cloud AI (Gemma 3 27B) analysed: $sender")
                    return cloudResult.copy(source = "Gemma 3 27B")
                }
                Log.w(TAG, "Cloud AI failed for $sender — falling back to Nano")
            }
        }
        return GeminiNanoAnalyzer.analyzeDetailed(smsSender = sender, smsBody = body)
    }

    private fun String.toTransactionType(): TransactionType {
        return when (trim().lowercase()) {
            "debit" -> TransactionType.DEBIT
            "credit" -> TransactionType.CREDIT
            else -> TransactionType.UNKNOWN
        }
    }

    private fun PendingSmsEntity.toTransactionEntity(
        amount: Double,
        type: TransactionType,
        merchant: String,
        bank: String,
        aiReason: String,
        verificationSource: String = "Gemini Nano",
        aiCardLast4: String = "",
        aiCardType: String = ""
    ) = TransactionFactory.create(
        context = appContext,
        amount = amount,
        type = type,
        merchant = merchant,
        bank = bank,
        rawSms = body,
        sourceSender = sender,
        timestamp = receivedAt,
        isVerifiedByAi = true,
        verificationSource = verificationSource,
        aiReason = aiReason,
        aiCardLast4 = aiCardLast4,
        aiCardType  = aiCardType
    )
}
