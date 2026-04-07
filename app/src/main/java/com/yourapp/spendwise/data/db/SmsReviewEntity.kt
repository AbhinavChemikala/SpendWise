package com.yourapp.spendwise.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "sms_review_events",
    indices = [
        Index(value = ["eventSource"]),
        Index(value = ["finalStatus"]),
        Index(value = ["receivedAt"])
    ]
)
data class SmsReviewEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val sender: String,
    val body: String,
    val receivedAt: Long,
    val eventSource: String,
    val prefilterDecision: String,
    val previewAmount: Double = 0.0,
    val previewType: TransactionType = TransactionType.UNKNOWN,
    val previewMerchant: String = "",
    val previewBank: String = "",
    val aiJson: String = "",
    val aiReason: String = "",
    val aiEngine: String = "Gemini Nano",
    val finalStatus: String,
    val transactionId: Long? = null,
    val debugLog: String = ""
)
