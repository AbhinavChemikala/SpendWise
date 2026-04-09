package com.yourapp.spendwise.data

import java.util.Locale

object CategoryCatalog {
    val defaultCategories = listOf(
        "UPI",
        "Food",
        "Travel",
        "Shopping",
        "Entertainment",
        "Bills",
        "Loans & EMI",
        "Gifts & Rewards",
        "Income",
        "Salary",
        "Refunds",
        "Cash Withdrawal",
        "Other",
        "Uncategorized"
    )

    fun allCategories(
        customCategories: List<CustomCategory> = emptyList(),
        transactionCategories: List<String> = emptyList(),
        currentCategory: String? = null
    ): List<String> {
        return (defaultCategories +
            customCategories.map { it.name } +
            transactionCategories +
            listOfNotNull(currentCategory))
            .map { it.trim() }
            .filter { it.isNotBlank() }
            .distinctBy { it.lowercase(Locale.ENGLISH) }
            .sortedBy { it.lowercase(Locale.ENGLISH) }
    }
}
