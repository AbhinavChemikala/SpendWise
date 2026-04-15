package com.yourapp.spendwise.ui

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import com.google.gson.Gson
import com.google.android.gms.auth.api.signin.GoogleSignIn
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Checkbox
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
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.window.DialogProperties
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.yourapp.spendwise.backup.BackupDestination
import com.yourapp.spendwise.backup.BackupHistoryEntry
import com.yourapp.spendwise.backup.BackupStatus
import com.yourapp.spendwise.backup.BackupTrigger
import com.yourapp.spendwise.backup.SpendWiseBackupManager
import com.yourapp.spendwise.data.CustomCategory
import com.yourapp.spendwise.data.BudgetGoal
import com.yourapp.spendwise.data.BudgetProgress
import com.yourapp.spendwise.data.AnomalyAlert
import com.yourapp.spendwise.data.CashflowDay
import com.yourapp.spendwise.data.CategoryCatalog
import com.yourapp.spendwise.data.CategoryDecisionSource
import com.yourapp.spendwise.data.CategoryResolution
import com.yourapp.spendwise.data.CategoryRefinementStatus
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
import com.yourapp.spendwise.data.db.TransactionCategoryAiEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.mail.AxisEmailSyncHistoryEntry
import com.yourapp.spendwise.mail.GmailAxisSyncManager
import com.yourapp.spendwise.mail.AxisEmailSyncTrigger
import java.text.NumberFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.absoluteValue
import kotlinx.coroutines.flow.distinctUntilChanged

internal val AccentPink = Color(0xFFFF577A)
internal val AccentTeal = Color(0xFF24B6D3)
internal val AccentPurple = Color(0xFF6F49FF)
internal val AccentGreen = Color(0xFF2E8B57)
internal val AccentAmber = Color(0xFFF5A623)
private val SpendWiseUiGson = Gson()

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

private object HomeCardId {
    const val STATUS = "status"
    const val QUICK_SUMMARY = "quick_summary"
    const val HERO_SUMMARY = "hero_summary"
    const val SAVINGS_SCORE = "savings_score"
    const val BUDGETS = "budgets"
    const val ANOMALY_ALERTS = "anomaly_alerts"
    const val CASHFLOW = "cashflow"
    const val INSIGHTS_PREVIEW = "insights_preview"
    const val RECENT_TRANSACTIONS = "recent_transactions"
}

private val DefaultHomeCardOrder = listOf(
    HomeCardId.STATUS,
    HomeCardId.QUICK_SUMMARY,
    HomeCardId.HERO_SUMMARY,
    HomeCardId.SAVINGS_SCORE,
    HomeCardId.BUDGETS,
    HomeCardId.ANOMALY_ALERTS,
    HomeCardId.CASHFLOW,
    HomeCardId.INSIGHTS_PREVIEW,
    HomeCardId.RECENT_TRANSACTIONS
)

private val HomeCardLabels = mapOf(
    HomeCardId.STATUS to "Status",
    HomeCardId.QUICK_SUMMARY to "Quick Summary",
    HomeCardId.HERO_SUMMARY to "Total Expenses",
    HomeCardId.SAVINGS_SCORE to "Savings Score",
    HomeCardId.BUDGETS to "Budgets",
    HomeCardId.ANOMALY_ALERTS to "Alerts",
    HomeCardId.CASHFLOW to "Cashflow",
    HomeCardId.INSIGHTS_PREVIEW to "Insights",
    HomeCardId.RECENT_TRANSACTIONS to "Recent Transactions"
)

private val MainTabOrder = listOf(
    SpendWiseTab.HOME,
    SpendWiseTab.ACTIVITY,
    SpendWiseTab.REVIEW_CENTER,
    SpendWiseTab.INSIGHTS,
    SpendWiseTab.SETTINGS
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
    val backupExportLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.CreateDocument("application/json")
    ) { uri: Uri? ->
        uri?.let(vm::exportBackupToUri)
    }
    val backupRestoreLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let(vm::restoreBackupFromUri)
    }
    val gmailConnectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = runCatching { task.getResult(Exception::class.java) }.getOrNull()
        if (account?.email.isNullOrBlank()) {
            vm.setDebugStatus("Unable to connect Gmail for Axis email sync.")
        } else {
            vm.connectAxisEmailAccount(account?.email.orEmpty())
        }
    }
    val driveConnectLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        val data = result.data ?: return@rememberLauncherForActivityResult
        val task = GoogleSignIn.getSignedInAccountFromIntent(data)
        val account = runCatching { task.getResult(Exception::class.java) }.getOrNull()
        if (account?.email.isNullOrBlank()) {
            vm.setDebugStatus("Unable to connect Google Drive backup.")
        } else {
            vm.connectDriveBackupAccount(account?.email.orEmpty())
        }
    }

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

    val activeSelectedTransaction = selectedTransaction?.id?.let { selectedId ->
        uiState.transactions.firstOrNull { it.id == selectedId } ?: selectedTransaction
    }

    activeSelectedTransaction?.let { transaction ->
        TransactionDetailDialog(
            transaction = transaction,
            initialMode = transactionDialogMode,
            availableCategories = availableCategories(uiState.customCategories, uiState.transactions, transaction.category),
            isFindingSimilarTransactions = uiState.isFindingSimilarTransactions &&
                uiState.similarTransactionSourceId == transaction.id,
            similarTransactions = if (uiState.similarTransactionSourceId == transaction.id) {
                uiState.similarTransactions
            } else {
                emptyList()
            },
            categoryRefinementRecord = uiState.categoryRefinementRecord?.takeIf { it.transactionId == transaction.id },
            isCategoryRefinementLoading = uiState.categoryRefinementLoadingId == transaction.id,
            onObserveCategoryRefinement = vm::observeCategoryRefinement,
            onClearCategoryRefinementObservation = vm::clearCategoryRefinementObservation,
            onRequestCategoryRefinement = vm::requestCategoryRefinement,
            onDismiss = {
                vm.clearCategoryRefinementObservation()
                selectedTransaction = null
            },
            onSave = { updated ->
                vm.updateTransaction(updated)
                selectedTransaction = updated
                transactionDialogMode = TransactionDialogMode.VIEW
            },
            onFindSimilar = vm::findSimilarTransactions,
            onClearSimilar = vm::clearSimilarTransactions,
            onApplyToSimilar = vm::applyTransactionChangesToSimilar
        )
    }

    val selectedPage = MainTabOrder.indexOf(uiState.selectedTab).takeIf { it >= 0 } ?: 0
    val pagerState = rememberPagerState(
        initialPage = selectedPage,
        pageCount = { MainTabOrder.size }
    )

    LaunchedEffect(selectedPage) {
        if (pagerState.currentPage != selectedPage) {
            pagerState.animateScrollToPage(selectedPage)
        }
    }

    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.settledPage }
            .distinctUntilChanged()
            .collect { page ->
                MainTabOrder.getOrNull(page)?.let { tab ->
                    vm.selectTab(tab)
                }
            }
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
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.fillMaxSize(),
            beyondViewportPageCount = 1
        ) { page ->
            when (MainTabOrder[page]) {
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
                    onMonthClick = { showMonthPicker = true },
                    onUpdateHomeCardOrder = vm::updateHomeCardOrder,
                    onToggleHomeCardVisibility = vm::toggleHomeCardVisibility
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
                    uiState = uiState,
                    onRetryItem = vm::retryAiReview,
                    onRetryAllFailed = vm::retryAllFailedAiReviews
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
                    onConnectAxisEmail = {
                        val client = GmailAxisSyncManager.buildSignInClient(context)
                        client.signOut().addOnCompleteListener {
                            gmailConnectLauncher.launch(client.signInIntent)
                        }
                    },
                    onDisconnectAxisEmail = {
                        GmailAxisSyncManager.buildSignInClient(context).signOut()
                        vm.disconnectAxisEmailAccount()
                    },
                    onToggleAxisEmailAutoSync = vm::toggleAxisEmailAutoSync,
                    onSyncAxisEmails = { vm.syncAxisEmailsNow(trigger = AxisEmailSyncTrigger.MANUAL) },
                    onToggleSparkMailTrigger = vm::toggleSparkMailTrigger,
                    onOpenSparkNotificationAccess = {
                        vm.refreshNotificationAccessState()
                        context.startActivity(Intent(Settings.ACTION_NOTIFICATION_LISTENER_SETTINGS))
                    },
                    onRecoverLegacyAiFailures = vm::recoverLegacyAiFailures,
                    onSetThemeMode = vm::setThemeMode,
                    onToggleDailyReminder = vm::toggleDailyReminder,
                    onSetDailyReminderTime = vm::setDailyReminderTime,
                    onExportLocalBackup = {
                        val stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmm"))
                        backupExportLauncher.launch("spendwise-backup-$stamp.json")
                    },
                    onRestoreLocalBackup = {
                        backupRestoreLauncher.launch(arrayOf("application/json", "text/*", "*/*"))
                    },
                    onConnectDriveBackup = {
                        val client = SpendWiseBackupManager.buildDriveSignInClient(context)
                        client.signOut().addOnCompleteListener {
                            driveConnectLauncher.launch(client.signInIntent)
                        }
                    },
                    onDisconnectDriveBackup = {
                        SpendWiseBackupManager.buildDriveSignInClient(context).signOut()
                        vm.disconnectDriveBackupAccount()
                    },
                    onToggleDriveBackupAuto = vm::toggleDriveBackupAuto,
                    onSetDriveBackupTime = vm::setDriveBackupTime,
                    onPushBackupToDrive = vm::pushBackupToDrive,
                    onRestoreBackupFromDrive = vm::restoreBackupFromDrive,
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
    onMonthClick: () -> Unit,
    onUpdateHomeCardOrder: (List<String>) -> Unit,
    onToggleHomeCardVisibility: (String) -> Unit
) {
    var isEditingLayout by rememberSaveable { mutableStateOf(false) }
    val cardOrder = remember(uiState.homeCardOrder) {
        normalizeHomeCardOrder(uiState.homeCardOrder)
    }
    val hiddenCardIds = remember(uiState.homeHiddenCardIds) {
        normalizeHomeCardIds(uiState.homeHiddenCardIds)
    }
    val displayedCardOrder = remember(cardOrder, hiddenCardIds, isEditingLayout) {
        if (isEditingLayout) cardOrder else cardOrder.filterNot { it in hiddenCardIds }
    }
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
                onMonthClick = onMonthClick,
                trailingContent = {
                    IconButton(onClick = { isEditingLayout = !isEditingLayout }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = if (isEditingLayout) {
                                "Done editing home layout"
                            } else {
                                "Edit home layout"
                            },
                            tint = if (isEditingLayout) {
                                AccentPurple
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            )
        }
        items(displayedCardOrder, key = { it }) { cardId ->
            EditableHomeCard(
                cardId = cardId,
                isEditing = isEditingLayout,
                isVisible = cardId !in hiddenCardIds,
                order = cardOrder,
                onMove = onUpdateHomeCardOrder,
                onToggleVisibility = { onToggleHomeCardVisibility(cardId) }
            ) {
                when (cardId) {
                    HomeCardId.STATUS -> StatusCard(uiState)
                    HomeCardId.QUICK_SUMMARY -> SummaryRangeCard(
                        selected = uiState.selectedSummaryRange,
                        ranges = uiState.rangeSummaries,
                        onSelect = onSelectSummaryRange
                    )
                    HomeCardId.HERO_SUMMARY -> HeroSummaryCard(uiState)
                    HomeCardId.SAVINGS_SCORE -> SavingsScoreCard(uiState.savingsScore, uiState.incomeTrend)
                    HomeCardId.BUDGETS -> BudgetProgressCard(uiState.budgetProgress)
                    HomeCardId.ANOMALY_ALERTS -> AnomalyAlertsCard(uiState.anomalyAlerts)
                    HomeCardId.CASHFLOW -> CashflowCard(uiState.cashflowDays)
                    HomeCardId.INSIGHTS_PREVIEW -> InsightsPreviewCard(
                        totalSpent = uiState.totalSpent,
                        topCategories = uiState.topCategories,
                        paymentModes = uiState.paymentModes,
                        onViewAll = onOpenInsights
                    )
                    HomeCardId.RECENT_TRANSACTIONS -> RecentTransactionsHomeCard(
                        transactions = uiState.transactions.take(5),
                        duplicateTransactionIds = uiState.duplicateTransactionIds,
                        onOpenActivity = onOpenActivity,
                        onDeleteTransaction = onDeleteTransaction,
                        onEditTransaction = onEditTransaction,
                        onOpenTransaction = onOpenTransaction,
                        onRemoveDuplicateRequest = onRemoveDuplicateRequest
                    )
                }
            }
        }
    }
}

private fun normalizeHomeCardOrder(order: List<String>): List<String> {
    return order
        .filter { it in DefaultHomeCardOrder }
        .distinct() + DefaultHomeCardOrder.filterNot { it in order }
}

private fun normalizeHomeCardIds(ids: Set<String>): Set<String> {
    return ids
        .filter { it in DefaultHomeCardOrder }
        .toSet()
}

private fun moveHomeCard(order: List<String>, cardId: String, direction: Int): List<String> {
    val currentIndex = order.indexOf(cardId)
    if (currentIndex == -1) return order
    val targetIndex = (currentIndex + direction).coerceIn(order.indices)
    if (targetIndex == currentIndex) return order
    return order.toMutableList().apply {
        val item = removeAt(currentIndex)
        add(targetIndex, item)
    }
}

@Composable
private fun EditableHomeCard(
    cardId: String,
    isEditing: Boolean,
    isVisible: Boolean,
    order: List<String>,
    onMove: (List<String>) -> Unit,
    onToggleVisibility: () -> Unit,
    content: @Composable () -> Unit
) {
    if (!isEditing) {
        if (isVisible) {
            content()
        }
        return
    }

    val index = order.indexOf(cardId)
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f))
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = HomeCardLabels[cardId].orEmpty(),
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f)
            )
            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (isVisible) {
                            "Hide ${HomeCardLabels[cardId].orEmpty()}"
                        } else {
                            "Show ${HomeCardLabels[cardId].orEmpty()}"
                        },
                        tint = if (isVisible) {
                            MaterialTheme.colorScheme.onSurfaceVariant
                        } else {
                            AccentPink
                        }
                    )
                }
                TextButton(
                    onClick = { onMove(moveHomeCard(order, cardId, -1)) },
                    enabled = index > 0
                ) { Text("Up") }
                TextButton(
                    onClick = { onMove(moveHomeCard(order, cardId, 1)) },
                    enabled = index >= 0 && index < order.lastIndex
                ) { Text("Down") }
            }
        }
        if (isVisible) {
            content()
        } else {
            HiddenHomeCardPlaceholder(cardId)
        }
    }
}

@Composable
private fun HiddenHomeCardPlaceholder(cardId: String) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.45f)
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Text(
            text = "${HomeCardLabels[cardId].orEmpty()} is hidden on Home",
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(16.dp)
        )
    }
}

@Composable
private fun RecentTransactionsHomeCard(
    transactions: List<TransactionEntity>,
    duplicateTransactionIds: Set<Long>,
    onOpenActivity: () -> Unit,
    onDeleteTransaction: (TransactionEntity) -> Unit,
    onEditTransaction: (TransactionEntity) -> Unit,
    onOpenTransaction: (TransactionEntity) -> Unit,
    onRemoveDuplicateRequest: (TransactionEntity) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        SectionTitle(
            title = "Recent Transactions",
            actionLabel = "See all",
            onAction = onOpenActivity
        )
        transactions.forEach { transaction ->
            TransactionListItem(
                transaction = transaction,
                onDeleteRequest = { onDeleteTransaction(transaction) },
                onEditRequest = { onEditTransaction(transaction) },
                onOpenRequest = { onOpenTransaction(transaction) },
                onRemoveDuplicateRequest = { onRemoveDuplicateRequest(transaction) },
                isLikelyDuplicate = transaction.id in duplicateTransactionIds
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

@OptIn(ExperimentalLayoutApi::class)
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
    onConnectAxisEmail: () -> Unit,
    onDisconnectAxisEmail: () -> Unit,
    onToggleAxisEmailAutoSync: (Boolean) -> Unit,
    onSyncAxisEmails: () -> Unit,
    onToggleSparkMailTrigger: (Boolean) -> Unit,
    onOpenSparkNotificationAccess: () -> Unit,
    onRecoverLegacyAiFailures: () -> Unit,
    onSetThemeMode: (String) -> Unit,
    onToggleDailyReminder: (Boolean) -> Unit,
    onSetDailyReminderTime: (Int, Int) -> Unit,
    onExportLocalBackup: () -> Unit,
    onRestoreLocalBackup: () -> Unit,
    onConnectDriveBackup: () -> Unit,
    onDisconnectDriveBackup: () -> Unit,
    onToggleDriveBackupAuto: (Boolean) -> Unit,
    onSetDriveBackupTime: (Int, Int) -> Unit,
    onPushBackupToDrive: () -> Unit,
    onRestoreBackupFromDrive: () -> Unit,
    onPhoneChange: (String) -> Unit,
    onSimulateTemplate: (DebugSmsTemplate) -> Unit,
    onSendTemplate: (DebugSmsTemplate) -> Unit
) {
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TransactionRule?>(null) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showEmailSyncHistory by rememberSaveable { mutableStateOf(false) }
    var showBackupHistory by rememberSaveable { mutableStateOf(false) }
    var showLocalRestoreConfirm by rememberSaveable { mutableStateOf(false) }
    var showDriveRestoreConfirm by rememberSaveable { mutableStateOf(false) }
    var showReviewCenter by rememberSaveable { mutableStateOf(false) }
    var showSpamInbox by rememberSaveable { mutableStateOf(false) }
    var showDebugConsole by rememberSaveable { mutableStateOf(false) }
    var showSourceExplorer by rememberSaveable { mutableStateOf(false) }
    val baseThemeOptions = listOf(
        THEME_MODE_SYSTEM to "System",
        THEME_MODE_LIGHT to "Light"
    )
    val darkThemeOptions = listOf(
        THEME_MODE_DARK to "Default dark",
        THEME_MODE_DARK_AMOLED to "AMOLED",
        THEME_MODE_DARK_OCEAN to "Ocean",
        THEME_MODE_DARK_FOREST to "Forest"
    )

    if (showCategoryDialog) {
        CategoryEditorDialog(
            onDismiss = { showCategoryDialog = false },
            onSave = {
                onAddCategory(it)
                showCategoryDialog = false
            }
        )
    }

    if (showEmailSyncHistory) {
        EmailSyncHistoryDialog(
            entries = uiState.axisEmailSyncHistory,
            onDismiss = { showEmailSyncHistory = false }
        )
    }

    if (showBackupHistory) {
        BackupHistoryDialog(
            entries = uiState.backupHistory,
            onDismiss = { showBackupHistory = false }
        )
    }

    if (showLocalRestoreConfirm) {
        RestoreBackupConfirmDialog(
            source = "a local backup file",
            onDismiss = { showLocalRestoreConfirm = false },
            onConfirm = {
                showLocalRestoreConfirm = false
                onRestoreLocalBackup()
            }
        )
    }

    if (showDriveRestoreConfirm) {
        RestoreBackupConfirmDialog(
            source = "Google Drive",
            onDismiss = { showDriveRestoreConfirm = false },
            onConfirm = {
                showDriveRestoreConfirm = false
                onRestoreBackupFromDrive()
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
        item {
            DailyReminderCard(
                enabled = uiState.dailyReminderEnabled,
                hour = uiState.dailyReminderHour,
                minute = uiState.dailyReminderMinute,
                onToggle = onToggleDailyReminder,
                onTimeChange = onSetDailyReminderTime
            )
        }
        item {
            BackupSettingsCard(
                driveAccount = uiState.driveBackupAccount,
                driveAutoEnabled = uiState.driveBackupAutoEnabled,
                driveHour = uiState.driveBackupHour,
                driveMinute = uiState.driveBackupMinute,
                history = uiState.backupHistory,
                isBusy = uiState.isBackupBusy,
                onExportLocal = onExportLocalBackup,
                onRestoreLocal = { showLocalRestoreConfirm = true },
                onConnectDrive = onConnectDriveBackup,
                onDisconnectDrive = onDisconnectDriveBackup,
                onToggleDriveAuto = onToggleDriveBackupAuto,
                onDriveTimeChange = onSetDriveBackupTime,
                onPushDrive = onPushBackupToDrive,
                onRestoreDrive = { showDriveRestoreConfirm = true },
                onOpenHistory = { showBackupHistory = true }
            )
        }
        // ── Theme Toggle ────────────────────────────────────────────────────────
        item {
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("App Theme", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Mode",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        baseThemeOptions.forEach { (mode, label) ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(label) }
                            )
                        }
                    }
                    Text(
                        "Dark themes",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        darkThemeOptions.forEach { (mode, label) ->
                            FilterChip(
                                selected = uiState.themeMode == mode,
                                onClick = { onSetThemeMode(mode) },
                                label = { Text(label) }
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
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                Column(
                    modifier = Modifier.padding(18.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Text("Legacy AI recovery", fontWeight = FontWeight.SemiBold)
                    Text(
                        text = "Scan older AI review items that were marked rejected because the model timed out or returned nothing, and convert them into retryable failures.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.End
                    ) {
                        TextButton(
                            onClick = onRecoverLegacyAiFailures,
                            enabled = !uiState.isScanningLegacyAiFailures
                        ) {
                            if (uiState.isScanningLegacyAiFailures) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(16.dp),
                                    strokeWidth = 2.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text("Scanning")
                            } else {
                                Text("Scan once")
                            }
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
                            Text("Axis Email Sync", fontWeight = FontWeight.SemiBold)
                            Text(
                                text = "Read Axis Bank alert emails from Gmail, skip them when a matching SMS already exists, and send the rest through the same AI pipeline as SMS.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (uiState.axisEmailAccount.isBlank()) {
                        TextButton(
                            onClick = onConnectAxisEmail,
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Connect Gmail")
                        }
                    } else {
                        Text(
                            text = "Connected: ${uiState.axisEmailAccount}",
                            color = MaterialTheme.colorScheme.onSurface,
                            fontWeight = FontWeight.Medium
                        )
                        Text(
                            text = if (uiState.axisEmailLastSyncMs > 0L) {
                                "Last checked ${formatDate(uiState.axisEmailLastSyncMs)}"
                            } else {
                                "No Axis email sync has run yet."
                            },
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Auto-check Axis emails", fontWeight = FontWeight.SemiBold)
                                Text(
                                    text = "SpendWise polls Gmail on-device every few minutes using WorkManager.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = uiState.axisEmailAutoSyncEnabled,
                                onCheckedChange = onToggleAxisEmailAutoSync
                            )
                        }
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End
                        ) {
                            TextButton(
                                onClick = onSyncAxisEmails,
                                enabled = !uiState.isAxisEmailSyncing
                            ) {
                                if (uiState.isAxisEmailSyncing) {
                                    CircularProgressIndicator(
                                        modifier = Modifier.size(16.dp),
                                        strokeWidth = 2.dp
                                    )
                                    Spacer(modifier = Modifier.width(8.dp))
                                    Text("Syncing")
                                } else {
                                    Text("Sync now")
                                }
                            }
                            TextButton(onClick = onDisconnectAxisEmail) {
                                Text("Disconnect")
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                            Column(
                                modifier = Modifier.padding(14.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text("Spark notifications trigger sync", fontWeight = FontWeight.SemiBold)
                                        Text(
                                            text = "Optional. When Spark Mail posts an Axis-looking notification, SpendWise immediately starts a Gmail sync.",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    Switch(
                                        checked = uiState.sparkMailTriggerEnabled,
                                        onCheckedChange = onToggleSparkMailTrigger
                                    )
                                }
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = if (uiState.hasSparkNotificationAccess) {
                                            "Notification access granted"
                                        } else {
                                            "Notification access required"
                                        },
                                        color = if (uiState.hasSparkNotificationAccess) AccentGreen else AccentAmber,
                                        fontWeight = FontWeight.Medium
                                    )
                                    TextButton(onClick = onOpenSparkNotificationAccess) {
                                        Text(if (uiState.hasSparkNotificationAccess) "Manage access" else "Grant access")
                                    }
                                }
                            }
                        }
                    }
                    if (uiState.axisEmailAccount.isNotBlank() || uiState.axisEmailSyncHistory.isNotEmpty()) {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { showEmailSyncHistory = true }
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Email sync history", fontWeight = FontWeight.SemiBold)
                                    Text(
                                        text = uiState.axisEmailSyncHistory.firstOrNull()?.let { latest ->
                                            "${AxisEmailSyncTrigger.label(latest.trigger)} · ${formatDate(latest.startedAt)} · ${latest.message}"
                                        } ?: "See manual, periodic, and mail-notification sync runs in one place.",
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                        maxLines = 2,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                                Icon(
                                    imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                                    contentDescription = "Open email sync history",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
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
    NavigationBar(containerColor = MaterialTheme.colorScheme.surface, tonalElevation = 0.dp) {
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
    onMonthClick: (() -> Unit)? = null,
    trailingContent: (@Composable () -> Unit)? = null
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
        if (trailingContent != null || (monthLabel != null && onMonthClick != null)) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                trailingContent?.invoke()
                if (monthLabel != null && onMonthClick != null) {
                    AssistChip(
                        onClick = onMonthClick,
                        label = { Text(monthLabel) },
                        trailingIcon = { Icon(Icons.Rounded.ArrowDropDown, contentDescription = null) },
                        colors = AssistChipDefaults.assistChipColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    )
                }
            }
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
private fun DailyReminderCard(
    enabled: Boolean,
    hour: Int,
    minute: Int,
    onToggle: (Boolean) -> Unit,
    onTimeChange: (Int, Int) -> Unit
) {
    var showTimeDialog by rememberSaveable { mutableStateOf(false) }
    val formattedTime = formatReminderTime(hour, minute)

    if (showTimeDialog) {
        ReminderTimeDialog(
            initialHour = hour,
            initialMinute = minute,
            onDismiss = { showTimeDialog = false },
            onSave = { selectedHour, selectedMinute ->
                showTimeDialog = false
                onTimeChange(selectedHour, selectedMinute)
            }
        )
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("DAILY REMINDER", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            Text(
                text = "Get a daily check-in with today's spend total and transaction count.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                AssistChip(
                    onClick = { onToggle(!enabled) },
                    label = { Text(if (enabled) "Reminder On" else "Reminder Off") },
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = if (enabled) AccentTeal.copy(alpha = 0.18f) else MaterialTheme.colorScheme.surfaceVariant
                    )
                )
                Text(
                    text = if (enabled) "Every day at $formattedTime" else "Daily summary is paused",
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f)
                )
            }
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text("Reminder time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                        Text(formattedTime, fontWeight = FontWeight.Black)
                        Text(
                            text = ZoneId.systemDefault().id,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium
                        )
                    }
                    TextButton(onClick = { showTimeDialog = true }) {
                        Text("Change time")
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun BackupSettingsCard(
    driveAccount: String,
    driveAutoEnabled: Boolean,
    driveHour: Int,
    driveMinute: Int,
    history: List<BackupHistoryEntry>,
    isBusy: Boolean,
    onExportLocal: () -> Unit,
    onRestoreLocal: () -> Unit,
    onConnectDrive: () -> Unit,
    onDisconnectDrive: () -> Unit,
    onToggleDriveAuto: (Boolean) -> Unit,
    onDriveTimeChange: (Int, Int) -> Unit,
    onPushDrive: () -> Unit,
    onRestoreDrive: () -> Unit,
    onOpenHistory: () -> Unit
) {
    var showDriveTimeDialog by rememberSaveable { mutableStateOf(false) }
    val latest = history.firstOrNull()
    val formattedTime = formatReminderTime(driveHour, driveMinute)

    if (showDriveTimeDialog) {
        ReminderTimeDialog(
            initialHour = driveHour,
            initialMinute = driveMinute,
            title = "Drive backup time",
            description = "Use 24-hour time. Backups use this phone's current timezone: ${ZoneId.systemDefault().id}.",
            validText = { hour, minute -> "Drive backup will run at ${formatReminderTime(hour, minute)}." },
            onDismiss = { showDriveTimeDialog = false },
            onSave = { hour, minute ->
                showDriveTimeDialog = false
                onDriveTimeChange(hour, minute)
            }
        )
    }

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text("BACKUP, EXPORT & RESTORE", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            Text(
                text = "Export a local SpendWise backup or keep one private backup in your Google Drive app data.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Text("Local backup", fontWeight = FontWeight.SemiBold)
                    Text(
                        "Creates a JSON file with transactions, review history, category AI records, budgets, rules, categories, and layout settings.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    FlowRow(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        TextButton(onClick = onExportLocal, enabled = !isBusy) {
                            Text(if (isBusy) "Working" else "Export file")
                        }
                        TextButton(onClick = onRestoreLocal, enabled = !isBusy) {
                            Text("Restore file")
                        }
                    }
                }
            }

            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(
                    modifier = Modifier.padding(14.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Google Drive backup", fontWeight = FontWeight.SemiBold)
                            Text(
                                if (driveAccount.isBlank()) {
                                    "Connect Google Drive to push and restore a private SpendWise backup."
                                } else {
                                    "Connected: $driveAccount"
                                },
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        AssistChip(
                            onClick = if (driveAccount.isBlank()) onConnectDrive else onDisconnectDrive,
                            label = { Text(if (driveAccount.isBlank()) "Connect" else "Disconnect") }
                        )
                    }

                    if (driveAccount.isNotBlank()) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text("Daily Drive backup", fontWeight = FontWeight.SemiBold)
                                Text(
                                    if (driveAutoEnabled) "Every day at $formattedTime" else "Automatic backup is paused.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Switch(
                                checked = driveAutoEnabled,
                                onCheckedChange = onToggleDriveAuto
                            )
                        }

                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text("Backup time", color = MaterialTheme.colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                                    Text(formattedTime, fontWeight = FontWeight.Black)
                                    Text(
                                        ZoneId.systemDefault().id,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                TextButton(onClick = { showDriveTimeDialog = true }) {
                                    Text("Change time")
                                }
                            }
                        }

                        FlowRow(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            TextButton(onClick = onPushDrive, enabled = !isBusy) {
                                Text(if (isBusy) "Working" else "Push now")
                            }
                            TextButton(onClick = onRestoreDrive, enabled = !isBusy) {
                                Text("Restore latest")
                            }
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onOpenHistory() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Backup history", fontWeight = FontWeight.SemiBold)
                        Text(
                            text = latest?.let {
                                "${BackupTrigger.label(it.trigger)} · ${BackupDestination.label(it.destination)} · ${BackupStatus.label(it.status)} · ${it.transactionCount} transactions"
                            } ?: "See exports, restores, Drive pushes, and automatic backup runs.",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Rounded.ReceiptLong,
                        contentDescription = "Open backup history",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun RestoreBackupConfirmDialog(
    source: String,
    onDismiss: () -> Unit,
    onConfirm: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Restore backup?") },
        text = {
            Text("This will replace the current SpendWise transactions and saved backup-supported settings with data from $source.")
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Restore")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun ReminderTimeDialog(
    initialHour: Int,
    initialMinute: Int,
    title: String = "Daily reminder time",
    description: String = "Use 24-hour time. The reminder uses this phone's current timezone: ${ZoneId.systemDefault().id}.",
    validText: (Int, Int) -> String = { hour, minute ->
        "Reminder will run at ${formatReminderTime(hour, minute)}."
    },
    onDismiss: () -> Unit,
    onSave: (Int, Int) -> Unit
) {
    var hourText by rememberSaveable { mutableStateOf(initialHour.coerceIn(0, 23).toString().padStart(2, '0')) }
    var minuteText by rememberSaveable { mutableStateOf(initialMinute.coerceIn(0, 59).toString().padStart(2, '0')) }
    val parsedHour = hourText.toIntOrNull()
    val parsedMinute = minuteText.toIntOrNull()
    val isValid = parsedHour != null && parsedHour in 0..23 && parsedMinute != null && parsedMinute in 0..59

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    text = description,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = hourText,
                        onValueChange = { hourText = it.filter(Char::isDigit).take(2) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Hour") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                    OutlinedTextField(
                        value = minuteText,
                        onValueChange = { minuteText = it.filter(Char::isDigit).take(2) },
                        modifier = Modifier.weight(1f),
                        singleLine = true,
                        label = { Text("Minute") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
                Text(
                    text = if (isValid) {
                        validText(parsedHour ?: 0, parsedMinute ?: 0)
                    } else {
                        "Enter an hour from 0-23 and minute from 0-59."
                    },
                    color = if (isValid) AccentPurple else AccentPink,
                    fontWeight = FontWeight.SemiBold
                )
            }
        },
        confirmButton = {
            TextButton(
                enabled = isValid,
                onClick = { onSave(parsedHour ?: 0, parsedMinute ?: 0) }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun BackupHistoryDialog(
    entries: List<BackupHistoryEntry>,
    onDismiss: () -> Unit
) {
    val background = MaterialTheme.colorScheme.background
    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 18.dp, vertical = 16.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Close backup history"
                            )
                        }
                        Column {
                            Text("Backup history", fontWeight = FontWeight.Bold)
                            Text(
                                "Local exports, restores, Drive pushes, and automatic runs.",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }

                if (entries.isEmpty()) {
                    EmptyStateCard("No backup history yet.")
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(entries, key = { it.id }) { entry ->
                            BackupHistoryCard(entry)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BackupHistoryCard(entry: BackupHistoryEntry) {
    val statusColor = if (entry.status == BackupStatus.FAILED) AccentPink else AccentGreen
    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    "${BackupTrigger.label(entry.trigger)} · ${BackupDestination.label(entry.destination)}",
                    fontWeight = FontWeight.SemiBold
                )
                Text(formatDate(entry.timestamp), color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Text(
                BackupStatus.label(entry.status),
                color = statusColor,
                fontWeight = FontWeight.SemiBold
            )
            Text(
                "Items ${entry.itemCount} · Transactions ${entry.transactionCount}",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (entry.message.isNotBlank()) {
                Text(entry.message, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun EmailSyncHistoryDialog(
    entries: List<AxisEmailSyncHistoryEntry>,
    onDismiss: () -> Unit
) {
    var selectedEntry by remember(entries) { mutableStateOf<AxisEmailSyncHistoryEntry?>(null) }
    val background = MaterialTheme.colorScheme.background

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(background)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            if (selectedEntry == null) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = onDismiss) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    contentDescription = "Close email sync history"
                                )
                            }
                            Column {
                                Text("Email sync history", fontWeight = FontWeight.Bold)
                                Text(
                                    "Manual, periodic, and mail-triggered sync runs.",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                    if (entries.isEmpty()) {
                        EmptyStateCard("No email sync history yet.")
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                            contentPadding = PaddingValues(bottom = 24.dp)
                        ) {
                            items(entries, key = { it.id }) { entry ->
                                Card(
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { selectedEntry = entry }
                                ) {
                                    Column(
                                        modifier = Modifier.padding(16.dp),
                                        verticalArrangement = Arrangement.spacedBy(8.dp)
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(
                                                AxisEmailSyncTrigger.label(entry.trigger),
                                                fontWeight = FontWeight.SemiBold
                                            )
                                            Text(
                                                formatDate(entry.startedAt),
                                                color = MaterialTheme.colorScheme.onSurfaceVariant
                                            )
                                        }
                                        Text(
                                            entry.message,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                        Text(
                                            "Scanned ${entry.scanned} · Imported ${entry.imported} · Duplicates ${entry.duplicates} · Skipped ${entry.skipped}",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            } else {
                val entry = selectedEntry ?: return@Dialog
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState())
                        .padding(horizontal = 18.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            IconButton(onClick = { selectedEntry = null }) {
                                Icon(
                                    Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                    contentDescription = "Back to email sync history"
                                )
                            }
                            Column {
                                Text("Sync run", fontWeight = FontWeight.Bold)
                                Text(
                                    "${AxisEmailSyncTrigger.label(entry.trigger)} · ${formatDate(entry.startedAt)}",
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close email sync history")
                        }
                    }

                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(entry.message, fontWeight = FontWeight.SemiBold)
                            Text(
                                "Account: ${entry.account.ifBlank { "Not connected" }}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Status: ${entry.status}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Text(
                                "Scanned ${entry.scanned} · Imported ${entry.imported} · Duplicates ${entry.duplicates} · Skipped ${entry.skipped}",
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (entry.items.isEmpty()) {
                        EmptyStateCard("This sync run did not pull any new Axis mail details.")
                    } else {
                        entry.items.forEach { item ->
                            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                                Column(
                                    modifier = Modifier.padding(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth(),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Text(item.outcome, fontWeight = FontWeight.SemiBold)
                                        Text(
                                            formatDate(item.receivedAt),
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.from.isNotBlank()) {
                                        Text(
                                            item.from,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.summary.isNotBlank()) {
                                        Text(item.summary)
                                    }
                                    val parsedSummary = buildList {
                                        item.parsedAmount?.let { add(formatRupees(it)) }
                                        item.parsedType.takeIf { it.isNotBlank() }?.let { add(it) }
                                        item.parsedMerchant.takeIf { it.isNotBlank() }?.let { add(it) }
                                    }.joinToString(" · ")
                                    if (parsedSummary.isNotBlank()) {
                                        Text(
                                            "Parsed: $parsedSummary",
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.cleanedBody.isNotBlank()) {
                                        Text("Sent to AI", fontWeight = FontWeight.Medium)
                                        Text(
                                            item.cleanedBody,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (item.fullBody.isNotBlank() && item.fullBody != item.cleanedBody) {
                                        Text("Full message", fontWeight = FontWeight.Medium)
                                        Text(
                                            item.fullBody,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(24.dp))
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
    val chartValues = topCategories.ifEmpty { listOf(CategoryTotal("Other", 1.0)) }
    val previewCategories = topCategories.ifEmpty { listOf(CategoryTotal("Other", 0.0)) }
    val chartColors = donutColors(chartValues.size)
    val topCategoryName = topCategories.firstOrNull()?.category ?: "No category yet"
    val topPaymentMode = paymentModes.firstOrNull()

    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            SectionTitle(title = "Insights", actionLabel = "View all", onAction = onViewAll)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(18.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.42f))
                    .padding(16.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(18.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(modifier = Modifier.size(104.dp), contentAlignment = Alignment.Center) {
                        DonutChart(
                            values = chartValues,
                            colors = chartColors
                        )
                    }
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Text(
                            "This month spent",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            formatRupees(totalSpent),
                            color = AccentPink,
                            fontWeight = FontWeight.Black,
                            style = MaterialTheme.typography.titleLarge
                        )
                        Text(
                            text = "Top category: $topCategoryName",
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.Medium,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                Text(
                    "All categories",
                    color = MaterialTheme.colorScheme.onSurface,
                    fontWeight = FontWeight.Bold
                )
                previewCategories.forEachIndexed { index, categoryTotal ->
                    InsightPreviewCategoryRow(
                        categoryTotal = categoryTotal,
                        totalSpent = totalSpent,
                        color = chartColors[index % chartColors.size]
                    )
                }
            }

            if (topPaymentMode != null) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(16.dp))
                        .background(AccentPurple.copy(alpha = 0.12f))
                        .padding(horizontal = 14.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "Top payment mode",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        topPaymentMode.mode,
                        color = AccentPurple,
                        fontWeight = FontWeight.Black
                    )
                }
            }
        }
    }
}

@Composable
private fun InsightPreviewCategoryRow(
    categoryTotal: CategoryTotal,
    totalSpent: Double,
    color: Color
) {
    val percent = if (totalSpent <= 0.0) 0 else ((categoryTotal.totalAmount / totalSpent) * 100).toInt()
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(color)
                )
                Text(
                    text = categoryTotal.category,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Text(
                "$percent%",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Bold
            )
        }
        LinearProgressIndicator(
            progress = { (percent / 100f).coerceIn(0f, 1f) },
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(99.dp)),
            color = color,
            trackColor = MaterialTheme.colorScheme.surfaceVariant
        )
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
    isFindingSimilarTransactions: Boolean,
    similarTransactions: List<TransactionEntity>,
    categoryRefinementRecord: TransactionCategoryAiEntity?,
    isCategoryRefinementLoading: Boolean,
    onObserveCategoryRefinement: (Long) -> Unit,
    onClearCategoryRefinementObservation: () -> Unit,
    onRequestCategoryRefinement: (TransactionEntity) -> Unit,
    onDismiss: () -> Unit,
    onSave: (TransactionEntity) -> Unit,
    onFindSimilar: (TransactionEntity) -> Unit,
    onClearSimilar: () -> Unit,
    onApplyToSimilar: (TransactionEntity, Set<Long>) -> Unit
) {
    var amount by rememberSaveable(transaction.id) { mutableStateOf(transaction.amount.toString()) }
    var merchant by rememberSaveable(transaction.id) { mutableStateOf(transaction.merchant) }
    var bank by rememberSaveable(transaction.id) { mutableStateOf(transaction.bank) }
    var category by rememberSaveable(transaction.id) { mutableStateOf(transaction.category) }
    var note by rememberSaveable(transaction.id) { mutableStateOf(transaction.note) }
    var tags by rememberSaveable(transaction.id) { mutableStateOf(transaction.tags) }
    var selectedTypeIndex by rememberSaveable(transaction.id) {
        mutableIntStateOf(if (transaction.type == TransactionType.DEBIT) 0 else 1)
    }
    var showAmountEditor by rememberSaveable(transaction.id) {
        mutableStateOf(initialMode == TransactionDialogMode.EDIT)
    }
    var showMerchantEditor by rememberSaveable(transaction.id) { mutableStateOf(false) }
    var showNoteEditor by rememberSaveable(transaction.id) { mutableStateOf(false) }
    var showCategoryPicker by rememberSaveable(transaction.id) { mutableStateOf(false) }
    var showSimilarSheet by rememberSaveable(transaction.id) { mutableStateOf(false) }
    var showAiDecisionSheet by rememberSaveable(transaction.id) { mutableStateOf(false) }

    val selectedType = if (selectedTypeIndex == 0) TransactionType.DEBIT else TransactionType.CREDIT
    val editedTransaction = transaction.copy(
        amount = amount.toDoubleOrNull() ?: transaction.amount,
        merchant = merchant.ifBlank { transaction.merchant },
        bank = bank.ifBlank { transaction.bank },
        category = category.ifBlank { transaction.category },
        note = note,
        tags = tags,
        type = selectedType,
        paymentMode = paymentModeFor(
            merchant = merchant.ifBlank { transaction.merchant },
            rawSms = transaction.rawSms
        )
    )
    val isDirty = editedTransaction.amount != transaction.amount ||
        editedTransaction.merchant != transaction.merchant ||
        editedTransaction.bank != transaction.bank ||
        editedTransaction.category != transaction.category ||
        editedTransaction.note != transaction.note ||
        editedTransaction.tags != transaction.tags ||
        editedTransaction.type != transaction.type
    val amountColor = if (selectedType == TransactionType.CREDIT) AccentGreen else AccentPink
    val amountPrefix = if (selectedType == TransactionType.CREDIT) "+" else "-"
    val colorScheme = MaterialTheme.colorScheme
    val detailBg = colorScheme.background
    val detailCard = colorScheme.surface
    val detailVariant = colorScheme.surfaceVariant
    val detailStroke = colorScheme.outline.copy(alpha = 0.45f)
    val detailOnSurface = colorScheme.onSurface
    val detailMuted = colorScheme.onSurfaceVariant
    val isAiRunning = isCategoryRefinementLoading ||
        transaction.categoryRefinementStatus == CategoryRefinementStatus.PENDING ||
        transaction.categoryRefinementStatus == CategoryRefinementStatus.RUNNING
    val canRequestAiRefinement = transaction.categoryDecisionSource != CategoryDecisionSource.RULE &&
        !isAiRunning
    val canOpenAiInsight = transaction.categoryDecisionSource == CategoryDecisionSource.RULE ||
        categoryRefinementRecord != null ||
        transaction.categoryRefinementStatus != CategoryRefinementStatus.NONE ||
        isCategoryRefinementLoading ||
        canRequestAiRefinement

    LaunchedEffect(transaction.id) {
        onObserveCategoryRefinement(transaction.id)
    }
    DisposableEffect(transaction.id) {
        onDispose { onClearCategoryRefinementObservation() }
    }

    if (showAmountEditor) {
        TransactionTextEditorDialog(
            title = "Edit amount",
            label = "Amount",
            initialValue = amount,
            keyboardType = KeyboardType.Number,
            isValid = { it.toDoubleOrNull()?.let { value -> value > 0.0 } == true },
            onDismiss = { showAmountEditor = false },
            onSave = {
                amount = it.toDoubleOrNull()?.toString() ?: amount
                showAmountEditor = false
            }
        )
    }
    if (showMerchantEditor) {
        TransactionTextEditorDialog(
            title = "Edit merchant",
            label = "Merchant",
            initialValue = merchant,
            keyboardType = KeyboardType.Text,
            isValid = { it.isNotBlank() },
            onDismiss = { showMerchantEditor = false },
            onSave = {
                merchant = it.trim()
                showMerchantEditor = false
            }
        )
    }
    if (showNoteEditor) {
        TransactionTextEditorDialog(
            title = "Edit note",
            label = "Note",
            initialValue = note,
            keyboardType = KeyboardType.Text,
            isValid = { true },
            onDismiss = { showNoteEditor = false },
            onSave = {
                note = it.trim()
                showNoteEditor = false
            }
        )
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(detailBg)
                .statusBarsPadding()
                .navigationBarsPadding()
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .imePadding(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 18.dp, bottom = 108.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(48.dp)
                                .clip(CircleShape)
                                .background(detailCard)
                                .clickable(onClick = onDismiss),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.AutoMirrored.Rounded.KeyboardArrowLeft,
                                contentDescription = "Back",
                                tint = detailOnSurface
                            )
                        }
                        Text("Transaction Details", color = detailOnSurface, fontWeight = FontWeight.Black)
                    }
                }
                item {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = detailCard),
                        border = BorderStroke(1.dp, detailStroke),
                        shape = RoundedCornerShape(28.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(22.dp),
                            verticalArrangement = Arrangement.spacedBy(18.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CategoryImage(
                                    category = category,
                                    size = 72,
                                    background = colorForCategory(category).copy(alpha = 0.18f)
                                )
                                Column(horizontalAlignment = Alignment.End) {
                                    Text(
                                        text = amountPrefix + formatRupees(editedTransaction.amount),
                                        color = amountColor,
                                        fontWeight = FontWeight.Black,
                                        modifier = Modifier.clickable { showAmountEditor = true }
                                    )
                                    Text(
                                        text = formatDate(transaction.timestamp),
                                        color = detailMuted,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }
                            }
                            HorizontalDivider(color = detailStroke)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = merchant.ifBlank { "Unknown Merchant" },
                                    color = detailOnSurface,
                                    fontWeight = FontWeight.Black,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier.weight(1f)
                                )
                                IconButton(onClick = { showMerchantEditor = true }) {
                                    Icon(Icons.Rounded.Edit, contentDescription = "Edit merchant", tint = detailMuted)
                                }
                            }
                            Row(
                                modifier = Modifier
                                    .align(Alignment.CenterHorizontally)
                                    .clickable { showNoteEditor = true },
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = note.ifBlank { "Add a note..." },
                                    color = detailMuted
                                )
                                Icon(Icons.Rounded.Edit, contentDescription = "Edit note", tint = detailMuted, modifier = Modifier.size(18.dp))
                            }
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                AssistChip(
                                    onClick = {},
                                    enabled = false,
                                    label = {
                                        Text(
                                            categoryRefinementStatusLabel(
                                                status = transaction.categoryRefinementStatus,
                                                source = transaction.categoryDecisionSource
                                            )
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            Icons.Rounded.Psychology,
                                            contentDescription = null,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    },
                                    colors = AssistChipDefaults.assistChipColors(
                                        disabledContainerColor = colorScheme.surfaceVariant,
                                        disabledLabelColor = detailOnSurface,
                                        disabledLeadingIconContentColor = AccentPurple
                                    )
                                )
                                Row(
                                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    if (canRequestAiRefinement) {
                                        TextButton(onClick = { onRequestCategoryRefinement(transaction) }) {
                                            Text("Run AI", color = AccentPurple, fontWeight = FontWeight.Black)
                                        }
                                    }
                                    if (canOpenAiInsight) {
                                        IconButton(onClick = { showAiDecisionSheet = true }) {
                                            Icon(
                                                Icons.Rounded.Psychology,
                                                contentDescription = "AI category details",
                                                tint = AccentPurple
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                item {
                    SegmentedToggle(listOf("Expense", "Income"), selectedTypeIndex) { selectedTypeIndex = it }
                }
                item {
                    DetailActionCard(
                        iconCategory = category,
                        title = category,
                        subtitle = "Category",
                        actionLabel = "Change",
                        onClick = { showCategoryPicker = true }
                    )
                }
                item {
                    DetailActionCard(
                        iconCategory = "Other",
                        title = "Find Similar Transactions",
                        subtitle = "Apply these edits to matching transactions",
                        actionLabel = ">",
                        onClick = {
                            showSimilarSheet = true
                            onFindSimilar(editedTransaction)
                        }
                    )
                }
                item {
                    Text(
                        text = "ORIGINAL SMS",
                        color = detailMuted,
                        fontWeight = FontWeight.Black
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = detailVariant),
                        border = BorderStroke(1.dp, detailStroke),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(18.dp),
                            verticalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Text(formatDate(transaction.timestamp).uppercase(Locale.ENGLISH), color = AccentPurple, fontWeight = FontWeight.Black)
                            Text(transaction.rawSms, color = detailOnSurface, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }
            }

            Card(
                colors = CardDefaults.cardColors(containerColor = if (isDirty) AccentPink else detailCard),
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 18.dp)
                    .navigationBarsPadding()
                    .clickable(enabled = isDirty) { onSave(editedTransaction) }
            ) {
                Text(
                    text = "Save Changes",
                    color = if (isDirty) Color.White else detailMuted,
                    fontWeight = FontWeight.Black,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 20.dp)
                )
            }

            if (showCategoryPicker) {
                CategoryPickerSheet(
                    categories = availableCategories,
                    selectedCategory = category,
                    onDismiss = { showCategoryPicker = false },
                    onSelect = {
                        category = it
                        showCategoryPicker = false
                    }
                )
            }

            if (showSimilarSheet && isFindingSimilarTransactions) {
                FindingSimilarOverlay()
            } else if (showSimilarSheet) {
                SimilarTransactionsSheet(
                    editedTransaction = editedTransaction,
                    similarTransactions = similarTransactions,
                    onDismiss = {
                        showSimilarSheet = false
                        onClearSimilar()
                    },
                    onOnlyThisOne = {
                        onSave(editedTransaction)
                        showSimilarSheet = false
                        onClearSimilar()
                    },
                    onApplyToSelected = { selectedIds ->
                        onSave(editedTransaction)
                        onApplyToSimilar(editedTransaction, selectedIds)
                        showSimilarSheet = false
                    }
                )
            }

            if (showAiDecisionSheet) {
                CategoryAiDecisionSheet(
                    transaction = transaction,
                    refinementRecord = categoryRefinementRecord,
                    isLoading = isCategoryRefinementLoading,
                    onRequestCategoryRefinement = { onRequestCategoryRefinement(transaction) },
                    onDismiss = { showAiDecisionSheet = false }
                )
            }
        }
    }
}

@Composable
private fun CategoryAiDecisionSheet(
    transaction: TransactionEntity,
    refinementRecord: TransactionCategoryAiEntity?,
    isLoading: Boolean,
    onRequestCategoryRefinement: () -> Unit,
    onDismiss: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val canRequestAiRefinement = transaction.categoryDecisionSource != CategoryDecisionSource.RULE &&
        transaction.categoryRefinementStatus != CategoryRefinementStatus.PENDING &&
        transaction.categoryRefinementStatus != CategoryRefinementStatus.RUNNING &&
        !isLoading
    val resolution = remember(refinementRecord?.resolverSignalsJson) {
        refinementRecord?.resolverSignalsJson
            ?.takeIf { it.isNotBlank() }
            ?.let { raw ->
                runCatching { SpendWiseUiGson.fromJson(raw, CategoryResolution::class.java) }.getOrNull()
            }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.4f)),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.74f)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                item {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            Text("AI category review", color = colorScheme.onSurface, fontWeight = FontWeight.Black)
                            Text(
                                categoryRefinementStatusDescription(
                                    status = transaction.categoryRefinementStatus,
                                    source = transaction.categoryDecisionSource
                                ),
                                color = colorScheme.onSurfaceVariant
                            )
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colorScheme.onSurface)
                        }
                    }
                }
                item {
                    DetailInfoCard(
                        title = "Current category",
                        value = transaction.category,
                        supporting = "Decision source: ${decisionSourceLabel(transaction.categoryDecisionSource)}"
                    )
                }
                if (isLoading) {
                    item {
                        Card(
                            colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(24.dp)
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(18.dp),
                                verticalArrangement = Arrangement.spacedBy(12.dp),
                                horizontalAlignment = Alignment.CenterHorizontally
                            ) {
                                CircularProgressIndicator(color = AccentPurple)
                                Text("AI is refining this category.", color = colorScheme.onSurface, fontWeight = FontWeight.Black)
                                Text(
                                    "SpendWise already saved the transaction and is double-checking the category in the background.",
                                    color = colorScheme.onSurfaceVariant,
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                } else if (refinementRecord != null) {
                    item {
                        DetailInfoCard(
                            title = "AI suggestion",
                            value = refinementRecord.suggestedCategory.ifBlank { transaction.category },
                            supporting = buildString {
                                val outcomeLabel = refinementRecord.outcome
                                    .replace('_', ' ')
                                    .lowercase(Locale.ENGLISH)
                                    .replaceFirstChar { it.uppercase() }
                                append("Outcome: ")
                                append(outcomeLabel)
                                append(" • Confidence: ")
                                append((refinementRecord.confidence * 100).toInt())
                                append("%")
                            }
                        )
                    }
                    item {
                        DetailInfoCard(
                            title = "AI reason",
                            value = refinementRecord.reason.ifBlank { "No extra explanation returned." },
                            supporting = refinementRecord.outcomeDetail.ifBlank { "No additional outcome detail." }
                        )
                    }
                    item {
                        DetailInfoCard(
                            title = "Model",
                            value = refinementRecord.model.ifBlank { "Unknown" },
                            supporting = formatDate(refinementRecord.finishedAt)
                        )
                    }
                    item {
                        DetailInfoCard(
                            title = "Resolver guess",
                            value = refinementRecord.resolverCategory,
                            supporting = buildString {
                                append("Bucket: ")
                                append(resolution?.bucketLabel ?: "Unknown")
                                val keywords = resolution?.matchedKeywords.orEmpty()
                                if (keywords.isNotEmpty()) {
                                    append(" • Keywords: ")
                                    append(keywords.joinToString(", "))
                                }
                            }
                        )
                    }
                } else if (transaction.categoryDecisionSource == CategoryDecisionSource.RULE) {
                    item {
                        DetailInfoCard(
                            title = "Rule locked",
                            value = transaction.categoryRuleName.ifBlank { "A matching SpendWise rule set this category." },
                            supporting = "AI did not run because user rules are treated as the final authority."
                        )
                    }
                } else {
                    item {
                        DetailInfoCard(
                            title = "No AI record yet",
                            value = "This transaction has not produced a saved AI refinement result.",
                            supporting = "SpendWise is using the local resolver result for now."
                        )
                    }
                }
                item {
                    DetailInfoCard(
                        title = "Merchant context",
                        value = transaction.merchant,
                        supporting = transaction.rawSms
                    )
                }
                if (canRequestAiRefinement) {
                    item {
                        TextButton(
                            onClick = onRequestCategoryRefinement,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Icon(
                                Icons.Rounded.Psychology,
                                contentDescription = null,
                                tint = AccentPurple,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("Run AI category check", color = AccentPurple, fontWeight = FontWeight.Black)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DetailInfoCard(
    title: String,
    value: String,
    supporting: String
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(title, color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.Black)
            Text(value, color = colorScheme.onSurface, fontWeight = FontWeight.SemiBold)
            Text(supporting, color = colorScheme.onSurfaceVariant)
        }
    }
}

private fun categoryRefinementStatusLabel(status: String, source: String): String {
    return when {
        source == CategoryDecisionSource.RULE -> "Rule locked"
        status == CategoryRefinementStatus.PENDING -> "AI refining..."
        status == CategoryRefinementStatus.RUNNING -> "AI refining..."
        status == CategoryRefinementStatus.APPLIED -> "AI updated"
        status == CategoryRefinementStatus.KEPT_RESOLVER -> "AI checked"
        status == CategoryRefinementStatus.SKIPPED_STALE -> "AI skipped"
        status == CategoryRefinementStatus.FAILED -> "AI unavailable"
        else -> "Resolver"
    }
}

private fun categoryRefinementStatusDescription(status: String, source: String): String {
    return when {
        source == CategoryDecisionSource.RULE -> "A user rule set this category, so AI was intentionally skipped."
        status == CategoryRefinementStatus.PENDING || status == CategoryRefinementStatus.RUNNING ->
            "SpendWise saved the transaction immediately and is refining the category in the background."
        status == CategoryRefinementStatus.APPLIED ->
            "AI confidently replaced the resolver category."
        status == CategoryRefinementStatus.KEPT_RESOLVER ->
            "AI reviewed the transaction and kept SpendWise's original category."
        status == CategoryRefinementStatus.SKIPPED_STALE ->
            "AI finished after the transaction changed, so SpendWise ignored the old result."
        status == CategoryRefinementStatus.FAILED ->
            "The AI pass did not return a usable category, so SpendWise kept the existing one."
        else -> "This transaction is currently using the local keyword resolver."
    }
}

private fun decisionSourceLabel(source: String): String {
    return when (source) {
        CategoryDecisionSource.RULE -> "Rule"
        CategoryDecisionSource.AI -> "AI"
        CategoryDecisionSource.USER_EDIT -> "User"
        else -> "Resolver"
    }
}

@Composable
private fun DetailActionCard(
    iconCategory: String,
    title: String,
    subtitle: String,
    actionLabel: String,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
        border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CategoryImage(
                category = iconCategory,
                size = 52,
                background = colorForCategory(iconCategory).copy(alpha = 0.18f)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(title, color = colorScheme.onSurface, fontWeight = FontWeight.Black)
                Text(subtitle, color = colorScheme.onSurfaceVariant)
            }
            Card(
                colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                shape = RoundedCornerShape(14.dp)
            ) {
                Text(
                    actionLabel,
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 10.dp)
                )
            }
        }
    }
}

@Composable
private fun CategoryImage(
    category: String,
    size: Int,
    background: Color
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Box(
            modifier = Modifier
                .size(size.dp)
                .clip(RoundedCornerShape(20.dp))
                .background(background),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                iconForCategory(category),
                contentDescription = category,
                tint = colorForCategory(category),
                modifier = Modifier.size((size * 0.46f).dp)
            )
        }
        if (size >= 64) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(category, color = colorForCategory(category), fontWeight = FontWeight.Black)
        }
    }
}

@Composable
private fun CategoryPickerSheet(
    categories: List<String>,
    selectedCategory: String,
    onDismiss: () -> Unit,
    onSelect: (String) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.68f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.78f)
        ) {
            LazyColumn(
                contentPadding = PaddingValues(18.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                item {
                    Text("Change category", color = colorScheme.onSurface, fontWeight = FontWeight.Black)
                    Text("Pick from SpendWise categories", color = colorScheme.onSurfaceVariant, fontWeight = FontWeight.SemiBold)
                }
                items(categories.chunked(3)) { row ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        row.forEach { category ->
                            CategoryChoiceTile(
                                modifier = Modifier.weight(1f),
                                category = category,
                                selected = category == selectedCategory,
                                onSelect = { onSelect(category) }
                            )
                        }
                        repeat(3 - row.size) {
                            Spacer(modifier = Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryChoiceTile(
    modifier: Modifier,
    category: String,
    selected: Boolean,
    onSelect: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AccentPurple.copy(alpha = 0.18f) else colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (selected) AccentPurple else colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .height(116.dp)
            .clickable(onClick = onSelect)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(10.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                iconForCategory(category),
                contentDescription = category,
                tint = colorForCategory(category),
                modifier = Modifier.size(34.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                category,
                color = colorScheme.onSurface,
                fontWeight = FontWeight.Black,
                textAlign = TextAlign.Center,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TransactionTextEditorDialog(
    title: String,
    label: String,
    initialValue: String,
    keyboardType: KeyboardType,
    isValid: (String) -> Boolean,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var value by rememberSaveable(initialValue) { mutableStateOf(initialValue) }
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(title) },
        text = {
            OutlinedTextField(
                value = value,
                onValueChange = { value = it },
                modifier = Modifier.fillMaxWidth(),
                singleLine = keyboardType != KeyboardType.Text,
                label = { Text(label) },
                keyboardOptions = KeyboardOptions(keyboardType = keyboardType)
            )
        },
        confirmButton = {
            TextButton(
                enabled = isValid(value),
                onClick = { onSave(value) }
            ) {
                Text("Save")
            }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text("Cancel") } }
    )
}

@Composable
private fun FindingSimilarOverlay() {
    val colorScheme = MaterialTheme.colorScheme
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.Center
    ) {
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(28.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                CategoryImage(
                    category = "Other",
                    size = 76,
                    background = AccentPink.copy(alpha = 0.18f)
                )
                Text("Finding similar transactions", color = colorScheme.onSurface, fontWeight = FontWeight.Black)
                Text(
                    "Checking matching merchants, amounts, and transaction type.",
                    color = colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                    fontWeight = FontWeight.SemiBold
                )
                CircularProgressIndicator(color = AccentPink)
            }
        }
    }
}

@Composable
private fun SimilarTransactionsSheet(
    editedTransaction: TransactionEntity,
    similarTransactions: List<TransactionEntity>,
    onDismiss: () -> Unit,
    onOnlyThisOne: () -> Unit,
    onApplyToSelected: (Set<Long>) -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    val rangeOptions = listOf("Last 3 months", "All", "Custom")
    var selectedRangeIndex by rememberSaveable(similarTransactions.size) { mutableIntStateOf(1) }
    var customMonth by rememberSaveable(similarTransactions.size) { mutableStateOf(YearMonth.now()) }
    var showCustomMonthPicker by rememberSaveable { mutableStateOf(false) }
    var selectedIds by remember(similarTransactions) {
        mutableStateOf(similarTransactions.map { it.id }.toSet())
    }
    var query by rememberSaveable(similarTransactions.size) { mutableStateOf("") }
    val filteredTransactions = remember(similarTransactions, query, selectedRangeIndex, customMonth) {
        filterSimilarTransactions(
            transactions = similarTransactions,
            query = query,
            rangeIndex = selectedRangeIndex,
            customMonth = customMonth
        )
    }

    LaunchedEffect(selectedRangeIndex, customMonth, similarTransactions) {
        selectedIds = filteredTransactions.map { it.id }.toSet()
    }

    if (showCustomMonthPicker) {
        MonthPickerDialog(
            initialYear = customMonth.year,
            selectedMonth = customMonth.monthValue,
            onDismiss = { showCustomMonthPicker = false },
            onMonthSelected = { year, month ->
                customMonth = YearMonth.of(year, month)
                selectedRangeIndex = 2
                showCustomMonthPicker = false
            }
        )
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.72f)),
        contentAlignment = Alignment.BottomCenter
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable(onClick = onDismiss)
        )
        Card(
            colors = CardDefaults.cardColors(containerColor = colorScheme.background),
            border = BorderStroke(1.dp, colorScheme.outline.copy(alpha = 0.45f)),
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.9f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(18.dp)
                    .navigationBarsPadding(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = if (editedTransaction.type == TransactionType.CREDIT) {
                                "Apply to Similar Income"
                            } else {
                                "Apply to Similar Expenses"
                            },
                            color = colorScheme.onSurface,
                            fontWeight = FontWeight.Black
                        )
                        Text(
                            "${filteredTransactions.size} shown (${similarTransactions.size} total) • ${selectedIds.size} selected",
                            color = colorScheme.onSurfaceVariant,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(colorScheme.surfaceVariant)
                            .clickable(onClick = onDismiss),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Rounded.Close, contentDescription = "Close", tint = colorScheme.onSurface)
                    }
                }
                SegmentedToggle(
                    options = rangeOptions,
                    selectedIndex = selectedRangeIndex,
                    onSelected = { index ->
                        selectedRangeIndex = index
                        if (index == 2) {
                            showCustomMonthPicker = true
                        }
                    }
                )
                if (selectedRangeIndex == 2) {
                    Text(
                        "Custom month: ${customMonth.format(DateTimeFormatter.ofPattern("MMM yyyy"))}",
                        color = colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.SemiBold
                    )
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SimilarSheetButton(
                        label = "Select all",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedIds = filteredTransactions.map { it.id }.toSet() }
                    )
                    SimilarSheetButton(
                        label = "Clear all",
                        modifier = Modifier.weight(1f),
                        onClick = { selectedIds = emptySet() }
                    )
                }
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surface),
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        singleLine = true,
                        label = { Text("Search any transaction") },
                        leadingIcon = { Icon(Icons.Rounded.Search, contentDescription = null) }
                    )
                }
                if (similarTransactions.isEmpty()) {
                    EmptyStateCard("No similar transactions were found for this merchant and amount.")
                } else {
                    LazyColumn(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        items(filteredTransactions, key = { it.id }) { transaction ->
                            SimilarTransactionRow(
                                transaction = transaction,
                                selected = transaction.id in selectedIds,
                                onToggle = {
                                    selectedIds = if (transaction.id in selectedIds) {
                                        selectedIds - transaction.id
                                    } else {
                                        selectedIds + transaction.id
                                    }
                                }
                            )
                        }
                    }
                }
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    SimilarSheetButton(
                        label = "Only this one",
                        modifier = Modifier.weight(1f),
                        onClick = onOnlyThisOne
                    )
                    SimilarSheetButton(
                        label = "Apply to selected (${selectedIds.size})",
                        modifier = Modifier.weight(1f),
                        enabled = selectedIds.isNotEmpty(),
                        accent = AccentPink,
                        onClick = { onApplyToSelected(selectedIds) }
                    )
                }
            }
        }
    }
}

@Composable
private fun SimilarSheetButton(
    label: String,
    modifier: Modifier,
    enabled: Boolean = true,
    accent: Color = AccentPurple,
    onClick: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (enabled) accent.copy(alpha = 0.18f) else colorScheme.surfaceVariant
        ),
        border = BorderStroke(1.dp, if (enabled) accent.copy(alpha = 0.58f) else colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(22.dp),
        modifier = modifier.clickable(enabled = enabled, onClick = onClick)
    ) {
        Text(
            label,
            color = if (enabled) colorScheme.onSurface else colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Black,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp)
        )
    }
}

@Composable
private fun SimilarTransactionRow(
    transaction: TransactionEntity,
    selected: Boolean,
    onToggle: () -> Unit
) {
    val colorScheme = MaterialTheme.colorScheme
    Card(
        colors = CardDefaults.cardColors(
            containerColor = if (selected) AccentPink.copy(alpha = 0.12f) else colorScheme.surface
        ),
        border = BorderStroke(1.dp, if (selected) AccentPink else colorScheme.outline.copy(alpha = 0.45f)),
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onToggle)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = selected,
                onCheckedChange = { onToggle() }
            )
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(6.dp)) {
                Text(
                    "${transaction.merchant} • ${formatRupees(transaction.amount)} • ${formatDate(transaction.timestamp)}",
                    color = colorScheme.onSurface,
                    fontWeight = FontWeight.Black,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Card(
                    colors = CardDefaults.cardColors(containerColor = colorScheme.surfaceVariant),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        transaction.rawSms,
                        color = colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.padding(12.dp)
                    )
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
    return CategoryCatalog.allCategories(
        customCategories = customCategories,
        transactionCategories = transactions.map { it.category },
        currentCategory = currentCategory
    )
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

private fun filterSimilarTransactions(
    transactions: List<TransactionEntity>,
    query: String,
    rangeIndex: Int,
    customMonth: YearMonth
): List<TransactionEntity> {
    val zone = ZoneId.systemDefault()
    val lastThreeMonthsStart = LocalDateTime.now()
        .minusMonths(3)
        .atZone(zone)
        .toInstant()
        .toEpochMilli()
    val term = query.trim().lowercase(Locale.ENGLISH)

    return transactions.filter { transaction ->
        val matchesRange = when (rangeIndex) {
            0 -> transaction.timestamp >= lastThreeMonthsStart
            2 -> YearMonth.from(
                Instant.ofEpochMilli(transaction.timestamp)
                    .atZone(zone)
                    .toLocalDate()
            ) == customMonth
            else -> true
        }
        val matchesQuery = term.isBlank() || listOf(
            transaction.merchant,
            transaction.bank,
            transaction.category,
            transaction.rawSms,
            formatRupees(transaction.amount)
        ).any { it.lowercase(Locale.ENGLISH).contains(term) }

        matchesRange && matchesQuery
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

private fun formatReminderTime(hour: Int, minute: Int): String =
    java.time.LocalTime.of(hour.coerceIn(0, 23), minute.coerceIn(0, 59))
        .format(DateTimeFormatter.ofPattern("hh:mm a"))

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



