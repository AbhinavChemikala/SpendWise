package com.yourapp.spendwise.data

import java.util.UUID

data class CustomCategory(
    val id: String = UUID.randomUUID().toString(),
    val name: String
)

data class TransactionRule(
    val id: String = UUID.randomUUID().toString(),
    val name: String = "",
    val senderContains: String = "",
    val merchantContains: String = "",
    val smsContains: String = "",
    val excludeFromTracking: Boolean = false,
    val assignCategory: String = "",
    val assignBank: String = "",
    val assignMerchant: String = ""
)

data class BudgetGoal(
    val id: String = UUID.randomUUID().toString(),
    val category: String,
    val monthlyLimit: Double
)
