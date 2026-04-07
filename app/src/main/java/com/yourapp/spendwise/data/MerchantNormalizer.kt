package com.yourapp.spendwise.data

import java.util.Locale

object MerchantNormalizer {
    fun normalize(merchant: String, rawSms: String): String {
        val raw = merchant.trim().ifBlank { "Unknown Merchant" }
        val haystack = "$raw $rawSms".lowercase(Locale.ENGLISH)

        return when {
            haystack.contains("salary") || haystack.contains("payroll") -> "Salary"
            haystack.contains("amazon") || haystack.contains("amzpay") -> "Amazon Pay"
            haystack.contains("flipkart") || haystack.contains("flipkrt") -> "Flipkart"
            haystack.contains("swiggy") -> "Swiggy"
            haystack.contains("zomato") -> "Zomato"
            haystack.contains("phonepe") -> "PhonePe"
            haystack.contains("gpay") || haystack.contains("google pay") -> "Google Pay"
            haystack.contains("paytm") -> "Paytm"
            haystack.contains("netflix") -> "Netflix"
            haystack.contains("prime") || haystack.contains("amazon prime") -> "Amazon Prime"
            haystack.contains("bookmyshow") -> "BookMyShow"
            haystack.contains("pvr") || haystack.contains("inox") -> "PVR INOX"
            haystack.contains("uber") -> "Uber"
            haystack.contains("ola") -> "Ola"
            else -> raw
                .replace(Regex("""[/|]+"""), " ")
                .replace(Regex("""\s+"""), " ")
                .trim()
                .take(30)
        }
    }
}
