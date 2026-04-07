package com.yourapp.spendwise.sms

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.common.util.concurrent.ListenableFuture
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.google.mlkit.genai.common.DownloadCallback
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.common.GenAiException
import com.google.mlkit.genai.prompt.GenerateContentRequest
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.GenerationConfig
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.java.GenerativeModelFutures
import java.util.concurrent.Executor
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

data class AiTransactionResult(
    @SerializedName("thought_process") val thoughtProcess: String = "",
    @SerializedName("is_genuine") val isGenuine: Boolean = false,
    val type: String = "unknown",
    val amount: Double = 0.0,
    val merchant: String = "",
    val bank: String = "",
    val reason: String = "",
    @SerializedName("card_last4") val cardLast4: String = "",
    @SerializedName("card_type")  val cardType: String = ""   // "credit", "debit", or ""
)

data class AiAnalysisOutput(
    val result: AiTransactionResult?,
    val rawResponse: String,
    val source: String = "Gemini Nano"  // "Gemini Nano" or "Gemma 3 27B"
)

object GeminiNanoAnalyzer {
    private const val TAG = "GeminiNanoAnalyzer"

    private val gson = Gson()
    private val mainHandler = Handler(Looper.getMainLooper())
    private val analyzerScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val directExecutor = Executor { runnable -> runnable.run() }

    private val generativeModel by lazy {
        Generation.getClient(GenerationConfig.Builder().build())
    }

    private val futuresClient by lazy {
        GenerativeModelFutures.from(generativeModel)
    }

    fun ensureModelReady(
        onReady: () -> Unit = {},
        onError: (Throwable) -> Unit = {}
    ) {
        analyzerScope.launch {
            runCatching {
                when (futuresClient.checkStatus().await()) {
                    FeatureStatus.AVAILABLE -> {
                        runCatching { futuresClient.warmup().await() }
                    }

                    FeatureStatus.DOWNLOADABLE,
                    FeatureStatus.DOWNLOADING -> {
                        suspendCancellableCoroutine<Unit> { continuation ->
                            val future = futuresClient.download(object : DownloadCallback {
                                override fun onDownloadCompleted() {
                                    if (continuation.isActive) {
                                        continuation.resume(Unit)
                                    }
                                }

                                override fun onDownloadFailed(exception: GenAiException) {
                                    if (continuation.isActive) {
                                        continuation.resumeWithException(exception)
                                    }
                                }

                                override fun onDownloadProgress(bytesDownloaded: Long) = Unit

                                override fun onDownloadStarted(totalBytes: Long) = Unit
                            })

                            continuation.invokeOnCancellation { future.cancel(true) }
                        }
                        runCatching { futuresClient.warmup().await() }
                    }

                    else -> error("Gemini Nano is not available on this device.")
                }
            }.onSuccess {
                postToMain(onReady)
            }.onFailure { throwable ->
                postToMain { onError(throwable) }
            }
        }
    }

    suspend fun analyze(smsSender: String, smsBody: String): AiTransactionResult? {
        return analyzeDetailed(smsSender = smsSender, smsBody = smsBody)?.result
    }

    suspend fun analyzeDetailed(smsSender: String, smsBody: String): AiAnalysisOutput? {
        // Retry up to 3 times with exponential backoff.
        // Gemini Nano is an on-device model with strict resource limits. Under
        // continuous load (batch import), it can transiently fail. Retrying with
        // a pause gives the model time to recover instead of silently rejecting
        // a legitimate transaction.
        val maxAttempts = 3
        var lastResult: AiAnalysisOutput? = null

        for (attempt in 1..maxAttempts) {
            lastResult = analyzeOnce(smsSender, smsBody)

            if (lastResult != null) {
                return lastResult  // Successful response — done
            }

            if (attempt < maxAttempts) {
                // Exponential backoff: 1s, 2s before the 2nd and 3rd attempts
                val backoffMs = attempt * 1000L
                Log.w(TAG, "Nano returned null for sender=$smsSender, attempt=$attempt. Retrying in ${backoffMs}ms...")
                delay(backoffMs)
            }
        }

        Log.e(TAG, "Nano failed all $maxAttempts attempts for sender=$smsSender")
        return null
    }

    private suspend fun analyzeOnce(smsSender: String, smsBody: String): AiAnalysisOutput? {
        val prompt = buildPrompt(smsSender = smsSender, smsBody = smsBody)
        val request = GenerateContentRequest.Builder(TextPart(prompt)).build()

        return suspendCancellableCoroutine { continuation ->
            val future = futuresClient.generateContent(request)
            future.addListener(
                {
                    val analysis = runCatching {
                        val response = future.get()
                        val rawText = response.candidates.firstOrNull()?.text.orEmpty()
                        if (rawText.isBlank()) {
                            // Blank response — treat as failure so retry kicks in
                            null
                        } else {
                            AiAnalysisOutput(
                                result = safeParseJson(rawText),
                                rawResponse = rawText
                            )
                        }
                    }.getOrElse { throwable ->
                        Log.w(TAG, "Gemini Nano inference threw for sender=$smsSender", throwable)
                        null
                    }

                    if (continuation.isActive) {
                        continuation.resume(analysis)
                    }
                },
                directExecutor
            )

            continuation.invokeOnCancellation { future.cancel(true) }
        }
    }

    fun safeParseJson(raw: String): AiTransactionResult? {
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

            gson.fromJson(
                cleaned.substring(start, end + 1),
                AiTransactionResult::class.java
            )
        }.getOrNull()
    }

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
              "card_last4": "<last 4 digits of card/account, or empty string>",
              "card_type": "credit" or "debit" or "",
              "reason": "<one sentence explaining the decision>"
            }
        """.trimIndent()
    }

    private fun postToMain(action: () -> Unit) {
        mainHandler.post(action)
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
            continuation.invokeOnCancellation { cancel(true) }
        }
    }
}
