package com.yourapp.spendwise.data

import com.yourapp.spendwise.data.db.TransactionType
import java.util.Locale

object TransactionCategoryResolver {
    fun resolve(
        merchant: String,
        rawSms: String,
        type: TransactionType
    ): String {
        val normalizedMerchant = MerchantNormalizer.normalize(merchant, rawSms)
        if (type == TransactionType.CREDIT) {
            return when {
                rawSms.contains("salary", ignoreCase = true) || normalizedMerchant == "Salary" -> "Salary"
                rawSms.contains("refund", ignoreCase = true) -> "Refunds"
                rawSms.contains("cashback", ignoreCase = true) -> "Gifts & Rewards"
                else -> "Income"
            }
        }

        val haystack = buildString {
            append(normalizedMerchant)
            append(' ')
            append(rawSms)
        }.lowercase(Locale.ENGLISH)

        return when {
            haystack.contains("upi") || haystack.contains("phonepe") || haystack.contains("gpay") || haystack.contains("paytm") -> "UPI"
            haystack.contains("swiggy") || haystack.contains("zomato") || haystack.contains("restaurant") || haystack.contains("cafe") || haystack.contains("hotel") -> "Food"
            haystack.contains("uber") || haystack.contains("ola") || haystack.contains("metro") || haystack.contains("irctc") || haystack.contains("fuel") || haystack.contains("petrol") -> "Travel"
            haystack.contains("amazon") || haystack.contains("flipkart") || haystack.contains("myntra") || haystack.contains("ajio") || haystack.contains("shopping") -> "Shopping"
            haystack.contains("pvr") || haystack.contains("netflix") || haystack.contains("prime") || haystack.contains("bookmyshow") || haystack.contains("inox") -> "Entertainment"
            haystack.contains("emi") || haystack.contains("loan") || haystack.contains("bnpl") -> "Loans & EMI"
            haystack.contains("electricity") || haystack.contains("broadband") || haystack.contains("recharge") || haystack.contains("bill") || haystack.contains("insurance") -> "Bills"
            haystack.contains("cashback") || haystack.contains("reward") || haystack.contains("gift") -> "Gifts & Rewards"
            haystack.contains("atm") || haystack.contains("withdrawn") -> "Cash Withdrawal"
            else -> "Other"
        }
    }
}
