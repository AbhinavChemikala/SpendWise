package com.yourapp.spendwise.mail

import android.text.Html
import com.yourapp.spendwise.data.db.TransactionType
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import kotlin.math.abs

data class AxisEmailCandidate(
    val amount: Double,
    val type: TransactionType,
    val timestampMs: Long,
    val reference: String,
    val merchantHint: String,
    val normalizedBody: String,
    val fullBody: String
)

object AxisEmailParser {
    private val amountRegex = Regex("""(?i)\bINR\s*([0-9,]+(?:\.\d{1,2})?)""")
    private val refRegex = Regex("""(?i)\b(?:ref|utr|txn(?:\s*id)?)\s*[:#-]?\s*([A-Z0-9-]{6,})""")
    private val upiRegex = Regex("""(?i)\bUPI/[^/\s]+/([^/\n]+)/""")
    private val upiLineRegex = Regex("""(?i)\bUPI/[^\n]+""")
    private val accountRegex = Regex("""(?i)\b(?:account|a/c)\s*(?:number|no\.?)?\s*:\s*([A-Z0-9*X-]{4,})""")
    private val dateRegex = Regex("""\b(\d{2}-\d{2}-\d{4}),\s*(\d{2}:\d{2}:\d{2})\b""")
    private val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy, HH:mm:ss", Locale.ENGLISH)

    fun normalizeBody(raw: String): String {
        val htmlDecoded = Html.fromHtml(raw, Html.FROM_HTML_MODE_LEGACY).toString()
        return htmlDecoded
            .replace('\u00A0', ' ')
            .replace(Regex("""(?i)\bAmount\s+(Debited|Credited):"""), "\nAmount $1:")
            .replace(Regex("""(?i)\bAccount\s+Number:"""), "\nAccount Number:")
            .replace(Regex("""(?i)\bDate\s*&\s*Time:"""), "\nDate & Time:")
            .replace(Regex("""(?i)\bRef(?:erence)?\s*No:"""), "\nRef No:")
            .replace(Regex("""(?i)(\s)(UPI/[A-Z0-9@/_-]+)"""), "\n$2")
            .replace(Regex("""[ \t]+"""), " ")
            .replace(Regex("""\n\s+"""), "\n")
            .replace(Regex("""\n{3,}"""), "\n\n")
            .trim()
    }

    fun parse(rawBody: String, fallbackTimestampMs: Long): AxisEmailCandidate? {
        val fullBody = normalizeBody(rawBody)
        val normalized = extractRelevantBody(fullBody)
        val lower = normalized.lowercase(Locale.ENGLISH)
        val type = when {
            "debited" in lower || "amount debited" in lower -> TransactionType.DEBIT
            "credited" in lower || "amount credited" in lower -> TransactionType.CREDIT
            else -> TransactionType.UNKNOWN
        }
        val amount = amountRegex.find(normalized)
            ?.groupValues
            ?.getOrNull(1)
            ?.replace(",", "")
            ?.toDoubleOrNull()
            ?: return null
        if (type == TransactionType.UNKNOWN || amount <= 0.0) return null

        val reference = refRegex.find(normalized)?.groupValues?.getOrNull(1).orEmpty()
        val merchant = upiRegex.find(normalized)?.groupValues?.getOrNull(1)
            ?.replace(Regex("""\s+"""), " ")
            ?.trim()
            .orEmpty()
        val timestampMs = parseTimestamp(normalized, fallbackTimestampMs)

        return AxisEmailCandidate(
            amount = amount,
            type = type,
            timestampMs = timestampMs,
            reference = reference,
            merchantHint = merchant,
            normalizedBody = normalized,
            fullBody = fullBody
        )
    }

    private fun extractRelevantBody(body: String): String {
        val lines = body.lines()
            .map { it.trim() }
            .filter { it.isNotBlank() }
        if (lines.isEmpty()) return body

        val compactLines = linkedSetOf<String>()
        val amountLine = lines.firstOrNull {
            it.contains("amount debited", ignoreCase = true) ||
                it.contains("amount credited", ignoreCase = true)
        } ?: amountRegex.find(body)?.value?.let { amountValue ->
            val typeLabel = when {
                body.contains("credited", ignoreCase = true) -> "Amount Credited"
                body.contains("debited", ignoreCase = true) -> "Amount Debited"
                else -> "Amount"
            }
            "$typeLabel: $amountValue"
        }
        amountLine?.let { compactLines += it.take(140) }

        accountRegex.find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { compactLines += "Account Number: $it" }

        dateRegex.find(body)
            ?.value
            ?.takeIf { it.isNotBlank() }
            ?.let { compactLines += "Date & Time: $it" }

        upiLineRegex.find(body)
            ?.value
            ?.replace(Regex("""\s+"""), " ")
            ?.trim()
            ?.takeIf { it.isNotBlank() }
            ?.let { compactLines += it.take(180) }

        refRegex.find(body)
            ?.groupValues
            ?.getOrNull(1)
            ?.takeIf { it.isNotBlank() }
            ?.let { compactLines += "Reference: $it" }

        if (compactLines.isEmpty()) {
            lines.firstOrNull {
                it.contains("debited", ignoreCase = true) ||
                    it.contains("credited", ignoreCase = true)
            }?.let { compactLines += it.take(180) }
        }

        return buildString {
            appendLine("Axis Bank transaction email")
            compactLines.forEach { line ->
                appendLine(line)
            }
        }.trim().take(420).ifBlank { body.take(420) }
    }

    private fun parseTimestamp(body: String, fallbackTimestampMs: Long): Long {
        val match = dateRegex.find(body) ?: return fallbackTimestampMs
        val parsed = runCatching {
            val value = "${match.groupValues[1]}, ${match.groupValues[2]}"
            LocalDateTime.parse(value, formatter)
                .atZone(ZoneId.systemDefault())
                .toInstant()
                .toEpochMilli()
        }.getOrNull()
        if (parsed == null) return fallbackTimestampMs
        return if (abs(parsed - fallbackTimestampMs) > 2L * 24L * 60L * 60L * 1000L) {
            fallbackTimestampMs
        } else {
            parsed
        }
    }
}
