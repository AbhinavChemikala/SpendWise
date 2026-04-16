package com.yourapp.spendwise.mail

import android.content.Context
import android.util.Base64
import android.util.Log
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.OutOfQuotaPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import com.yourapp.spendwise.data.SettingsStore
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.data.db.PendingSmsEntity
import com.yourapp.spendwise.data.db.TransactionEntity
import com.yourapp.spendwise.sms.AiProcessingService
import com.yourapp.spendwise.sms.SmsIntakeManager
import com.yourapp.spendwise.sms.SmsIntakeOutcome
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.abs
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

data class AxisEmailSyncResult(
    val scanned: Int = 0,
    val imported: Int = 0,
    val duplicates: Int = 0,
    val skipped: Int = 0,
    val message: String = ""
)

private data class GmailListResponse(
    val messages: List<GmailMessageRef> = emptyList()
)

private data class GmailMessageRef(
    val id: String = ""
)

private data class GmailMessageResponse(
    val id: String = "",
    val snippet: String = "",
    val internalDate: String = "",
    val payload: GmailPayload? = null
)

private data class GmailPayload(
    val mimeType: String? = null,
    val headers: List<GmailHeader> = emptyList(),
    val body: GmailBody? = null,
    val parts: List<GmailPayload> = emptyList()
)

private data class GmailHeader(
    val name: String = "",
    val value: String = ""
)

private data class GmailBody(
    val data: String? = null
)

object GmailAxisSyncManager {
    private const val TAG = "GmailAxisSync"
    private const val QUERY_SENDER = "from:alerts@axis.bank.in"
    private const val WORK_NAME_PERIODIC = "axis_email_periodic_sync"
    private const val WORK_NAME_IMMEDIATE = "axis_email_immediate_sync"
    const val INPUT_TRIGGER = "axis_email_sync_trigger"
    private const val SCOPE_URI = "https://www.googleapis.com/auth/gmail.readonly"
    private const val OAUTH_SCOPE = "oauth2:$SCOPE_URI"
    private const val MAX_MESSAGES_PER_SYNC = 20L
    private const val DUPLICATE_WINDOW_MS = 10L * 60L * 1000L
    private val accountNumberRegex = Regex("""(?i)\b(?:account|a/c)\s*(?:number|no\.?)?\s*:\s*([A-Z0-9*X-]{4,})""")

    private val gson = Gson()
    private val client = OkHttpClient()

    fun gmailScope(): Scope = Scope(SCOPE_URI)

    fun buildSignInClient(context: Context): GoogleSignInClient {
        val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestEmail()
            .requestScopes(gmailScope())
            .build()
        return GoogleSignIn.getClient(context.applicationContext, options)
    }

    fun onAccountConnected(context: Context, email: String) {
        val settings = SettingsStore(context.applicationContext)
        settings.setAxisEmailAccount(email)
        if (settings.getAxisEmailLastSyncMs() == 0L) {
            settings.setAxisEmailLastSyncMs(System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30))
        }
        settings.setAxisEmailAutoSyncEnabled(true)
        schedulePeriodicSync(context)
    }

    fun disconnect(context: Context) {
        val appContext = context.applicationContext
        SettingsStore(appContext).clearAxisEmailAccount()
        WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME_PERIODIC)
        WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME_IMMEDIATE)
    }

    fun setAutoSyncEnabled(context: Context, enabled: Boolean) {
        val appContext = context.applicationContext
        val settings = SettingsStore(appContext)
        settings.setAxisEmailAutoSyncEnabled(enabled)
        if (enabled && settings.getAxisEmailAccount().isNotBlank()) {
            schedulePeriodicSync(appContext)
        } else {
            WorkManager.getInstance(appContext).cancelUniqueWork(WORK_NAME_PERIODIC)
        }
    }

    fun ensureScheduled(context: Context) {
        val settings = SettingsStore(context.applicationContext)
        if (settings.getAxisEmailAccount().isNotBlank() && settings.isAxisEmailAutoSyncEnabled()) {
            schedulePeriodicSync(context)
        }
    }

    fun enqueueImmediateSync(
        context: Context,
        trigger: String = AxisEmailSyncTrigger.MANUAL
    ) {
        val request = OneTimeWorkRequestBuilder<GmailAxisSyncWorker>()
            .setInputData(Data.Builder().putString(INPUT_TRIGGER, trigger).build())
            .setExpedited(OutOfQuotaPolicy.RUN_AS_NON_EXPEDITED_WORK_REQUEST)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniqueWork(
            WORK_NAME_IMMEDIATE,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    private fun schedulePeriodicSync(context: Context) {
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        val request = PeriodicWorkRequestBuilder<GmailAxisSyncWorker>(15, TimeUnit.MINUTES)
            .setInputData(Data.Builder().putString(INPUT_TRIGGER, AxisEmailSyncTrigger.PERIODIC).build())
            .setConstraints(constraints)
            .build()
        WorkManager.getInstance(context.applicationContext).enqueueUniquePeriodicWork(
            WORK_NAME_PERIODIC,
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    suspend fun syncNow(
        context: Context,
        trigger: String = AxisEmailSyncTrigger.MANUAL
    ): AxisEmailSyncResult = withContext(Dispatchers.IO) {
        val appContext = context.applicationContext
        val settings = SettingsStore(appContext)
        val accountEmail = settings.getAxisEmailAccount()
        val startedAt = System.currentTimeMillis()
        val historyItems = mutableListOf<AxisEmailSyncHistoryItem>()
        if (accountEmail.isBlank()) {
            val result = AxisEmailSyncResult(message = "Connect Gmail first.")
            settings.appendAxisEmailSyncHistory(
                buildHistoryEntry(
                    account = "",
                    trigger = trigger,
                    startedAt = startedAt,
                    result = result,
                    items = historyItems,
                    status = "NO_ACCOUNT"
                )
            )
            return@withContext result
        }

        val accessToken = try {
            GoogleAuthUtil.getToken(appContext, accountEmail, OAUTH_SCOPE)
        } catch (recoverable: UserRecoverableAuthException) {
            Log.w(TAG, "User action required for Gmail token", recoverable)
            val result = AxisEmailSyncResult(message = "Reconnect Gmail to continue Axis mail sync.")
            settings.appendAxisEmailSyncHistory(
                buildHistoryEntry(
                    account = accountEmail,
                    trigger = trigger,
                    startedAt = startedAt,
                    result = result,
                    items = historyItems,
                    status = "AUTH_REQUIRED"
                )
            )
            return@withContext result
        } catch (throwable: Throwable) {
            Log.e(TAG, "Unable to get Gmail access token", throwable)
            val result = AxisEmailSyncResult(message = "Unable to access Gmail right now.")
            settings.appendAxisEmailSyncHistory(
                buildHistoryEntry(
                    account = accountEmail,
                    trigger = trigger,
                    startedAt = startedAt,
                    result = result,
                    items = historyItems,
                    status = "AUTH_FAILED"
                )
            )
            return@withContext result
        }

        val lastSyncMs = settings.getAxisEmailLastSyncMs()
        var recentMessageIds = settings.getAxisEmailRecentMessageIds().toMutableList()
        val recentIdSet = recentMessageIds.toMutableSet()
        val messageIds = listMessageIds(accessToken, lastSyncMs)
        if (messageIds.isEmpty()) {
            settings.setAxisEmailLastSyncMs(System.currentTimeMillis())
            val result = AxisEmailSyncResult(message = "No new Axis emails found.")
            settings.appendAxisEmailSyncHistory(
                buildHistoryEntry(
                    account = accountEmail,
                    trigger = trigger,
                    startedAt = startedAt,
                    result = result,
                    items = historyItems,
                    status = "NO_MATCHES"
                )
            )
            return@withContext result
        }

        val database = AppDatabase.getInstance(appContext)
        val transactionDao = database.transactionDao()
        val pendingSmsDao = database.pendingSmsDao()

        var scanned = 0
        var imported = 0
        var duplicates = 0
        var skipped = 0
        var maxSeenTimestamp = lastSyncMs

        messageIds.forEach { messageId ->
            if (!recentIdSet.add(messageId)) {
                return@forEach
            }
            scanned += 1
            val message = fetchMessage(accessToken, messageId)
            if (message == null) {
                historyItems += AxisEmailSyncHistoryItem(
                    messageId = messageId,
                    receivedAt = System.currentTimeMillis(),
                    outcome = "Fetch failed",
                    from = "alerts@axis.bank.in"
                )
                skipped += 1
                return@forEach
            }
            val internalDate = message.internalDate.toLongOrNull() ?: System.currentTimeMillis()
            maxSeenTimestamp = maxOf(maxSeenTimestamp, internalDate)
            val body = extractBody(message).ifBlank { message.snippet }
            val candidate = AxisEmailParser.parse(body, internalDate)
            if (candidate == null) {
                historyItems += AxisEmailSyncHistoryItem(
                    messageId = messageId,
                    receivedAt = internalDate,
                    outcome = "No transaction found",
                    from = extractHeader(message, "From").ifBlank { "alerts@axis.bank.in" },
                    summary = message.snippet.ifBlank { body.take(180) },
                    fullBody = body
                )
                skipped += 1
                return@forEach
            }
            if (isDuplicate(candidate, transactionDao.getAllTransactionsList(), pendingSmsDao.getAll())) {
                historyItems += AxisEmailSyncHistoryItem(
                    messageId = messageId,
                    receivedAt = candidate.timestampMs,
                    outcome = "Skipped duplicate",
                    from = extractHeader(message, "From").ifBlank { "alerts@axis.bank.in" },
                    summary = message.snippet.ifBlank { candidate.normalizedBody.take(180) },
                    parsedAmount = candidate.amount,
                    parsedType = candidate.type.name,
                    parsedMerchant = candidate.merchantHint,
                    cleanedBody = candidate.normalizedBody,
                    fullBody = candidate.fullBody
                )
                duplicates += 1
                return@forEach
            }

            when (val intakeOutcome =
                SmsIntakeManager.ingestEmailCandidate(
                    context = appContext,
                    sender = "alerts@axis.bank.in",
                    body = candidate.normalizedBody,
                    timestamp = candidate.timestampMs,
                    amount = candidate.amount,
                    type = candidate.type,
                    merchant = candidate.merchantHint,
                    bank = "Axis Bank",
                    eventSource = "EMAIL",
                    emitNotifications = true,
                    emitPendingEvent = true
                )
            ) {
                is SmsIntakeOutcome.Confirmed -> {
                    imported += 1
                    historyItems += AxisEmailSyncHistoryItem(
                        messageId = messageId,
                        receivedAt = candidate.timestampMs,
                        outcome = "Added instantly",
                        from = extractHeader(message, "From").ifBlank { "alerts@axis.bank.in" },
                        summary = message.snippet.ifBlank { candidate.normalizedBody.take(180) },
                        parsedAmount = candidate.amount,
                        parsedType = candidate.type.name,
                        parsedMerchant = candidate.merchantHint,
                        cleanedBody = candidate.normalizedBody,
                        fullBody = candidate.fullBody
                    )
                }
                is SmsIntakeOutcome.Pending -> {
                    AiProcessingService.start(appContext)
                    imported += 1
                    historyItems += AxisEmailSyncHistoryItem(
                        messageId = messageId,
                        receivedAt = candidate.timestampMs,
                        outcome = "Queued for AI",
                        from = extractHeader(message, "From").ifBlank { "alerts@axis.bank.in" },
                        summary = message.snippet.ifBlank { candidate.normalizedBody.take(180) },
                        parsedAmount = candidate.amount,
                        parsedType = candidate.type.name,
                        parsedMerchant = candidate.merchantHint,
                        cleanedBody = candidate.normalizedBody,
                        fullBody = candidate.fullBody
                    )
                }
                SmsIntakeOutcome.Discarded -> {
                    skipped += 1
                    historyItems += AxisEmailSyncHistoryItem(
                        messageId = messageId,
                        receivedAt = candidate.timestampMs,
                        outcome = "Skipped by intake",
                        from = extractHeader(message, "From").ifBlank { "alerts@axis.bank.in" },
                        summary = message.snippet.ifBlank { candidate.normalizedBody.take(180) },
                        parsedAmount = candidate.amount,
                        parsedType = candidate.type.name,
                        parsedMerchant = candidate.merchantHint,
                        cleanedBody = candidate.normalizedBody,
                        fullBody = candidate.fullBody
                    )
                }
            }
        }

        if (recentIdSet.size != recentMessageIds.size) {
            recentMessageIds = recentIdSet.toList().takeLast(200).toMutableList()
            settings.setAxisEmailRecentMessageIds(recentMessageIds)
        }
        settings.setAxisEmailLastSyncMs(maxOf(maxSeenTimestamp, System.currentTimeMillis()))

        val result = AxisEmailSyncResult(
            scanned = scanned,
            imported = imported,
            duplicates = duplicates,
            skipped = skipped,
            message = when {
                scanned == 0 -> "No new Axis emails found."
                imported > 0 -> "Imported $imported Axis email transactions."
                duplicates > 0 -> "Axis emails checked. Matching SMS/transactions already existed."
                else -> "Axis emails checked."
            }
        )
        settings.appendAxisEmailSyncHistory(
            buildHistoryEntry(
                account = accountEmail,
                trigger = trigger,
                startedAt = startedAt,
                result = result,
                items = historyItems,
                status = "SUCCESS"
            )
        )
        result
    }

    private fun listMessageIds(accessToken: String, lastSyncMs: Long): List<String> {
        val afterSeconds = ((if (lastSyncMs > 0L) lastSyncMs else System.currentTimeMillis() - TimeUnit.DAYS.toMillis(30)) / 1000L) - 3600L
        val url = "https://gmail.googleapis.com/gmail/v1/users/me/messages?q=${java.net.URLEncoder.encode("$QUERY_SENDER after:$afterSeconds", "UTF-8")}&maxResults=$MAX_MESSAGES_PER_SYNC"
        val request = Request.Builder()
            .url(url)
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gmail list failed ${response.code}")
                    emptyList()
                } else {
                    val body = response.body?.string().orEmpty()
                    gson.fromJson(body, GmailListResponse::class.java)
                        ?.messages
                        .orEmpty()
                        .mapNotNull { it.id.takeIf(String::isNotBlank) }
                }
            }
        }.getOrElse {
            Log.e(TAG, "Unable to list Gmail messages", it)
            emptyList()
        }
    }

    private fun fetchMessage(accessToken: String, messageId: String): GmailMessageResponse? {
        val request = Request.Builder()
            .url("https://gmail.googleapis.com/gmail/v1/users/me/messages/$messageId?format=full")
            .addHeader("Authorization", "Bearer $accessToken")
            .build()
        return runCatching {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) {
                    Log.w(TAG, "Gmail fetch failed ${response.code} for $messageId")
                    null
                } else {
                    gson.fromJson(response.body?.string().orEmpty(), GmailMessageResponse::class.java)
                }
            }
        }.getOrElse {
            Log.e(TAG, "Unable to fetch Gmail message $messageId", it)
            null
        }
    }

    private fun extractBody(message: GmailMessageResponse): String {
        return extractPayloadBody(message.payload).ifBlank { message.snippet }
    }

    private fun extractHeader(message: GmailMessageResponse, name: String): String {
        return message.payload?.headers
            .orEmpty()
            .firstOrNull { it.name.equals(name, ignoreCase = true) }
            ?.value
            .orEmpty()
    }

    private fun extractPayloadBody(payload: GmailPayload?): String {
        if (payload == null) return ""
        val directBody = decodeBody(payload.body?.data)
        if (payload.mimeType.equals("text/plain", ignoreCase = true) && directBody.isNotBlank()) {
            return directBody
        }
        payload.parts.forEach { part ->
            val body = extractPayloadBody(part)
            if (body.isNotBlank()) return body
        }
        return if (payload.mimeType.equals("text/html", ignoreCase = true)) directBody else ""
    }

    private fun decodeBody(data: String?): String {
        if (data.isNullOrBlank()) return ""
        return runCatching {
            String(
                Base64.decode(data, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING),
                Charsets.UTF_8
            )
        }.getOrDefault("")
    }

    private fun isDuplicate(
        candidate: AxisEmailCandidate,
        transactions: List<TransactionEntity>,
        pendingSms: List<PendingSmsEntity>
    ): Boolean {
        val candidateAccountHint = extractAccountHint(candidate.normalizedBody)
        val hasTransactionMatch = transactions.any { transaction ->
            abs(transaction.timestamp - candidate.timestampMs) <= DUPLICATE_WINDOW_MS &&
                transaction.type == candidate.type &&
                abs(transaction.amount - candidate.amount) < 0.01 &&
                isAxisRelated(transaction) &&
                accountMatches(candidateAccountHint, transaction) &&
                (
                    candidate.reference.isBlank() ||
                        transaction.rawSms.contains(candidate.reference, ignoreCase = true) ||
                        transaction.note.contains(candidate.reference, ignoreCase = true)
                    )
        }
        if (hasTransactionMatch) return true

        return pendingSms.any { pending ->
            abs(pending.receivedAt - candidate.timestampMs) <= DUPLICATE_WINDOW_MS &&
                pending.sender.contains("AXIS", ignoreCase = true) &&
                pending.body.contains(candidate.amount.toInt().toString()) &&
                accountMatches(candidateAccountHint, pending.body)
        }
    }

    private fun isAxisRelated(transaction: TransactionEntity): Boolean {
        return transaction.bank.contains("axis", ignoreCase = true) ||
            transaction.sourceSender.contains("axis", ignoreCase = true) ||
            transaction.accountLabel.contains("axis", ignoreCase = true)
    }

    private fun extractAccountHint(body: String): String {
        return accountNumberRegex.find(body)
            ?.groupValues
            ?.getOrNull(1)
            .orEmpty()
            .takeLast(4)
            .filter(Char::isDigit)
    }

    private fun accountMatches(candidateAccountHint: String, transaction: TransactionEntity): Boolean {
        if (candidateAccountHint.isBlank()) return true
        val source = listOf(transaction.accountLabel, transaction.rawSms, transaction.note, transaction.bank)
            .joinToString(" ")
        return source.contains(candidateAccountHint, ignoreCase = true)
    }

    private fun accountMatches(candidateAccountHint: String, body: String): Boolean {
        if (candidateAccountHint.isBlank()) return true
        return body.contains(candidateAccountHint, ignoreCase = true)
    }

    private fun buildHistoryEntry(
        account: String,
        trigger: String,
        startedAt: Long,
        result: AxisEmailSyncResult,
        items: List<AxisEmailSyncHistoryItem>,
        status: String
    ): AxisEmailSyncHistoryEntry {
        return AxisEmailSyncHistoryEntry(
            id = "$startedAt-${trigger.lowercase(Locale.ENGLISH)}",
            startedAt = startedAt,
            finishedAt = System.currentTimeMillis(),
            trigger = trigger,
            account = account,
            status = status,
            scanned = result.scanned,
            imported = result.imported,
            duplicates = result.duplicates,
            skipped = result.skipped,
            message = result.message,
            items = items
        )
    }
}
