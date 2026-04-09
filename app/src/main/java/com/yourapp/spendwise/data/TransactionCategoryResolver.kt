package com.yourapp.spendwise.data

import com.yourapp.spendwise.data.db.TransactionType
import java.util.Locale

object TransactionCategoryResolver {
    fun resolve(merchant: String, rawSms: String, type: TransactionType): String {
        return resolveDetailed(merchant = merchant, rawSms = rawSms, type = type).category
    }

    fun resolveDetailed(merchant: String, rawSms: String, type: TransactionType): CategoryResolution {
        val normalizedMerchant = MerchantNormalizer.normalize(merchant, rawSms)
        if (type == TransactionType.CREDIT) {
            return when {
                rawSms.contains("salary", ignoreCase = true) || normalizedMerchant == "Salary" ->
                    CategoryResolution(
                        category = "Salary",
                        bucketLabel = "Salary credit",
                        normalizedMerchant = normalizedMerchant,
                        matchedKeywords = listOf("salary")
                    )
                rawSms.contains("refund", ignoreCase = true) ->
                    CategoryResolution(
                        category = "Refunds",
                        bucketLabel = "Refund credit",
                        normalizedMerchant = normalizedMerchant,
                        matchedKeywords = listOf("refund")
                    )
                else ->
                    CategoryResolution(
                        category = "Income",
                        bucketLabel = "Generic credit",
                        normalizedMerchant = normalizedMerchant,
                        matchedKeywords = emptyList()
                    )
            }
        }

        val haystack =
                buildString {
                            append(normalizedMerchant)
                            append(' ')
                            append(rawSms)
                        }
                        .lowercase(Locale.ENGLISH)

        val buckets = listOf(
            CategoryBucket(
                category = "UPI",
                label = "UPI rail",
                keywords = listOf(
                    "upi", "vpa", "phonepe", "gpay", "googlepay", "paytm",
                    "bhim", "amazon pay", "mobikwik", "@oksbi", "@okhdfc", "@okaxis"
                )
            ),
            CategoryBucket(
                category = "Food",
                label = "Food spend",
                keywords = listOf("swiggy", "zomato", "eatclub", "dominos")
            ),
            CategoryBucket(
                category = "Travel",
                label = "Travel spend",
                keywords = listOf(
                    "uber", "ola", "rapido", "metro", "irctc", "redbus",
                    "abhibus", "makemytrip", "goibibo", "fuel", "petrol",
                    "diesel", "fastag", "toll"
                )
            ),
            CategoryBucket(
                category = "Shopping",
                label = "Shopping spend",
                keywords = listOf(
                    "amazon", "flipkart", "myntra", "ajio", "meesho", "jiomart",
                    "bigbasket", "blinkit", "zepto", "grofers", "supermarket",
                    "grocery", "reliance fresh", "dmart"
                )
            ),
            CategoryBucket(
                category = "Entertainment",
                label = "Entertainment spend",
                keywords = listOf(
                    "pvr", "inox", "bookmyshow", "netflix", "prime",
                    "hotstar", "sonyliv", "spotify", "youtube"
                )
            ),
            CategoryBucket(
                category = "Loans & EMI",
                label = "Loan payment",
                keywords = listOf("emi", "loan", "bnpl")
            ),
            CategoryBucket(
                category = "Bills",
                label = "Bill payment",
                keywords = listOf("electricity", "broadband", "recharge", "bill", "insurance")
            ),
            CategoryBucket(
                category = "Gifts & Rewards",
                label = "Rewards",
                keywords = listOf("cashback", "reward", "gift")
            ),
            CategoryBucket(
                category = "Cash Withdrawal",
                label = "Cash withdrawal",
                keywords = listOf("atm", "withdrawn")
            )
        )
        val match = buckets.firstOrNull { bucket -> bucket.keywords.any(haystack::contains) }

        return if (match != null) {
            CategoryResolution(
                category = match.category,
                bucketLabel = match.label,
                normalizedMerchant = normalizedMerchant,
                matchedKeywords = match.keywords.filter(haystack::contains)
            )
        } else {
            CategoryResolution(
                category = "Other",
                bucketLabel = "Fallback",
                normalizedMerchant = normalizedMerchant,
                matchedKeywords = emptyList()
            )
        }
    }

    private data class CategoryBucket(
        val category: String,
        val label: String,
        val keywords: List<String>
    )
}
