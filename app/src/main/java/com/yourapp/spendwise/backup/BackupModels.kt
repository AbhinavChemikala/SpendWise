package com.yourapp.spendwise.backup

import com.yourapp.spendwise.data.BudgetGoal
import com.yourapp.spendwise.data.CustomCategory
import com.yourapp.spendwise.data.TransactionRule
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.SmsReviewEntity
import com.yourapp.spendwise.data.db.TransactionCategoryAiEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import java.util.UUID

data class SpendWiseBackupFile(
    val schemaVersion: Int = 1,
    val createdAt: Long = System.currentTimeMillis(),
    val appPackage: String = "com.yourapp.spendwise",
    val transactions: List<TransactionEntity> = emptyList(),
    val smsReviewEvents: List<SmsReviewEntity> = emptyList(),
    val pendingSms: List<PendingSmsEntity> = emptyList(),
    val categoryAiRecords: List<TransactionCategoryAiEntity> = emptyList(),
    val settings: BackupSettings = BackupSettings()
)

data class BackupSettings(
    val debugModeEnabled: Boolean = false,
    val debugPhoneNumber: String = "",
    val aiReviewEnabled: Boolean = true,
    val cloudAiEnabled: Boolean = false,
    val axisEmailAccount: String = "",
    val axisEmailAutoSyncEnabled: Boolean = true,
    val sparkMailTriggerEnabled: Boolean = false,
    val themeMode: String = "system",
    val dailyReminderEnabled: Boolean = true,
    val dailyReminderHour: Int = 22,
    val dailyReminderMinute: Int = 0,
    val homeCardOrder: List<String> = emptyList(),
    val hiddenHomeCardIds: List<String> = emptyList(),
    val customCategories: List<CustomCategory> = emptyList(),
    val transactionRules: List<TransactionRule> = emptyList(),
    val budgetGoals: List<BudgetGoal> = emptyList()
)

data class BackupResult(
    val location: String = "",
    val itemCount: Int = 0,
    val transactionCount: Int = 0,
    val message: String = ""
)

data class BackupHistoryEntry(
    val id: String = UUID.randomUUID().toString(),
    val timestamp: Long = System.currentTimeMillis(),
    val trigger: String = BackupTrigger.MANUAL,
    val destination: String = BackupDestination.LOCAL,
    val status: String = BackupStatus.SUCCESS,
    val itemCount: Int = 0,
    val transactionCount: Int = 0,
    val message: String = ""
)

object BackupTrigger {
    const val MANUAL = "manual"
    const val AUTO = "auto"
    const val RESTORE = "restore"

    fun label(trigger: String): String = when (trigger) {
        AUTO -> "Daily auto"
        RESTORE -> "Restore"
        else -> "Manual"
    }
}

object BackupDestination {
    const val LOCAL = "local"
    const val GOOGLE_DRIVE = "google_drive"

    fun label(destination: String): String = when (destination) {
        GOOGLE_DRIVE -> "Google Drive"
        else -> "Local file"
    }
}

object BackupStatus {
    const val SUCCESS = "success"
    const val FAILED = "failed"

    fun label(status: String): String = when (status) {
        FAILED -> "Failed"
        else -> "Success"
    }
}
