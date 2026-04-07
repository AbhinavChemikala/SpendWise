package com.yourapp.spendwise.sms

import com.yourapp.spendwise.data.db.TransactionType
import java.util.Locale

sealed class PreFilterResult {
    data class Confident(
        val amount: Double,
        val type: TransactionType,
        val merchant: String,
        val bank: String
    ) : PreFilterResult()

    data object NeedsAiReview : PreFilterResult()

    data object Discard : PreFilterResult()
}

data class SmsDetectionPreview(
    val amount: Double?,
    val type: TransactionType,
    val merchant: String,
    val bank: String
)

data class SmsPreFilterInspection(
    val amount: Double?,
    val type: TransactionType,
    val merchant: String,
    val bank: String,
    val spamScore: Int,
    val hasFinancialKeyword: Boolean,
    val knownSender: Boolean,
    val resultLabel: String
)

object SmsPreFilter {
    private val spamFlags = listOf(
        "click",
        "http",
        "www.",
        "bit.ly",
        "tinyurl",
        "verify",
        "otp",
        "won",
        "prize",
        "lucky",
        "offer",
        "free",
        "congratulations",
        "claim",
        "kyc",
        "suspend",
        "block",
        "urgent",
        "immediately",
        "reward",
        "cashback offer",
        "act now",
        "limited time"
    )

    private val financialKeywords = listOf(
        "debited",
        "credited",
        "debit",
        "credit",
        "spent",
        "paid",
        "received",
        "transferred",
        "withdrawn",
        "deposited",
        "payment",
        "transaction",
        "upi",
        "neft",
        "imps",
        "rtgs"
    )

    private val debitKeywords = listOf("debited", "spent", "paid", "withdrawn", "deducted")
    private val creditKeywords = listOf("credited", "received", "deposited", "refund")

    private val knownSenders = setOf(
        "HDFCBK",
        "ICICIB",
        "SBIINB",
        "AXISBK",
        "KOTAKB",
        "INDUSB",
        "YESBNK",
        "PAYTMB",
        "PHONEPE",
        "GPAY",
        "BOIIND",
        "PNBSMS",
        "CANBNK",
        "UNIONB",
        "CENTBK",
        "IDFCBK",
        "RBLBNK",
        "FEDERL",
        "SCBANK",
        "CSFB",
        "AMZPAY",
        "FLIPKRT",
        "SWIGGY",
        "ZOMATO"
    )

    private val amountRegex = Regex(
        pattern = """(?:rs\.?|inr|₹)\s*([0-9,]+(?:\.[0-9]{1,2})?)""",
        option = RegexOption.IGNORE_CASE
    )

    private val merchantRegex = Regex(
        pattern = """(?:at|to|for)\s+([A-Za-z0-9\s\-_@.]{3,30})""",
        option = RegexOption.IGNORE_CASE
    )

    fun evaluate(sender: String, body: String): PreFilterResult {
        return when (inspect(sender, body).resultLabel) {
            "CONFIDENT" -> {
                val inspection = inspect(sender, body)
                PreFilterResult.Confident(
                    amount = inspection.amount ?: 0.0,
                    type = inspection.type,
                    merchant = inspection.merchant,
                    bank = inspection.bank
                )
            }

            "NEEDS_AI" -> PreFilterResult.NeedsAiReview
            else -> PreFilterResult.Discard
        }
    }

    fun inspect(sender: String, body: String): SmsPreFilterInspection {
        val normalizedBody = body.lowercase(Locale.ENGLISH)
        val spamScore = spamFlags.count { normalizedBody.contains(it) }
        val senderKey = sender.take(10).uppercase(Locale.ENGLISH)
        val knownSender = knownSenders.any { senderKey.contains(it) || it.contains(senderKey) }
        val amount = extractAmount(body)
        val type = determineType(normalizedBody)
        val hasFinancialKeyword = financialKeywords.any { normalizedBody.contains(it) }
        val resultLabel = when {
            spamScore >= 2 -> "SPAM_DISCARDED"
            !hasFinancialKeyword -> "DISCARDED"
            amount == null -> "NEEDS_AI"
            knownSender && type != TransactionType.UNKNOWN && spamScore == 0 -> "CONFIDENT"
            else -> "NEEDS_AI"
        }
        return SmsPreFilterInspection(
            amount = amount,
            type = type,
            merchant = extractMerchant(body),
            bank = extractBank(senderKey, sender),
            spamScore = spamScore,
            hasFinancialKeyword = hasFinancialKeyword,
            knownSender = knownSender,
            resultLabel = resultLabel
        )
    }

    fun preview(sender: String, body: String): SmsDetectionPreview {
        val inspection = inspect(sender, body)
        return SmsDetectionPreview(
            amount = inspection.amount,
            type = inspection.type,
            merchant = inspection.merchant,
            bank = inspection.bank
        )
    }

    fun buildDebugLog(sender: String, body: String): String {
        val inspection = inspect(sender, body)
        return buildString {
            appendLine("sender=$sender")
            appendLine("spamScore=${inspection.spamScore}")
            appendLine("hasFinancialKeyword=${inspection.hasFinancialKeyword}")
            appendLine("knownSender=${inspection.knownSender}")
            appendLine("amount=${inspection.amount ?: "none"}")
            appendLine("type=${inspection.type.name}")
            appendLine("merchant=${inspection.merchant}")
            appendLine("bank=${inspection.bank}")
            append("prefilter=${inspection.resultLabel}")
        }
    }

    fun fallbackMerchant(body: String): String = extractMerchant(body)

    fun fallbackBank(sender: String): String {
        val senderKey = sender.take(10).uppercase(Locale.ENGLISH)
        return extractBank(senderKey, sender)
    }

    private fun extractAmount(body: String): Double? {
        val rawAmount = amountRegex.find(body)?.groupValues?.getOrNull(1) ?: return null
        return rawAmount.replace(",", "").toDoubleOrNull()
    }

    private fun determineType(normalizedBody: String): TransactionType {
        return when {
            debitKeywords.any { normalizedBody.contains(it) } -> TransactionType.DEBIT
            creditKeywords.any { normalizedBody.contains(it) } -> TransactionType.CREDIT
            else -> TransactionType.UNKNOWN
        }
    }

    private fun extractMerchant(body: String): String {
        return merchantRegex.find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.trim()
            ?.take(30)
            ?.ifBlank { "Unknown Merchant" }
            ?: "Unknown Merchant"
    }

    private fun extractBank(senderKey: String, sender: String): String {
        return when {
            senderKey.contains("HDFC") -> "HDFC Bank"
            senderKey.contains("ICICI") -> "ICICI Bank"
            senderKey.contains("SBI") -> "SBI"
            senderKey.contains("AXIS") -> "Axis Bank"
            senderKey.contains("KOTAK") -> "Kotak Bank"
            senderKey.contains("PAYTM") -> "Paytm"
            senderKey.contains("PHONEPE") -> "PhonePe"
            senderKey.contains("GPAY") -> "Google Pay"
            else -> sender.take(6).ifBlank { "Unknown" }
        }
    }
}
