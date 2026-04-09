package com.yourapp.spendwise.data.db

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "transaction_category_ai",
    indices = [Index(value = ["transactionId"], unique = true)]
)
data class TransactionCategoryAiEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val transactionId: Long,
    val resolverCategory: String,
    val resolverSignalsJson: String,
    val currentCategory: String,
    val suggestedCategory: String,
    val confidence: Double,
    val reason: String,
    val model: String,
    val rawJson: String,
    val outcome: String,
    val outcomeDetail: String,
    val startedAt: Long,
    val finishedAt: Long,
    val keepCurrent: Boolean = false
)
