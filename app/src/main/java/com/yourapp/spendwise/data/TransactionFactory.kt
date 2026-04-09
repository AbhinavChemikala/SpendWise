package com.yourapp.spendwise.data

import android.content.Context
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.data.db.TransactionType
import com.yourapp.spendwise.sms.AccountLabelExtractor

object TransactionFactory {
    fun create(
        context: Context,
        amount: Double,
        type: TransactionType,
        merchant: String,
        bank: String,
        rawSms: String,
        sourceSender: String,
        timestamp: Long,
        isVerifiedByAi: Boolean,
        note: String = "",
        tags: String = "",
        verificationSource: String = if (isVerifiedByAi) "Gemini Nano" else "Prefilter",
        aiReason: String = "",
        aiCardLast4: String = "",
        aiCardType: String = ""
    ): TransactionEntity {
        val settingsStore = SettingsStore(context.applicationContext)
        val now = System.currentTimeMillis()
        val safeMerchant = MerchantNormalizer.normalize(merchant, rawSms)
        val safeBank = bank.ifBlank { "Unknown" }
        val resolution = TransactionCategoryResolver.resolveDetailed(
            merchant = safeMerchant,
            rawSms = rawSms,
            type = type
        )
        val ruleResult = TransactionRuleEngine.applyRulesDetailed(
            draft = TransactionDraft(
                merchant = safeMerchant,
                bank = safeBank,
                rawSms = rawSms,
                sourceSender = sourceSender,
                category = resolution.category
            ),
            rules = settingsStore.getRules()
        )
        val enrichedDraft = ruleResult.draft
        val matchedRule = ruleResult.matchedRule
        return TransactionEntity(
            amount = amount,
            type = type,
            merchant = enrichedDraft.merchant,
            bank = enrichedDraft.bank,
            rawSms = rawSms,
            sourceSender = sourceSender,
            timestamp = timestamp,
            isVerifiedByAi = isVerifiedByAi,
            category = enrichedDraft.category,
            note = note,
            tags = tags,
            verificationSource = verificationSource,
            aiReason = aiReason,
            paymentMode = PaymentModeResolver.resolve(
                rawSms = rawSms,
                merchant = enrichedDraft.merchant
            ),
            accountLabel = AccountLabelExtractor.extract(
                sender    = sourceSender,
                body      = rawSms,
                bank      = enrichedDraft.bank,
                aiCardLast4 = aiCardLast4,
                aiCardType  = aiCardType
            ),
            updatedAt = now,
            categoryDecisionSource = if (matchedRule != null) {
                CategoryDecisionSource.RULE
            } else {
                CategoryDecisionSource.RESOLVER
            },
            categoryRefinementStatus = if (matchedRule != null) {
                CategoryRefinementStatus.SKIPPED_RULE
            } else {
                CategoryRefinementStatus.PENDING
            },
            categoryRuleName = matchedRule?.name?.trim().orEmpty()
        )
    }
}
