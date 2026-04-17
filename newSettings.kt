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
    onToggleLegacyThemes: (Boolean) -> Unit,
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
    onMergeAccountLabels: (Set<String>, String) -> Unit,
    onPhoneChange: (String) -> Unit,
    onSimulateTemplate: (DebugSmsTemplate) -> Unit,
    onSendTemplate: (DebugSmsTemplate) -> Unit
) {
    val context = androidx.compose.ui.platform.LocalContext.current
    var showCategoryDialog by rememberSaveable { mutableStateOf(false) }
    var editingRule by remember { mutableStateOf<TransactionRule?>(null) }
    var showBudgetDialog by rememberSaveable { mutableStateOf(false) }
    var showEmailSyncHistory by rememberSaveable { mutableStateOf(false) }
    var showBackupHistory by rememberSaveable { mutableStateOf(false) }
    var showAccountMergeDialog by rememberSaveable { mutableStateOf(false) }
    var showLocalRestoreConfirm by rememberSaveable { mutableStateOf(false) }
    var showDriveRestoreConfirm by rememberSaveable { mutableStateOf(false) }
    
    // Expandable states
    var showReviewCenter by rememberSaveable { mutableStateOf(false) }
    var showSpamInbox by rememberSaveable { mutableStateOf(false) }
    var showDebugConsole by rememberSaveable { mutableStateOf(false) }
    var showSourceExplorer by rememberSaveable { mutableStateOf(false) }

    var expandedTheme by rememberSaveable { mutableStateOf(false) }
    var expandedAiKey by rememberSaveable { mutableStateOf(false) }
    var expandedDrive by rememberSaveable { mutableStateOf(false) }
    var expandedAxis by rememberSaveable { mutableStateOf(false) }
    var expandedDailyReminder by rememberSaveable { mutableStateOf(false) }
    
    var keyText by remember { mutableStateOf(uiState.cloudAiApiKey) }
    var showKey by remember { mutableStateOf(false) }

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
            onSave = { onAddCategory(it); showCategoryDialog = false }
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

    if (showAccountMergeDialog) {
        AccountMergeDialog(
            accounts = uiState.accountSummaries,
            onDismiss = { showAccountMergeDialog = false },
            onSave = { sourceKeys, targetLabel ->
                onMergeAccountLabels(sourceKeys, targetLabel)
                showAccountMergeDialog = false
            }
        )
    }

    if (showLocalRestoreConfirm) {
        RestoreBackupConfirmDialog(
            source = "a local backup file",
            onDismiss = { showLocalRestoreConfirm = false },
            onConfirm = { showLocalRestoreConfirm = false; onRestoreLocalBackup() }
        )
    }

    if (showDriveRestoreConfirm) {
        RestoreBackupConfirmDialog(
            source = "Google Drive",
            onDismiss = { showDriveRestoreConfirm = false },
            onConfirm = { showDriveRestoreConfirm = false; onRestoreBackupFromDrive() }
        )
    }

    editingRule?.let { rule ->
        RuleEditorDialog(
            initialRule = rule,
            onDismiss = { editingRule = null },
            onSave = { onSaveRule(it); editingRule = null }
        )
    }

    if (showBudgetDialog) {
        BudgetEditorDialog(
            availableCategories = availableCategories(uiState.customCategories, uiState.transactions),
            onDismiss = { showBudgetDialog = false },
            onSave = { category, amount -> onSaveBudget(category, amount); showBudgetDialog = false }
        )
    }

    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Text(
                "Settings",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(bottom = 8.dp)
            )
        }

        // ── PROFILE ──────────────────────────────────────────────────
        item { SettingsSectionHeader("PROFILE") }
        item {
            val account = com.google.android.gms.auth.api.signin.GoogleSignIn.getLastSignedInAccount(context)
            val displayName = account?.displayName ?: "SpendWise User"
            val displayEmail = account?.email ?: "Tap to connect account"
            val initial = displayName.firstOrNull()?.uppercase() ?: "S"

            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier.clickable {
                    if (account == null) onConnectDriveBackup()
                }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(48.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Rounded.AccountBalanceWallet,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Spacer(modifier = Modifier.width(16.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(displayName, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                        Text(displayEmail, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodyMedium)
                    }
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(AccentPurple), 
                        contentAlignment = Alignment.Center
                    ) {
                        Text(initial, color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }

        // ── PREFERENCES ──────────────────────────────────────────────
        item { SettingsSectionHeader("PREFERENCES") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Psychology,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Dark Mode",
                        subtitle = "Currently ${if (uiState.themeMode.contains("dark", true)) "dark" else "light"}",
                        checked = uiState.themeMode.contains("dark", true),
                        onCheckedChange = { isDark ->
                            onSetThemeMode(if (isDark) THEME_MODE_DARK else THEME_MODE_LIGHT)
                        }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Search,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Currency",
                        subtitle = "₹ Indian Rupee (INR)",
                        onClick = { /* mock */ }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Search,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Notifications",
                        subtitle = "Push & email alerts",
                        onClick = { expandedDailyReminder = !expandedDailyReminder }
                    )
                    if (expandedDailyReminder) {
                        Box(modifier = Modifier.padding(16.dp)) {
                            DailyReminderCard(
                                enabled = uiState.dailyReminderEnabled,
                                hour = uiState.dailyReminderHour,
                                minute = uiState.dailyReminderMinute,
                                onToggle = onToggleDailyReminder,
                                onTimeChange = onSetDailyReminderTime
                            )
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Edit,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "App Theme Engine",
                        subtitle = "Change base theme mode and legacy options",
                        onClick = { expandedTheme = !expandedTheme }
                    )
                    if (expandedTheme) {
                        Column(modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 16.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                                Text("Legacy Themes", style = MaterialTheme.typography.bodySmall)
                                Switch(checked = uiState.legacyThemesEnabled, onCheckedChange = onToggleLegacyThemes, modifier = Modifier.scale(0.8f))
                            }
                            FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                baseThemeOptions.forEach { (mode, label) ->
                                    FilterChip(selected = uiState.themeMode == mode, onClick = { onSetThemeMode(mode) }, label = { Text(label) })
                                }
                            }
                            if (uiState.legacyThemesEnabled) {
                                FlowRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    darkThemeOptions.forEach { (mode, label) ->
                                        FilterChip(selected = uiState.themeMode == mode, onClick = { onSetThemeMode(mode) }, label = { Text(label) })
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── AI CONFIGURATION ─────────────────────────────────────────
        item { SettingsSectionHeader("AI CONFIGURATION") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsArrowRow(
                        icon = Icons.Rounded.Psychology,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "AI Model",
                        subtitle = "Gemma 3 27B (Primary) + Nano (Fallback)",
                        onClick = { onToggleCloudAi(!uiState.isCloudAiEnabled) }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Psychology,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "API Key",
                        subtitle = if (uiState.cloudAiApiKey.isBlank()) "Not configured" else "****" + uiState.cloudAiApiKey.takeLast(4),
                        onClick = { expandedAiKey = !expandedAiKey }
                    )
                    if (expandedAiKey) {
                        Column(modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 16.dp)) {
                            OutlinedTextField(
                                value = keyText,
                                onValueChange = { keyText = it },
                                modifier = Modifier.fillMaxWidth(),
                                placeholder = { Text("AIza...") },
                                singleLine = true,
                                trailingIcon = { IconButton(onClick = { showKey = !showKey }) { Icon(if (showKey) Icons.Rounded.VisibilityOff else Icons.Rounded.Visibility, null) } },
                                visualTransformation = if (showKey) VisualTransformation.None else PasswordVisualTransformation()
                            )
                            Button(onClick = { onUpdateCloudAiApiKey(keyText); expandedAiKey = false }, modifier = Modifier.align(Alignment.End).padding(top = 8.dp)) { Text("Save Key") }
                        }
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Replay,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Legacy Re-scan",
                        subtitle = if (uiState.isScanningLegacyAiFailures) "Scanning..." else "Recover failed parses",
                        onClick = { if (!uiState.isScanningLegacyAiFailures) onRecoverLegacyAiFailures() }
                    )
                }
            }
        }

        // ── ACCOUNTS ─────────────────────────────────────────────────
        item { SettingsSectionHeader("ACCOUNTS") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsArrowRow(
                        icon = Icons.Rounded.AccountBalanceWallet,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Linked Accounts",
                        subtitle = "${uiState.accountSummaries.size} active accounts",
                        onClick = { showAccountMergeDialog = true }
                    )
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.ChatBubbleOutline,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Axis Email Check",
                        subtitle = if (uiState.axisEmailAccount.isBlank()) "Not connected" else "Checking via ${uiState.axisEmailAccount}",
                        onClick = { expandedAxis = !expandedAxis }
                    )
                    if (expandedAxis) {
                        Column(modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 16.dp)) {
                            if (uiState.axisEmailAccount.isBlank()) {
                                Button(onClick = onConnectAxisEmail) { Text("Connect Gmail") }
                            } else {
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Auto-check", style = MaterialTheme.typography.bodySmall)
                                    Switch(checked = uiState.axisEmailAutoSyncEnabled, onCheckedChange = onToggleAxisEmailAutoSync, modifier = Modifier.scale(0.8f))
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    Text("Spark App Trigger", style = MaterialTheme.typography.bodySmall)
                                    Switch(checked = uiState.sparkMailTriggerEnabled, onCheckedChange = onToggleSparkMailTrigger, modifier = Modifier.scale(0.8f))
                                }
                                if (uiState.sparkMailTriggerEnabled && !uiState.hasSparkNotificationAccess) {
                                    TextButton(onClick = onOpenSparkNotificationAccess) { Text("Grant Notification Access") }
                                }
                                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                                    TextButton(onClick = { showEmailSyncHistory = true }) { Text("Sync History") }
                                    Row {
                                        TextButton(onClick = onSyncAxisEmails, enabled = !uiState.isAxisEmailSyncing) { Text(if (uiState.isAxisEmailSyncing) "Syncing" else "Sync now") }
                                        TextButton(onClick = onDisconnectAxisEmail) { Text("Disconnect") }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        // ── ORGANIZATION & RULES ─────────────────────────────────────
        item { SettingsSectionHeader("ORGANIZATION & RULES") }
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

        // ── DATA & SYSTEM ────────────────────────────────────────────
        item { SettingsSectionHeader("DATA & SYSTEM") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsArrowRow(
                        icon = Icons.Rounded.Search,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Backups",
                        subtitle = "Drive & Local storage",
                        onClick = { expandedDrive = !expandedDrive }
                    )
                    if (expandedDrive) {
                        Box(modifier = Modifier.padding(16.dp)) {
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
                    }
                    HorizontalDivider(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.05f))
                    SettingsArrowRow(
                        icon = Icons.Rounded.Search,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Import SMS",
                        subtitle = if (uiState.isImportingSms) "Scanning..." else "Scan inbox historically",
                        onClick = onScanExistingSms
                    )
                }
            }
        }

        // ── DEVELOPER OPTIONS ────────────────────────────────────────
        item { SettingsSectionHeader("DEVELOPER OPTIONS") }
        item {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(16.dp)
            ) {
                Column {
                    SettingsSwitchRow(
                        icon = Icons.Rounded.Edit,
                        iconBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.15f),
                        iconTint = MaterialTheme.colorScheme.primary,
                        title = "Debug Mode",
                        subtitle = "Enable template tools",
                        checked = uiState.debugModeEnabled,
                        onCheckedChange = onToggleDebug
                    )
                    if (uiState.debugModeEnabled) {
                        Column(modifier = Modifier.padding(start = 72.dp, end = 16.dp, bottom = 16.dp)) {
                            OutlinedTextField(
                                value = uiState.debugPhoneNumber,
                                onValueChange = onPhoneChange,
                                modifier = Modifier.fillMaxWidth(),
                                singleLine = true,
                                label = { Text("Phone number for tests") }
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
        item {
            ExpandableReviewCard(title = "Review center", subtitle = "See queued AI-reviewed items.", items = uiState.reviewCenterItems, expanded = showReviewCenter, onToggle = { showReviewCenter = !showReviewCenter })
        }
        item {
            ExpandableReviewCard(title = "Spam inbox", subtitle = "Suspicious or rejected SMS.", items = uiState.spamInboxItems, expanded = showSpamInbox, onToggle = { showSpamInbox = !showSpamInbox })
        }
        item {
            ExpandableReviewCard(title = "Debug console", subtitle = "Regex, pre-filter decisions.", items = uiState.debugConsoleItems, expanded = showDebugConsole, onToggle = { showDebugConsole = !showDebugConsole })
        }
        item {
            ExpandableReviewCard(title = "SMS source explorer", subtitle = "Imported SMS details.", items = uiState.importSourceItems, expanded = showSourceExplorer, onToggle = { showSourceExplorer = !showSourceExplorer })
        }
    }
}

@Composable
private fun SettingsSectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        modifier = Modifier.padding(start = 16.dp, top = 8.dp, bottom = 4.dp)
    )
}

@Composable
private fun SettingsArrowRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Icon(
            Icons.AutoMirrored.Rounded.KeyboardArrowRight,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SettingsSwitchRow(
    icon: ImageVector,
    iconBg: Color,
    iconTint: Color,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 14.dp, horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(iconBg),
            contentAlignment = Alignment.Center
        ) {
            Icon(icon, contentDescription = null, tint = iconTint, modifier = Modifier.size(20.dp))
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(title, fontWeight = FontWeight.SemiBold, style = MaterialTheme.typography.bodyLarge, color = MaterialTheme.colorScheme.onSurface)
            Text(subtitle, color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}
