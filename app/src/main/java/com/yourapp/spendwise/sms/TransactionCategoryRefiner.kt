package com.yourapp.spendwise.sms

import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.annotation.VisibleForTesting
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.java.GenerativeModelFutures
import com.yourapp.spendwise.data.CategoryResolution
import com.yourapp.spendwise.data.SettingsStore
import com.yourapp.spendwise.data.db.TransactionEntity
import java.util.concurrent.Executor
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AiCategorySuggestion(
    @SerializedName("suggested_category") val suggestedCategory: String = "",
    val confidence: Double = 0.0,
    val reason: String = "",
    @SerializedName("keep_current") val keepCurrent: Boolean = false
)

data class AiCategorySuggestionOutput(
    val result: AiCategorySuggestion?,
    val rawResponse: String,
    val source: String
)

object TransactionCategoryRefiner {
    private const val TAG = "TxCategoryRefiner"
    private const val CLOUD_MODEL = "gemma-3-27b-it"
    private const val CLOUD_URL =
        "https://generativelanguage.googleapis.com/v1beta/models/$CLOUD_MODEL:generateContent"

    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val directExecutor = Executor { runnable -> runnable.run() }
    private val cloudClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .build()

    private val generativeModel by lazy {
        Generation.getClient(GenerationConfig.Builder().build())
    }

    private val futuresClient by lazy {
        GenerativeModelFutures.from(generativeModel)
    }

    suspend fun refine(
        transaction: TransactionEntity,
        resolution: CategoryResolution,
        allowedCategories: List<String>,
        settingsStore: SettingsStore
    ): AiCategorySuggestionOutput? {
        if (settingsStore.isCloudAiEnabled()) {
            val apiKey = settingsStore.getCloudAiApiKey()
            if (apiKey.isNotBlank()) {
                val cloudResult = analyzeWithCloud(
                    transaction = transaction,
                    resolution = resolution,
                    allowedCategories = allowedCategories,
                    apiKey = apiKey
                )
                if (cloudResult != null) {
                    return cloudResult.copy(source = "Gemma 3 27B")
                }
                Log.w(TAG, "Cloud category refinement failed for tx=${transaction.id}, falling back to Nano")
            }
        }
        return analyzeWithNano(
            transaction = transaction,
            resolution = resolution,
            allowedCategories = allowedCategories
        )
    }

    private suspend fun analyzeWithNano(
        transaction: TransactionEntity,
        resolution: CategoryResolution,
        allowedCategories: List<String>
    ): AiCategorySuggestionOutput? {
        val prompt = buildPrompt(transaction, resolution, allowedCategories)
        val request = GenerateContentRequest.Builder(TextPart(prompt)).build()
        val maxAttempts = 2
        repeat(maxAttempts) { attempt ->
            val result = suspendCancellableCoroutine<AiCategorySuggestionOutput?> { continuation ->
                val future = futuresClient.generateContent(request)
                future.addListener(
                    {
                        val parsed = runCatching {
                            val response = future.get()
                            val rawText = response.candidates.firstOrNull()?.text.orEmpty()
                            if (rawText.isBlank()) {
                                null
                            } else {
                                AiCategorySuggestionOutput(
                                    result = safeParseJson(rawText),
                                    rawResponse = rawText,
                                    source = "Gemini Nano"
                                )
                            }
                        }.getOrElse { throwable ->
                            when (throwable) {
                                is GenAiException -> Log.w(TAG, "Nano category refine GenAI exception", throwable)
                                else -> Log.w(TAG, "Nano category refine failed", throwable)
                            }
                            null
                        }
                        if (continuation.isActive) continuation.resume(parsed)
                    },
                    directExecutor
                )
                continuation.invokeOnCancellation { future.cancel(true) }
            }
            if (result != null) {
                return result
            }
            if (attempt < maxAttempts - 1) {
                delay(750L)
            }
        }
        return null
    }

    private suspend fun analyzeWithCloud(
        transaction: TransactionEntity,
        resolution: CategoryResolution,
        allowedCategories: List<String>,
        apiKey: String
    ): AiCategorySuggestionOutput? = withContext(Dispatchers.IO) {
        val prompt = buildPrompt(transaction, resolution, allowedCategories)
        val requestJson = gson.toJson(
            mapOf(
                "contents" to listOf(
                    mapOf(
                        "parts" to listOf(mapOf("text" to prompt))
                    )
                ),
                "generationConfig" to mapOf(
                    "temperature" to 0.1,
                    "maxOutputTokens" to 256,
                    "topP" to 0.9
                )
            )
        )
        val request = Request.Builder()
            .url("$CLOUD_URL?key=$apiKey")
            .post(requestJson.toRequestBody("application/json".toMediaType()))
            .build()
        runCatching {
            cloudClient.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Cloud category refine HTTP ${response.code}")
                    return@withContext null
                }
                val body = response.body?.string().orEmpty()
                val rawText = extractCloudText(body).orEmpty()
                if (rawText.isBlank()) {
                    return@withContext null
                }
                AiCategorySuggestionOutput(
                    result = safeParseJson(rawText),
                    rawResponse = rawText,
                    source = "Gemma 3 27B"
                )
            }
        }.getOrElse { throwable ->
            Log.e(TAG, "Cloud category refine failed", throwable)
            null
        }
    }

    @VisibleForTesting
    fun safeParseJson(raw: String): AiCategorySuggestion? {
        return runCatching {
            val cleaned = raw
                .replace("```json", "", ignoreCase = true)
                .replace("```", "")
                .trim()
            val start = cleaned.indexOf('{')
            val end = cleaned.lastIndexOf('}')
            if (start == -1 || end == -1 || start >= end) {
                return null
            }
            gson.fromJson(cleaned.substring(start, end + 1), AiCategorySuggestion::class.java)
        }.getOrNull()
    }

    private fun buildPrompt(
        transaction: TransactionEntity,
        resolution: CategoryResolution,
        allowedCategories: List<String>
    ): String {
        return """
            You are refining a SpendWise transaction category for an Indian finance app.

            Pick ONLY from these allowed categories:
            ${allowedCategories.joinToString(", ")}

            Transaction:
            - type: ${transaction.type.name.lowercase()}
            - amount: ${"%.2f".format(transaction.amount)}
            - merchant: ${transaction.merchant}
            - bank/account: ${transaction.accountLabel.ifBlank { transaction.bank }}
            - current_category: ${transaction.category}
            - raw_sms: ${transaction.rawSms}

            Resolver context:
            - resolver_category: ${resolution.category}
            - resolver_bucket: ${resolution.bucketLabel}
            - normalized_merchant: ${resolution.normalizedMerchant}
            - matched_keywords: ${resolution.matchedKeywords.joinToString(", ").ifBlank { "none" }}

            Rules:
            - Do not invent a category outside the allowed list.
            - If the current category already looks correct, set keep_current to true.
            - Use merchant meaning over payment rail when merchant context is clearly stronger.
            - Return JSON only.

            {
              "suggested_category": "<one allowed category>",
              "confidence": <number between 0 and 1>,
              "reason": "<short reason>",
              "keep_current": true or false
            }
        """.trimIndent()
    }

    private fun extractCloudText(responseBody: String): String? {
        return runCatching {
            val root = gson.fromJson(responseBody, Map::class.java)
            val candidates = root["candidates"] as? List<*> ?: return null
            val candidate = candidates.firstOrNull() as? Map<*, *> ?: return null
            val content = candidate["content"] as? Map<*, *> ?: return null
            val parts = content["parts"] as? List<*> ?: return null
            val part = parts.firstOrNull() as? Map<*, *> ?: return null
            part["text"] as? String
        }.getOrNull()
    }

    private suspend fun <T> ListenableFuture<T>.await(): T {
        return suspendCancellableCoroutine { continuation ->
            addListener(
                {
                    try {
                        continuation.resume(get())
                    } catch (throwable: Throwable) {
                        continuation.resumeWithException(throwable)
                    }
                },
                directExecutor
            )
            continuation.invokeOnCancellation {
                cancel(true)
                mainHandler.removeCallbacksAndMessages(null)
            }
        }
    }
}
