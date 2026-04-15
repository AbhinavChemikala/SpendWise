package com.yourapp.spendwise.mail

data class AxisEmailSyncHistoryItem(
    val messageId: String,
    val receivedAt: Long,
    val outcome: String,
    val from: String = "",
    val summary: String = "",
    val parsedAmount: Double? = null,
    val parsedType: String = "",
    val parsedMerchant: String = "",
    val cleanedBody: String = "",
    val fullBody: String = ""
)

data class AxisEmailSyncHistoryEntry(
    val id: String,
    val startedAt: Long,
    val finishedAt: Long,
    val trigger: String,
    val account: String,
    val status: String,
    val scanned: Int,
    val imported: Int,
    val duplicates: Int,
    val skipped: Int,
    val message: String,
    val items: List<AxisEmailSyncHistoryItem> = emptyList()
)

object AxisEmailSyncTrigger {
    const val MANUAL = "MANUAL"
    const val PERIODIC = "PERIODIC"
    const val SPARK_NOTIFICATION = "SPARK_NOTIFICATION"
    const val FOREGROUND = "FOREGROUND"
    const val CONNECT = "CONNECT"

    fun label(trigger: String): String {
        return when (trigger) {
            PERIODIC -> "Periodic"
            SPARK_NOTIFICATION -> "Mail notification"
            FOREGROUND -> "Foreground check"
            CONNECT -> "Initial connect"
            else -> "Manual"
        }
    }
}
