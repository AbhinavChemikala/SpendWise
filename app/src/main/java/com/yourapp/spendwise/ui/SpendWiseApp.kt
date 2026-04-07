package com.yourapp.spendwise.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.pm.PackageManager
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.rounded.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.rounded.ReceiptLong
import androidx.compose.material.icons.automirrored.rounded.Send
import androidx.compose.material.icons.rounded.AccountBalanceWallet
import androidx.compose.material.icons.rounded.Add
import androidx.compose.material.icons.rounded.ArrowDropDown
import androidx.compose.material.icons.rounded.ArrowDownward
import androidx.compose.material.icons.rounded.CreditCard
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material.icons.rounded.DeleteOutline
import androidx.compose.material.icons.rounded.DirectionsCar
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.LocalAtm
import androidx.compose.material.icons.rounded.Movie
import androidx.compose.material.icons.rounded.Payments
import androidx.compose.material.icons.rounded.PieChart
import androidx.compose.material.icons.rounded.PlayArrow
import androidx.compose.material.icons.rounded.Psychology
import androidx.compose.material.icons.rounded.Restaurant
import androidx.compose.material.icons.rounded.Search
import androidx.compose.material.icons.rounded.Settings
import androidx.compose.material.icons.rounded.ShoppingBag
import androidx.compose.material.icons.rounded.SwapHoriz
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.Visibility
import androidx.compose.material.icons.rounded.VisibilityOff
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.InputChip
import androidx.compose.material3.InputChipDefaults
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.listSaver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourapp.spendwise.data.CustomCategory
import com.yourapp.spendwise.data.BudgetGoal
import com.yourapp.spendwise.data.BudgetProgress
import com.yourapp.spendwise.data.AnomalyAlert
import com.yourapp.spendwise.data.CashflowDay
import com.yourapp.spendwise.data.CompareMetric
import com.yourapp.spendwise.data.DuplicateInsight
import com.yourapp.spendwise.data.InsightFact
import com.yourapp.spendwise.data.IncomeTrendSummary
import com.yourapp.spendwise.data.MerchantAnalytics
import com.yourapp.spendwise.data.PaymentModeTotal
import com.yourapp.spendwise.data.RangeSummary
import com.yourapp.spendwise.data.RecurringInsight
import com.yourapp.spendwise.data.SummaryRangeType
import com.yourapp.spendwise.data.TransactionRule
import com.yourapp.spendwise.data.TrendPoint
import com.yourapp.spendwise.data.db.CategoryTotal
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue

internal val AccentPink = Color(0xFFFF577A)
internal val AccentTeal = Color(0xFF24B6D3)
internal val AccentPurple = Color(0xFF6F49FF)
internal val AccentGreen = Color(0xFF2E8B57)
internal val AccentAmber = Color(0xFFF5A623)

private data class DebugSmsTemplate(
    val title: String,
    val subtitle: String,
    val sender: String,
    val body: String
)

private data class AdvancedFilterState(
    val merchant: String = "",
    val bank: String = "",
    val category: String = "",
    val minAmount: String = "",
    val maxAmount: String = "",
    val aiOnly: Boolean = false
)

private val AdvancedFilterStateSaver: Saver<AdvancedFilterState, Any> = listSaver(
    save = { state ->
        listOf(
            state.merchant,
            state.bank,
            state.category,
            state.minAmount,
            state.maxAmount,
            state.aiOnly
        )
    },
    restore = { restored ->
        AdvancedFilterState(
            merchant = restored.getOrNull(0) as? String ?: "",
            bank = restored.getOrNull(1) as? String ?: "",
            category = restored.getOrNull(2) as? String ?: "",
            minAmount = restored.getOrNull(3) as? String ?: "",
            maxAmount = restored.getOrNull(4) as? String ?: "",
            aiOnly = restored.getOrNull(5) as? Boolean ?: false
        )
    }
)

private enum class TransactionDialogMode {
    VIEW,
    EDIT
}

private val debugTemplates = listOf(
    DebugSmsTemplate(
        title = "Salary credit",
        subtitle = "Income template",
        sender = "HDFCBK",
        body = "Rs. 45000 credited to A/c XX0213 via NEFT SALARY on 05-Apr. Avl bal INR 1,14,550.00"
    ),
    DebugSmsTemplate(
        title = "UPI debit",
        subtitle = "Expense template",
        sender = "SBIINB",
        body = "INR 550 debited from A/c XX1890 for UPI txn to GUNTUR on 05-Apr. Ref 69053378638"
    ),
    DebugSmsTemplate(
        title = "Refund received",
        subtitle = "Credit template",
        sender = "ICICIB",
        body = "Rs 1500 credited to your account as refund from AMAZON PAY on 05-Apr. Available balance INR 82,540"
    ),
    DebugSmsTemplate(
        title = "Scam KYC",
        subtitle = "Fraud template",
        sender = "ALERTS",
        body = "Urgent KYC update needed. Click http://bit.ly/kyc-now to verify or your account will be blocked immediately."
    )
)

@Composable
fun SpendWiseApp(vm: MainViewModel) {
    val uiState by vm.uiState.collectAsStateWithLifecycle()
    val lifecycleOwner = LocalLifecycleOwner.current
    val context = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }
    var showMonthPicker by rememberSaveable { mutableStateOf(false) }
    var transactionPendingDelete by remember { mutableStateOf<TransactionEntity?>(null) }
    var selectedTransaction by remember { mutableStateOf<TransactionEntity?>(null) }
    var transactionDialogMode by rememberSaveable { mutableStateOf(TransactionDialogMode.VIEW) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> vm.setAppForeground(true)
                Lifecycle.Event.ON_PAUSE -> vm.setAppForeground(false)
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            vm.setAppForeground(false)
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    LaunchedEffect(uiState.debugStatusMessage) {
        val message = uiState.debugStatusMessage ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(message)
        vm.clearDebugStatus()
    }

    LaunchedEffect(uiState.pendingUndoDeleteTransaction?.id) {
        val deleted = uiState.pendingUndoDeleteTransaction ?: return@LaunchedEffect
        val result = snackbarHostState.showSnackbar(
            message = "${deleted.merchant} deleted",
            actionLabel = "Undo",
            duration = SnackbarDuration.Short
        )
        if (result == SnackbarResult.ActionPerformed) {
            vm.restoreDeletedTransaction()
        } else {
            vm.clearUndoDelete()
        }
    }

    if (showMonthPicker) {
        MonthPickerDialog(
            initialYear = uiState.selectedYear,
            selectedMonth = uiState.selectedMonth,
            onDismiss = { showMonthPicker = false },
            onMonthSelected = { year, month ->
                showMonthPicker = false
                vm.changeMonth(year, month)
            }
        )
    }

    if (uiState.showManualAddDialog) {
        ManualTransactionDialog(
            onDismiss = vm::closeManualAddDialog,
            onSave = { amount, type, merchant, bank ->
                vm.closeManualAddDialog()
                vm.addManualTransaction(amount, type, merchant, bank)
            }
        )
    }

    transactionPendingDelete?.let { transaction ->
        AlertDialog(
            onDismissRequest = { transactionPendingDelete = null },
            title = { Text("Delete transaction") },
            text = {
                Text("Remove ${transaction.merchant} for ${formatRupees(transaction.amount)} from your records?")
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        vm.deleteTransaction(transaction)
                        transactionPendingDelete = null
                    }
                ) {
                    Text("Delete")
                }
            },
            dismissButton = {
                TextButton(onClick = { transactionPendingDelete = null }) {
                    Text("Cancel")
                }
            }
        )
    }

    selectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            initialMode = transactionDialogMode,
            availableCategories = availableCategories(uiState.customCategories, uiState.transactions, transaction.category),
            onDismiss = { selectedTransaction = null },
            onSave = { updated ->
                vm.updateTransaction(updated)
                selectedTransaction = updated
                transactionDialogMode = TransactionDialogMode.VIEW
            }
        )
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        snackbarHost = { SnackbarHost(snackbarHostState) },
        floatingActionButton = {
            FloatingActionButton(
                onClick = vm::openManualAddDialog,
                containerColor = AccentPurple,
                contentColor = Color.White
            ) {
                Icon(Icons.Rounded.Add, contentDescription = "Add transaction")
            }
        },
        bottomBar = {
            SpendWiseBottomBar(
                selectedTab = uiState.selectedTab,
                onSelected = vm::selectTab
            )
        }
    ) { innerPadding ->
        when (uiState.selectedTab) {
            SpendWiseTab.HOME -> HomeScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onDeleteTransaction = { transactionPendingDelete = it },
                onOpenTransaction = {
                    selectedTransaction = it
                    transactionDialogMode = TransactionDialogMode.VIEW
                },
                onEditTransaction = {
                    selectedTransaction = it
                    transactionDialogMode = TransactionDialogMode.EDIT
                },
                onRemoveDuplicateRequest = vm::ignoreDuplicate,
                onSelectSummaryRange = vm::selectSummaryRange,
                onOpenInsights = { vm.selectTab(SpendWiseTab.INSIGHTS) },
                onOpenActivity = { vm.selectTab(SpendWiseTab.ACTIVITY) },
                onMonthClick = { showMonthPicker = true }
            )

            SpendWiseTab.ACTIVITY -> ActivityScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onDeleteTransaction = { transactionPendingDelete = it },
                onOpenTransaction = {
                    selectedTransaction = it
                    transactionDialogMode = TransactionDialogMode.VIEW
                },
                onEditTransaction = {
                    selectedTransaction = it
                    transactionDialogMode = TransactionDialogMode.EDIT
                },
                onRemoveDuplicateRequest = vm::ignoreDuplicate,
                onMonthClick = { showMonthPicker = true }
            )

            SpendWiseTab.INSIGHTS -> InsightsScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onSelectSummaryRange = vm::selectSummaryRange,
                onMonthClick = { showMonthPicker = true }
            )

            SpendWiseTab.REVIEW_CENTER -> AiReviewScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState
            )

            SpendWiseTab.SETTINGS -> SettingsScreen(
                modifier = Modifier.padding(innerPadding),
                uiState = uiState,
                onScanExistingSms = {
                    if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.READ_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        context.findActivity()?.let { activity ->
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.READ_SMS),
                                103
                            )
                        }
                    } else {
                        vm.importExistingSms()
                    }
                },
                onAddCategory = vm::addCustomCategory,
                onRemoveCategory = vm::removeCustomCategory,
                onSaveRule = vm::saveRule,
                onDeleteRule = vm::deleteRule,
                onSaveBudget = vm::saveBudgetGoal,
                onDeleteBudget = vm::deleteBudgetGoal,
                onToggleDebug = vm::toggleDebugMode,
                onToggleAiReview = vm::toggleAiReview,
                onToggleCloudAi = vm::toggleCloudAi,
                onUpdateCloudAiApiKey = vm::updateCloudAiApiKey,
                onSetThemeMode = vm::setThemeMode,
                onPhoneChange = vm::updateDebugPhoneNumber,
                onSimulateTemplate = { template ->
                    vm.simulateIncomingSms(template.sender, template.body, template.title)
                },
                onSendTemplate = { template ->
                    val phone = uiState.debugPhoneNumber.trim()
                    if (phone.isBlank()) {
                        vm.setDebugStatus("Add a phone number before sending a real SMS.")
                    } else if (ContextCompat.checkSelfPermission(
                            context,
                            Manifest.permission.SEND_SMS
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        context.findActivity()?.let { activity ->
                            ActivityCompat.requestPermissions(
                                activity,
                                arrayOf(Manifest.permission.SEND_SMS),
                                102
                            )
                        }
                    } else {
                        vm.sendDebugSms(phone, template.body, template.title)
                    }
                }
            )
        }
    }
}

@Composable
private fun HomeScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onRemoveDuplicateRequest: (TransactionEntity) -> Unit,
    onSelectSummaryRange: (SummaryRangeType) -> Unit,
    onOpenInsights: () -> Unit,
    onOpenActivity: () -> Unit,
    onMonthClick: () -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "SpendWise",
                subtitle = "Smart money tracking",
                monthLabel = formatMonth(uiState.selectedYear, uiState.selectedMonth),
                onMonthClick = onMonthClick
            )
        }
        item { StatusCard(uiState) }
        item {
            SummaryRangeCard(
                selected = uiState.selectedSummaryRange,
                ranges = uiState.rangeSummaries,
                onSelect = onSelectSummaryRange
            )
        }
        item { HeroSummaryCard(uiState) }
        item { SavingsScoreCard(uiState.savingsScore, uiState.incomeTrend) }
        item { BudgetProgressCard(uiState.budgetProgress) }
        item { AnomalyAlertsCard(uiState.anomalyAlerts) }
        item { CashflowCard(uiState.cashflowDays) }
        item {
            InsightsPreviewCard(
                totalSpent = uiState.totalSpent,
                topCategories = uiState.topCategories,
                paymentModes = uiState.paymentModes,
                onViewAll = onOpenInsights
            )
        }
        item {
            SectionTitle(
                title = "Recent Transactions",
                actionLabel = "See all",
                onAction = onOpenActivity
            )
        }
        items(uiState.transactions.take(5), key = { it.id }) { transaction ->
            TransactionListItem(
                transaction = transaction,
                onDeleteRequest = { onDeleteTransaction(transaction) },
                onEditRequest = { onEditTransaction(transaction) },
                onOpenRequest = { onOpenTransaction(transaction) },
                onRemoveDuplicateRequest = { onRemoveDuplicateRequest(transaction) },
                isLikelyDuplicate = transaction.id in uiState.duplicateTransactionIds
            )
        }
    }
}

@Composable
private fun ActivityScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onRemoveDuplicateRequest: (TransactionEntity) -> Unit,
    onMonthClick: () -> Unit
) {
    var query by rememberSaveable { mutableStateOf("") }
    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(0) }
    var showAdvancedFilters by rememberSaveable { mutableStateOf(false) }
    var advancedFilters by rememberSaveable(stateSaver = AdvancedFilterStateSaver) {
        mutableStateOf(AdvancedFilterState())
    }
    val filteredTransactions = remember(uiState.transactions, query, selectedTypeIndex, advancedFilters) {
        uiState.transactions.filter { transaction ->
            val matchesType = when (selectedTypeIndex) {
                1 -> transaction.type == TransactionType.DEBIT
                2 -> transaction.type == TransactionType.CREDIT
                else -> true
            }
            val searchTerm = query.trim().lowercase(Locale.ENGLISH)
            val matchesQuery = searchTerm.isBlank() || listOf(
                transaction.merchant,
                transaction.bank,
                transaction.category,
                transaction.rawSms
            ).any { it.lowercase(Locale.ENGLISH).contains(searchTerm) }
            val merchantFilter = advancedFilters.merchant.trim().lowercase(Locale.ENGLISH)
            val bankFilter = advancedFilters.bank.trim().lowercase(Locale.ENGLISH)
            val categoryFilter = advancedFilters.category.trim().lowercase(Locale.ENGLISH)
            val minAmount = advancedFilters.minAmount.toDoubleOrNull()
            val maxAmount = advancedFilters.maxAmount.toDoubleOrNull()
            val matchesMerchant = merchantFilter.isBlank() ||
                transaction.merchant.lowercase(Locale.ENGLISH).contains(merchantFilter)
            val matchesBank = bankFilter.isBlank() ||
                transaction.bank.lowercase(Locale.ENGLISH).contains(bankFilter) ||
                transaction.accountLabel.lowercase(Locale.ENGLISH).contains(bankFilter)
            val matchesCategory = categoryFilter.isBlank() ||
                transaction.category.lowercase(Locale.ENGLISH) == categoryFilter
            val matchesAi = !advancedFilters.aiOnly || transaction.isVerifiedByAi
            val matchesMin = minAmount == null || transaction.amount >= minAmount
            val matchesMax = maxAmount == null || transaction.amount <= maxAmount
            matchesType && matchesQuery && matchesMerchant && matchesBank &&
                matchesCategory && matchesAi && matchesMin && matchesMax
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Transactions",
                subtitle = "Track every debit and credit",
                monthLabel = formatMonth(uiState.selectedYear, uiState.selectedMonth),
                onMonthClick = onMonthClick
            )
        }
        item {
            SegmentedToggle(
                options = listOf("All", "Expenses", "Income"),
                selectedIndex = selectedTypeIndex,
                onSelected = { selectedTypeIndex = it }
            )
        }
        item {
            OutlinedTextField(
                value = query,
                onValueChange = { query = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Search transactions") },
                leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
            )
        }
        item {
            // ── Account filter chips ─────────────────────────────────
            val accounts = remember(uiState.transactions) {
                uiState.transactions
                    .map { it.accountLabel.ifBlank { it.bank } }
                    .distinct()
                    .filter { it.isNotBlank() && it != "Unknown" }
                    .sorted()
            }
            if (accounts.size > 1) {
                var selectedAccount by rememberSaveable { mutableStateOf("") }
                // Also feed into filter — expose via key change
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    contentPadding = PaddingValues(horizontal = 2.dp)
                ) {
                    item {
                        FilterChip(
                            selected = selectedAccount.isEmpty(),
                            onClick = { selectedAccount = ""; advancedFilters = advancedFilters.copy(bank = "") },
                            label = { Text("All Accounts") }
                        )
                    }
                    items(accounts) { acc ->
                        FilterChip(
                            selected = selectedAccount == acc,
                            onClick = {
                                selectedAccount = acc
                                advancedFilters = advancedFilters.copy(bank = acc)
                            },
                            label = { Text(acc) }
                        )
                    }
                }
            }
        }
        item {
            FilterBar(
                showAdvancedFilters = showAdvancedFilters,
                advancedFilters = advancedFilters,
                categoryOptions = availableCategories(uiState.customCategories, uiState.transactions),
                onToggleAdvanced = { showAdvancedFilters = !showAdvancedFilters },
                onFilterChange = { advancedFilters = it }
            )
        }
        if (filteredTransactions.isEmpty()) {
            item { EmptyStateCard("No transactions match this view yet.") }
        } else {
            items(filteredTransactions, key = { it.id }) { transaction ->
                TransactionListItem(
                    transaction = transaction,
                    onDeleteRequest = { onDeleteTransaction(transaction) },
                    onEditRequest = { onEditTransaction(transaction) },
                    onOpenRequest = { onOpenTransaction(transaction) },
                    onRemoveDuplicateRequest = { onRemoveDuplicateRequest(transaction) },
                    isLikelyDuplicate = transaction.id in uiState.duplicateTransactionIds
                )
            }
        }
    }
}

@Composable
private fun InsightsScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onSelectSummaryRange: (SummaryRangeType) -> Unit,
    onMonthClick: () -> Unit
) {
    var selectedTrendIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Insights",
                subtitle = "This month",
                monthLabel = formatMonth(uiState.selectedYear, uiState.selectedMonth),
                onMonthClick = onMonthClick
            )
        }
        item { StatusCard(uiState) }
        item {
            SummaryRangeCard(
                selected = uiState.selectedSummaryRange,
                ranges = uiState.rangeSummaries,
                onSelect = onSelectSummaryRange
            )
        }
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Expenses",
                    amount = uiState.totalSpent,
                    color = AccentPink
                )
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Income",
                    amount = uiState.totalReceived,
                    color = AccentTeal
                )
            }
        }
        item { FactsCard(uiState.insightFacts) }
        item { CompareMetricsCard(uiState.compareMetrics, uiState.totalSpent, uiState.totalReceived) }
        item { IncomeTrendCard(uiState.incomeTrend) }
        item { BudgetProgressCard(uiState.budgetProgress) }
        item { AnomalyAlertsCard(uiState.anomalyAlerts) }
        item { CashflowCard(uiState.cashflowDays) }
        item { SpecialTrackingCard(uiState) }
        item {
            SpendingBreakdownCard(
                totalSpent = uiState.totalSpent,
                topCategories = uiState.topCategories
            )
        }
        item { TopCategoriesCard(uiState.topCategories, uiState.totalSpent) }
        item { PaymentModeCard(uiState.paymentModes, uiState.totalSpent) }
        item { MerchantAnalyticsCard(uiState.topMerchants) }
        item { RecurringInsightsCard(uiState.recurringInsights, title = "Subscriptions & Recurring") }
        item { DuplicateInsightsCard(uiState.duplicateInsights) }
        item {
            TrendCard(
                trend = uiState.trend,
                selectedIndex = selectedTrendIndex,
                onSelected = { selectedTrendIndex = it },
                latestSpent = uiState.totalSpent,
                latestIncome = uiState.totalReceived
            )
        }
    }
}

@Composable
private fun SettingsScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onScanExistingSms: () -> Unit,
    onAddCategory: (String) -> Unit,
    onRemoveCategory: (String) -> Unit,
    onSaveRule: (TransactionRule) -> Unit,
    onDeleteRule: (String) -> Unit,
    onSaveBudget: (String, Double) -> Unit,
    onDeleteBudget: (String) -> Unit,
    onToggleDebug: (Boolean) -> Unit,
    onToggleAiReview: (Boolean) -> Unit,
    onToggleCloudAi: (Boolean) -> Unit,
    onUpdateCloudAiApiKey: (String) -> Unit,
    onSetThemeMode: (String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSimulateTemplate: (DebugSmsTemplate) -> Unit,
    onSendTemplate: (DebugSmsTemplate) -> Unit
) {
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TransactionRule?>(null) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showReviewCenter by rememberSaveable { mutableStateOf(false) }
    var showSpamInbox by rememberSaveable { mutableStateOf(false) }
    var showDebugConsole by rememberSaveable { mutableStateOf(false) }
    var showSourceExplorer by rememberSaveable { mutableStateOf(false) }

    if (showCategoryDialog) {
        CategoryEditorDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = {
                onAddCategory(it)
                showCategoryDialog = false
            }
        )
    }

    editingRule?.let { rule ->
        RuleEditorDialog(
            initialRule = rule,
            onDismiss = { editingRule = null },
            onSave = {
                onSaveRule(it)
                editingRule = null
            }
        )
    }

    if (showBudgetDialog) {
        BudgetEditorDialog(
            availableCategories = availableCategories(uiState.customCategories, uiState.transactions),
            onDismiss = { showBudgetDialog = false },
            onSave = { category, amount ->
                onSaveBudget(category, amount)
                showBudgetDialog = false
            }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            ScreenHeader(
                title = "Settings",
                subtitle = "Control testing and preferences"
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Notifications", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "SpendWise sends alerts for instant detections and keeps AI-review items visible until they are confirmed or dismissed.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
        // ── Theme Toggle ────────────────────────────────────────────────────────
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("App Theme", fontWeight = FontWeight.SemiBold)
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        listOf("system" to "📱 System", "light" to "☀️ Light", "dark" to "🌙 Dark").forEach { (mode, label) ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(label) },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Nano for AI Review", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Allow Gemini Nano to process and evaluate SMS locally on your device. Turning this off directly adds messages without AI review.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isAiReviewEnabled,
                            onCheckedChange = onToggleAiReview
                        )
                    }
                }
            }
        }
        item {
            var keyText by remember { mutableStateOf(uiState.cloudAiApiKey) }
            var showKey by remember { mutableStateOf(false) }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Use Gemma 3 27B (Cloud AI)", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Google’s Gemma 3 27B via AI Studio API — primary engine, falls back to Nano when offline. Free 14,400 req/day.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isCloudAiEnabled,
                            onCheckedChange = { onToggleCloudAi(it) }
                        )
                    }
                    if (uiState.isCloudAiEnabled) {
                        Text("Google AI Studio API Key", fontWeight = FontWeight.Medium)
                        OutlinedTextField(
                            value = keyText,
                            onValueChange = { keyText = it },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("AIza...") },
                            singleLine = true,
                            visualTransformation = if (showKey) VisualTransformation.None
                                else PasswordVisualTransformation(),
                            trailingIcon = {
                                IconButton(onClick = { showKey = !showKey }) {
                                    Icon(
                                        if (showKey) Icons.Rounded.VisibilityOff
                                        else Icons.Rounded.Visibility,
                                        contentDescription = if (showKey) "Hide" else "Show"
                                    )
                                }
                            }
                        )
                        Row(
                            horizontalArrangement = Arrangement.End,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            TextButton(onClick = { onUpdateCloudAiApiKey(keyText) }) {
                                Text("Save Key")
                            }
                        }
                        Text(
                            text = "🔗 Get free API key: aistudio.google.com → Get API Key",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Text("Import existing SMS", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Scan the SMS inbox on this device and pull in older bank alerts, UPI spends, and credits using the same offline pipeline.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    TextButton(
                        onClick = onScanExistingSms,
                        enabled = !uiState.isImportingSms
                    ) {
                        Icon(Icons.Rounded.Search, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (uiState.isImportingSms) "Scanning messages..." else "Scan device messages")
                    }
                    if (uiState.isImportingSms) {
                        val progress = uiState.importProgress
                        val total = progress.second.coerceAtLeast(1)
                        LinearProgressIndicator(
                            progress = { progress.first.toFloat() / total.toFloat() },
                            modifier = Modifier.fillMaxWidth(),
                            color = AccentTeal,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        Text(
                            text = "${progress.first} / ${progress.second} messages scanned",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
        item {
            CategoryManagementCard(
                categories = uiState.customCategories,
                onAddCategory = { showCategoryDialog = true },
                onRemoveCategory = onRemoveCategory
            )
        }
        item {
            RuleManagementCard(
                rules = uiState.transactionRules,
                onAddRule = { editingRule = TransactionRule() },
                onEditRule = { editingRule = it },
                onDeleteRule = onDeleteRule
            )
        }
        item {
            BudgetManagementCard(
                goals = uiState.budgetGoals,
                progress = uiState.budgetProgress,
                onAddBudget = { showBudgetDialog = true },
                onDeleteBudget = onDeleteBudget
            )
        }
        item {
            ExpandableReviewCard(
                title = "Review center",
                subtitle = "See queued and AI-reviewed SMS decisions.",
                items = uiState.reviewCenterItems,
                expanded = showReviewCenter,
                onToggle = { showReviewCenter = !showReviewCenter }
            )
        }
        item {
            ExpandableReviewCard(
                title = "Spam inbox",
                subtitle = "Suspicious or rejected SMS kept for manual inspection.",
                items = uiState.spamInboxItems,
                expanded = showSpamInbox,
                onToggle = { showSpamInbox = !showSpamInbox }
            )
        }
        item {
            ExpandableReviewCard(
                title = "Debug console",
                subtitle = "Regex, pre-filter, AI output, and final pipeline decisions.",
                items = uiState.debugConsoleItems,
                expanded = showDebugConsole,
                onToggle = { showDebugConsole = !showDebugConsole }
            )
        }
        item {
            ExpandableReviewCard(
                title = "SMS source explorer",
                subtitle = "Imported SMS and whether each one was added, queued, skipped, or rejected.",
                items = uiState.importSourceItems,
                expanded = showSourceExplorer,
                onToggle = { showSourceExplorer = !showSourceExplorer }
            )
        }
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Debug mode", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Enable template-driven SMS testing tools.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.debugModeEnabled,
                            onCheckedChange = onToggleDebug
                        )
                    }

                    if (uiState.debugModeEnabled) {
                        OutlinedTextField(
                            value = uiState.debugPhoneNumber,
                            onValueChange = onPhoneChange,
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true,
                            label = { Text("Phone number for real SMS tests") },
                            keyboardOptions = KeyboardOptions(autoCorrectEnabled = false)
                        )

                        debugTemplates.forEach { template ->
                            DebugTemplateCard(
                                template = template,
                                onSimulate = { onSimulateTemplate(template) },
                                onSend = { onSendTemplate(template) }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SpendWiseBottomBar(
    selectedTab: SpendWiseTab,
    onSelected: (SpendWiseTab) -> Unit
) {
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 8.dp) {
        listOf(
            SpendWiseTab.HOME to Pair("Home", Icons.Rounded.Home),
            SpendWiseTab.ACTIVITY to Pair("Activity", Icons.AutoMirrored.Rounded.ReceiptLong),
            SpendWiseTab.REVIEW_CENTER to Pair("AI Review", Icons.Rounded.Psychology),
            SpendWiseTab.INSIGHTS to Pair("Insights", Icons.Rounded.PieChart),
            SpendWiseTab.SETTINGS to Pair("Settings", Icons.Rounded.Settings)
        ).forEach { (tab, content) ->
            NavigationBarItem(
                selected = selectedTab == tab,
                onClick = { onSelected(tab) },
                icon = { Icon(content.second, contentDescription = content.first) },
                label = { Text(content.first) }
            )
        }
    }
}

@Composable
internal fun ScreenHeader(
    title: String,
    subtitle: String,
    monthLabel: String? = null,
    onMonthClick: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column {
            Text(text = title, fontWeight = FontWeight.Bold)
            Text(text = subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        if (monthLabel != null && onMonthClick != null) {
            AssistChip(
                onClick = onMonthClick,
                label = { Text(monthLabel) },
                trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                colors = AssistChipDefaults.assistChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            )
        }
    }
}

@Composable
private fun StatusCard(uiState: DashboardUiState) {
    when {
        uiState.isProcessingPending -> {
            Card(colors = CardDefaults.cardColors(containerColor = AccentPurple.copy(alpha = 0.12f))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(26.dp),
                        color = AccentPurple,
                        strokeWidth = 3.dp
                    )
                    Column {
                        Text("On-device AI is checking pending SMS...", fontWeight = FontWeight.SemiBold)
                        Text(
                            "${uiState.processingProgress.first} / ${uiState.processingProgress.second} messages",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }

        uiState.pendingAiCount > 0 -> {
            Card(colors = CardDefaults.cardColors(containerColor = AccentAmber.copy(alpha = 0.15f))) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(Icons.Rounded.Psychology, contentDescription = null, tint = AccentAmber)
                    Text(
                        text = "${uiState.pendingAiCount} SMS waiting for AI verification",
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@Composable
private fun SummaryRangeCard(
    selected: SummaryRangeType,
    ranges: List<RangeSummary>,
    onSelect: (SummaryRangeType) -> Unit
) {
    val selectedSummary = ranges.firstOrNull { it.type == selected }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Quick Summary", fontWeight = FontWeight.Bold)
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ranges) { summary ->
                    FilterChip(
                        selected = selected == summary.type,
                        onClick = { onSelect(summary.type) },
                        label = { Text(summary.label) }
                    )
                }
            }
            selectedSummary?.let { summary ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    CompactAmountCard(
                        modifier = Modifier.weight(1f),
                        label = "Spent",
                        amount = summary.spent,
                        color = AccentPink
                    )
                    CompactAmountCard(
                        modifier = Modifier.weight(1f),
                        label = "Income",
                        amount = summary.income,
                        color = AccentTeal
                    )
                }
                Text("${summary.transactionCount} transactions in ${summary.label.lowercase(Locale.ENGLISH)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun HeroSummaryCard(uiState: DashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = Color.Transparent), shape = RoundedCornerShape(28.dp)) {
        Column(
            modifier = Modifier
                .background(
                    brush = Brush.linearGradient(listOf(AccentPink, Color(0xFFFF6E90)))
                )
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            Text("TOTAL EXPENSES", color = Color.White.copy(alpha = 0.85f), fontWeight = FontWeight.Bold)
            Text(
                text = formatRupees(uiState.totalSpent),
                color = Color.White,
                fontWeight = FontWeight.Black
            )
            Row(modifier = Modifier.fillMaxWidth()) {
                StatBlock(Modifier.weight(1f), "Transactions", uiState.transactionCount.toString(), Color.White)
                StatBlock(
                    Modifier.weight(1f),
                    "Cash out",
                    formatRupees(uiState.specialTracking.cashWithdrawals),
                    Color.White
                )
                StatBlock(
                    Modifier.weight(1f),
                    "Top category",
                    uiState.topCategoryName,
                    Color.White
                )
            }
        }
    }
}

@Composable
private fun SavingsScoreCard(
    savingsScore: com.yourapp.spendwise.data.SavingsScore,
    incomeTrend: IncomeTrendSummary
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text("Savings Score", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("${savingsScore.score}/100", fontWeight = FontWeight.Black, color = AccentGreen)
                Text(formatRupees(savingsScore.disposableIncome), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            Text(savingsScore.summary, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(incomeTrend.salaryConsistencyText, color = AccentPurple)
        }
    }
}

@Composable
private fun StatBlock(
    modifier: Modifier,
    label: String,
    value: String,
    textColor: Color
) {
    Column(modifier = modifier) {
        Text(label.uppercase(Locale.ENGLISH), color = textColor.copy(alpha = 0.8f), fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(6.dp))
        Text(
            text = value,
            color = textColor,
            fontWeight = FontWeight.Bold,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun BudgetProgressCard(progress: List<BudgetProgress>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Budgets", fontWeight = FontWeight.Bold)
            if (progress.isEmpty()) {
                Text("Add category budgets from Settings to track overspend.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                progress.forEach { budget ->
                    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(budget.category, fontWeight = FontWeight.SemiBold)
                            Text("${formatRupees(budget.spentAmount)} / ${formatRupees(budget.monthlyLimit)}", color = if (budget.isExceeded) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        LinearProgressIndicator(
                            progress = { budget.progress.toFloat().coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(100.dp)),
                            color = if (budget.isExceeded) AccentPink else AccentTeal,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                        if (budget.isExceeded) {
                            Text("Over budget", color = AccentPink, fontWeight = FontWeight.Medium)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun InsightsPreviewCard(
    totalSpent: Double,
    topCategories: List<CategoryTotal>,
    paymentModes: List<PaymentModeTotal>,
    onViewAll: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            SectionTitle(title = "Insights", actionLabel = "View all", onAction = onViewAll)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(124.dp), contentAlignment = Alignment.Center) {
                    DonutChart(
                        values = topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) },
                        colors = donutColors(topCategories.size)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    topCategories.ifEmpty { listOf(CategoryTotal("Other", 0.0)) }.take(3).forEachIndexed { index, categoryTotal ->
                        LegendRow(
                            color = donutColors(topCategories.size)[index % donutColors(topCategories.size).size],
                            label = categoryTotal.category,
                            percent = if (totalSpent <= 0.0) 0 else ((categoryTotal.totalAmount / totalSpent) * 100).toInt()
                        )
                    }
                    if (paymentModes.isNotEmpty()) {
                        Text(
                            text = "Top payment mode: ${paymentModes.first().mode}",
                            color = AccentPurple,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SectionTitle(
    title: String,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = title, fontWeight = FontWeight.Bold)
        if (actionLabel != null && onAction != null) {
            Text(
                text = actionLabel,
                color = AccentPurple,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.clickable(onClick = onAction)
            )
        }
    }
}

@Composable
private fun LegendRow(color: Color, label: String, percent: Int) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Text(text = label, fontWeight = FontWeight.Medium)
        }
        Text("$percent%", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
    }
}

@Composable
private fun CompactAmountCard(
    modifier: Modifier,
    label: String,
    amount: Double,
    color: Color
) {
    Card(modifier = modifier, colors = CardDefaults.cardColors(containerColor = color.copy(alpha = 0.14f))) {
        Column(
            modifier = Modifier.padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(label.uppercase(Locale.ENGLISH), color = color, fontWeight = FontWeight.Bold)
            Text(text = formatRupees(amount), color = color, fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun FactsCard(facts: List<InsightFact>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Facts", fontWeight = FontWeight.Bold)
            facts.forEach { fact ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(fact.title, color = AccentPurple, fontWeight = FontWeight.Bold)
                        Text(fact.body, color = MaterialTheme.colorScheme.onSurface)
                    }
                }
            }
        }
    }
}

@Composable
private fun SpecialTrackingCard(uiState: DashboardUiState) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Tracked Flows", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Salary",
                    amount = uiState.specialTracking.salaryCredits,
                    color = AccentGreen
                )
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Refunds",
                    amount = uiState.specialTracking.refundCredits,
                    color = AccentTeal
                )
            }
            CompactAmountCard(
                modifier = Modifier.fillMaxWidth(),
                label = "Cash Withdrawal",
                amount = uiState.specialTracking.cashWithdrawals,
                color = AccentAmber
            )
        }
    }
}

@Composable
private fun SpendingBreakdownCard(totalSpent: Double, topCategories: List<CategoryTotal>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Spending Breakdown", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(18.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(modifier = Modifier.size(132.dp), contentAlignment = Alignment.Center) {
                    DonutChart(
                        values = topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) },
                        colors = donutColors(topCategories.size)
                    )
                }
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    topCategories.ifEmpty { listOf(CategoryTotal("Other", 0.0)) }.forEachIndexed { index, category ->
                        LegendRow(
                            color = donutColors(topCategories.size)[index % donutColors(topCategories.size).size],
                            label = category.category,
                            percent = if (totalSpent <= 0.0) 0 else ((category.totalAmount / totalSpent) * 100).toInt()
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PaymentModeCard(paymentModes: List<PaymentModeTotal>, totalSpent: Double) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Payment Modes", fontWeight = FontWeight.Bold)
            if (paymentModes.isEmpty()) {
                EmptyStateCard("Payment mode insights appear after debit transactions are detected.")
            } else {
                paymentModes.forEach { mode ->
                    val percent = if (totalSpent <= 0.0) 0 else ((mode.amount / totalSpent) * 100).toInt()
                    LegendRow(
                        color = colorForPaymentMode(mode.mode),
                        label = "${mode.mode} • ${mode.transactionCount}",
                        percent = percent
                    )
                }
            }
        }
    }
}

@Composable
private fun TopCategoriesCard(topCategories: List<CategoryTotal>, totalSpent: Double) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Top Categories", fontWeight = FontWeight.Bold)
            if (topCategories.isEmpty()) {
                EmptyStateCard("Top categories will appear once spends are detected.")
            } else {
                topCategories.forEach { category ->
                    TopCategoryRow(category, totalSpent)
                }
            }
        }
    }
}

@Composable
private fun MerchantAnalyticsCard(topMerchants: List<MerchantAnalytics>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Merchant Analytics", fontWeight = FontWeight.Bold)
            if (topMerchants.isEmpty()) {
                EmptyStateCard("Merchant insights will appear once more transactions are available.")
            } else {
                topMerchants.forEach { merchant ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(merchant.merchant, fontWeight = FontWeight.Bold)
                            Text("${merchant.transactionCount} transactions", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Text(formatRupees(merchant.totalAmount), fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CompareMetricsCard(
    compareMetrics: List<CompareMetric>,
    currentSpent: Double,
    currentIncome: Double
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Compare", fontWeight = FontWeight.Bold)
            compareMetrics.forEach { metric ->
                Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(metric.label, fontWeight = FontWeight.Bold)
                        Text("Previous spend ${formatRupees(metric.spent)} • income ${formatRupees(metric.income)}", color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Current spend ${formatRupees(currentSpent)} • income ${formatRupees(currentIncome)}", color = AccentPurple)
                        Text(metric.changeText, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun IncomeTrendCard(incomeTrend: IncomeTrendSummary) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Income Trend", fontWeight = FontWeight.Bold)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Salary",
                    amount = incomeTrend.salaryIncome,
                    color = AccentGreen
                )
                CompactAmountCard(
                    modifier = Modifier.weight(1f),
                    label = "Irregular",
                    amount = incomeTrend.irregularIncome,
                    color = AccentAmber
                )
            }
            Text(incomeTrend.salaryConsistencyText, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun TopCategoryRow(category: CategoryTotal, totalSpent: Double) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(MaterialTheme.colorScheme.secondary.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(iconForCategory(category.category), contentDescription = null, tint = AccentPurple)
        }
        Column(modifier = Modifier.weight(1f)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(category.category, fontWeight = FontWeight.Bold)
                Text(formatRupees(category.totalAmount), color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
            }
            Spacer(modifier = Modifier.height(8.dp))
            LinearProgressIndicator(
                progress = { if (totalSpent <= 0.0) 0f else (category.totalAmount / totalSpent).toFloat() },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(100.dp)),
                color = colorForCategory(category.category),
                trackColor = MaterialTheme.colorScheme.surfaceVariant
            )
        }
    }
}

@Composable
private fun RecurringInsightsCard(
    recurringInsights: List<RecurringInsight>,
    title: String = "Recurring Bills"
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text(title, fontWeight = FontWeight.Bold)
            if (recurringInsights.isEmpty()) {
                EmptyStateCard("No recurring bill pattern has been detected yet.")
            } else {
                recurringInsights.forEach { recurring ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(recurring.merchant, fontWeight = FontWeight.Bold)
                            Text(
                                "${recurring.category} • ${recurring.occurrences} repeats • approx ${formatRupees(recurring.amountEstimate)}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text("Last seen ${formatDate(recurring.lastSeenTimestamp)}", color = AccentPurple)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun AnomalyAlertsCard(alerts: List<AnomalyAlert>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Alerts", fontWeight = FontWeight.Bold)
            if (alerts.isEmpty()) {
                Text("No anomalies detected for the selected period.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                alerts.forEach { alert ->
                    Card(colors = CardDefaults.cardColors(containerColor = alertColor(alert.severity).copy(alpha = 0.12f))) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            Text(alert.title, fontWeight = FontWeight.Bold, color = alertColor(alert.severity))
                            Text(alert.body, color = MaterialTheme.colorScheme.onSurface)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CashflowCard(cashflowDays: List<CashflowDay>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text("Cashflow Calendar", fontWeight = FontWeight.Bold)
            if (cashflowDays.isEmpty()) {
                Text("Daily cashflow will appear once transactions are available.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                cashflowDays.forEach { day ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(day.label, fontWeight = FontWeight.Medium)
                        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text("-${formatCompactCurrency(day.spent)}", color = AccentPink)
                            Text("+${formatCompactCurrency(day.income)}", color = AccentGreen)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateInsightsCard(duplicateInsights: List<DuplicateInsight>) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("Duplicate Watchlist", fontWeight = FontWeight.Bold)
            if (duplicateInsights.isEmpty()) {
                EmptyStateCard("No likely duplicates found this month.")
            } else {
                duplicateInsights.forEach { duplicate ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(duplicate.merchant, fontWeight = FontWeight.Bold)
                                Text(
                                    "${duplicate.transactionCount} matching ${duplicate.type.name.lowercase(Locale.ENGLISH)} entries",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Text(formatRupees(duplicate.amount), color = AccentPink, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun TrendCard(
    trend: List<TrendPoint>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit,
    latestSpent: Double,
    latestIncome: Double
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("Monthly Trend", fontWeight = FontWeight.Bold)
            SegmentedToggle(listOf("Expense", "Income"), selectedIndex, onSelected)

            val values = trend.map { if (selectedIndex == 0) it.expense else it.income }
            val maxValue = values.maxOrNull().orZero()
            val latestValue = if (selectedIndex == 0) latestSpent else latestIncome

            Text("Last 6 months", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
            Text(formatCompactCurrency(maxValue), fontWeight = FontWeight.Black)
            TrendChart(
                labels = trend.map { it.monthLabel },
                values = values,
                lineColor = if (selectedIndex == 0) AccentPink else AccentTeal
            )
            Text(
                text = "Latest month ${formatRupees(latestValue)}",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.End,
                fontWeight = FontWeight.Bold
            )
        }
    }
}

@Composable
private fun FilterBar(
    showAdvancedFilters: Boolean,
    advancedFilters: AdvancedFilterState,
    categoryOptions: List<String>,
    onToggleAdvanced: () -> Unit,
    onFilterChange: (AdvancedFilterState) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Advanced filters", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onToggleAdvanced) {
                    Icon(Icons.Rounded.Tune, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (showAdvancedFilters) "Hide" else "Show")
                }
            }
            if (showAdvancedFilters) {
                OutlinedTextField(
                    value = advancedFilters.merchant,
                    onValueChange = { onFilterChange(advancedFilters.copy(merchant = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Merchant contains") }
                )
                OutlinedTextField(
                    value = advancedFilters.bank,
                    onValueChange = { onFilterChange(advancedFilters.copy(bank = it)) },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    label = { Text("Bank contains") }
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(categoryOptions) { category ->
                        FilterChip(
                            selected = advancedFilters.category == category,
                            onClick = {
                                onFilterChange(
                                    advancedFilters.copy(
                                        category = if (advancedFilters.category == category) "" else category
                                    )
                                )
                            },
                            label = { Text(category) }
                        )
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = advancedFilters.minAmount,
                        onValueChange = { onFilterChange(advancedFilters.copy(minAmount = it)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Min ₹") }
                    )
                    OutlinedTextField(
                        value = advancedFilters.maxAmount,
                        onValueChange = { onFilterChange(advancedFilters.copy(maxAmount = it)) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Max ₹") }
                    )
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("AI verified only", color = MaterialTheme.colorScheme.onSurfaceVariant)
                    Switch(
                        checked = advancedFilters.aiOnly,
                        onCheckedChange = { onFilterChange(advancedFilters.copy(aiOnly = it)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun CategoryManagementCard(
    categories: List<CustomCategory>,
    onAddCategory: () -> Unit,
    onRemoveCategory: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Custom categories", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onAddCategory) { Text("Add") }
            }
            if (categories.isEmpty()) {
                Text("Create your own categories and reuse them when editing transactions.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    categories.forEach { category ->
                        InputChip(
                            selected = false,
                            onClick = { onRemoveCategory(category.id) },
                            label = { Text(category.name) },
                            trailingIcon = {
                                Icon(
                                    Icons.Rounded.DeleteOutline,
                                    contentDescription = "Remove category",
                                    modifier = Modifier.size(16.dp)
                                )
                            },
                            colors = InputChipDefaults.inputChipColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun RuleManagementCard(
    rules: List<TransactionRule>,
    onAddRule: () -> Unit,
    onEditRule: (TransactionRule) -> Unit,
    onDeleteRule: (String) -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Rules engine", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onAddRule) { Text("New rule") }
            }
            Text("Examples: sender contains HDFC, merchant contains Amazon, or SMS contains recharge.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            if (rules.isEmpty()) {
                Text("No rules yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                rules.forEach { rule ->
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(rule.name.ifBlank { "Untitled rule" }, fontWeight = FontWeight.Bold)
                            Text(buildRuleSummary(rule), color = MaterialTheme.colorScheme.onSurfaceVariant)
                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                TextButton(onClick = { onEditRule(rule) }) { Text("Edit") }
                                TextButton(onClick = { onDeleteRule(rule.id) }) { Text("Delete") }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetManagementCard(
    goals: List<BudgetGoal>,
    progress: List<BudgetProgress>,
    onAddBudget: () -> Unit,
    onDeleteBudget: (String) -> Unit
) {
    val progressByCategory = progress.associateBy { it.category }
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Budgets", fontWeight = FontWeight.SemiBold)
                TextButton(onClick = onAddBudget) { Text("Add budget") }
            }
            if (goals.isEmpty()) {
                Text("Set monthly category limits and SpendWise will track progress across Home and Insights.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            } else {
                goals.forEach { goal ->
                    val budgetProgress = progressByCategory[goal.category]
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(goal.category, fontWeight = FontWeight.Bold)
                                    Text(formatRupees(goal.monthlyLimit), color = MaterialTheme.colorScheme.onSurfaceVariant)
                                }
                                TextButton(onClick = { onDeleteBudget(goal.id) }) {
                                    Text("Remove")
                                }
                            }
                            if (budgetProgress != null) {
                                LinearProgressIndicator(
                                    progress = { budgetProgress.progress.toFloat().coerceIn(0f, 1f) },
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(8.dp)
                                        .clip(RoundedCornerShape(100.dp)),
                                    color = if (budgetProgress.isExceeded) AccentPink else AccentTeal,
                                    trackColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                                Text(
                                    "${formatRupees(budgetProgress.spentAmount)} spent",
                                    color = if (budgetProgress.isExceeded) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BudgetEditorDialog(
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (String, Double) -> Unit
) {
    var category by rememberSaveable { mutableStateOf(availableCategories.firstOrNull().orEmpty()) }
    var amount by rememberSaveable { mutableStateOf("") }
    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text("Budget goal", fontWeight = FontWeight.Bold)
                OutlinedTextField(
                    value = category,
                    onValueChange = { category = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Category") },
                    singleLine = true
                )
                LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(availableCategories) { item ->
                        FilterChip(
                            selected = category == item,
                            onClick = { category = item },
                            label = { Text(item) }
                        )
                    }
                }
                OutlinedTextField(
                    value = amount,
                    onValueChange = { amount = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text("Monthly limit") },
                    singleLine = true
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            val numericAmount = amount.toDoubleOrNull() ?: return@TextButton
                            if (category.isNotBlank()) {
                                onSave(category, numericAmount)
                            }
                        }
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun ExpandableReviewCard(
    title: String,
    subtitle: String,
    items: List<SmsReviewEntity>,
    expanded: Boolean,
    onToggle: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(title, fontWeight = FontWeight.SemiBold)
                    Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                TextButton(onClick = onToggle) {
                    Icon(Icons.Rounded.Visibility, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (expanded) "Hide" else "Open")
                }
            }
            if (expanded) {
                if (items.isEmpty()) {
                    Text("Nothing here yet.", color = MaterialTheme.colorScheme.onSurfaceVariant)
                } else {
                    items.take(12).forEach { event ->
                        SmsReviewCard(event)
                    }
                }
            }
        }
    }
}

@Composable
private fun SmsReviewCard(event: SmsReviewEntity) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(event.finalStatus.replace('_', ' '), fontWeight = FontWeight.Bold)
                Text(formatDate(event.receivedAt), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text("${event.sender} • ${event.eventSource}", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Text(
                event.body,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis
            )
            if (event.aiReason.isNotBlank()) {
                Text("AI reason: ${event.aiReason}", color = AccentPurple)
            }
            if (event.debugLog.isNotBlank()) {
                Text(
                    event.debugLog,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 6,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

@Composable
private fun CategoryEditorDialog(
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var name by rememberSaveable { mutableStateOf("") }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add category") },
        text = {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                label = { Text("Category name") }
            )
        },
        confirmButton = {
            TextButton(onClick = {
                if (name.isNotBlank()) onSave(name)
            }) { Text("Save") }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun RuleEditorDialog(
    initialRule: TransactionRule,
    onDismiss: () -> Unit,
    onSave: (TransactionRule) -> Unit
) {
    var name by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.name) }
    var senderContains by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.senderContains) }
    var merchantContains by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.merchantContains) }
    var smsContains by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.smsContains) }
    var assignCategory by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.assignCategory) }
    var assignBank by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.assignBank) }
    var assignMerchant by rememberSaveable(initialRule.id) { mutableStateOf(initialRule.assignMerchant) }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Rule editor", fontWeight = FontWeight.Bold)
                OutlinedTextField(name, { name = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Rule name") })
                OutlinedTextField(senderContains, { senderContains = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Sender contains") })
                OutlinedTextField(merchantContains, { merchantContains = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Merchant contains") })
                OutlinedTextField(smsContains, { smsContains = it }, modifier = Modifier.fillMaxWidth(), label = { Text("SMS contains") })
                OutlinedTextField(assignCategory, { assignCategory = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Assign category") })
                OutlinedTextField(assignBank, { assignBank = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Assign bank") })
                OutlinedTextField(assignMerchant, { assignMerchant = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Assign merchant") })
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Cancel") }
                    TextButton(
                        onClick = {
                            onSave(
                                initialRule.copy(
                                    name = name,
                                    senderContains = senderContains,
                                    merchantContains = merchantContains,
                                    smsContains = smsContains,
                                    assignCategory = assignCategory,
                                    assignBank = assignBank,
                                    assignMerchant = assignMerchant
                                )
                            )
                        }
                    ) { Text("Save") }
                }
            }
        }
    }
}

@Composable
private fun TransactionDetailDialog(
    transaction: TransactionEntity,
    initialMode: TransactionDialogMode,
    availableCategories: List<String>,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit
) {
    var editMode by remember(transaction.id, initialMode) { mutableStateOf(initialMode == TransactionDialogMode.EDIT) }
    var amount by rememberSaveable(transaction.id) { mutableStateOf(transaction.amount.toString()) }
    var merchant by rememberSaveable(transaction.id) { mutableStateOf(transaction.merchant) }
    var bank by rememberSaveable(transaction.id) { mutableStateOf(transaction.bank) }
    var category by rememberSaveable(transaction.id) { mutableStateOf(transaction.category) }
    var note by rememberSaveable(transaction.id) { mutableStateOf(transaction.note) }
    var tags by rememberSaveable(transaction.id) { mutableStateOf(transaction.tags) }
    var selectedTypeIndex by rememberSaveable(transaction.id) {
        mutableIntStateOf(if (transaction.type == TransactionType.DEBIT) 0 else 1)
    }

    Dialog(onDismissRequest = onDismiss) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(18.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Transaction detail", fontWeight = FontWeight.Bold)
                    TextButton(onClick = { editMode = !editMode }) {
                        Icon(Icons.Rounded.Edit, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(if (editMode) "Preview" else "Edit")
                    }
                }
                if (editMode) {
                    SegmentedToggle(listOf("Expense", "Income"), selectedTypeIndex) { selectedTypeIndex = it }
                    OutlinedTextField(amount, { amount = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Amount") }, singleLine = true)
                    OutlinedTextField(merchant, { merchant = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Merchant") }, singleLine = true)
                    OutlinedTextField(bank, { bank = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Bank") }, singleLine = true)
                    OutlinedTextField(category, { category = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Category") }, singleLine = true)
                    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        items(availableCategories) { item ->
                            FilterChip(
                                selected = category == item,
                                onClick = { category = item },
                                label = { Text(item) }
                            )
                        }
                    }
                    OutlinedTextField(note, { note = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Note / receipt reference") })
                    OutlinedTextField(tags, { tags = it }, modifier = Modifier.fillMaxWidth(), label = { Text("Tags (comma separated)") })
                } else {
                    DetailLabelValue("Merchant", transaction.merchant)
                    DetailLabelValue("Amount", formatRupees(transaction.amount))
                    DetailLabelValue("Type", transaction.type.name)
                    DetailLabelValue("Category", transaction.category)
                    DetailLabelValue("Payment mode", transaction.paymentMode)
                    DetailLabelValue("Bank", transaction.bank)
                    DetailLabelValue("Source sender", transaction.sourceSender.ifBlank { "Unavailable" })
                    DetailLabelValue("Verification", transaction.verificationSource)
                    if (transaction.aiReason.isNotBlank()) {
                        DetailLabelValue("AI reason", transaction.aiReason)
                    }
                    if (transaction.note.isNotBlank()) {
                        DetailLabelValue("Note", transaction.note)
                    }
                    if (transaction.tags.isNotBlank()) {
                        DetailLabelValue("Tags", transaction.tags)
                    }
                    DetailLabelValue("Raw SMS", transaction.rawSms)
                }
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) { Text("Close") }
                    if (editMode) {
                        TextButton(
                            onClick = {
                                val updatedAmount = amount.toDoubleOrNull() ?: transaction.amount
                                onSave(
                                    transaction.copy(
                                        amount = updatedAmount,
                                        merchant = merchant.ifBlank { transaction.merchant },
                                        bank = bank.ifBlank { transaction.bank },
                                        category = category.ifBlank { transaction.category },
                                        note = note,
                                        tags = tags,
                                        type = if (selectedTypeIndex == 0) TransactionType.DEBIT else TransactionType.CREDIT,
                                        paymentMode = paymentModeFor(
                                            merchant = merchant.ifBlank { transaction.merchant },
                                            rawSms = transaction.rawSms
                                        )
                                    )
                                )
                            }
                        ) { Text("Save") }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailLabelValue(label: String, value: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Medium)
        Text(value)
    }
}

@Composable
private fun SwipeBackground(
    alignment: Alignment,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp)
    ) {
        Row(
            modifier = Modifier
                .align(alignment)
                .clip(RoundedCornerShape(18.dp))
                .background(color)
                .padding(horizontal = 14.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null)
            Text(label, fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun TransactionListItem(
    transaction: TransactionEntity,
    onDeleteRequest: (() -> Unit)? = null,
    onEditRequest: (() -> Unit)? = null,
    onOpenRequest: (() -> Unit)? = null,
    onRemoveDuplicateRequest: (() -> Unit)? = null,
    isLikelyDuplicate: Boolean = false
) {
    val amountColor = when (transaction.type) {
        TransactionType.DEBIT -> AccentPink
        TransactionType.CREDIT -> AccentGreen
        TransactionType.UNKNOWN -> Color(0xFF39424E)
    }
    val amountPrefix = when (transaction.type) {
        TransactionType.DEBIT -> "-"
        TransactionType.CREDIT -> "+"
        TransactionType.UNKNOWN -> ""
    }

    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { target ->
            when (target) {
                SwipeToDismissBoxValue.StartToEnd -> {
                    onEditRequest?.invoke()
                    false
                }

                SwipeToDismissBoxValue.EndToStart -> {
                    onDeleteRequest?.invoke()
                    false
                }

                SwipeToDismissBoxValue.Settled -> false
            }
        }
    )

    SwipeToDismissBox(
        state = dismissState,
        backgroundContent = {
            SwipeBackground(
                alignment = Alignment.CenterStart,
                color = AccentTeal.copy(alpha = 0.2f),
                icon = Icons.Rounded.Edit,
                label = "Edit"
            )
            SwipeBackground(
                alignment = Alignment.CenterEnd,
                color = AccentPink.copy(alpha = 0.2f),
                icon = Icons.Rounded.DeleteOutline,
                label = "Delete"
            )
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface), elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenRequest?.invoke() }
                    .padding(16.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(colorForCategory(transaction.category).copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(iconForCategory(transaction.category), contentDescription = null, tint = colorForCategory(transaction.category))
                }
                Column(modifier = Modifier.weight(1f)) {
                    FlowRow(
                        modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            transaction.merchant,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.align(Alignment.CenterVertically)
                        )
                        if (transaction.isVerifiedByAi) {
                            AssistChip(
                                onClick = {},
                                label = { Text("AI") },
                                leadingIcon = { Icon(Icons.Rounded.Psychology, contentDescription = null, modifier = Modifier.size(14.dp)) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = AccentPurple.copy(alpha = 0.12f)),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                        if (isLikelyDuplicate && onRemoveDuplicateRequest != null) {
                            AssistChip(
                                onClick = onRemoveDuplicateRequest,
                                label = { Text("Duplicate") },
                                leadingIcon = { Icon(Icons.Rounded.Close, contentDescription = "Not Duplicate", modifier = Modifier.size(14.dp)) },
                                colors = AssistChipDefaults.assistChipColors(containerColor = AccentPink.copy(alpha = 0.12f)),
                                modifier = Modifier.height(24.dp)
                            )
                        } else if (isLikelyDuplicate) {
                             AssistChip(
                                onClick = {},
                                label = { Text("Duplicate") },
                                colors = AssistChipDefaults.assistChipColors(containerColor = AccentPink.copy(alpha = 0.12f)),
                                modifier = Modifier.height(24.dp)
                            )
                        }
                    }
                    // Account label row
                    val label = transaction.accountLabel.ifBlank { transaction.bank }
                    Text(
                        text = "$label · ${transaction.category} · ${formatDate(transaction.timestamp)}",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                if (onDeleteRequest != null) {
                    IconButton(onClick = onDeleteRequest) {
                        Icon(
                            Icons.Rounded.DeleteOutline,
                            contentDescription = "Delete transaction",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                Text(amountPrefix + formatRupees(transaction.amount), color = amountColor, fontWeight = FontWeight.Black)
            }
        }
    }
}

@Composable
private fun DebugTemplateCard(
    template: DebugSmsTemplate,
    onSimulate: () -> Unit,
    onSend: () -> Unit
) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(template.title, fontWeight = FontWeight.Bold)
            Text(template.subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant)
            HorizontalDivider(color = MaterialTheme.colorScheme.outline)
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                TextButton(onClick = onSimulate) {
                    Icon(Icons.Rounded.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Simulate")
                }
                TextButton(onClick = onSend) {
                    Icon(Icons.AutoMirrored.Rounded.Send, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Send SMS")
                }
            }
        }
    }
}

@Composable
internal fun EmptyStateCard(message: String) {
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Text(
            text = message,
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SegmentedToggle(
    options: List<String>,
    selectedIndex: Int,
    onSelected: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.12f))
            .padding(4.dp)
    ) {
        options.forEachIndexed { index, label ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(20.dp))
                    .background(
                        if (selectedIndex == index) MaterialTheme.colorScheme.errorContainer else Color.Transparent
                    )
                    .clickable { onSelected(index) }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = label,
                    fontWeight = if (selectedIndex == index) FontWeight.Bold else FontWeight.Medium,
                    color = if (selectedIndex == index) AccentPink else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun DonutChart(values: List<CategoryTotal>, colors: List<Color>) {
    val total = values.sumOf { it.totalAmount }.takeIf { it > 0.0 } ?: 1.0
    Canvas(modifier = Modifier.fillMaxSize()) {
        var startAngle = -90f
        val thickness = size.minDimension * 0.22f
        values.forEachIndexed { index, category ->
            val sweep = ((category.totalAmount / total) * 360f).toFloat()
            drawArc(
                color = colors[index % colors.size],
                startAngle = startAngle,
                sweepAngle = sweep,
                useCenter = false,
                size = Size(size.width, size.height),
                style = Stroke(width = thickness, cap = StrokeCap.Butt)
            )
            startAngle += sweep
        }
    }
}

@Composable
private fun TrendChart(
    labels: List<String>,
    values: List<Double>,
    lineColor: Color
) {
    val safeValues = if (values.isEmpty()) listOf(0.0) else values
    val max = safeValues.maxOrNull().orZero().coerceAtLeast(1.0)
    val min = safeValues.minOrNull().orZero()
    val range = (max - min).takeIf { it > 0.0 } ?: 1.0

    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp)
                .background(color = MaterialTheme.colorScheme.surfaceVariant, shape = RoundedCornerShape(24.dp))
                .padding(16.dp)
        ) {
            val widthStep = if (safeValues.size == 1) 0f else size.width / (safeValues.size - 1)
            val points = safeValues.mapIndexed { index, value ->
                val x = widthStep * index
                val normalized = ((value - min) / range).toFloat()
                val y = size.height - (normalized * size.height)
                Offset(x, y)
            }

            val fillPath = Path().apply {
                moveTo(points.first().x, size.height)
                points.forEach { offset -> lineTo(offset.x, offset.y) }
                lineTo(points.last().x, size.height)
                close()
            }
            val linePath = Path().apply {
                points.forEachIndexed { index, offset ->
                    if (index == 0) moveTo(offset.x, offset.y) else lineTo(offset.x, offset.y)
                }
            }

            drawPath(
                path = fillPath,
                brush = Brush.verticalGradient(listOf(lineColor.copy(alpha = 0.24f), Color.Transparent))
            )
            drawPath(linePath, color = lineColor, style = Stroke(width = 6f, cap = StrokeCap.Round))
            points.forEach { point ->
                drawCircle(Color.White, radius = 10f, center = point)
                drawCircle(lineColor, radius = 6f, center = point)
            }
        }

        Row(modifier = Modifier.fillMaxWidth()) {
            labels.forEach { label ->
                Text(
                    text = label,
                    modifier = Modifier.weight(1f),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun MonthPickerDialog(
    initialYear: Int,
    selectedMonth: Int,
    onDismiss: () -> Unit,
    onMonthSelected: (year: Int, month: Int) -> Unit
) {
    var displayedYear by remember { mutableIntStateOf(initialYear) }
    val months = remember {
        listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Select month") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                        contentDescription = "Previous year",
                        modifier = Modifier.clip(CircleShape).clickable { displayedYear -= 1 }.padding(6.dp)
                    )
                    Text(displayedYear.toString(), fontWeight = FontWeight.Bold)
                    Icon(
                        Icons.AutoMirrored.Rounded.KeyboardArrowRight,
                        contentDescription = "Next year",
                        modifier = Modifier.clip(CircleShape).clickable { displayedYear += 1 }.padding(6.dp)
                    )
                }
                FlowRow(
                    maxItemsInEachRow = 3,
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    months.forEachIndexed { index, month ->
                        val monthNumber = index + 1
                        val isSelected = displayedYear == initialYear && selectedMonth == monthNumber
                        Box(
                            modifier = Modifier
                                .width(88.dp)
                                .clip(RoundedCornerShape(16.dp))
                                .background(if (isSelected) AccentPurple.copy(alpha = 0.14f) else MaterialTheme.colorScheme.surfaceVariant)
                                .clickable { onMonthSelected(displayedYear, monthNumber) }
                                .padding(vertical = 14.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = month,
                                color = if (isSelected) AccentPurple else MaterialTheme.colorScheme.onSurface,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                            )
                        }
                    }
                }
            }
        },
        confirmButton = { TextButton(onClick = onDismiss) { Text("Close") } }
    )
}

@Composable
private fun ManualTransactionDialog(
    onDismiss: () -> Unit,
    onSave: (amount: Double, type: TransactionType, merchant: String, bank: String) -> Unit
) {
    var amount by rememberSaveable { mutableStateOf("") }
    var merchant by rememberSaveable { mutableStateOf("") }
    var bank by rememberSaveable { mutableStateOf("") }
    var selectedTypeIndex by rememberSaveable { mutableIntStateOf(0) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add transaction") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                SegmentedToggle(listOf("Expense", "Income"), selectedTypeIndex) { selectedTypeIndex = it }
                OutlinedTextField(amount, { amount = it }, label = { Text("Amount") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(merchant, { merchant = it }, label = { Text("Merchant") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
                OutlinedTextField(bank, { bank = it }, label = { Text("Bank / source") }, modifier = Modifier.fillMaxWidth(), singleLine = true)
            }
        },
        confirmButton = {
            TextButton(onClick = {
                val numericAmount = amount.toDoubleOrNull() ?: return@TextButton
                onSave(
                    numericAmount,
                    if (selectedTypeIndex == 0) TransactionType.DEBIT else TransactionType.CREDIT,
                    merchant.ifBlank { "Manual Entry" },
                    bank.ifBlank { "SpendWise" }
                )
            }) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

private fun iconForCategory(category: String) = when (category) {
    "UPI" -> Icons.Rounded.Payments
    "Food" -> Icons.Rounded.Restaurant
    "Shopping" -> Icons.Rounded.ShoppingBag
    "Entertainment" -> Icons.Rounded.Movie
    "Bills" -> Icons.Rounded.AccountBalanceWallet
    "Loans & EMI" -> Icons.Rounded.AccountBalanceWallet
    "Travel" -> Icons.Rounded.DirectionsCar
    "Gifts & Rewards" -> Icons.Rounded.SwapHoriz
    "Income" -> Icons.Rounded.ArrowDownward
    "Salary" -> Icons.Rounded.ArrowDownward
    "Refunds" -> Icons.Rounded.SwapHoriz
    "Cash Withdrawal" -> Icons.Rounded.LocalAtm
    else -> Icons.Rounded.CreditCard
}

private fun colorForCategory(category: String) = when (category) {
    "UPI" -> Color(0xFF26A7E8)
    "Food" -> Color(0xFFFA8C3D)
    "Shopping" -> Color(0xFF9C6ADE)
    "Entertainment" -> Color(0xFFFF7096)
    "Bills" -> Color(0xFFFFB11C)
    "Loans & EMI" -> Color(0xFF7A63FF)
    "Travel" -> Color(0xFF2BB4A0)
    "Gifts & Rewards" -> Color(0xFF7FB069)
    "Income" -> AccentGreen
    "Salary" -> AccentGreen
    "Refunds" -> AccentTeal
    "Cash Withdrawal" -> Color(0xFF7A706B)
    else -> Color(0xFF5A8F55)
}

private fun colorForPaymentMode(mode: String) = when (mode) {
    "UPI" -> Color(0xFF26A7E8)
    "Card" -> Color(0xFFFF7096)
    "ATM" -> Color(0xFFF5A623)
    "Bank Transfer" -> Color(0xFF2BB4A0)
    else -> Color(0xFF9C6ADE)
}

private fun alertColor(severity: String) = when (severity) {
    "high" -> AccentPink
    "medium" -> AccentAmber
    else -> AccentPurple
}

private fun donutColors(count: Int): List<Color> {
    val palette = listOf(Color(0xFF26A7E8), Color(0xFF5A8F55), Color(0xFFFF7096), Color(0xFFFA8C3D), Color(0xFF9C6ADE))
    return if (count <= 0) palette else palette.take(count.coerceAtMost(palette.size))
}

private fun availableCategories(
    customCategories: List<CustomCategory>,
    transactions: List<TransactionEntity>,
    currentCategory: String? = null
): List<String> {
    val defaults = listOf(
        "UPI",
        "Food",
        "Shopping",
        "Entertainment",
        "Bills",
        "Loans & EMI",
        "Travel",
        "Gifts & Rewards",
        "Income",
        "Salary",
        "Refunds",
        "Cash Withdrawal",
        "Other"
    )
    return (defaults +
        customCategories.map { it.name } +
        transactions.map { it.category } +
        listOfNotNull(currentCategory))
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .distinctBy { it.lowercase(Locale.ENGLISH) }
        .sortedBy { it.lowercase(Locale.ENGLISH) }
}

private fun buildRuleSummary(rule: TransactionRule): String {
    val conditions = listOfNotNull(
        rule.senderContains.takeIf { it.isNotBlank() }?.let { "sender has \"$it\"" },
        rule.merchantContains.takeIf { it.isNotBlank() }?.let { "merchant has \"$it\"" },
        rule.smsContains.takeIf { it.isNotBlank() }?.let { "sms has \"$it\"" }
    )
    val actions = listOfNotNull(
        rule.assignCategory.takeIf { it.isNotBlank() }?.let { "category → $it" },
        rule.assignBank.takeIf { it.isNotBlank() }?.let { "bank → $it" },
        rule.assignMerchant.takeIf { it.isNotBlank() }?.let { "merchant → $it" }
    )
    return buildString {
        append(if (conditions.isEmpty()) "Always match" else conditions.joinToString(", "))
        if (actions.isNotEmpty()) {
            append(" | ")
            append(actions.joinToString(", "))
        }
    }
}

private fun paymentModeFor(merchant: String, rawSms: String): String {
    val haystack = "$merchant $rawSms".lowercase(Locale.ENGLISH)
    return when {
        haystack.contains("upi") || haystack.contains("gpay") || haystack.contains("phonepe") || haystack.contains("paytm") -> "UPI"
        haystack.contains("card") || haystack.contains("visa") || haystack.contains("mastercard") || haystack.contains("pos") -> "Card"
        haystack.contains("atm") || haystack.contains("withdrawn") -> "ATM"
        haystack.contains("neft") || haystack.contains("imps") || haystack.contains("rtgs") -> "Bank Transfer"
        else -> "Other"
    }
}

private fun formatRupees(amount: Double): String =
    NumberFormat.getCurrencyInstance(Locale("en", "IN")).format(amount)

private fun formatCompactCurrency(amount: Double): String {
    val absolute = amount.absoluteValue
    return when {
        absolute >= 100_000 -> "₹%.2f L".format(Locale.ENGLISH, amount / 100_000)
        absolute >= 1_000 -> "₹%.2f K".format(Locale.ENGLISH, amount / 1_000)
        else -> formatRupees(amount)
    }
}

private fun formatDate(timestamp: Long): String =
    LocalDateTime.ofInstant(Instant.ofEpochMilli(timestamp), ZoneId.systemDefault())
        .format(DateTimeFormatter.ofPattern("dd MMM, hh:mm a"))

private fun formatMonth(year: Int, month: Int): String =
    YearMonth.of(year, month).format(DateTimeFormatter.ofPattern("MMM yyyy"))

private fun Double?.orZero(): Double = this ?: 0.0

private fun Context.findActivity(): Activity? {
    var current: Context? = this
    while (current is ContextWrapper) {
        if (current is Activity) return current
        current = current.baseContext
    }
    return null
}



