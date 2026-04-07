package com.yourapp.spendwise.sms

/**
 * Pure regex-based extractor that builds a human-readable account label from
 * an SMS body + sender, e.g.:
 *   "SBI Credit ••2013"
 *   "HDFC Debit ••4521"
 *   "PhonePe UPI"
 *   "Google Pay"
 *   "HDFC Bank"  (fallback — no card number found)
 *
 * No AI needed for the vast majority of bank SMSes because they follow
 * predictable patterns. The AI (Gemma/Nano) can optionally supply card_last4
 * and card_type as a fallback for unusual formats.
 */
object AccountLabelExtractor {

    // Matches: "card ending 2013", "card xx2013", "Card No. XX 2013", "card ****2013"
    private val last4Regex = Regex(
        pattern = """(?:card|a/?c|account|ac\.?)[\s\w.*-]{0,25}?(?:x{2,4}|\.{2,4}|\*{2,4}|ending\s+|no\.?\s*)(\d{4})""",
        option  = RegexOption.IGNORE_CASE
    )

    // Matches: "A/c xx1234", "Ac 1234", "account 1234"
    private val accountNumRegex = Regex(
        pattern = """(?:a/?c|account)(?:\s+xx|\s+\*+|\s+)(\d{4})""",
        option  = RegexOption.IGNORE_CASE
    )

    // Detects card type from body keywords
    private val creditKeywords = listOf("credit card", "credit a/c", "creditcard")
    private val debitKeywords  = listOf("debit card", "debit a/c", "savings a/c", "savings account")

    fun extract(
        sender: String,
        body:   String,
        bank:   String,
        // Optional AI-supplied values (fall back to regex)
        aiCardLast4: String = "",
        aiCardType:  String = ""
    ): String {
        val bodyLower = body.lowercase()

        // ── 1. UPI / wallet shortcuts (no card number needed) ─────────────────
        when {
            sender.contains("PHONEPE", ignoreCase = true)       -> return "PhonePe UPI"
            sender.contains("GPAY",    ignoreCase = true)       -> return "Google Pay"
            sender.contains("PAYTM",   ignoreCase = true) &&
                bodyLower.contains("upi")                       -> return "Paytm UPI"
            // Generic UPI: body mentions upi ref but no card
            bodyLower.contains("upi") && !bodyLower.contains("card") -> {
                return "${bank.ifBlank { sender.take(6) }} UPI"
            }
        }

        // ── 2. Extract last 4 digits ──────────────────────────────────────────
        val last4 = aiCardLast4.ifBlank {
            last4Regex.find(body)?.groupValues?.getOrNull(1)
                ?: accountNumRegex.find(body)?.groupValues?.getOrNull(1)
                ?: ""
        }

        // ── 3. Detect card type ───────────────────────────────────────────────
        val cardType = aiCardType.ifBlank {
            when {
                creditKeywords.any { bodyLower.contains(it) } -> "Credit"
                debitKeywords.any  { bodyLower.contains(it) } -> "Debit"
                else                                           -> ""
            }
        }.replaceFirstChar { it.uppercaseChar() }

        // ── 4. Build label ────────────────────────────────────────────────────
        val safeBank = bank.ifBlank { sender.take(6) }
        return when {
            last4.isNotBlank() && cardType.isNotBlank() -> "$safeBank $cardType ••$last4"
            last4.isNotBlank()                          -> "$safeBank ••$last4"
            cardType.isNotBlank()                       -> "$safeBank $cardType"
            else                                         -> safeBank  // bare bank name
        }
    }
}
