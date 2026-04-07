package com.yourapp.spendwise.data

import java.util.Locale

object PaymentModeResolver {
    fun resolve(rawSms: String, merchant: String): String {
        val normalizedMerchant = MerchantNormalizer.normalize(merchant, rawSms)
        val haystack = "$normalizedMerchant $rawSms".lowercase(Locale.ENGLISH)
        return when {
            haystack.contains("upi") || haystack.contains("gpay") || haystack.contains("phonepe") || haystack.contains("paytm") -> "UPI"
            haystack.contains("card") || haystack.contains("visa") || haystack.contains("mastercard") || haystack.contains("pos") -> "Card"
            haystack.contains("atm") || haystack.contains("withdrawn") -> "ATM"
            haystack.contains("neft") || haystack.contains("imps") || haystack.contains("rtgs") || haystack.contains("bank transfer") -> "Bank Transfer"
            else -> "Other"
        }
    }
}
