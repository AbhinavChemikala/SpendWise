package com.yourapp.spendwise.data

import android.content.Context
import android.provider.Telephony
import android.telephony.SmsManager
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.CategoryTotal
import com.yourapp.spendwise.data.db.MonthlySummary
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.sms.SmsIntakeOutcome
import com.yourapp.spendwise.sms.SmsIntakeManager
import com.yourapp.spendwise.sms.SmsProcessor
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
    private val pendingSmsDao = database.pendingSmsDao()
    private val reviewDao = database.smsReviewDao()
    private val smsProcessor = SmsProcessor(appContext)
    private val settingsStore = SettingsStore(appContext)

    fun getTransactionsForMonth(year: Int, month: Int): Flow<List<TransactionEntity>> {
        val (startMs, endMs) = getMonthRange(year, month)
        return transactionDao.getTransactionsByMonth(startMs, endMs)
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
        transactionDao.insert(
            TransactionFactory.create(
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
        )
    }

    suspend fun updateTransaction(transaction: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        val normalized = transaction.copy(
            merchant = MerchantNormalizer.normalize(transaction.merchant, transaction.rawSms),
            category = if (transaction.category.isBlank()) {
                TransactionCategoryResolver.resolve(
                    merchant = transaction.merchant,
                    rawSms = transaction.rawSms,
                    type = transaction.type
                )
            } else {
                transaction.category
            },
            paymentMode = PaymentModeResolver.resolve(
                rawSms = transaction.rawSms,
                merchant = transaction.merchant
            )
        )
        transactionDao.update(normalized) > 0
    }

    suspend fun deleteTransaction(transactionId: Long): Boolean = withContext(Dispatchers.IO) {
        transactionDao.deleteById(transactionId) > 0
    }

    suspend fun restoreTransaction(transaction: TransactionEntity): Boolean = withContext(Dispatchers.IO) {
        transactionDao.insert(transaction.copy(id = 0L)) != -1L
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

    fun getThemeMode(): String = settingsStore.getThemeMode()

    fun setThemeMode(mode: String) {
        settingsStore.setThemeMode(mode)
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
