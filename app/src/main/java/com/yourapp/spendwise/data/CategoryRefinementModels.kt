package com.yourapp.spendwise.data

object CategoryDecisionSource {
    const val RESOLVER = "RESOLVER"
    const val RULE = "RULE"
    const val AI = "AI"
    const val USER_EDIT = "USER_EDIT"
}

object CategoryRefinementStatus {
    const val NONE = "NONE"
    const val PENDING = "PENDING"
    const val RUNNING = "RUNNING"
    const val APPLIED = "APPLIED"
    const val KEPT_RESOLVER = "KEPT_RESOLVER"
    const val SKIPPED_RULE = "SKIPPED_RULE"
    const val SKIPPED_STALE = "SKIPPED_STALE"
    const val FAILED = "FAILED"
}

data class CategoryResolution(
    val category: String,
    val bucketLabel: String,
    val normalizedMerchant: String,
    val matchedKeywords: List<String>
)

data class RuleApplicationResult(
    val draft: TransactionDraft,
    val matchedRule: TransactionRule? = null
)
