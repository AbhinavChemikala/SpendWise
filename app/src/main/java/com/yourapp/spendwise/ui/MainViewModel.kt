package com.yourapp.spendwise.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.spendwise.data.CustomCategory
import com.yourapp.spendwise.data.InsightFact
import com.yourapp.spendwise.data.DuplicateInsight
import com.yourapp.spendwise.data.MerchantAnalytics
import com.yourapp.spendwise.data.PaymentModeTotal
import com.yourapp.spendwise.data.RecurringInsight
import com.yourapp.spendwise.data.BudgetGoal
import com.yourapp.spendwise.data.BudgetProgress
import com.yourapp.spendwise.data.AnomalyAlert
import com.yourapp.spendwise.data.CashflowDay
import com.yourapp.spendwise.data.CompareMetric
import com.yourapp.spendwise.data.IncomeTrendSummary
import com.yourapp.spendwise.data.RangeSummary
import com.yourapp.spendwise.data.SpecialTrackingSummary
import com.yourapp.spendwise.data.SavingsScore
import com.yourapp.spendwise.data.SummaryRangeType
import com.yourapp.spendwise.data.TransactionRule
import com.yourapp.spendwise.data.TransactionRepository
import com.yourapp.spendwise.data.TrendPoint
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.CategoryTotal
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.sms.AiProcessingService
import com.yourapp.spendwise.sms.SmsIntakeOutcome
import com.yourapp.spendwise.sms.SmsPipelineEvent
import com.yourapp.spendwise.sms.SmsPipelineEvents
import java.time.LocalDate
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

enum class SpendWiseTab {
    HOME,
    ACTIVITY,
    INSIGHTS,
    SETTINGS,
    REVIEW_CENTER
}

data class DashboardUiState(
    val transactions: List<TransactionEntity> = emptyList(),
    val totalSpent: Double = 0.0,
    val totalReceived: Double = 0.0,
    val transactionCount: Int = 0,
    val averageDailySpend: Double = 0.0,
    val topCategories: List<CategoryTotal> = emptyList(),
    val topCategoryName: String = "No category yet",
    val topCategoryAmount: Double = 0.0,
    val insightFacts: List<InsightFact> = emptyList(),
    val trend: List<TrendPoint> = emptyList(),
    val paymentModes: List<PaymentModeTotal> = emptyList(),
    val topMerchants: List<MerchantAnalytics> = emptyList(),
    val duplicateInsights: List<DuplicateInsight> = emptyList(),
    val recurringInsights: List<RecurringInsight> = emptyList(),
    val specialTracking: SpecialTrackingSummary = SpecialTrackingSummary(0.0, 0.0, 0.0),
    val budgetGoals: List<BudgetGoal> = emptyList(),
    val budgetProgress: List<BudgetProgress> = emptyList(),
    val anomalyAlerts: List<AnomalyAlert> = emptyList(),
    val cashflowDays: List<CashflowDay> = emptyList(),
    val compareMetrics: List<CompareMetric> = emptyList(),
    val incomeTrend: IncomeTrendSummary = IncomeTrendSummary(0.0, 0.0, 0.0, "No income data yet."),
    val savingsScore: SavingsScore = SavingsScore(0, 0.0, "No savings score yet."),
    val rangeSummaries: List<RangeSummary> = emptyList(),
    val selectedSummaryRange: SummaryRangeType = SummaryRangeType.THIS_MONTH,
    val duplicateTransactionIds: Set<Long> = emptySet(),
    val pendingAiCount: Int = 0,
    val isProcessingPending: Boolean = false,
    val processingProgress: Pair<Int, Int> = 0 to 0,
    val selectedYear: Int = LocalDate.now().year,
    val selectedMonth: Int = LocalDate.now().monthValue,
    val selectedTab: SpendWiseTab = SpendWiseTab.HOME,
    val debugModeEnabled: Boolean = false,
    val isAiReviewEnabled: Boolean = true,
    val isCloudAiEnabled: Boolean = false,
    val cloudAiApiKey: String = "",
    val themeMode: String = "system",  // "system", "light", "dark"
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 22,
    val dailyReminderMinute: Int = 0,
    val debugPhoneNumber: String = "",
    val customCategories: List<CustomCategory> = emptyList(),
    val transactionRules: List<TransactionRule> = emptyList(),
    val reviewCenterItems: List<SmsReviewEntity> = emptyList(),
    val spamInboxItems: List<SmsReviewEntity> = emptyList(),
    val debugConsoleItems: List<SmsReviewEntity> = emptyList(),
    val importSourceItems: List<SmsReviewEntity> = emptyList(),
    val currentAiReviewItem: com.yourapp.spendwise.data.db.PendingSmsEntity? = null,
    val pendingUndoDeleteTransaction: TransactionEntity? = null,
    val debugStatusMessage: String? = null,
    val isImportingSms: Boolean = false,
    val importProgress: Pair<Int, Int> = 0 to 0,
    val showManualAddDialog: Boolean = false
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            debugModeEnabled = repository.isDebugModeEnabled(),
            isAiReviewEnabled = repository.isAiReviewEnabled(),
            isCloudAiEnabled = repository.isCloudAiEnabled(),
            cloudAiApiKey = repository.getCloudAiApiKey(),
            themeMode = repository.getThemeMode(),
            dailyReminderEnabled = repository.isDailyReminderEnabled(),
            dailyReminderHour = repository.getDailyReminderHour(),
            dailyReminderMinute = repository.getDailyReminderMinute(),
            debugPhoneNumber = repository.getDebugPhoneNumber()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var transactionsJob: Job? = null
    private var isAppInForeground = false
    private var shouldRerunPendingProcessing = false

    init {
        loadCustomizations()
        loadCurrentMonth()
        checkPendingCount()
        observeSmsPipeline()
        observeReviewStreams()
    }

    fun selectTab(tab: SpendWiseTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    fun openManualAddDialog() {
        _uiState.update { it.copy(showManualAddDialog = true) }
    }

    fun closeManualAddDialog() {
        _uiState.update { it.copy(showManualAddDialog = false) }
    }

    fun loadCurrentMonth() {
        val year = _uiState.value.selectedYear
        val month = _uiState.value.selectedMonth

        transactionsJob?.cancel()
        transactionsJob = viewModelScope.launch {
            repository.getTransactionsForMonth(year, month).collectLatest { transactions ->
                val snapshot = repository.getMonthInsightSnapshot(year, month)
                _uiState.update {
                    it.copy(
                        transactions = transactions,
                        totalSpent = snapshot.summary.totalSpent,
                        totalReceived = snapshot.summary.totalReceived,
                        transactionCount = snapshot.transactionCount,
                        averageDailySpend = snapshot.averageDailySpend,
                        topCategories = snapshot.topCategories,
                        topCategoryName = snapshot.topCategoryName,
                        topCategoryAmount = snapshot.topCategoryAmount,
                        insightFacts = snapshot.facts,
                        trend = snapshot.trend,
                        paymentModes = snapshot.paymentModes,
                        topMerchants = snapshot.topMerchants,
                        duplicateInsights = snapshot.duplicateInsights,
                        recurringInsights = snapshot.recurringInsights,
                        specialTracking = snapshot.specialTracking,
                        budgetProgress = snapshot.budgetProgress,
                        anomalyAlerts = snapshot.anomalyAlerts,
                        cashflowDays = snapshot.cashflowDays,
                        compareMetrics = snapshot.compareMetrics,
                        incomeTrend = snapshot.incomeTrend,
                        savingsScore = snapshot.savingsScore,
                        rangeSummaries = snapshot.rangeSummaries,
                        duplicateTransactionIds = snapshot.duplicateInsights
                            .flatMap { insight -> insight.transactionIds }
                            .toSet()
                    )
                }
            }
        }

        checkPendingCount()
    }

    fun changeMonth(year: Int, month: Int) {
        _uiState.update {
            it.copy(
                selectedYear = year,
                selectedMonth = month
            )
        }
        loadCurrentMonth()
    }

    fun setAppForeground(isForeground: Boolean) {
        isAppInForeground = isForeground
        if (isForeground) {
            checkPendingCount()
            // Delegate to the foreground service — it handles both foreground
            // and background processing with the singleton Mutex guard.
            processPendingSmsWithAi()
        }
    }

    fun processPendingSmsWithAi() {
        if (!_uiState.value.isAiReviewEnabled) return

        viewModelScope.launch {
            val pendingCount = repository.getPendingCount()
            if (pendingCount == 0) return@launch

            // Delegate all actual AI processing to AiProcessingService.
            // The service uses a singleton Mutex so duplicate starts are safe —
            // it will simply return if a drain is already in progress.
            AiProcessingService.start(getApplication())

            // Keep the UI pending count accurate
            _uiState.update { it.copy(pendingAiCount = pendingCount) }
        }
    }

    fun toggleDebugMode(enabled: Boolean) {
        repository.setDebugModeEnabled(enabled)
        _uiState.update { it.copy(debugModeEnabled = enabled) }
    }

    fun toggleAiReview(enabled: Boolean) {
        repository.setAiReviewEnabled(enabled)
        _uiState.update { it.copy(isAiReviewEnabled = enabled) }
        if (enabled && isAppInForeground) {
            processPendingSmsWithAi()
        }
    }

    fun toggleCloudAi(enabled: Boolean) {
        repository.setCloudAiEnabled(enabled)
        _uiState.update { it.copy(isCloudAiEnabled = enabled) }
    }

    fun updateCloudAiApiKey(key: String) {
        repository.setCloudAiApiKey(key)
        _uiState.update { it.copy(cloudAiApiKey = key) }
    }

    fun setThemeMode(mode: String) {
        repository.setThemeMode(mode)
        _uiState.update { it.copy(themeMode = mode) }
    }

    fun toggleDailyReminder(enabled: Boolean) {
        repository.setDailyReminderEnabled(enabled)
        _uiState.update {
            it.copy(
                dailyReminderEnabled = enabled,
                debugStatusMessage = if (enabled) "Daily reminder turned on." else "Daily reminder turned off."
            )
        }
    }

    fun setDailyReminderTime(hour: Int, minute: Int) {
        repository.setDailyReminderTime(hour, minute)
        _uiState.update {
            it.copy(
                dailyReminderHour = hour.coerceIn(0, 23),
                dailyReminderMinute = minute.coerceIn(0, 59),
                dailyReminderEnabled = repository.isDailyReminderEnabled(),
                debugStatusMessage = "Daily reminder time updated."
            )
        }
    }

    fun updateDebugPhoneNumber(phoneNumber: String) {
        repository.setDebugPhoneNumber(phoneNumber)
        _uiState.update { it.copy(debugPhoneNumber = phoneNumber) }
    }

    fun updateTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val updated = repository.updateTransaction(transaction)
            _uiState.update {
                it.copy(
                    debugStatusMessage = if (updated) {
                        "${transaction.merchant} updated."
                    } else {
                        "Unable to update ${transaction.merchant}."
                    }
                )
            }
            loadCurrentMonth()
        }
    }

    fun clearDebugStatus() {
        _uiState.update { it.copy(debugStatusMessage = null) }
    }

    fun clearUndoDelete() {
        _uiState.update { it.copy(pendingUndoDeleteTransaction = null) }
    }

    fun setDebugStatus(message: String) {
        _uiState.update { it.copy(debugStatusMessage = message) }
    }

    fun selectSummaryRange(type: SummaryRangeType) {
        _uiState.update { it.copy(selectedSummaryRange = type) }
    }

    fun addManualTransaction(
        amount: Double,
        type: TransactionType,
        merchant: String,
        bank: String
    ) {
        viewModelScope.launch {
            repository.addManualTransaction(
                amount = amount,
                type = type,
                merchant = merchant,
                bank = bank
            )
            _uiState.update {
                it.copy(
                    debugStatusMessage = "Manual transaction added."
                )
            }
            loadCurrentMonth()
        }
    }

    fun deleteTransaction(transaction: TransactionEntity) {
        viewModelScope.launch {
            val deleted = repository.deleteTransaction(transaction.id)
            _uiState.update {
                it.copy(
                    debugStatusMessage = if (deleted) null else "Unable to delete ${transaction.merchant}.",
                    pendingUndoDeleteTransaction = if (deleted) transaction else it.pendingUndoDeleteTransaction
                )
            }
            loadCurrentMonth()
        }
    }

    fun ignoreDuplicate(transaction: TransactionEntity) {
        viewModelScope.launch {
            val updated = repository.ignoreDuplicate(transaction.id)
            if (updated) {
                // Update UI state with a message optionally, and reload data
                _uiState.update { it.copy(debugStatusMessage = "Transaction dismissed as duplicate.") }
                loadCurrentMonth()
            }
        }
    }

    fun restoreDeletedTransaction() {
        val pending = _uiState.value.pendingUndoDeleteTransaction ?: return
        viewModelScope.launch {
            val restored = repository.restoreTransaction(pending)
            _uiState.update {
                it.copy(
                    pendingUndoDeleteTransaction = null,
                    debugStatusMessage = if (restored) {
                        "${pending.merchant} restored."
                    } else {
                        "Unable to restore ${pending.merchant}."
                    }
                )
            }
            loadCurrentMonth()
        }
    }

    fun simulateIncomingSms(
        sender: String,
        body: String,
        label: String
    ) {
        viewModelScope.launch {
            val result = repository.simulateIncomingSms(sender = sender, body = body)
            val message = when (result) {
                is SmsIntakeOutcome.Confirmed -> {
                    "$label simulated and added instantly."
                }

                is SmsIntakeOutcome.Pending -> {
                    "$label queued for AI verification."
                }

                SmsIntakeOutcome.Discarded -> {
                    "$label was discarded by the pre-filter."
                }
            }
            _uiState.update { it.copy(debugStatusMessage = message) }
            loadCurrentMonth()
        }
    }

    fun importExistingSms() {
        if (_uiState.value.isImportingSms) {
            return
        }

        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isImportingSms = true,
                    importProgress = 0 to 0
                )
            }

            var shouldProcessPending = false

            try {
                val summary = repository.importExistingSms { processed, total ->
                    _uiState.update { state ->
                        state.copy(importProgress = processed to total)
                    }
                }

                shouldProcessPending = summary.queuedForAi > 0 && isAppInForeground
                val statusMessage = when {
                    summary.scanned == 0 -> "No SMS messages were available to scan on this device."
                    summary.importedInstantly == 0 && summary.queuedForAi == 0 ->
                        "Scanned ${summary.scanned} messages. Nothing new looked like a spend or income alert."

                    else ->
                        "Scanned ${summary.scanned} messages. Added ${summary.importedInstantly} instantly, queued ${summary.queuedForAi} for AI, skipped ${summary.skipped} others."
                }
                _uiState.update {
                    it.copy(debugStatusMessage = statusMessage)
                }
            } catch (throwable: Throwable) {
                _uiState.update {
                    it.copy(
                        debugStatusMessage = "Unable to scan existing SMS: ${throwable.message.orEmpty()}"
                    )
                }
            } finally {
                _uiState.update {
                    it.copy(
                        isImportingSms = false,
                        importProgress = 0 to 0
                    )
                }
                loadCurrentMonth()
                if (shouldProcessPending) {
                    processPendingSmsWithAi()
                } else {
                    checkPendingCount()
                }
            }
        }
    }

    fun sendDebugSms(phoneNumber: String, body: String, label: String) {
        viewModelScope.launch {
            runCatching {
                repository.sendDebugSms(phoneNumber = phoneNumber, body = body)
            }.onSuccess {
                _uiState.update {
                    it.copy(debugStatusMessage = "$label SMS sent to $phoneNumber.")
                }
            }.onFailure { throwable ->
                _uiState.update {
                    it.copy(debugStatusMessage = "Unable to send SMS: ${throwable.message.orEmpty()}")
                }
            }
        }
    }

    fun checkPendingCount() {
        viewModelScope.launch {
            val pendingCount = repository.getPendingCount()
            _uiState.update { it.copy(pendingAiCount = pendingCount) }
        }
    }

    fun addCustomCategory(name: String) {
        repository.addCustomCategory(name)
        loadCustomizations()
        _uiState.update { it.copy(debugStatusMessage = "Custom category added.") }
    }

    fun removeCustomCategory(categoryId: String) {
        repository.removeCustomCategory(categoryId)
        loadCustomizations()
        _uiState.update { it.copy(debugStatusMessage = "Custom category removed.") }
    }

    fun saveRule(rule: TransactionRule) {
        repository.saveRule(rule)
        loadCustomizations()
        _uiState.update { it.copy(debugStatusMessage = "Rule saved.") }
    }

    fun deleteRule(ruleId: String) {
        repository.deleteRule(ruleId)
        loadCustomizations()
        _uiState.update { it.copy(debugStatusMessage = "Rule deleted.") }
    }

    fun saveBudgetGoal(category: String, monthlyLimit: Double) {
        repository.saveBudgetGoal(
            BudgetGoal(
                category = category,
                monthlyLimit = monthlyLimit
            )
        )
        loadCustomizations()
        loadCurrentMonth()
        _uiState.update { it.copy(debugStatusMessage = "Budget saved.") }
    }

    fun deleteBudgetGoal(goalId: String) {
        repository.deleteBudgetGoal(goalId)
        loadCustomizations()
        loadCurrentMonth()
        _uiState.update { it.copy(debugStatusMessage = "Budget removed.") }
    }

    private fun observeSmsPipeline() {
        viewModelScope.launch {
            SmsPipelineEvents.events.collect { event ->
                when (event) {
                    SmsPipelineEvent.PendingQueued -> {
                        checkPendingCount()
                        // Service already started by SmsReceiver; just refresh count
                    }

                    is SmsPipelineEvent.ProcessingStarted -> {
                        _uiState.update {
                            it.copy(
                                isProcessingPending = true,
                                processingProgress = 0 to event.total
                            )
                        }
                    }

                    is SmsPipelineEvent.ProcessingProgress -> {
                        _uiState.update {
                            it.copy(
                                isProcessingPending = true,
                                processingProgress = event.processed to event.total,
                                currentAiReviewItem = event.current
                            )
                        }
                    }

                    SmsPipelineEvent.ProcessingComplete -> {
                        val pendingCount = repository.getPendingCount()
                        _uiState.update {
                            it.copy(
                                isProcessingPending = false,
                                processingProgress = 0 to 0,
                                pendingAiCount = pendingCount,
                                currentAiReviewItem = null
                            )
                        }
                        loadCurrentMonth()
                    }
                }
            }
        }
    }

    private fun loadCustomizations() {
        _uiState.update {
            it.copy(
                customCategories = repository.getCustomCategories(),
                transactionRules = repository.getRules(),
                budgetGoals = repository.getBudgetGoals()
            )
        }
    }

    private fun observeReviewStreams() {
        viewModelScope.launch {
            repository.observeReviewCenter().collectLatest { events ->
                _uiState.update { it.copy(reviewCenterItems = events) }
            }
        }
        viewModelScope.launch {
            repository.observeSpamInbox().collectLatest { events ->
                _uiState.update { it.copy(spamInboxItems = events) }
            }
        }
        viewModelScope.launch {
            repository.observeDebugConsole().collectLatest { events ->
                _uiState.update { it.copy(debugConsoleItems = events) }
            }
        }
        viewModelScope.launch {
            repository.observeImportSourceEvents().collectLatest { events ->
                _uiState.update { it.copy(importSourceItems = events) }
            }
        }
    }
}
