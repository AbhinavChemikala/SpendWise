package com.yourapp.spendwise.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

enum class TransactionType {
    DEBIT,
    CREDIT,
    UNKNOWN
}

@Entity(
    tableName = "transactions",
    indices = [
        Index(value = ["rawSms", "timestamp"], unique = true)
    ]
)
data class TransactionEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val bank: String,
    val rawSms: String,
    val sourceSender: String,
    val timestamp: Long,
    val isVerifiedByAi: Boolean,
    val category: String = "Uncategorized",
    val note: String = "",
    val tags: String = "",
    val verificationSource: String = "Prefilter",
    val aiReason: String = "",
    val paymentMode: String = "Other",
    val accountLabel: String = "",   // e.g. "HDFC Credit ••4521" or "PhonePe UPI"
    val isIgnoredDuplicate: Boolean = false,
    val updatedAt: Long = 0L,
    val categoryDecisionSource: String = "RESOLVER",
    val categoryRefinementStatus: String = "NONE",
    val categoryRuleName: String = "",
    val latitude: Double? = null,
    val longitude: Double? = null
)
