package com.yourapp.spendwise.sms

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.util.concurrent.TimeUnit

/**
 * Calls Google AI Studio's Gemma 3 27B model via REST API.
 * Returns the same AiAnalysisOutput shape as GeminiNanoAnalyzer so
 * SmsProcessor can use either interchangeably.
 *
 * API docs: https://ai.google.dev/api/generate-content
 */
object GemmaCloudAnalyzer {
    private const val TAG = "GemmaCloudAnalyzer"
    private const val MODEL = "gemma-3-27b-it"
    private const val BASE_URL = "https://generativelanguage.googleapis.com/v1beta/models/$MODEL:generateContent"

    private val gson = Gson()
    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    suspend fun analyzeDetailed(
        smsSender: String,
        smsBody: String,
        apiKey: String
    ): AiAnalysisOutput? = withContext(Dispatchers.IO) {
        if (apiKey.isBlank()) {
            Log.w(TAG, "Cloud AI API key not set — skipping cloud analysis.")
            return@withContext null
        }

        val prompt = buildPrompt(smsSender, smsBody)
        val requestJson = buildRequestJson(prompt)
        val url = "$BASE_URL?key=$apiKey"

        val request = Request.Builder()
            .url(url)
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()

        runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Cloud AI HTTP ${response.code}: ${response.body?.string()?.take(300)}")
                    return@withContext null
                }

                val body = response.body?.string() ?: return@withContext null
                val rawText = extractTextFromResponse(body)

                if (rawText.isNullOrBlank()) {
                    Log.w(TAG, "Cloud AI returned blank text")
                    return@withContext null
                }

                val parsed = GeminiNanoAnalyzer.safeParseJson(rawText)
                AiAnalysisOutput(result = parsed, rawResponse = rawText)
            }
        }.getOrElse { e ->
            Log.e(TAG, "Cloud AI request failed", e)
            null
        }
    }

    private fun buildRequestJson(prompt: String): String {
        val obj = JsonObject().apply {
            add("contents", gson.toJsonTree(
                listOf(mapOf("parts" to listOf(mapOf("text" to prompt))))
            ))
            add("generationConfig", gson.toJsonTree(mapOf(
                "temperature" to 0.1,
                "maxOutputTokens" to 512,
                "topP" to 0.9
            )))
        }
        return gson.toJson(obj)
    }

    private fun extractTextFromResponse(responseBody: String): String? {
        return runCatching {
            val root = gson.fromJson(responseBody, JsonObject::class.java)
            root.getAsJsonArray("candidates")
                ?.get(0)?.asJsonObject
                ?.getAsJsonObject("content")
                ?.getAsJsonArray("parts")
                ?.get(0)?.asJsonObject
                ?.get("text")?.asString
        }.getOrNull()
    }

    // Reuses the same proven prompt from GeminiNanoAnalyzer
    private fun buildPrompt(smsSender: String, smsBody: String): String {
        return """
            You are a financial SMS analyzer for Indian bank accounts.

            SMS Sender: $smsSender
            SMS Body: $smsBody

            Step 1 - Think silently:
            Ask yourself: Did money actually move from or into the user's account as a FINAL settled transaction?
            - OTP messages: the user is approving a transaction, funds have NOT moved yet. NOT genuine.
            - Credit/Debit limit alerts: informational notice, no funds moved. NOT genuine.
            - "Not you? Report/Block" footers: this is a STANDARD bank security footer on REAL transactions. It does NOT make the message fake.
            - Dispute links (sbicard.com/Dispute): added by banks on real debit/credit alerts. STILL genuine.
            - Marketing, due dates, cashback offers, KYC requests: NOT genuine.

            Step 2 - Output ONLY this JSON, no extra text:
            {
              "thought_process": "<one sentence: what is this message>",
              "is_genuine": true or false,
              "type": "debit" or "credit" or "unknown",
              "amount": <number, 0 if not found>,
              "merchant": "<merchant name or empty string>",
              "bank": "<bank name>",
              "card_last4": "<last 4 digits of card/account number, or empty string>",
              "card_type": "credit" or "debit" or "",
              "reason": "<one sentence explaining the decision>"
            }
        """.trimIndent()
    }
}
