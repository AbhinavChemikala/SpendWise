package com.yourapp.spendwise.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.yourapp.spendwise.backup.BackupHistoryEntry
import com.yourapp.spendwise.backup.BackupSettings
import com.yourapp.spendwise.mail.AxisEmailSyncHistoryEntry

class SettingsStore(context: Context) {
    private val prefs = context.applicationContext.getSharedPreferences(
        PREFS_NAME,
        Context.MODE_PRIVATE
    )
    private val gson = Gson()

    fun isDebugModeEnabled(): Boolean = prefs.getBoolean(KEY_DEBUG_MODE, false)

    fun setDebugModeEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DEBUG_MODE, enabled).apply()
    }

    fun getDebugPhoneNumber(): String = prefs.getString(KEY_DEBUG_PHONE, "").orEmpty()

    fun setDebugPhoneNumber(phoneNumber: String) {
        prefs.edit().putString(KEY_DEBUG_PHONE, phoneNumber.trim()).apply()
    }

    fun isAiReviewEnabled(): Boolean = prefs.getBoolean(KEY_AI_REVIEW, true)

    fun setAiReviewEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AI_REVIEW, enabled).apply()
    }

    fun isCloudAiEnabled(): Boolean = prefs.getBoolean(KEY_CLOUD_AI_ENABLED, false)

    fun setCloudAiEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_CLOUD_AI_ENABLED, enabled).apply()
    }

    fun getCloudAiApiKey(): String = prefs.getString(KEY_CLOUD_AI_API_KEY, "").orEmpty()

    fun setCloudAiApiKey(key: String) {
        prefs.edit().putString(KEY_CLOUD_AI_API_KEY, key.trim()).apply()
    }

    fun getAxisEmailAccount(): String = prefs.getString(KEY_AXIS_EMAIL_ACCOUNT, "").orEmpty()

    fun setAxisEmailAccount(email: String) {
        prefs.edit().putString(KEY_AXIS_EMAIL_ACCOUNT, email.trim()).apply()
    }

    fun clearAxisEmailAccount() {
        prefs.edit()
            .remove(KEY_AXIS_EMAIL_ACCOUNT)
            .remove(KEY_AXIS_EMAIL_LAST_SYNC_MS)
            .remove(KEY_AXIS_EMAIL_MESSAGE_IDS)
            .apply()
    }

    fun isAxisEmailAutoSyncEnabled(): Boolean = prefs.getBoolean(KEY_AXIS_EMAIL_AUTO_SYNC, true)

    fun setAxisEmailAutoSyncEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_AXIS_EMAIL_AUTO_SYNC, enabled).apply()
    }

    fun getAxisEmailLastSyncMs(): Long = prefs.getLong(KEY_AXIS_EMAIL_LAST_SYNC_MS, 0L)

    fun setAxisEmailLastSyncMs(timestampMs: Long) {
        prefs.edit().putLong(KEY_AXIS_EMAIL_LAST_SYNC_MS, timestampMs.coerceAtLeast(0L)).apply()
    }

    fun getAxisEmailRecentMessageIds(): List<String> {
        return readList<String>(KEY_AXIS_EMAIL_MESSAGE_IDS)
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinct()
    }

    fun setAxisEmailRecentMessageIds(messageIds: List<String>) {
        writeList(KEY_AXIS_EMAIL_MESSAGE_IDS, messageIds.filter { it.isNotBlank() }.distinct().takeLast(200))
    }

    fun getAxisEmailSyncHistory(): List<AxisEmailSyncHistoryEntry> {
        return readList<AxisEmailSyncHistoryEntry>(KEY_AXIS_EMAIL_SYNC_HISTORY)
            .sortedByDescending { it.startedAt }
    }

    fun appendAxisEmailSyncHistory(entry: AxisEmailSyncHistoryEntry) {
        val trimmedItems = entry.items
            .take(8)
            .map { item ->
                item.copy(
                    summary = item.summary.take(180),
                    cleanedBody = item.cleanedBody.take(1200),
                    fullBody = item.fullBody.take(4000)
                )
            }
        val updated = buildList {
            add(entry.copy(items = trimmedItems))
            addAll(getAxisEmailSyncHistory())
        }.distinctBy { it.id }
            .take(40)
        writeList(KEY_AXIS_EMAIL_SYNC_HISTORY, updated)
    }

    fun isSparkMailTriggerEnabled(): Boolean = prefs.getBoolean(KEY_SPARK_MAIL_TRIGGER_ENABLED, false)

    fun setSparkMailTriggerEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SPARK_MAIL_TRIGGER_ENABLED, enabled).apply()
    }

    fun getSparkMailTriggerLastSyncMs(): Long = prefs.getLong(KEY_SPARK_MAIL_TRIGGER_LAST_SYNC_MS, 0L)

    fun setSparkMailTriggerLastSyncMs(timestampMs: Long) {
        prefs.edit().putLong(KEY_SPARK_MAIL_TRIGGER_LAST_SYNC_MS, timestampMs.coerceAtLeast(0L)).apply()
    }

    /** "system", "light", or one of the dark theme mode strings. */
    fun getThemeMode(): String = prefs.getString(KEY_THEME_MODE, "system").orEmpty().ifBlank { "system" }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
    }

    fun isLegacyThemesEnabled(): Boolean = prefs.getBoolean(KEY_LEGACY_THEMES_ENABLED, false)

    fun setLegacyThemesEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_LEGACY_THEMES_ENABLED, enabled).apply()
    }

    fun isDailyReminderEnabled(): Boolean = prefs.getBoolean(KEY_DAILY_REMINDER_ENABLED, true)

    fun setDailyReminderEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DAILY_REMINDER_ENABLED, enabled).apply()
    }

    fun getDailyReminderHour(): Int = prefs.getInt(KEY_DAILY_REMINDER_HOUR, 22).coerceIn(0, 23)

    fun getDailyReminderMinute(): Int = prefs.getInt(KEY_DAILY_REMINDER_MINUTE, 0).coerceIn(0, 59)

    fun setDailyReminderTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_DAILY_REMINDER_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_DAILY_REMINDER_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun getDriveBackupAccount(): String = prefs.getString(KEY_DRIVE_BACKUP_ACCOUNT, "").orEmpty()

    fun setDriveBackupAccount(email: String) {
        prefs.edit().putString(KEY_DRIVE_BACKUP_ACCOUNT, email.trim()).apply()
    }

    fun clearDriveBackupAccount() {
        prefs.edit()
            .remove(KEY_DRIVE_BACKUP_ACCOUNT)
            .putBoolean(KEY_DRIVE_BACKUP_AUTO_ENABLED, false)
            .apply()
    }

    fun isDriveBackupAutoEnabled(): Boolean = prefs.getBoolean(KEY_DRIVE_BACKUP_AUTO_ENABLED, false)

    fun setDriveBackupAutoEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_DRIVE_BACKUP_AUTO_ENABLED, enabled).apply()
    }

    fun getDriveBackupHour(): Int = prefs.getInt(KEY_DRIVE_BACKUP_HOUR, 2).coerceIn(0, 23)

    fun getDriveBackupMinute(): Int = prefs.getInt(KEY_DRIVE_BACKUP_MINUTE, 0).coerceIn(0, 59)

    fun setDriveBackupTime(hour: Int, minute: Int) {
        prefs.edit()
            .putInt(KEY_DRIVE_BACKUP_HOUR, hour.coerceIn(0, 23))
            .putInt(KEY_DRIVE_BACKUP_MINUTE, minute.coerceIn(0, 59))
            .apply()
    }

    fun getBackupHistory(): List<BackupHistoryEntry> {
        return readList<BackupHistoryEntry>(KEY_BACKUP_HISTORY)
            .sortedByDescending { it.timestamp }
    }

    fun appendBackupHistory(entry: BackupHistoryEntry) {
        val updated = buildList {
            add(entry.copy(message = entry.message.take(220)))
            addAll(getBackupHistory())
        }.distinctBy { it.id }
            .take(60)
        writeList(KEY_BACKUP_HISTORY, updated)
    }

    fun getHomeCardOrder(): List<String> {
        val saved = readList<String>(KEY_HOME_CARD_ORDER)
        return normalizeHomeCardOrder(saved)
    }

    fun setHomeCardOrder(order: List<String>) {
        writeList(KEY_HOME_CARD_ORDER, normalizeHomeCardOrder(order))
    }

    fun resetHomeCardOrder() {
        prefs.edit().remove(KEY_HOME_CARD_ORDER).apply()
    }

    fun getHiddenHomeCardIds(): Set<String> {
        return normalizeHomeCardIds(readList<String>(KEY_HOME_HIDDEN_CARD_IDS)).toSet()
    }

    fun setHiddenHomeCardIds(hiddenCardIds: Set<String>) {
        writeList(KEY_HOME_HIDDEN_CARD_IDS, normalizeHomeCardIds(hiddenCardIds.toList()))
    }
    fun getInsightsCardOrder(): List<String> {
        val saved = readList<String>(KEY_INSIGHTS_CARD_ORDER)
        return normalizeInsightsCardOrder(saved)
    }

    fun setInsightsCardOrder(order: List<String>) {
        writeList(KEY_INSIGHTS_CARD_ORDER, normalizeInsightsCardOrder(order))
    }

    fun getHiddenInsightsCardIds(): Set<String> {
        return normalizeInsightsCardIds(readList<String>(KEY_INSIGHTS_HIDDEN_CARD_IDS)).toSet()
    }

    fun setHiddenInsightsCardIds(hiddenCardIds: Set<String>) {
        writeList(KEY_INSIGHTS_HIDDEN_CARD_IDS, normalizeInsightsCardIds(hiddenCardIds.toList()))
    }

    fun getCustomCategories(): List<CustomCategory> {
        return readList<CustomCategory>(KEY_CUSTOM_CATEGORIES)
            .mapNotNull { item ->
                item.name.trim().takeIf { it.isNotBlank() }?.let { item.copy(name = it) }
            }
            .distinctBy { it.name.lowercase() }
            .sortedBy { it.name.lowercase() }
    }

    fun addCustomCategory(name: String) {
        val normalized = name.trim()
        if (normalized.isBlank()) return
        val updated = getCustomCategories().toMutableList()
        if (updated.any { it.name.equals(normalized, ignoreCase = true) }) return
        updated.add(CustomCategory(name = normalized))
        writeList(KEY_CUSTOM_CATEGORIES, updated.sortedBy { it.name.lowercase() })
    }

    fun removeCustomCategory(categoryId: String) {
        writeList(
            KEY_CUSTOM_CATEGORIES,
            getCustomCategories().filterNot { it.id == categoryId }
        )
    }

    fun getRules(): List<TransactionRule> {
        return readList<TransactionRule>(KEY_TRANSACTION_RULES)
            .filter { rule ->
                rule.name.isNotBlank() || rule.senderContains.isNotBlank() ||
                    rule.merchantContains.isNotBlank() || rule.smsContains.isNotBlank()
            }
    }

    fun getBudgetGoals(): List<BudgetGoal> {
        return readList<BudgetGoal>(KEY_BUDGET_GOALS)
            .filter { it.category.isNotBlank() && it.monthlyLimit > 0.0 }
            .sortedBy { it.category.lowercase() }
    }

    fun saveBudgetGoal(goal: BudgetGoal) {
        val goals = getBudgetGoals().toMutableList()
        val index = goals.indexOfFirst { it.id == goal.id || it.category.equals(goal.category, ignoreCase = true) }
        if (index >= 0) {
            goals[index] = goal
        } else {
            goals.add(goal)
        }
        writeList(KEY_BUDGET_GOALS, goals)
    }

    fun deleteBudgetGoal(goalId: String) {
        writeList(
            KEY_BUDGET_GOALS,
            getBudgetGoals().filterNot { it.id == goalId }
        )
    }

    fun saveRule(rule: TransactionRule) {
        val rules = getRules().toMutableList()
        val index = rules.indexOfFirst { it.id == rule.id }
        if (index >= 0) {
            rules[index] = rule
        } else {
            rules.add(rule)
        }
        writeList(KEY_TRANSACTION_RULES, rules)
    }

    fun deleteRule(ruleId: String) {
        writeList(
            KEY_TRANSACTION_RULES,
            getRules().filterNot { it.id == ruleId }
        )
    }

    fun exportBackupSettings(): BackupSettings {
        return BackupSettings(
            debugModeEnabled = isDebugModeEnabled(),
            debugPhoneNumber = getDebugPhoneNumber(),
            aiReviewEnabled = isAiReviewEnabled(),
            cloudAiEnabled = isCloudAiEnabled(),
            axisEmailAccount = getAxisEmailAccount(),
            axisEmailAutoSyncEnabled = isAxisEmailAutoSyncEnabled(),
            sparkMailTriggerEnabled = isSparkMailTriggerEnabled(),
            themeMode = getThemeMode(),
            dailyReminderEnabled = isDailyReminderEnabled(),
            dailyReminderHour = getDailyReminderHour(),
            dailyReminderMinute = getDailyReminderMinute(),
            homeCardOrder = getHomeCardOrder(),
            hiddenHomeCardIds = getHiddenHomeCardIds().toList(),
            customCategories = getCustomCategories(),
            transactionRules = getRules(),
            budgetGoals = getBudgetGoals()
        )
    }

    fun applyBackupSettings(settings: BackupSettings) {
        prefs.edit()
            .putBoolean(KEY_DEBUG_MODE, settings.debugModeEnabled)
            .putString(KEY_DEBUG_PHONE, settings.debugPhoneNumber)
            .putBoolean(KEY_AI_REVIEW, settings.aiReviewEnabled)
            .putBoolean(KEY_CLOUD_AI_ENABLED, settings.cloudAiEnabled)
            .putString(KEY_AXIS_EMAIL_ACCOUNT, settings.axisEmailAccount)
            .putBoolean(KEY_AXIS_EMAIL_AUTO_SYNC, settings.axisEmailAutoSyncEnabled)
            .putBoolean(KEY_SPARK_MAIL_TRIGGER_ENABLED, settings.sparkMailTriggerEnabled)
            .putString(KEY_THEME_MODE, settings.themeMode.ifBlank { "system" })
            .putBoolean(KEY_DAILY_REMINDER_ENABLED, settings.dailyReminderEnabled)
            .putInt(KEY_DAILY_REMINDER_HOUR, settings.dailyReminderHour.coerceIn(0, 23))
            .putInt(KEY_DAILY_REMINDER_MINUTE, settings.dailyReminderMinute.coerceIn(0, 59))
            .apply()
        setHomeCardOrder(settings.homeCardOrder)
        setHiddenHomeCardIds(settings.hiddenHomeCardIds.toSet())
        writeList(KEY_CUSTOM_CATEGORIES, settings.customCategories)
        writeList(KEY_TRANSACTION_RULES, settings.transactionRules)
        writeList(KEY_BUDGET_GOALS, settings.budgetGoals)
    }

    private inline fun <reified T> readList(key: String): List<T> {
        val raw = prefs.getString(key, null) ?: return emptyList()
        val type = object : TypeToken<List<T>>() {}.type
        return runCatching { gson.fromJson<List<T>>(raw, type).orEmpty() }
            .getOrDefault(emptyList())
    }

    private fun <T> writeList(key: String, items: List<T>) {
        prefs.edit().putString(key, gson.toJson(items)).apply()
    }

    private fun normalizeHomeCardOrder(order: List<String>): List<String> {
        val valid = DEFAULT_HOME_CARD_ORDER.toSet()
        return order
            .filter { it in valid }
            .distinct() + DEFAULT_HOME_CARD_ORDER.filterNot { it in order }
    }

    private fun normalizeHomeCardIds(ids: List<String>): List<String> {
        val valid = DEFAULT_HOME_CARD_ORDER.toSet()
        return ids
            .filter { it in valid }
            .distinct()
    }
    private fun normalizeInsightsCardOrder(order: List<String>): List<String> {
        val valid = DEFAULT_INSIGHTS_CARD_ORDER.toSet()
        return order
            .filter { it in valid }
            .distinct() + DEFAULT_INSIGHTS_CARD_ORDER.filterNot { it in order }
    }

    private fun normalizeInsightsCardIds(ids: List<String>): List<String> {
        val valid = DEFAULT_INSIGHTS_CARD_ORDER.toSet()
        return ids
            .filter { it in valid }
            .distinct()
    }

    companion object {
        private const val PREFS_NAME = "spendwise_settings"
        private const val KEY_DEBUG_MODE = "debug_mode_enabled"
        private const val KEY_DEBUG_PHONE = "debug_phone_number"
        private const val KEY_AI_REVIEW = "ai_review_enabled"
        private const val KEY_CUSTOM_CATEGORIES = "custom_categories"
        private const val KEY_TRANSACTION_RULES = "transaction_rules"
        private const val KEY_BUDGET_GOALS = "budget_goals"
        private const val KEY_CLOUD_AI_ENABLED = "cloud_ai_enabled"
        private const val KEY_CLOUD_AI_API_KEY = "cloud_ai_api_key"
        private const val KEY_AXIS_EMAIL_ACCOUNT = "axis_email_account"
        private const val KEY_AXIS_EMAIL_AUTO_SYNC = "axis_email_auto_sync"
        private const val KEY_AXIS_EMAIL_LAST_SYNC_MS = "axis_email_last_sync_ms"
        private const val KEY_AXIS_EMAIL_MESSAGE_IDS = "axis_email_message_ids"
        private const val KEY_AXIS_EMAIL_SYNC_HISTORY = "axis_email_sync_history"
        private const val KEY_SPARK_MAIL_TRIGGER_ENABLED = "spark_mail_trigger_enabled"
        private const val KEY_SPARK_MAIL_TRIGGER_LAST_SYNC_MS = "spark_mail_trigger_last_sync_ms"
        private const val KEY_THEME_MODE = "theme_mode"
        private const val KEY_LEGACY_THEMES_ENABLED = "legacy_themes_enabled"
        private const val KEY_DAILY_REMINDER_ENABLED = "daily_reminder_enabled"
        private const val KEY_DAILY_REMINDER_HOUR = "daily_reminder_hour"
        private const val KEY_DAILY_REMINDER_MINUTE = "daily_reminder_minute"
        private const val KEY_DRIVE_BACKUP_ACCOUNT = "drive_backup_account"
        private const val KEY_DRIVE_BACKUP_AUTO_ENABLED = "drive_backup_auto_enabled"
        private const val KEY_DRIVE_BACKUP_HOUR = "drive_backup_hour"
        private const val KEY_DRIVE_BACKUP_MINUTE = "drive_backup_minute"
        private const val KEY_BACKUP_HISTORY = "backup_history"
        private const val KEY_HOME_CARD_ORDER = "home_card_order"
        private const val KEY_HOME_HIDDEN_CARD_IDS = "home_hidden_card_ids"
        private const val KEY_INSIGHTS_CARD_ORDER = "insights_card_order"
        private const val KEY_INSIGHTS_HIDDEN_CARD_IDS = "insights_hidden_card_ids"

        private val DEFAULT_HOME_CARD_ORDER = listOf(
            "status",
            "quick_summary",
            "accounts",
            "hero_summary",
            "savings_score",
            "budgets",
            "anomaly_alerts",
            "cashflow",
            "insights_preview",
            "recent_transactions"
        )

        private val DEFAULT_INSIGHTS_CARD_ORDER = listOf(
            "status",
            "quick_summary",
            "facts",
            "compare_metrics",
            "income_trend",
            "budgets",
            "anomaly_alerts",
            "cashflow",
            "special_tracking",
            "bank_split",
            "spending_breakdown",
            "top_categories",
            "payment_mode",
            "merchant_analytics",
            "recurring_insights",
            "duplicate_insights",
            "income_vs_expense_chart"
        )
    }
}
