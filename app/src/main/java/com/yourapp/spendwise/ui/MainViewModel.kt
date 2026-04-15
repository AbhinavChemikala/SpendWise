package com.yourapp.spendwise.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.yourapp.spendwise.backup.BackupHistoryEntry
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
import com.yourapp.spendwise.data.db.TransactionCategoryAiEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.mail.AxisEmailSyncHistoryEntry
import com.yourapp.spendwise.mail.AxisEmailSyncTrigger
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
    val axisEmailAccount: String = "",
    val axisEmailAutoSyncEnabled: Boolean = true,
    val axisEmailLastSyncMs: Long = 0L,
    val axisEmailSyncHistory: List<AxisEmailSyncHistoryEntry> = emptyList(),
    val isAxisEmailSyncing: Boolean = false,
    val sparkMailTriggerEnabled: Boolean = false,
    val hasSparkNotificationAccess: Boolean = false,
    val themeMode: String = THEME_MODE_SYSTEM,
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 22,
    val dailyReminderMinute: Int = 0,
    val driveBackupAccount: String = "",
    val driveBackupAutoEnabled: Boolean = false,
    val driveBackupHour: Int = 2,
    val driveBackupMinute: Int = 0,
    val backupHistory: List<BackupHistoryEntry> = emptyList(),
    val isBackupBusy: Boolean = false,
    val homeCardOrder: List<String> = emptyList(),
    val homeHiddenCardIds: Set<String> = emptySet(),
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
    val showManualAddDialog: Boolean = false,
    val isFindingSimilarTransactions: Boolean = false,
    val similarTransactionSourceId: Long? = null,
    val similarTransactions: List<TransactionEntity> = emptyList(),
    val isScanningLegacyAiFailures: Boolean = false,
    val categoryRefinementRecord: TransactionCategoryAiEntity? = null,
    val categoryRefinementLoadingId: Long? = null
)

class MainViewModel(application: Application) : AndroidViewModel(application) {
    private val repository = TransactionRepository(application.applicationContext)

    private val _uiState = MutableStateFlow(
        DashboardUiState(
            debugModeEnabled = repository.isDebugModeEnabled(),
            isAiReviewEnabled = repository.isAiReviewEnabled(),
            isCloudAiEnabled = repository.isCloudAiEnabled(),
            cloudAiApiKey = repository.getCloudAiApiKey(),
            axisEmailAccount = repository.getAxisEmailAccount(),
            axisEmailAutoSyncEnabled = repository.isAxisEmailAutoSyncEnabled(),
            axisEmailLastSyncMs = repository.getAxisEmailLastSyncMs(),
            axisEmailSyncHistory = repository.getAxisEmailSyncHistory(),
            sparkMailTriggerEnabled = repository.isSparkMailTriggerEnabled(),
            hasSparkNotificationAccess = repository.hasSparkNotificationAccess(),
            themeMode = normalizeThemeMode(repository.getThemeMode()),
            dailyReminderEnabled = repository.isDailyReminderEnabled(),
            dailyReminderHour = repository.getDailyReminderHour(),
            dailyReminderMinute = repository.getDailyReminderMinute(),
            driveBackupAccount = repository.getDriveBackupAccount(),
            driveBackupAutoEnabled = repository.isDriveBackupAutoEnabled(),
            driveBackupHour = repository.getDriveBackupHour(),
            driveBackupMinute = repository.getDriveBackupMinute(),
            backupHistory = repository.getBackupHistory(),
            homeCardOrder = repository.getHomeCardOrder(),
            homeHiddenCardIds = repository.getHiddenHomeCardIds(),
            debugPhoneNumber = repository.getDebugPhoneNumber()
        )
    )
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    private var transactionsJob: Job? = null
    private var categoryRefinementJob: Job? = null
    private var isAppInForeground = false
    private var shouldRerunPendingProcessing = false

    init {
        repository.ensureAxisEmailSyncSchedule()
        repository.ensureDriveBackupSchedule()
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
            refreshNotificationAccessState()
            refreshAxisEmailSyncHistory()
            checkPendingCount()
            maybeSyncAxisEmailsInForeground()
            // Delegate to the foreground service — it handles both foreground
            // and background processing with the singleton Mutex guard.
            processPendingSmsWithAi()
        }
    }

    private fun maybeSyncAxisEmailsInForeground() {
        val state = _uiState.value
        if (state.axisEmailAccount.isBlank() || !state.axisEmailAutoSyncEnabled || state.isAxisEmailSyncing) {
            return
        }
        val staleEnough = System.currentTimeMillis() - state.axisEmailLastSyncMs > 15 * 60 * 1000L
        if (staleEnough) {
            syncAxisEmailsNow(trigger = AxisEmailSyncTrigger.FOREGROUND, silent = true)
        }
    }

    fun refreshAxisEmailSyncHistory() {
        _uiState.update {
            it.copy(axisEmailSyncHistory = repository.getAxisEmailSyncHistory())
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

    fun refreshNotificationAccessState() {
        _uiState.update {
            it.copy(hasSparkNotificationAccess = repository.hasSparkNotificationAccess())
        }
    }

    fun connectAxisEmailAccount(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(debugStatusMessage = "Unable to read the selected Gmail account.") }
            return
        }
        repository.connectAxisEmailAccount(email)
        _uiState.update {
            it.copy(
                axisEmailAccount = repository.getAxisEmailAccount(),
                axisEmailAutoSyncEnabled = repository.isAxisEmailAutoSyncEnabled(),
                axisEmailLastSyncMs = repository.getAxisEmailLastSyncMs(),
                debugStatusMessage = "Connected Gmail for Axis email sync."
            )
        }
        syncAxisEmailsNow(trigger = AxisEmailSyncTrigger.CONNECT, silent = true)
    }

    fun disconnectAxisEmailAccount() {
        repository.disconnectAxisEmailAccount()
        _uiState.update {
            it.copy(
                axisEmailAccount = "",
                axisEmailAutoSyncEnabled = false,
                axisEmailLastSyncMs = 0L,
                axisEmailSyncHistory = repository.getAxisEmailSyncHistory(),
                isAxisEmailSyncing = false,
                debugStatusMessage = "Disconnected Gmail Axis sync."
            )
        }
    }

    fun toggleAxisEmailAutoSync(enabled: Boolean) {
        repository.setAxisEmailAutoSyncEnabled(enabled)
        _uiState.update {
            it.copy(
                axisEmailAutoSyncEnabled = repository.isAxisEmailAutoSyncEnabled(),
                debugStatusMessage = if (enabled) {
                    "Axis email auto-sync enabled."
                } else {
                    "Axis email auto-sync paused."
                }
            )
        }
    }

    fun toggleSparkMailTrigger(enabled: Boolean) {
        repository.setSparkMailTriggerEnabled(enabled)
        _uiState.update {
            it.copy(
                sparkMailTriggerEnabled = repository.isSparkMailTriggerEnabled(),
                hasSparkNotificationAccess = repository.hasSparkNotificationAccess(),
                debugStatusMessage = if (enabled) {
                    "Spark Mail trigger enabled."
                } else {
                    "Spark Mail trigger disabled."
                }
            )
        }
    }

    fun syncAxisEmailsNow(
        trigger: String = AxisEmailSyncTrigger.MANUAL,
        silent: Boolean = false
    ) {
        if (_uiState.value.axisEmailAccount.isBlank()) return
        viewModelScope.launch {
            _uiState.update { it.copy(isAxisEmailSyncing = true) }
            val result = repository.syncAxisEmailsNow(trigger)
            _uiState.update {
                it.copy(
                    isAxisEmailSyncing = false,
                    axisEmailLastSyncMs = repository.getAxisEmailLastSyncMs(),
                    axisEmailSyncHistory = repository.getAxisEmailSyncHistory(),
                    debugStatusMessage = if (silent) {
                        it.debugStatusMessage
                    } else {
                        result.message
                    }
                )
            }
            checkPendingCount()
            loadCurrentMonth()
        }
    }

    fun setThemeMode(mode: String) {
        val normalizedMode = normalizeThemeMode(mode)
        repository.setThemeMode(normalizedMode)
        _uiState.update { it.copy(themeMode = normalizedMode) }
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

    fun connectDriveBackupAccount(email: String) {
        if (email.isBlank()) {
            _uiState.update { it.copy(debugStatusMessage = "Unable to read the selected Google account.") }
            return
        }
        repository.connectDriveBackupAccount(email)
        _uiState.update {
            it.copy(
                driveBackupAccount = repository.getDriveBackupAccount(),
                driveBackupAutoEnabled = repository.isDriveBackupAutoEnabled(),
                backupHistory = repository.getBackupHistory(),
                debugStatusMessage = "Connected Google Drive backup."
            )
        }
    }

    fun disconnectDriveBackupAccount() {
        repository.disconnectDriveBackupAccount()
        _uiState.update {
            it.copy(
                driveBackupAccount = "",
                driveBackupAutoEnabled = false,
                backupHistory = repository.getBackupHistory(),
                debugStatusMessage = "Disconnected Google Drive backup."
            )
        }
    }

    fun toggleDriveBackupAuto(enabled: Boolean) {
        repository.setDriveBackupAutoEnabled(enabled)
        _uiState.update {
            it.copy(
                driveBackupAutoEnabled = repository.isDriveBackupAutoEnabled(),
                backupHistory = repository.getBackupHistory(),
                debugStatusMessage = if (enabled) {
                    "Daily Drive backup enabled."
                } else {
                    "Daily Drive backup paused."
                }
            )
        }
    }

    fun setDriveBackupTime(hour: Int, minute: Int) {
        repository.setDriveBackupTime(hour, minute)
        _uiState.update {
            it.copy(
                driveBackupHour = repository.getDriveBackupHour(),
                driveBackupMinute = repository.getDriveBackupMinute(),
                debugStatusMessage = "Drive backup time updated."
            )
        }
    }

    fun exportBackupToUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true) }
            val result = runCatching { repository.exportBackupToUri(uri) }
            _uiState.update {
                it.copy(
                    isBackupBusy = false,
                    backupHistory = repository.getBackupHistory(),
                    debugStatusMessage = result.getOrNull()?.message
                        ?: result.exceptionOrNull()?.message
                        ?: "Unable to export backup."
                )
            }
        }
    }

    fun restoreBackupFromUri(uri: Uri) {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true) }
            val result = runCatching { repository.restoreBackupFromUri(uri) }
            refreshAfterBackupRestore()
            _uiState.update {
                it.copy(
                    isBackupBusy = false,
                    backupHistory = repository.getBackupHistory(),
                    debugStatusMessage = result.getOrNull()?.message
                        ?: result.exceptionOrNull()?.message
                        ?: "Unable to restore backup."
                )
            }
        }
    }

    fun pushBackupToDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true) }
            val result = repository.pushBackupToDrive()
            _uiState.update {
                it.copy(
                    isBackupBusy = false,
                    backupHistory = repository.getBackupHistory(),
                    debugStatusMessage = result.message
                )
            }
        }
    }

    fun restoreBackupFromDrive() {
        viewModelScope.launch {
            _uiState.update { it.copy(isBackupBusy = true) }
            val result = repository.restoreBackupFromDrive()
            refreshAfterBackupRestore()
            _uiState.update {
                it.copy(
                    isBackupBusy = false,
                    backupHistory = repository.getBackupHistory(),
                    debugStatusMessage = result.message
                )
            }
        }
    }

    fun refreshBackupHistory() {
        _uiState.update { it.copy(backupHistory = repository.getBackupHistory()) }
    }

    fun updateHomeCardOrder(order: List<String>) {
        repository.setHomeCardOrder(order)
        _uiState.update {
            it.copy(
                homeCardOrder = repository.getHomeCardOrder(),
                debugStatusMessage = "Home layout updated."
            )
        }
    }

    fun resetHomeCardOrder() {
        repository.resetHomeCardOrder()
        _uiState.update {
            it.copy(
                homeCardOrder = repository.getHomeCardOrder(),
                debugStatusMessage = "Home layout reset."
            )
        }
    }

    fun toggleHomeCardVisibility(cardId: String) {
        val hiddenCardIds = repository.getHiddenHomeCardIds().toMutableSet()
        if (cardId in hiddenCardIds) {
            hiddenCardIds.remove(cardId)
        } else {
            hiddenCardIds.add(cardId)
        }
        repository.setHiddenHomeCardIds(hiddenCardIds)
        _uiState.update {
            it.copy(
                homeHiddenCardIds = repository.getHiddenHomeCardIds(),
                debugStatusMessage = "Home card visibility updated."
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

    fun findSimilarTransactions(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isFindingSimilarTransactions = true,
                    similarTransactionSourceId = transaction.id,
                    similarTransactions = emptyList()
                )
            }
            val similar = repository.findSimilarTransactions(transaction)
            _uiState.update {
                it.copy(
                    isFindingSimilarTransactions = false,
                    similarTransactionSourceId = transaction.id,
                    similarTransactions = similar
                )
            }
        }
    }

    fun clearSimilarTransactions() {
        _uiState.update {
            it.copy(
                isFindingSimilarTransactions = false,
                similarTransactionSourceId = null,
                similarTransactions = emptyList()
            )
        }
    }

    fun observeCategoryRefinement(transactionId: Long) {
        categoryRefinementJob?.cancel()
        _uiState.update {
            it.copy(
                categoryRefinementLoadingId = transactionId,
                categoryRefinementRecord = null
            )
        }
        categoryRefinementJob = viewModelScope.launch {
            repository.observeCategoryRefinementRecord(transactionId).collectLatest { record ->
                _uiState.update {
                    it.copy(
                        categoryRefinementLoadingId = null,
                        categoryRefinementRecord = record
                    )
                }
            }
        }
    }

    fun clearCategoryRefinementObservation() {
        categoryRefinementJob?.cancel()
        categoryRefinementJob = null
        _uiState.update {
            it.copy(
                categoryRefinementLoadingId = null,
                categoryRefinementRecord = null
            )
        }
    }

    fun requestCategoryRefinement(transaction: TransactionEntity) {
        viewModelScope.launch {
            _uiState.update { it.copy(categoryRefinementLoadingId = transaction.id) }
            val requested = repository.requestCategoryRefinement(transaction.id)
            _uiState.update {
                it.copy(
                    categoryRefinementLoadingId = if (requested) transaction.id else null,
                    debugStatusMessage = if (requested) {
                        "AI category check started for ${transaction.merchant}."
                    } else {
                        "AI category check is unavailable for ${transaction.merchant}."
                    }
                )
            }
        }
    }

    fun retryAiReview(item: SmsReviewEntity) {
        viewModelScope.launch {
            val retried = repository.retryAiReview(item.id)
            _uiState.update {
                it.copy(
                    debugStatusMessage = if (retried) {
                        "Retrying AI review for ${item.sender}."
                    } else {
                        "Unable to retry this AI review item."
                    }
                )
            }
            checkPendingCount()
        }
    }

    fun retryAllFailedAiReviews() {
        viewModelScope.launch {
            val retriedCount = repository.retryAllFailedAiReviews()
            _uiState.update {
                it.copy(
                    debugStatusMessage = if (retriedCount > 0) {
                        "Retrying $retriedCount failed AI review items."
                    } else {
                        "No failed AI review items were ready to retry."
                    }
                )
            }
            checkPendingCount()
        }
    }

    fun recoverLegacyAiFailures() {
        viewModelScope.launch {
            _uiState.update { it.copy(isScanningLegacyAiFailures = true) }
            val recoveredCount = repository.recoverLegacyAiFailures()
            _uiState.update {
                it.copy(
                    isScanningLegacyAiFailures = false,
                    debugStatusMessage = if (recoveredCount > 0) {
                        "Recovered $recoveredCount old AI timeout items for retry."
                    } else {
                        "No old timeout-style AI review items were found."
                    }
                )
            }
        }
    }

    fun applyTransactionChangesToSimilar(
        editedTransaction: TransactionEntity,
        targetTransactionIds: Set<Long>
    ) {
        viewModelScope.launch {
            val updatedCount = repository.applyTransactionChangesToSimilar(
                editedTransaction = editedTransaction,
                targetTransactionIds = targetTransactionIds
            )
            _uiState.update {
                it.copy(
                    debugStatusMessage = if (updatedCount > 0) {
                        "Updated $updatedCount similar transactions."
                    } else {
                        "No similar transactions were updated."
                    },
                    similarTransactionSourceId = null,
                    similarTransactions = emptyList(),
                    isFindingSimilarTransactions = false
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
                budgetGoals = repository.getBudgetGoals(),
                themeMode = normalizeThemeMode(repository.getThemeMode()),
                dailyReminderEnabled = repository.isDailyReminderEnabled(),
                dailyReminderHour = repository.getDailyReminderHour(),
                dailyReminderMinute = repository.getDailyReminderMinute(),
                driveBackupAccount = repository.getDriveBackupAccount(),
                driveBackupAutoEnabled = repository.isDriveBackupAutoEnabled(),
                driveBackupHour = repository.getDriveBackupHour(),
                driveBackupMinute = repository.getDriveBackupMinute(),
                backupHistory = repository.getBackupHistory(),
                axisEmailAccount = repository.getAxisEmailAccount(),
                axisEmailAutoSyncEnabled = repository.isAxisEmailAutoSyncEnabled(),
                sparkMailTriggerEnabled = repository.isSparkMailTriggerEnabled(),
                debugModeEnabled = repository.isDebugModeEnabled(),
                isAiReviewEnabled = repository.isAiReviewEnabled(),
                isCloudAiEnabled = repository.isCloudAiEnabled(),
                homeCardOrder = repository.getHomeCardOrder(),
                homeHiddenCardIds = repository.getHiddenHomeCardIds()
            )
        }
    }

    private fun refreshAfterBackupRestore() {
        loadCustomizations()
        loadCurrentMonth()
        checkPendingCount()
        refreshAxisEmailSyncHistory()
        refreshNotificationAccessState()
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
