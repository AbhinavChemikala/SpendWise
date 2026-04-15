package com.yourapp.spendwise.data

import android.content.Context
import android.net.Uri
import android.provider.Telephony
import android.telephony.SmsManager
import com.google.gson.Gson
import com.yourapp.spendwise.backup.BackupHistoryEntry
import com.yourapp.spendwise.backup.BackupResult
import com.yourapp.spendwise.backup.BackupTrigger
import com.yourapp.spendwise.backup.SpendWiseBackupManager
import com.yourapp.spendwise.background.BackupScheduler
import com.yourapp.spendwise.background.DailyReminderScheduler
import com.yourapp.spendwise.background.TransactionCategoryRefinementWorker
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.CategoryTotal
import com.yourapp.spendwise.data.db.MonthlySummary
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionCategoryAiEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.mail.AxisEmailSyncResult
import com.yourapp.spendwise.mail.AxisEmailSyncHistoryEntry
import com.yourapp.spendwise.mail.AxisEmailSyncTrigger
import com.yourapp.spendwise.mail.GmailAxisSyncManager
import com.yourapp.spendwise.mail.NotificationAccessHelper
import com.yourapp.spendwise.sms.SmsIntakeOutcome
import com.yourapp.spendwise.sms.SmsIntakeManager
import com.yourapp.spendwise.sms.SmsProcessor
import com.yourapp.spendwise.sms.AiProcessingService
import com.yourapp.spendwise.sms.TransactionCategoryRefiner
import com.yourapp.spendwise.widget.WidgetUpdater
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

data class InsightFact(
    val title: String,
    val body: String
)

data class TrendPoint(
    val monthLabel: String,
    val expense: Double,
    val income: Double
)

data class PaymentModeTotal(
    val mode: String,
    val amount: Double,
    val transactionCount: Int
)

data class MerchantAnalytics(
    val merchant: String,
    val totalAmount: Double,
    val transactionCount: Int
)

data class DuplicateInsight(
    val merchant: String,
    val amount: Double,
    val type: TransactionType,
    val transactionIds: List<Long>,
    val transactionCount: Int,
    val latestTimestamp: Long
)

data class RecurringInsight(
    val merchant: String,
    val category: String,
    val amountEstimate: Double,
    val occurrences: Int,
    val lastSeenTimestamp: Long
)

data class SpecialTrackingSummary(
    val salaryCredits: Double,
    val refundCredits: Double,
    val cashWithdrawals: Double
)

enum class SummaryRangeType {
    TODAY,
    LAST_7_DAYS,
    THIS_MONTH,
    LAST_MONTH
}

data class BudgetProgress(
    val category: String,
    val monthlyLimit: Double,
    val spentAmount: Double,
    val progress: Double,
    val isExceeded: Boolean
)

data class AnomalyAlert(
    val title: String,
    val body: String,
    val severity: String
)

data class CashflowDay(
    val label: String,
    val dateEpochDay: Long,
    val spent: Double,
    val income: Double
)

data class CompareMetric(
    val label: String,
    val spent: Double,
    val income: Double,
    val changeText: String
)

data class IncomeTrendSummary(
    val totalIncome: Double,
    val salaryIncome: Double,
    val irregularIncome: Double,
    val salaryConsistencyText: String
)

data class SavingsScore(
    val score: Int,
    val disposableIncome: Double,
    val summary: String
)

data class RangeSummary(
    val type: SummaryRangeType,
    val label: String,
    val spent: Double,
    val income: Double,
    val transactionCount: Int
)

data class SmsImportSummary(
    val scanned: Int = 0,
    val importedInstantly: Int = 0,
    val queuedForAi: Int = 0,
    val skipped: Int = 0
)

data class MonthInsightSnapshot(
    val summary: MonthlySummary,
    val transactionCount: Int,
    val averageDailySpend: Double,
    val topCategories: List<CategoryTotal>,
    val topCategoryName: String,
    val topCategoryAmount: Double,
    val facts: List<InsightFact>,
    val trend: List<TrendPoint>,
    val paymentModes: List<PaymentModeTotal>,
    val topMerchants: List<MerchantAnalytics>,
    val duplicateInsights: List<DuplicateInsight>,
    val recurringInsights: List<RecurringInsight>,
    val specialTracking: SpecialTrackingSummary,
    val budgetProgress: List<BudgetProgress>,
    val anomalyAlerts: List<AnomalyAlert>,
    val cashflowDays: List<CashflowDay>,
    val compareMetrics: List<CompareMetric>,
    val incomeTrend: IncomeTrendSummary,
    val savingsScore: SavingsScore,
    val rangeSummaries: List<RangeSummary>
)

class TransactionRepository(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val transactionDao = database.transactionDao()
    private val transactionCategoryAiDao = database.transactionCategoryAiDao()
    private val pendingSmsDao = database.pendingSmsDao()
    private val reviewDao = database.smsReviewDao()
    private val smsProcessor = SmsProcessor(appContext)
    private val settingsStore = SettingsStore(appContext)
    private val backupManager = SpendWiseBackupManager(appContext)
    private val gson = Gson()

    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>> {
        val (startMs, endMs) = getMonthRange(year, month)
        return transactionDao.getTransactionsByMonth(startMs, endMs)
    }

    fun observeCategoryRefinementRecord(transactionId: Long): Flow<TransactionCategoryAiEntity?> {
        return transactionCategoryAiDao.observeByTransactionId(transactionId)
    }

    suspend fun getCategoryRefinementRecord(transactionId: Long): TransactionCategoryAiEntity? =
        withContext(Dispatchers.IO) {
            transactionCategoryAiDao.getByTransactionId(transactionId)
        }

    suspend fun getMonthlySummary(year: Int, month: Int): MonthlySummary = withContext(Dispatchers.IO) {
        val (startMs, endMs) = getMonthRange(year, month)
        transactionDao.getMonthlySummary(startMs, endMs)
    }

    suspend fun getTopCategories(year: Int, month: Int): List<CategoryTotal> = withContext(Dispatchers.IO) {
        val (startMs, endMs) = getMonthRange(year, month)
        transactionDao.getTopCategories(startMs, endMs)
    }

    suspend fun getPendingCount(): Int = withContext(Dispatchers.IO) {
        pendingSmsDao.getPendingCount()
    }

    suspend fun processPendingSms(onProgress: (processed: Int, total: Int, currentSms: com.yourapp.spendwise.data.db.PendingSmsEntity?) -> Unit) {
        smsProcessor.drainPendingQueue(onProgress)
    }

    suspend fun getMonthInsightSnapshot(year: Int, month: Int): MonthInsightSnapshot = withContext(Dispatchers.IO) {
        val (startMs, endMs) = getMonthRange(year, month)
        val summary = transactionDao.getMonthlySummary(startMs, endMs)
        val topCategories = transactionDao.getTopCategories(startMs, endMs)
        val transactionCount = transactionDao.getTransactionCount(startMs, endMs).count
        val monthTransactions = transactionDao.getTransactionsList(startMs, endMs)

        val selectedMonth = YearMonth.of(year, month)
        val previousMonth = selectedMonth.minusMonths(1)
        val previousSummary = getMonthlySummary(previousMonth.year, previousMonth.monthValue)
        val previousCategories = getTopCategories(previousMonth.year, previousMonth.monthValue)
        val lastYearMonth = selectedMonth.minusYears(1)
        val lastYearSummary = getMonthlySummary(lastYearMonth.year, lastYearMonth.monthValue)
        val recurringTransactions = transactionDao.getTransactionsList(
            startMs = selectedMonth.minusMonths(5)
                .atDay(1)
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli(),
            endMs = endMs
        )

        val daysDivisor = if (selectedMonth == YearMonth.now()) {
            LocalDate.now().dayOfMonth
        } else {
            selectedMonth.lengthOfMonth()
        }.coerceAtLeast(1)

        val averageDailySpend = summary.totalSpent / daysDivisor
        val previousAverage = previousSummary.totalSpent / previousMonth.lengthOfMonth().coerceAtLeast(1)
        val topCategory = topCategories.firstOrNull()

        MonthInsightSnapshot(
            summary = summary,
            transactionCount = transactionCount,
            averageDailySpend = averageDailySpend,
            topCategories = topCategories,
            topCategoryName = topCategory?.category ?: "No category yet",
            topCategoryAmount = topCategory?.totalAmount ?: 0.0,
            facts = buildInsightFacts(
                currentCategories = topCategories,
                previousCategories = previousCategories,
                averageDailySpend = averageDailySpend,
                previousAverageDailySpend = previousAverage,
                monthTransactions = monthTransactions
            ),
            trend = buildTrend(selectedMonth),
            paymentModes = buildPaymentModes(monthTransactions),
            topMerchants = buildMerchantAnalytics(monthTransactions),
            duplicateInsights = buildDuplicateInsights(monthTransactions),
            recurringInsights = buildRecurringInsights(recurringTransactions),
            specialTracking = buildSpecialTracking(monthTransactions),
            budgetProgress = buildBudgetProgress(topCategories),
            anomalyAlerts = buildAnomalyAlerts(monthTransactions, averageDailySpend),
            cashflowDays = buildCashflowDays(monthTransactions),
            compareMetrics = buildCompareMetrics(summary, previousSummary, lastYearSummary),
            incomeTrend = buildIncomeTrend(monthTransactions),
            savingsScore = buildSavingsScore(summary, topCategories),
            rangeSummaries = buildRangeSummaries(selectedMonth)
        )
    }

    suspend fun addManualTransaction(
        amount: Double,
        type: TransactionType,
        merchant: String,
        bank: String
    ) = withContext(Dispatchers.IO) {
        val transaction = TransactionFactory.create(
            context = appContext,
            amount = amount,
            type = type,
            merchant = merchant,
            bank = bank,
            rawSms = "Manual entry for $merchant",
            sourceSender = bank,
            timestamp = System.currentTimeMillis(),
            isVerifiedByAi = false,
            verificationSource = "Manual Entry"
        )
        val id = transactionDao.insert(transaction)
        if (id > 0L) {
            scheduleCategoryRefinementIfNeeded(transaction.copy(id = id))
        }
        WidgetUpdater.updateAll(appContext)
        id
    }

    suspend fun updateTransaction(transaction: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        val existing = transactionDao.getById(transaction.id) ?: return@withContext false
        val normalized = normalizeTransaction(existing = existing, edited = transaction)
        val success = transactionDao.update(normalized) > 0
        if (success) {
            if (didCategoryInputsChange(existing, normalized)) {
                transactionCategoryAiDao.deleteByTransactionId(normalized.id)
            }
            WidgetUpdater.updateAll(appContext)
        }
        success
    }

    suspend fun requestCategoryRefinement(transactionId: Long): Boolean = withContext(Dispatchers.IO) {
        val existing = transactionDao.getById(transactionId) ?: return@withContext false
        if (existing.categoryDecisionSource == CategoryDecisionSource.RULE) {
            return@withContext false
        }
        if (
            existing.categoryRefinementStatus == CategoryRefinementStatus.PENDING ||
            existing.categoryRefinementStatus == CategoryRefinementStatus.RUNNING
        ) {
            return@withContext true
        }

        val refreshed = existing.copy(
            updatedAt = System.currentTimeMillis(),
            categoryRefinementStatus = CategoryRefinementStatus.PENDING
        )
        val success = transactionDao.update(refreshed) > 0
        if (success) {
            transactionCategoryAiDao.deleteByTransactionId(transactionId)
            scheduleCategoryRefinementIfNeeded(refreshed)
        }
        success
    }

    suspend fun findSimilarTransactions(transaction: TransactionEntity): List<TransactionEntity> = withContext(Dispatchers.IO) {
        val targetMerchant = MerchantNormalizer.normalize(transaction.merchant, transaction.rawSms)
        val targetMerchantKey = targetMerchant.lowercase(java.util.Locale.ENGLISH)
        val targetAmount = "%.2f".format(java.util.Locale.ENGLISH, transaction.amount)
        val targetAccount = transaction.accountLabel.ifBlank { transaction.bank }
            .lowercase(java.util.Locale.ENGLISH)

        transactionDao.getAllTransactionsList()
            .asSequence()
            .filter { it.id != transaction.id }
            .mapNotNull { candidate ->
                val candidateMerchant = MerchantNormalizer.normalize(candidate.merchant, candidate.rawSms)
                val candidateMerchantKey = candidateMerchant.lowercase(java.util.Locale.ENGLISH)
                val candidateAmount = "%.2f".format(java.util.Locale.ENGLISH, candidate.amount)
                val candidateAccount = candidate.accountLabel.ifBlank { candidate.bank }
                    .lowercase(java.util.Locale.ENGLISH)

                var score = 0
                if (targetMerchantKey.isNotBlank() && targetMerchantKey != "unknown merchant" && candidateMerchantKey == targetMerchantKey) {
                    score += 5
                }
                if (candidateAmount == targetAmount) score += 3
                if (candidate.type == transaction.type) score += 2
                if (targetAccount.isNotBlank() && candidateAccount == targetAccount) score += 1

                if (score >= 5) candidate to score else null
            }
            .sortedWith(
                compareByDescending<Pair<TransactionEntity, Int>> { it.second }
                    .thenByDescending { it.first.timestamp }
            )
            .map { it.first }
            .toList()
    }

    suspend fun applyTransactionChangesToSimilar(
        editedTransaction: TransactionEntity,
        targetTransactionIds: Set<Long>
    ): Int = withContext(Dispatchers.IO) {
        if (targetTransactionIds.isEmpty()) return@withContext 0

        val sourceExisting = transactionDao.getById(editedTransaction.id)
        val normalizedSource = normalizeTransaction(
            existing = sourceExisting ?: editedTransaction,
            edited = editedTransaction
        )
        val candidates = transactionDao.getAllTransactionsList()
            .filter { it.id in targetTransactionIds && it.id != editedTransaction.id }
            .map { candidate ->
                normalizeTransaction(
                    existing = candidate,
                    edited = candidate.copy(
                        amount = normalizedSource.amount,
                        type = normalizedSource.type,
                        merchant = normalizedSource.merchant,
                        bank = normalizedSource.bank,
                        category = normalizedSource.category,
                        note = normalizedSource.note,
                        tags = normalizedSource.tags,
                        paymentMode = normalizedSource.paymentMode
                    )
                )
            }

        if (candidates.isEmpty()) return@withContext 0
        val updatedCount = transactionDao.updateAll(candidates)
        if (updatedCount > 0) {
            candidates.forEach { transactionCategoryAiDao.deleteByTransactionId(it.id) }
            WidgetUpdater.updateAll(appContext)
        }
        updatedCount
    }

    suspend fun deleteTransaction(transactionId: Long): Boolean = withContext(Dispatchers.IO) {
        val success = transactionDao.deleteById(transactionId) > 0
        if (success) {
            transactionCategoryAiDao.deleteByTransactionId(transactionId)
            WidgetUpdater.updateAll(appContext)
        }
        success
    }

    suspend fun ignoreDuplicate(transactionId: Long): Boolean = withContext(Dispatchers.IO) {
        transactionDao.ignoreDuplicate(transactionId) > 0
    }

    suspend fun restoreTransaction(transaction: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        val restored = transaction.copy(
            id = 0L,
            updatedAt = System.currentTimeMillis(),
            categoryRefinementStatus = if (transaction.categoryDecisionSource == CategoryDecisionSource.RULE) {
                CategoryRefinementStatus.SKIPPED_RULE
            } else {
                CategoryRefinementStatus.PENDING
            }
        )
        val newId = transactionDao.insert(restored)
        val success = newId != -1L
        if (success) {
            scheduleCategoryRefinementIfNeeded(restored.copy(id = newId))
            WidgetUpdater.updateAll(appContext)
        }
        success
    }

    suspend fun refineTransactionCategory(transactionId: Long) = withContext(Dispatchers.IO) {
        val original = transactionDao.getById(transactionId) ?: return@withContext
        if (original.categoryDecisionSource == CategoryDecisionSource.RULE) {
            if (original.categoryRefinementStatus != CategoryRefinementStatus.SKIPPED_RULE) {
                transactionDao.update(
                    original.copy(categoryRefinementStatus = CategoryRefinementStatus.SKIPPED_RULE)
                )
            }
            return@withContext
        }

        transactionDao.update(
            original.copy(categoryRefinementStatus = CategoryRefinementStatus.RUNNING)
        )

        val startedAt = System.currentTimeMillis()
        val resolution = TransactionCategoryResolver.resolveDetailed(
            merchant = original.merchant,
            rawSms = original.rawSms,
            type = original.type
        )
        val allowedCategories = CategoryCatalog.allCategories(
            customCategories = settingsStore.getCustomCategories(),
            currentCategory = original.category
        )
        val analysis = TransactionCategoryRefiner.refine(
            transaction = original,
            resolution = resolution,
            allowedCategories = allowedCategories,
            settingsStore = settingsStore
        )

        val current = transactionDao.getById(transactionId) ?: return@withContext
        if (current.updatedAt != original.updatedAt) {
            upsertCategoryAiRecord(
                transactionId = transactionId,
                resolverCategory = resolution.category,
                resolverSignalsJson = gson.toJson(resolution),
                currentCategory = original.category,
                suggestedCategory = analysis?.result?.suggestedCategory.orEmpty(),
                confidence = analysis?.result?.confidence ?: 0.0,
                reason = analysis?.result?.reason.orEmpty(),
                model = analysis?.source ?: "",
                rawJson = analysis?.rawResponse.orEmpty(),
                outcome = CategoryRefinementStatus.SKIPPED_STALE,
                outcomeDetail = "Transaction changed before AI refinement could be applied.",
                startedAt = startedAt,
                finishedAt = System.currentTimeMillis(),
                keepCurrent = analysis?.result?.keepCurrent == true
            )
            return@withContext
        }

        val aiResult = analysis?.result
        if (analysis == null || aiResult == null) {
            transactionDao.update(current.copy(categoryRefinementStatus = CategoryRefinementStatus.FAILED))
            upsertCategoryAiRecord(
                transactionId = transactionId,
                resolverCategory = resolution.category,
                resolverSignalsJson = gson.toJson(resolution),
                currentCategory = current.category,
                suggestedCategory = "",
                confidence = 0.0,
                reason = "",
                model = analysis?.source ?: "",
                rawJson = analysis?.rawResponse.orEmpty(),
                outcome = CategoryRefinementStatus.FAILED,
                outcomeDetail = "AI did not return a usable category suggestion.",
                startedAt = startedAt,
                finishedAt = System.currentTimeMillis(),
                keepCurrent = false
            )
            return@withContext
        }

        val normalizedSuggestion = allowedCategories.firstOrNull {
            it.equals(aiResult.suggestedCategory.trim(), ignoreCase = true)
        }
        if (normalizedSuggestion == null) {
            transactionDao.update(current.copy(categoryRefinementStatus = CategoryRefinementStatus.KEPT_RESOLVER))
            upsertCategoryAiRecord(
                transactionId = transactionId,
                resolverCategory = resolution.category,
                resolverSignalsJson = gson.toJson(resolution),
                currentCategory = current.category,
                suggestedCategory = aiResult.suggestedCategory,
                confidence = aiResult.confidence,
                reason = aiResult.reason,
                model = analysis.source,
                rawJson = analysis.rawResponse,
                outcome = CategoryRefinementStatus.KEPT_RESOLVER,
                outcomeDetail = "AI suggested a category outside SpendWise's allowed list.",
                startedAt = startedAt,
                finishedAt = System.currentTimeMillis(),
                keepCurrent = aiResult.keepCurrent
            )
            return@withContext
        }

        val shouldKeepResolver = aiResult.keepCurrent ||
            aiResult.confidence < 0.85 ||
            normalizedSuggestion.equals(current.category, ignoreCase = true)

        if (shouldKeepResolver) {
            transactionDao.update(current.copy(categoryRefinementStatus = CategoryRefinementStatus.KEPT_RESOLVER))
            upsertCategoryAiRecord(
                transactionId = transactionId,
                resolverCategory = resolution.category,
                resolverSignalsJson = gson.toJson(resolution),
                currentCategory = current.category,
                suggestedCategory = normalizedSuggestion,
                confidence = aiResult.confidence,
                reason = aiResult.reason,
                model = analysis.source,
                rawJson = analysis.rawResponse,
                outcome = CategoryRefinementStatus.KEPT_RESOLVER,
                outcomeDetail = if (aiResult.keepCurrent) {
                    "AI agreed the current category should stay."
                } else if (normalizedSuggestion.equals(current.category, ignoreCase = true)) {
                    "AI reached the same category SpendWise already picked."
                } else {
                    "Confidence was below the auto-apply threshold."
                },
                startedAt = startedAt,
                finishedAt = System.currentTimeMillis(),
                keepCurrent = aiResult.keepCurrent
            )
            return@withContext
        }

        transactionDao.update(
            current.copy(
                category = normalizedSuggestion,
                updatedAt = System.currentTimeMillis(),
                categoryDecisionSource = CategoryDecisionSource.AI,
                categoryRefinementStatus = CategoryRefinementStatus.APPLIED
            )
        )
        upsertCategoryAiRecord(
            transactionId = transactionId,
            resolverCategory = resolution.category,
            resolverSignalsJson = gson.toJson(resolution),
            currentCategory = current.category,
            suggestedCategory = normalizedSuggestion,
            confidence = aiResult.confidence,
            reason = aiResult.reason,
            model = analysis.source,
            rawJson = analysis.rawResponse,
            outcome = CategoryRefinementStatus.APPLIED,
            outcomeDetail = "AI refinement replaced the resolver category.",
            startedAt = startedAt,
            finishedAt = System.currentTimeMillis(),
            keepCurrent = aiResult.keepCurrent
        )
        WidgetUpdater.updateAll(appContext)
    }

    suspend fun simulateIncomingSms(sender: String, body: String): SmsIntakeOutcome = withContext(Dispatchers.IO) {
        SmsIntakeManager.ingest(
            context = appContext,
            sender = sender,
            body = body,
            timestamp = System.currentTimeMillis(),
            eventSource = "DEBUG"
        )
    }

    suspend fun importExistingSms(
        onProgress: (processed: Int, total: Int) -> Unit
    ): SmsImportSummary = withContext(Dispatchers.IO) {
        val projection = arrayOf(
            Telephony.Sms.ADDRESS,
            Telephony.Sms.BODY,
            Telephony.Sms.DATE
        )
        val sortOrder = "${Telephony.Sms.DATE} ASC"

        appContext.contentResolver.query(
            Telephony.Sms.Inbox.CONTENT_URI,
            projection,
            null,
            null,
            sortOrder
        )?.use { cursor ->
            val senderIndex = cursor.getColumnIndex(Telephony.Sms.ADDRESS)
            val bodyIndex = cursor.getColumnIndex(Telephony.Sms.BODY)
            val dateIndex = cursor.getColumnIndex(Telephony.Sms.DATE)
            val total = cursor.count.coerceAtLeast(0)

            var scanned = 0
            var importedInstantly = 0
            var queuedForAi = 0
            var skipped = 0

            while (cursor.moveToNext()) {
                val sender = cursor.getNullableString(senderIndex).orEmpty().trim()
                val body = cursor.getNullableString(bodyIndex).orEmpty().trim()
                val timestamp = cursor.getNullableLong(dateIndex) ?: System.currentTimeMillis()

                scanned += 1
                onProgress(scanned, total)

                if (body.isBlank()) {
                    skipped += 1
                    continue
                }

                when (
                    SmsIntakeManager.ingest(
                        context = appContext,
                        sender = sender,
                        body = body,
                        timestamp = timestamp,
                        eventSource = "IMPORT",
                        emitNotifications = false,
                        emitPendingEvent = false
                    )
                ) {
                    is SmsIntakeOutcome.Confirmed -> importedInstantly += 1
                    is SmsIntakeOutcome.Pending -> queuedForAi += 1
                    SmsIntakeOutcome.Discarded -> skipped += 1
                }
            }

            onProgress(total, total)
            SmsImportSummary(
                scanned = scanned,
                importedInstantly = importedInstantly,
                queuedForAi = queuedForAi,
                skipped = skipped
            )
        } ?: SmsImportSummary()
    }

    fun sendDebugSms(phoneNumber: String, body: String) {
        appContext.getSystemService(SmsManager::class.java)
            ?.sendTextMessage(phoneNumber, null, body, null, null)
            ?: error("SMS service unavailable on this device.")
    }

    fun isDebugModeEnabled(): Boolean = settingsStore.isDebugModeEnabled()

    fun setDebugModeEnabled(enabled: Boolean) {
        settingsStore.setDebugModeEnabled(enabled)
    }

    fun isAiReviewEnabled(): Boolean = settingsStore.isAiReviewEnabled()

    fun setAiReviewEnabled(enabled: Boolean) {
        settingsStore.setAiReviewEnabled(enabled)
    }

    fun isCloudAiEnabled(): Boolean = settingsStore.isCloudAiEnabled()

    fun setCloudAiEnabled(enabled: Boolean) {
        settingsStore.setCloudAiEnabled(enabled)
    }

    fun getCloudAiApiKey(): String = settingsStore.getCloudAiApiKey()

    fun setCloudAiApiKey(key: String) {
        settingsStore.setCloudAiApiKey(key)
    }

    fun getAxisEmailAccount(): String = settingsStore.getAxisEmailAccount()

    fun isAxisEmailConnected(): Boolean = getAxisEmailAccount().isNotBlank()

    fun connectAxisEmailAccount(email: String) {
        GmailAxisSyncManager.onAccountConnected(appContext, email)
    }

    fun disconnectAxisEmailAccount() {
        GmailAxisSyncManager.disconnect(appContext)
    }

    fun isAxisEmailAutoSyncEnabled(): Boolean = settingsStore.isAxisEmailAutoSyncEnabled()

    fun setAxisEmailAutoSyncEnabled(enabled: Boolean) {
        GmailAxisSyncManager.setAutoSyncEnabled(appContext, enabled)
    }

    fun getAxisEmailLastSyncMs(): Long = settingsStore.getAxisEmailLastSyncMs()

    fun getAxisEmailSyncHistory(): List<AxisEmailSyncHistoryEntry> = settingsStore.getAxisEmailSyncHistory()

    fun isSparkMailTriggerEnabled(): Boolean = settingsStore.isSparkMailTriggerEnabled()

    fun setSparkMailTriggerEnabled(enabled: Boolean) {
        settingsStore.setSparkMailTriggerEnabled(enabled)
    }

    fun hasSparkNotificationAccess(): Boolean = NotificationAccessHelper.hasNotificationAccess(appContext)

    fun ensureAxisEmailSyncSchedule() {
        GmailAxisSyncManager.ensureScheduled(appContext)
    }

    suspend fun syncAxisEmailsNow(trigger: String = AxisEmailSyncTrigger.MANUAL): AxisEmailSyncResult {
        return GmailAxisSyncManager.syncNow(appContext, trigger)
    }

    fun getThemeMode(): String = settingsStore.getThemeMode()

    fun setThemeMode(mode: String) {
        settingsStore.setThemeMode(mode)
    }

    fun isDailyReminderEnabled(): Boolean = settingsStore.isDailyReminderEnabled()

    fun setDailyReminderEnabled(enabled: Boolean) {
        settingsStore.setDailyReminderEnabled(enabled)
        DailyReminderScheduler.scheduleNext(appContext)
    }

    fun getDailyReminderHour(): Int = settingsStore.getDailyReminderHour()

    fun getDailyReminderMinute(): Int = settingsStore.getDailyReminderMinute()

    fun setDailyReminderTime(hour: Int, minute: Int) {
        settingsStore.setDailyReminderTime(hour, minute)
        DailyReminderScheduler.scheduleNext(appContext)
    }

    fun getDriveBackupAccount(): String = settingsStore.getDriveBackupAccount()

    fun connectDriveBackupAccount(email: String) {
        settingsStore.setDriveBackupAccount(email)
        BackupScheduler.scheduleNext(appContext)
    }

    fun disconnectDriveBackupAccount() {
        settingsStore.clearDriveBackupAccount()
        BackupScheduler.cancel(appContext)
    }

    fun isDriveBackupAutoEnabled(): Boolean = settingsStore.isDriveBackupAutoEnabled()

    fun setDriveBackupAutoEnabled(enabled: Boolean) {
        settingsStore.setDriveBackupAutoEnabled(enabled)
        BackupScheduler.scheduleNext(appContext)
    }

    fun getDriveBackupHour(): Int = settingsStore.getDriveBackupHour()

    fun getDriveBackupMinute(): Int = settingsStore.getDriveBackupMinute()

    fun setDriveBackupTime(hour: Int, minute: Int) {
        settingsStore.setDriveBackupTime(hour, minute)
        BackupScheduler.scheduleNext(appContext)
    }

    fun getBackupHistory(): List<BackupHistoryEntry> = settingsStore.getBackupHistory()

    fun ensureDriveBackupSchedule() {
        BackupScheduler.scheduleNext(appContext)
    }

    suspend fun exportBackupToUri(uri: Uri): BackupResult = backupManager.exportToUri(uri)

    suspend fun restoreBackupFromUri(uri: Uri): BackupResult = backupManager.restoreFromUri(uri)

    suspend fun pushBackupToDrive(trigger: String = BackupTrigger.MANUAL): BackupResult =
        backupManager.pushToDrive(trigger)

    suspend fun restoreBackupFromDrive(): BackupResult = backupManager.restoreLatestFromDrive()

    fun getHomeCardOrder(): List<String> = settingsStore.getHomeCardOrder()

    fun setHomeCardOrder(order: List<String>) {
        settingsStore.setHomeCardOrder(order)
    }

    fun resetHomeCardOrder() {
        settingsStore.resetHomeCardOrder()
    }

    fun getHiddenHomeCardIds(): Set<String> = settingsStore.getHiddenHomeCardIds()

    fun setHiddenHomeCardIds(hiddenCardIds: Set<String>) {
        settingsStore.setHiddenHomeCardIds(hiddenCardIds)
    }

    fun getDebugPhoneNumber(): String = settingsStore.getDebugPhoneNumber()

    fun setDebugPhoneNumber(phoneNumber: String) {
        settingsStore.setDebugPhoneNumber(phoneNumber)
    }

    fun getCustomCategories(): List<CustomCategory> = settingsStore.getCustomCategories()

    fun addCustomCategory(name: String) {
        settingsStore.addCustomCategory(name)
    }

    fun removeCustomCategory(categoryId: String) {
        settingsStore.removeCustomCategory(categoryId)
    }

    fun getRules(): List<TransactionRule> = settingsStore.getRules()

    fun saveRule(rule: TransactionRule) {
        settingsStore.saveRule(rule)
    }

    fun deleteRule(ruleId: String) {
        settingsStore.deleteRule(ruleId)
    }

    fun getBudgetGoals(): List<BudgetGoal> = settingsStore.getBudgetGoals()

    fun saveBudgetGoal(goal: BudgetGoal) {
        settingsStore.saveBudgetGoal(goal)
    }

    fun deleteBudgetGoal(goalId: String) {
        settingsStore.deleteBudgetGoal(goalId)
    }

    fun observeReviewCenter() = reviewDao.observeReviewCenter()

    fun observeSpamInbox() = reviewDao.observeSpamInbox()

    fun observeDebugConsole() = reviewDao.observeDebugConsole()

    fun observeImportSourceEvents() = reviewDao.observeSourceEvents("IMPORT")

    suspend fun retryAiReview(eventId: Long): Boolean = withContext(Dispatchers.IO) {
        val event = reviewDao.getById(eventId) ?: return@withContext false
        if (event.finalStatus != "AI_FAILED") return@withContext false
        if (pendingSmsDao.exists(event.sender, event.body, event.receivedAt)) return@withContext true

        reviewDao.updateOutcome(
            eventId = event.id,
            finalStatus = "QUEUED_FOR_AI",
            transactionId = null,
            aiJson = "",
            aiReason = "",
            aiEngine = event.aiEngine.ifBlank { "Gemini Nano" },
            debugLog = "${event.debugLog}\nretry=manual\nfinal=queued_for_ai_retry"
        )
        val pendingId = pendingSmsDao.insert(
            PendingSmsEntity(
                sender = event.sender,
                body = event.body,
                receivedAt = event.receivedAt,
                reviewEventId = event.id
            )
        )
        if (pendingId == -1L) return@withContext false
        AiProcessingService.start(appContext)
        true
    }

    suspend fun retryAllFailedAiReviews(): Int = withContext(Dispatchers.IO) {
        val failedEvents = reviewDao.getByStatus("AI_FAILED")
        var retriedCount = 0
        failedEvents.forEach { event ->
            if (pendingSmsDao.exists(event.sender, event.body, event.receivedAt)) {
                retriedCount += 1
                return@forEach
            }
            reviewDao.updateOutcome(
                eventId = event.id,
                finalStatus = "QUEUED_FOR_AI",
                transactionId = null,
                aiJson = "",
                aiReason = "",
                aiEngine = event.aiEngine.ifBlank { "Gemini Nano" },
                debugLog = "${event.debugLog}\nretry=batch\nfinal=queued_for_ai_retry"
            )
            val pendingId = pendingSmsDao.insert(
                PendingSmsEntity(
                    sender = event.sender,
                    body = event.body,
                    receivedAt = event.receivedAt,
                    reviewEventId = event.id
                )
            )
            if (pendingId != -1L) {
                retriedCount += 1
            }
        }
        if (retriedCount > 0) {
            AiProcessingService.start(appContext)
        }
        retriedCount
    }

    suspend fun recoverLegacyAiFailures(): Int = withContext(Dispatchers.IO) {
        val rejectedEvents = reviewDao.getByStatus("AI_REJECTED")
        var recoveredCount = 0
        rejectedEvents.forEach { event ->
            val reason = event.aiReason.lowercase()
            val debugLog = event.debugLog.lowercase()
            val looksLikeLegacyFailure =
                "no response" in reason ||
                    "timeout" in reason ||
                    "model unavailable" in reason ||
                    "ai_error_rejected" in debugLog

            if (!looksLikeLegacyFailure) return@forEach

            reviewDao.updateOutcome(
                eventId = event.id,
                finalStatus = "AI_FAILED",
                transactionId = null,
                aiJson = event.aiJson,
                aiReason = event.aiReason.ifBlank {
                    "AI model returned no usable response."
                },
                aiEngine = event.aiEngine.ifBlank { "Unknown" },
                debugLog = "${event.debugLog}\nrecovered=legacy_timeout\nfinal=ai_failed"
            )
            recoveredCount += 1
        }
        recoveredCount
    }

    private suspend fun upsertCategoryAiRecord(
        transactionId: Long,
        resolverCategory: String,
        resolverSignalsJson: String,
        currentCategory: String,
        suggestedCategory: String,
        confidence: Double,
        reason: String,
        model: String,
        rawJson: String,
        outcome: String,
        outcomeDetail: String,
        startedAt: Long,
        finishedAt: Long,
        keepCurrent: Boolean
    ) {
        transactionCategoryAiDao.upsert(
            TransactionCategoryAiEntity(
                transactionId = transactionId,
                resolverCategory = resolverCategory,
                resolverSignalsJson = resolverSignalsJson,
                currentCategory = currentCategory,
                suggestedCategory = suggestedCategory,
                confidence = confidence,
                reason = reason,
                model = model,
                rawJson = rawJson,
                outcome = outcome,
                outcomeDetail = outcomeDetail,
                startedAt = startedAt,
                finishedAt = finishedAt,
                keepCurrent = keepCurrent
            )
        )
    }

    private fun scheduleCategoryRefinementIfNeeded(transaction: TransactionEntity) {
        if (transaction.id <= 0L) return
        if (transaction.categoryDecisionSource == CategoryDecisionSource.RULE) return
        if (transaction.categoryRefinementStatus != CategoryRefinementStatus.PENDING) return
        TransactionCategoryRefinementWorker.enqueue(appContext, transaction.id)
    }

    private fun didCategoryInputsChange(
        existing: TransactionEntity,
        updated: TransactionEntity
    ): Boolean {
        return existing.merchant != updated.merchant ||
            existing.type != updated.type ||
            existing.category != updated.category
    }

    private fun normalizeTransaction(
        existing: TransactionEntity,
        edited: TransactionEntity
    ): TransactionEntity {
        val normalizedMerchant = MerchantNormalizer.normalize(edited.merchant, edited.rawSms)
        val normalizedCategory = edited.category.trim().ifBlank {
            TransactionCategoryResolver.resolve(
                merchant = normalizedMerchant,
                rawSms = edited.rawSms,
                type = edited.type
            )
        }
        val categoryInputsChanged = existing.merchant != normalizedMerchant ||
            existing.type != edited.type ||
            existing.category != normalizedCategory

        return edited.copy(
            merchant = normalizedMerchant,
            category = normalizedCategory,
            paymentMode = PaymentModeResolver.resolve(
                rawSms = edited.rawSms,
                merchant = normalizedMerchant
            ),
            updatedAt = System.currentTimeMillis(),
            categoryDecisionSource = if (categoryInputsChanged) {
                CategoryDecisionSource.USER_EDIT
            } else {
                existing.categoryDecisionSource
            },
            categoryRefinementStatus = if (categoryInputsChanged) {
                CategoryRefinementStatus.NONE
            } else {
                existing.categoryRefinementStatus
            },
            categoryRuleName = if (categoryInputsChanged) "" else existing.categoryRuleName
        )
    }

    private fun getMonthRange(year: Int, month: Int): Pair<Long, Long> {
        val zone = ZoneId.systemDefault()
        val start = LocalDate.of(year, month, 1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val end = LocalDate.of(year, month, 1)
            .plusMonths(1)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli() - 1L
        return start to end
    }

    private suspend fun buildTrend(selectedMonth: YearMonth): List<TrendPoint> {
        return (5 downTo 0).map { offset ->
            val yearMonth = selectedMonth.minusMonths(offset.toLong())
            val summary = getMonthlySummary(yearMonth.year, yearMonth.monthValue)
            TrendPoint(
                monthLabel = yearMonth.month.name.take(3).lowercase()
                    .replaceFirstChar { it.uppercase() },
                expense = summary.totalSpent,
                income = summary.totalReceived
            )
        }
    }

    private fun buildInsightFacts(
        currentCategories: List<CategoryTotal>,
        previousCategories: List<CategoryTotal>,
        averageDailySpend: Double,
        previousAverageDailySpend: Double,
        monthTransactions: List<TransactionEntity>
    ): List<InsightFact> {
        val previousMap = previousCategories.associate { it.category to it.totalAmount }
        val biggestDecline = currentCategories
            .mapNotNull { current ->
                val previous = previousMap[current.category] ?: return@mapNotNull null
                if (previous <= 0.0 || current.totalAmount >= previous) return@mapNotNull null
                val dropPercent = ((previous - current.totalAmount) / previous) * 100
                current.category to dropPercent
            }
            .maxByOrNull { it.second }

        val declineFact = if (biggestDecline != null) {
            InsightFact(
                title = "Biggest decline",
                body = "${biggestDecline.first} showed the largest decrease, down ${biggestDecline.second.toInt()}% from the previous period."
            )
        } else {
            InsightFact(
                title = "Steady category mix",
                body = "Your spending categories stayed fairly stable compared with the previous period."
            )
        }

        val averageFact = InsightFact(
            title = "Daily average",
            body = "Average daily spend was ${formatRupees(averageDailySpend)}, compared with ${formatRupees(previousAverageDailySpend)} previously."
        )

        val duplicateCandidates = buildDuplicateInsights(monthTransactions)
        val duplicateFact = if (duplicateCandidates.isNotEmpty()) {
            val topDuplicate = duplicateCandidates.first()
            InsightFact(
                title = "Duplicates spotted",
                body = "${topDuplicate.transactionCount} similar ${topDuplicate.merchant} transactions around ${formatRupees(topDuplicate.amount)} may need a review."
            )
        } else {
            InsightFact(
                title = "No obvious duplicates",
                body = "This month's activity does not show any obvious same-merchant duplicate clusters."
            )
        }

        return listOf(declineFact, averageFact, duplicateFact)
    }

    private fun buildPaymentModes(transactions: List<TransactionEntity>): List<PaymentModeTotal> {
        return transactions
            .filter { it.type == TransactionType.DEBIT }
            .groupBy { it.paymentMode }
            .map { (mode, items) ->
                PaymentModeTotal(
                    mode = mode,
                    amount = items.sumOf { it.amount },
                    transactionCount = items.size
                )
            }
            .sortedByDescending { it.amount }
    }

    private fun buildMerchantAnalytics(transactions: List<TransactionEntity>): List<MerchantAnalytics> {
        return transactions
            .groupBy { MerchantNormalizer.normalize(it.merchant, it.rawSms) }
            .map { (merchant, items) ->
                MerchantAnalytics(
                    merchant = merchant,
                    totalAmount = items.sumOf { it.amount },
                    transactionCount = items.size
                )
            }
            .sortedByDescending { it.totalAmount }
            .take(5)
    }

    private fun buildDuplicateInsights(transactions: List<TransactionEntity>): List<DuplicateInsight> {
        return transactions
            .filter { !it.isIgnoredDuplicate }
            .groupBy {
                Triple(
                    MerchantNormalizer.normalize(it.merchant, it.rawSms),
                    "%.2f".format(java.util.Locale.ENGLISH, it.amount),
                    it.type
                )
            }
            .mapNotNull { (key, items) ->
                if (items.size < 2) return@mapNotNull null
                val sortedItems = items.sortedByDescending { it.timestamp }
                val latest = sortedItems.first()
                DuplicateInsight(
                    merchant = key.first,
                    amount = key.second.toDouble(),
                    type = key.third,
                    transactionIds = sortedItems.map { it.id },
                    transactionCount = sortedItems.size,
                    latestTimestamp = latest.timestamp
                )
            }
            .sortedByDescending { it.transactionCount }
    }

    private fun buildRecurringInsights(transactions: List<TransactionEntity>): List<RecurringInsight> {
        return transactions
            .filter { transaction ->
                transaction.type == TransactionType.DEBIT &&
                    (
                        transaction.category in setOf("Bills", "Loans & EMI", "Entertainment", "Travel") ||
                            transaction.rawSms.contains("subscription", ignoreCase = true) ||
                            transaction.rawSms.contains("recharge", ignoreCase = true) ||
                            transaction.rawSms.contains("emi", ignoreCase = true)
                        )
            }
            .groupBy { MerchantNormalizer.normalize(it.merchant, it.rawSms) to it.category }
            .mapNotNull { (key, items) ->
                if (items.size < 2) return@mapNotNull null
                val average = items.sumOf { it.amount } / items.size
                val latestTimestamp = items.maxOfOrNull { it.timestamp } ?: return@mapNotNull null
                RecurringInsight(
                    merchant = key.first,
                    category = key.second,
                    amountEstimate = average,
                    occurrences = items.size,
                    lastSeenTimestamp = latestTimestamp
                )
            }
            .sortedByDescending { it.occurrences }
            .take(5)
    }

    private fun buildSpecialTracking(transactions: List<TransactionEntity>): SpecialTrackingSummary {
        val salaryCredits = transactions
            .filter { it.type == TransactionType.CREDIT && it.category == "Salary" }
            .sumOf { it.amount }
        val refundCredits = transactions
            .filter { it.type == TransactionType.CREDIT && it.category == "Refunds" }
            .sumOf { it.amount }
        val cashWithdrawals = transactions
            .filter { it.type == TransactionType.DEBIT && it.category == "Cash Withdrawal" }
            .sumOf { it.amount }
        return SpecialTrackingSummary(
            salaryCredits = salaryCredits,
            refundCredits = refundCredits,
            cashWithdrawals = cashWithdrawals
        )
    }

    private fun buildBudgetProgress(topCategories: List<CategoryTotal>): List<BudgetProgress> {
        val spentByCategory = topCategories.associate { it.category to it.totalAmount }
        return settingsStore.getBudgetGoals()
            .map { goal ->
                val spent = spentByCategory[goal.category] ?: 0.0
                val progress = if (goal.monthlyLimit <= 0.0) 0.0 else spent / goal.monthlyLimit
                BudgetProgress(
                    category = goal.category,
                    monthlyLimit = goal.monthlyLimit,
                    spentAmount = spent,
                    progress = progress,
                    isExceeded = spent > goal.monthlyLimit
                )
            }
            .sortedByDescending { it.progress }
    }

    private fun buildAnomalyAlerts(
        transactions: List<TransactionEntity>,
        averageDailySpend: Double
    ): List<AnomalyAlert> {
        val debitTransactions = transactions.filter { it.type == TransactionType.DEBIT }
        val debitAverage = debitTransactions.map { it.amount }.average().takeIf { !it.isNaN() } ?: 0.0
        val unusuallyHigh = debitTransactions
            .filter { it.amount >= maxOf(debitAverage * 2, 1500.0) }
            .sortedByDescending { it.amount }
            .firstOrNull()

        val dayTotals = transactions.groupBy { java.time.Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .mapValues { entry -> entry.value.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount } }
        val spikeDay = dayTotals.maxByOrNull { it.value }

        return buildList {
            if (unusuallyHigh != null) {
                add(
                    AnomalyAlert(
                        title = "Large spend detected",
                        body = "${unusuallyHigh.merchant} hit ${formatRupees(unusuallyHigh.amount)}, well above your usual spend size.",
                        severity = "high"
                    )
                )
            }
            if (spikeDay != null && spikeDay.value > averageDailySpend * 1.8 && spikeDay.value > 1000.0) {
                add(
                    AnomalyAlert(
                        title = "Daily spike",
                        body = "Spending on ${spikeDay.key.dayOfMonth} ${spikeDay.key.month.name.take(3)} jumped to ${formatRupees(spikeDay.value)}.",
                        severity = "medium"
                    )
                )
            }
            val incomeCredits = transactions.filter { it.type == TransactionType.CREDIT && it.category !in setOf("Salary", "Refunds") }
            if (incomeCredits.size >= 2) {
                add(
                    AnomalyAlert(
                        title = "Irregular credits present",
                        body = "You received ${incomeCredits.size} non-salary credits this period. SpendWise is tracking them as irregular income.",
                        severity = "info"
                    )
                )
            }
        }.take(3)
    }

    private fun buildCashflowDays(transactions: List<TransactionEntity>): List<CashflowDay> {
        return transactions
            .groupBy { java.time.Instant.ofEpochMilli(it.timestamp).atZone(ZoneId.systemDefault()).toLocalDate() }
            .toSortedMap()
            .entries
            .toList()
            .takeLast(10)
            .map { (date, items) ->
                CashflowDay(
                    label = "${date.dayOfMonth} ${date.month.name.take(3)}",
                    dateEpochDay = date.toEpochDay(),
                    spent = items.filter { it.type == TransactionType.DEBIT }.sumOf { it.amount },
                    income = items.filter { it.type == TransactionType.CREDIT }.sumOf { it.amount }
                )
            }
    }

    private fun buildCompareMetrics(
        current: MonthlySummary,
        previousMonth: MonthlySummary,
        sameMonthLastYear: MonthlySummary
    ): List<CompareMetric> {
        return listOf(
            CompareMetric(
                label = "Vs last month",
                spent = previousMonth.totalSpent,
                income = previousMonth.totalReceived,
                changeText = buildChangeText(current.totalSpent, previousMonth.totalSpent)
            ),
            CompareMetric(
                label = "Vs same month last year",
                spent = sameMonthLastYear.totalSpent,
                income = sameMonthLastYear.totalReceived,
                changeText = buildChangeText(current.totalSpent, sameMonthLastYear.totalSpent)
            )
        )
    }

    private fun buildIncomeTrend(transactions: List<TransactionEntity>): IncomeTrendSummary {
        val incomeTransactions = transactions.filter { it.type == TransactionType.CREDIT }
        val salaryIncome = incomeTransactions.filter { it.category == "Salary" }.sumOf { it.amount }
        val refundIncome = incomeTransactions.filter { it.category == "Refunds" }.sumOf { it.amount }
        val irregularIncome = incomeTransactions.sumOf { it.amount } - salaryIncome - refundIncome
        val salaryConsistencyText = when {
            salaryIncome <= 0.0 -> "No salary credit detected yet."
            irregularIncome <= 0.0 -> "Income looks mostly salary-driven this period."
            irregularIncome < salaryIncome * 0.25 -> "Salary remains steady with a small amount of irregular income."
            else -> "Salary is present, but irregular credits are meaningful this period."
        }
        return IncomeTrendSummary(
            totalIncome = incomeTransactions.sumOf { it.amount },
            salaryIncome = salaryIncome,
            irregularIncome = irregularIncome.coerceAtLeast(0.0),
            salaryConsistencyText = salaryConsistencyText
        )
    }

    private fun buildSavingsScore(
        summary: MonthlySummary,
        topCategories: List<CategoryTotal>
    ): SavingsScore {
        val essentialSpend = topCategories
            .filter { it.category in setOf("Bills", "Food", "Travel", "Loans & EMI") }
            .sumOf { it.totalAmount }
        val disposableIncome = summary.totalReceived - essentialSpend
        val ratio = if (summary.totalReceived <= 0.0) 0.0 else (disposableIncome / summary.totalReceived)
        val score = when {
            ratio >= 0.45 -> 90
            ratio >= 0.30 -> 75
            ratio >= 0.15 -> 60
            ratio >= 0.0 -> 45
            else -> 25
        }
        val summaryText = when {
            summary.totalReceived <= 0.0 -> "Add income to calculate a savings score."
            ratio >= 0.30 -> "You are keeping a healthy buffer after essentials."
            ratio >= 0.0 -> "Savings are positive, but essentials are taking a large share."
            else -> "Essentials are currently exceeding inflows."
        }
        return SavingsScore(
            score = score,
            disposableIncome = disposableIncome,
            summary = summaryText
        )
    }

    private suspend fun buildRangeSummaries(selectedMonth: YearMonth): List<RangeSummary> {
        val today = LocalDate.now()
        val ranges = listOf(
            SummaryRangeType.TODAY to (today to today),
            SummaryRangeType.LAST_7_DAYS to (today.minusDays(6) to today),
            SummaryRangeType.THIS_MONTH to (selectedMonth.atDay(1) to selectedMonth.atEndOfMonth()),
            SummaryRangeType.LAST_MONTH to {
                val lastMonth = selectedMonth.minusMonths(1)
                lastMonth.atDay(1) to lastMonth.atEndOfMonth()
            }.invoke()
        )

        return ranges.map { (type, bounds) ->
            val summary = getSummaryForRange(bounds.first, bounds.second)
            val transactionCount = getTransactionCountForRange(bounds.first, bounds.second)
            RangeSummary(
                type = type,
                label = when (type) {
                    SummaryRangeType.TODAY -> "Today"
                    SummaryRangeType.LAST_7_DAYS -> "7 days"
                    SummaryRangeType.THIS_MONTH -> "This month"
                    SummaryRangeType.LAST_MONTH -> "Last month"
                },
                spent = summary.totalSpent,
                income = summary.totalReceived,
                transactionCount = transactionCount
            )
        }
    }

    private suspend fun getSummaryForRange(startDate: LocalDate, endDate: LocalDate): MonthlySummary {
        val zone = ZoneId.systemDefault()
        val startMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L
        return transactionDao.getMonthlySummary(startMs, endMs)
    }

    private suspend fun getTransactionCountForRange(startDate: LocalDate, endDate: LocalDate): Int {
        val zone = ZoneId.systemDefault()
        val startMs = startDate.atStartOfDay(zone).toInstant().toEpochMilli()
        val endMs = endDate.plusDays(1).atStartOfDay(zone).toInstant().toEpochMilli() - 1L
        return transactionDao.getTransactionCount(startMs, endMs).count
    }

    private fun buildChangeText(current: Double, previous: Double): String {
        if (previous <= 0.0 && current <= 0.0) return "No change"
        if (previous <= 0.0) return "New activity"
        val diffPercent = ((current - previous) / previous) * 100
        val direction = if (diffPercent >= 0) "up" else "down"
        return "${kotlin.math.abs(diffPercent).toInt()}% $direction"
    }

    private fun formatRupees(amount: Double): String {
        return java.text.NumberFormat.getCurrencyInstance(java.util.Locale("en", "IN"))
            .format(amount)
    }

    private fun android.database.Cursor.getNullableString(index: Int): String? {
        if (index < 0 || isNull(index)) return null
        return getString(index)
    }

    private fun android.database.Cursor.getNullableLong(index: Int): Long? {
        if (index < 0 || isNull(index)) return null
        return getLong(index)
    }
}
