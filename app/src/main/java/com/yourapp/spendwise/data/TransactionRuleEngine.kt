package com.yourapp.spendwise.data

import java.util.Locale

data class TransactionDraft(
    val merchant: String,
    val bank: String,
    val rawSms: String,
    val sourceSender: String,
    val category: String
)

object TransactionRuleEngine {
    fun applyRules(
        draft: TransactionDraft,
        rules: List<TransactionRule>
    ): TransactionDraft {
        return applyRulesDetailed(draft = draft, rules = rules).draft
    }

    fun applyRulesDetailed(
        draft: TransactionDraft,
        rules: List<TransactionRule>
    ): RuleApplicationResult {
        val matchingRule = rules.firstOrNull { rule -> matches(rule, draft) }
            ?: return RuleApplicationResult(draft = draft)
        val enrichedDraft = draft.copy(
            merchant = matchingRule.assignMerchant.ifBlank { draft.merchant },
            bank = matchingRule.assignBank.ifBlank { draft.bank },
            category = matchingRule.assignCategory.ifBlank { draft.category }
        )
        return RuleApplicationResult(draft = enrichedDraft, matchedRule = matchingRule)
    }

    private fun matches(rule: TransactionRule, draft: TransactionDraft): Boolean {
        val sender = draft.sourceSender.lowercase(Locale.ENGLISH)
        val merchant = draft.merchant.lowercase(Locale.ENGLISH)
        val sms = draft.rawSms.lowercase(Locale.ENGLISH)

        val senderMatch = rule.senderContains.isBlank() ||
            sender.contains(rule.senderContains.trim().lowercase(Locale.ENGLISH))
        val merchantMatch = rule.merchantContains.isBlank() ||
            merchant.contains(rule.merchantContains.trim().lowercase(Locale.ENGLISH))
        val smsMatch = rule.smsContains.isBlank() ||
            sms.contains(rule.smsContains.trim().lowercase(Locale.ENGLISH))

        return senderMatch && merchantMatch && smsMatch
    }
}
