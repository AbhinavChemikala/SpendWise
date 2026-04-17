import re

def patch():
    app_file = 'app/src/main/java/com/yourapp/spendwise/ui/SpendWiseApp.kt'
    with open(app_file, 'r', encoding='utf-8') as f:
        app_content = f.read()

    # Define InsightsCardId and InsightsCardLabels
    insights_card_ids = """
private object InsightsCardId {
    const val STATUS = "status"
    const val QUICK_SUMMARY = "quick_summary"
    const val FACTS = "facts"
    const val COMPARE_METRICS = "compare_metrics"
    const val INCOME_TREND = "income_trend"
    const val BUDGETS = "budgets"
    const val ANOMALY_ALERTS = "anomaly_alerts"
    const val CASHFLOW = "cashflow"
    const val SPECIAL_TRACKING = "special_tracking"
    const val BANK_SPLIT = "bank_split"
    const val SPENDING_BREAKDOWN = "spending_breakdown"
    const val TOP_CATEGORIES = "top_categories"
    const val PAYMENT_MODE = "payment_mode"
    const val MERCHANT_ANALYTICS = "merchant_analytics"
    const val RECURRING_INSIGHTS = "recurring_insights"
    const val DUPLICATE_INSIGHTS = "duplicate_insights"
    const val INCOME_VS_EXPENSE_CHART = "income_vs_expense_chart"
}

private val InsightsCardLabels = mapOf(
    InsightsCardId.STATUS to "Status",
    InsightsCardId.QUICK_SUMMARY to "Summary Ranges",
    InsightsCardId.FACTS to "Insights & Facts",
    InsightsCardId.COMPARE_METRICS to "Compare Metrics",
    InsightsCardId.INCOME_TREND to "Income Trend",
    InsightsCardId.BUDGETS to "Budgets",
    InsightsCardId.ANOMALY_ALERTS to "Anomaly Alerts",
    InsightsCardId.CASHFLOW to "Cashflow Calendar",
    InsightsCardId.SPECIAL_TRACKING to "Tracked Tags",
    InsightsCardId.BANK_SPLIT to "Bank Split",
    InsightsCardId.SPENDING_BREAKDOWN to "Spending Breakdown",
    InsightsCardId.TOP_CATEGORIES to "Top Categories",
    InsightsCardId.PAYMENT_MODE to "Payment Modes",
    InsightsCardId.MERCHANT_ANALYTICS to "Merchant Analytics",
    InsightsCardId.RECURRING_INSIGHTS to "Subscriptions",
    InsightsCardId.DUPLICATE_INSIGHTS to "Duplicate Watchlist",
    InsightsCardId.INCOME_VS_EXPENSE_CHART to "Income vs Expense Chart"
)

private val DefaultInsightsCardOrder = listOf(
    InsightsCardId.STATUS,
    InsightsCardId.QUICK_SUMMARY,
    InsightsCardId.FACTS,
    InsightsCardId.COMPARE_METRICS,
    InsightsCardId.INCOME_TREND,
    InsightsCardId.BUDGETS,
    InsightsCardId.ANOMALY_ALERTS,
    InsightsCardId.CASHFLOW,
    InsightsCardId.SPECIAL_TRACKING,
    InsightsCardId.BANK_SPLIT,
    InsightsCardId.SPENDING_BREAKDOWN,
    InsightsCardId.TOP_CATEGORIES,
    InsightsCardId.PAYMENT_MODE,
    InsightsCardId.MERCHANT_ANALYTICS,
    InsightsCardId.RECURRING_INSIGHTS,
    InsightsCardId.DUPLICATE_INSIGHTS,
    InsightsCardId.INCOME_VS_EXPENSE_CHART
)

private fun normalizeInsightsCardOrder(order: List<String>): List<String> {
    val valid = DefaultInsightsCardOrder.toSet()
    return order
        .filter { it in valid }
        .distinct() + DefaultInsightsCardOrder.filterNot { it in order }
}

private fun normalizeInsightsCardIds(ids: Set<String>): Set<String> {
    return ids
        .filter { it in DefaultInsightsCardOrder }
        .toSet()
}

@Composable
private fun EditableInsightsCard(
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
                .padding(horizontal = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = InsightsCardLabels[cardId] ?: "Unknown",
                fontWeight = FontWeight.Bold,
                color = if (isVisible) MaterialTheme.colorScheme.onBackground else Color.Gray,
                modifier = Modifier.weight(1f)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleVisibility) {
                    Icon(
                        imageVector = if (isVisible) Icons.Rounded.Visibility else Icons.Rounded.VisibilityOff,
                        contentDescription = if (isVisible) "Hide" else "Show",
                        tint = if (isVisible) AccentTeal else Color.Gray
                    )
                }
                IconButton(
                    onClick = {
                        if (index > 0) {
                            val newOrder = order.toMutableList()
                            newOrder[index] = newOrder[index - 1]
                            newOrder[index - 1] = cardId
                            onMove(newOrder)
                        }
                    },
                    enabled = index > 0
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowUp,
                        contentDescription = "Move Up",
                        tint = if (index > 0) MaterialTheme.colorScheme.onBackground else Color.Gray
                    )
                }
                IconButton(
                    onClick = {
                        if (index < order.size - 1) {
                            val newOrder = order.toMutableList()
                            newOrder[index] = newOrder[index + 1]
                            newOrder[index + 1] = cardId
                            onMove(newOrder)
                        }
                    },
                    enabled = index < order.size - 1 && index != -1
                ) {
                    Icon(
                        imageVector = Icons.Rounded.KeyboardArrowDown,
                        contentDescription = "Move Down",
                        tint = if (index < order.size - 1) MaterialTheme.colorScheme.onBackground else Color.Gray
                    )
                }
            }
        }
        Box(modifier = Modifier.alpha(if (isVisible) 1f else 0.5f)) {
            content()
        }
    }
}
"""
    if "private object InsightsCardId" not in app_content:
        # Add after HomeCardId
        app_content = app_content.replace('private val DefaultHomeCardOrder = listOf(', insights_card_ids + '\nprivate val DefaultHomeCardOrder = listOf(')

    # Now update InsightsScreen signature and body
    old_insights_screen_sig = """private fun InsightsScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onSelectSummaryRange: (SummaryRangeType) -> Unit,
    onSelectAccountFilter: (String?) -> Unit,
    onMonthClick: () -> Unit
) {"""
    new_insights_screen_sig = """private fun InsightsScreen(
    modifier: Modifier,
    uiState: DashboardUiState,
    onSelectSummaryRange: (SummaryRangeType) -> Unit,
    onSelectAccountFilter: (String?) -> Unit,
    onMonthClick: () -> Unit,
    onUpdateInsightsCardOrder: (List<String>) -> Unit,
    onToggleInsightsCardVisibility: (String) -> Unit
) {"""
    app_content = app_content.replace(old_insights_screen_sig, new_insights_screen_sig)

    old_insights_screen_top = """    var selectedTrendIndex by rememberSaveable { mutableIntStateOf(0) }

    LazyColumn("""
    new_insights_screen_top = """    var selectedTrendIndex by rememberSaveable { mutableIntStateOf(0) }
    var isEditingLayout by rememberSaveable { mutableStateOf(false) }

    val cardOrder = remember(uiState.insightsCardOrder) {
        normalizeInsightsCardOrder(uiState.insightsCardOrder)
    }
    val hiddenCardIds = remember(uiState.insightsHiddenCardIds) {
        normalizeInsightsCardIds(uiState.insightsHiddenCardIds)
    }
    val displayedCardOrder = remember(cardOrder, hiddenCardIds, isEditingLayout) {
        if (isEditingLayout) cardOrder else cardOrder.filterNot { it in hiddenCardIds }
    }

    LazyColumn("""
    
    if "var isEditingLayout by rememberSaveable" not in app_content[app_content.find("private fun InsightsScreen"):app_content.find("private fun InsightsScreen")+1000]:
        app_content = app_content.replace(old_insights_screen_top, new_insights_screen_top)

    old_screen_header = """        item {
            ScreenHeader(
                title = "Insights",
                subtitle = "This month",
                monthLabel = formatMonth(uiState.selectedYear, uiState.selectedMonth),
                onMonthClick = onMonthClick
            )
        }"""
        
    old_screen_header_with_filter = """        item {
            AccountFilterChips("""
            
    head_start = app_content.find("item {\n            ScreenHeader(", app_content.find("private fun InsightsScreen"))
    head_end = app_content.find("item {\n            AccountFilterChips", head_start)
    if head_start != -1 and head_end != -1:
        new_header = """        item {
            ScreenHeader(
                title = "Insights",
                subtitle = "This month",
                monthLabel = formatMonth(uiState.selectedYear, uiState.selectedMonth),
                onMonthClick = onMonthClick,
                trailingContent = {
                    IconButton(onClick = { isEditingLayout = !isEditingLayout }) {
                        Icon(
                            imageVector = Icons.Rounded.Edit,
                            contentDescription = if (isEditingLayout) {
                                "Done editing insights layout"
                            } else {
                                "Edit insights layout"
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
        }\n"""
        app_content = app_content[:head_start] + new_header + "        " + app_content[head_end:]

    # Now replace the items block inside InsightsScreen
    # It starts from `item { StatusCard(uiState) }` till `item {\n            IncomeVsExpenseChart`
    
    # We will use regex to replace all `item { ... }` blocks representing the cards with the reorderable block.
    # To be safe, we just find the whole segment and hard replace.
    block_start = app_content.find("item { StatusCard(uiState) }", head_start)
    block_end = app_content.find("item {\n            IncomeVsExpenseChart(trend = uiState.trend)", head_start)
    actual_end = app_content.find("}", block_end + 30) # end of IncomeVsExpenseChart item
    
    if block_start != -1 and actual_end != -1:
        new_cards_block = """
        items(displayedCardOrder, key = { it }) { cardId ->
            EditableInsightsCard(
                cardId = cardId,
                isEditing = isEditingLayout,
                isVisible = cardId !in hiddenCardIds,
                order = cardOrder,
                onMove = onUpdateInsightsCardOrder,
                onToggleVisibility = { onToggleInsightsCardVisibility(cardId) }
            ) {
                when (cardId) {
                    InsightsCardId.STATUS -> StatusCard(uiState)
                    InsightsCardId.QUICK_SUMMARY -> SummaryRangeCard(
                        selected = uiState.selectedSummaryRange,
                        ranges = uiState.rangeSummaries,
                        onSelect = onSelectSummaryRange
                    )
                    InsightsCardId.FACTS -> FactsCard(uiState.insightFacts)
                    InsightsCardId.COMPARE_METRICS -> CompareMetricsCard(uiState.compareMetrics, uiState.totalSpent, uiState.totalReceived)
                    InsightsCardId.INCOME_TREND -> IncomeTrendCard(uiState.incomeTrend)
                    InsightsCardId.BUDGETS -> BudgetProgressCard(uiState.budgetProgress)
                    InsightsCardId.ANOMALY_ALERTS -> AnomalyAlertsCard(uiState.anomalyAlerts)
                    InsightsCardId.CASHFLOW -> CashflowCard(uiState.cashflowDays)
                    InsightsCardId.SPECIAL_TRACKING -> SpecialTrackingCard(uiState)
                    InsightsCardId.BANK_SPLIT -> BankSplitCard(uiState.bankSplit)
                    InsightsCardId.SPENDING_BREAKDOWN -> SpendingBreakdownCard(
                        totalSpent = uiState.totalSpent,
                        topCategories = uiState.topCategories
                    )
                    InsightsCardId.TOP_CATEGORIES -> TopCategoriesCard(uiState.topCategories, uiState.totalSpent)
                    InsightsCardId.PAYMENT_MODE -> PaymentModeCard(uiState.paymentRails, uiState.totalSpent)
                    InsightsCardId.MERCHANT_ANALYTICS -> MerchantAnalyticsCard(uiState.topMerchants)
                    InsightsCardId.RECURRING_INSIGHTS -> RecurringInsightsCard(uiState.recurringInsights, title = "Subscriptions & Recurring")
                    InsightsCardId.DUPLICATE_INSIGHTS -> DuplicateInsightsCard(uiState.duplicateInsights)
                    InsightsCardId.INCOME_VS_EXPENSE_CHART -> IncomeVsExpenseChart(trend = uiState.trend)
                }
            }
        }
"""
        app_content = app_content[:block_start] + new_cards_block.strip() + "\n" + app_content[actual_end + 1:]

    # Update callsite format
    old_callsite = """
                InsightsScreen(
                    modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                    uiState = uiState,
                    onSelectSummaryRange = { vm.changeSummaryRange(it) },
                    onSelectAccountFilter = { vm.changeAccountFilter(it) },
                    onMonthClick = { showMonthPicker = true }
                )"""
    new_callsite = """
                InsightsScreen(
                    modifier = Modifier.padding(innerPadding).consumeWindowInsets(innerPadding),
                    uiState = uiState,
                    onSelectSummaryRange = { vm.changeSummaryRange(it) },
                    onSelectAccountFilter = { vm.changeAccountFilter(it) },
                    onMonthClick = { showMonthPicker = true },
                    onUpdateInsightsCardOrder = { vm.updateInsightsCardOrder(it) },
                    onToggleInsightsCardVisibility = { vm.toggleInsightsCardVisibility(it) }
                )"""
    app_content = app_content.replace(old_callsite, new_callsite)

    with open(app_file, 'w', encoding='utf-8') as f:
        f.write(app_content)

patch()
