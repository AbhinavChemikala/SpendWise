package com.yourapp.spendwise.backup

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.room.withTransaction
import com.google.android.gms.auth.GoogleAuthUtil
import com.google.android.gms.auth.UserRecoverableAuthException
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.Scope
import com.google.gson.Gson
import com.yourapp.spendwise.background.BackupScheduler
import com.yourapp.spendwise.background.DailyReminderScheduler
import com.yourapp.spendwise.data.SettingsStore
import com.yourapp.spendwise.data.db.AppDatabase
import com.yourapp.spendwise.mail.GmailAxisSyncManager
import com.yourapp.spendwise.widget.WidgetUpdater
import java.net.URLEncoder
import java.nio.charset.StandardCharsets
import java.util.UUID
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody

private data class DriveFilesResponse(
    val files: List<DriveFile> = emptyList()
)

private data class DriveFile(
    val id: String = "",
    val name: String = "",
    val modifiedTime: String = "",
    val size: String? = null
)

class SpendWiseBackupManager(context: Context) {
    private val appContext = context.applicationContext
    private val database = AppDatabase.getInstance(appContext)
    private val settingsStore = SettingsStore(appContext)
    private val gson = Gson()

    suspend fun exportToUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        val backup = createBackupFile()
        val json = gson.toJson(backup)
        appContext.contentResolver.openOutputStream(uri)?.use { output ->
            output.write(json.toByteArray(StandardCharsets.UTF_8))
        } ?: error("Unable to open backup file.")
        val result = backup.toResult(location = "Local file", message = "Backup exported.")
        settingsStore.appendBackupHistory(
            result.toHistory(
                trigger = BackupTrigger.MANUAL,
                destination = BackupDestination.LOCAL,
                status = BackupStatus.SUCCESS
            )
        )
        result
    }

    suspend fun restoreFromUri(uri: Uri): BackupResult = withContext(Dispatchers.IO) {
        val json = appContext.contentResolver.openInputStream(uri)?.use { input ->
            input.bufferedReader(StandardCharsets.UTF_8).readText()
        } ?: error("Unable to open backup file.")
        val backup = parseBackup(json)
        restoreBackup(backup)
        val result = backup.toResult(location = "Local file", message = "Backup restored.")
        settingsStore.appendBackupHistory(
            result.toHistory(
                trigger = BackupTrigger.RESTORE,
                destination = BackupDestination.LOCAL,
                status = BackupStatus.SUCCESS
            )
        )
        result
    }

    suspend fun pushToDrive(trigger: String = BackupTrigger.MANUAL): BackupResult = withContext(Dispatchers.IO) {
        runCatching {
            val accountEmail = settingsStore.getDriveBackupAccount()
            if (accountEmail.isBlank()) {
                error("Connect Google Drive backup first.")
            }
            val token = getDriveToken(accountEmail)
            val backup = createBackupFile()
            val json = gson.toJson(backup)
            val existingFile = findLatestDriveBackup(token)
            if (existingFile == null) {
                createDriveBackup(token, json)
            } else {
                updateDriveBackup(token, existingFile.id, json)
            }
            backup.toResult(
                location = "Google Drive",
                message = "Drive backup pushed."
            )
        }.onSuccess { result ->
            settingsStore.appendBackupHistory(
                result.toHistory(
                    trigger = trigger,
                    destination = BackupDestination.GOOGLE_DRIVE,
                    status = BackupStatus.SUCCESS
                )
            )
        }.onFailure { throwable ->
            val message = throwable.friendlyBackupMessage()
            settingsStore.appendBackupHistory(
                BackupHistoryEntry(
                    trigger = trigger,
                    destination = BackupDestination.GOOGLE_DRIVE,
                    status = BackupStatus.FAILED,
                    message = message
                )
            )
            Log.w(TAG, "Drive backup failed.", throwable)
        }.getOrElse { throwable ->
            BackupResult(location = "Google Drive", message = throwable.friendlyBackupMessage())
        }
    }

    suspend fun restoreLatestFromDrive(): BackupResult = withContext(Dispatchers.IO) {
        runCatching {
            val accountEmail = settingsStore.getDriveBackupAccount()
            if (accountEmail.isBlank()) {
                error("Connect Google Drive backup first.")
            }
            val token = getDriveToken(accountEmail)
            val file = findLatestDriveBackup(token) ?: error("No SpendWise backup found in Drive.")
            val json = downloadDriveBackup(token, file.id)
            val backup = parseBackup(json)
            restoreBackup(backup)
            backup.toResult(location = "Google Drive", message = "Drive backup restored.")
        }.onSuccess { result ->
            settingsStore.appendBackupHistory(
                result.toHistory(
                    trigger = BackupTrigger.RESTORE,
                    destination = BackupDestination.GOOGLE_DRIVE,
                    status = BackupStatus.SUCCESS
                )
            )
        }.onFailure { throwable ->
            val message = throwable.friendlyBackupMessage()
            settingsStore.appendBackupHistory(
                BackupHistoryEntry(
                    trigger = BackupTrigger.RESTORE,
                    destination = BackupDestination.GOOGLE_DRIVE,
                    status = BackupStatus.FAILED,
                    message = message
                )
            )
            Log.w(TAG, "Drive restore failed.", throwable)
        }.getOrElse { throwable ->
            BackupResult(location = "Google Drive", message = throwable.friendlyBackupMessage())
        }
    }

    private suspend fun createBackupFile(): SpendWiseBackupFile {
        return SpendWiseBackupFile(
            transactions = database.transactionDao().getAllTransactionsList(),
            smsReviewEvents = database.smsReviewDao().getAll(),
            pendingSms = database.pendingSmsDao().getAll(),
            categoryAiRecords = database.transactionCategoryAiDao().getAll(),
            settings = settingsStore.exportBackupSettings()
        )
    }

    private suspend fun restoreBackup(backup: SpendWiseBackupFile) {
        if (backup.schemaVersion > CURRENT_SCHEMA_VERSION) {
            error("This backup was created by a newer SpendWise version.")
        }
        database.withTransaction {
            database.transactionCategoryAiDao().deleteAll()
            database.pendingSmsDao().deleteAll()
            database.smsReviewDao().deleteAll()
            database.transactionDao().deleteAll()
            if (backup.transactions.isNotEmpty()) {
                database.transactionDao().insertAll(backup.transactions)
            }
            if (backup.smsReviewEvents.isNotEmpty()) {
                database.smsReviewDao().insertAll(backup.smsReviewEvents)
            }
            if (backup.pendingSms.isNotEmpty()) {
                database.pendingSmsDao().insertAll(backup.pendingSms)
            }
            if (backup.categoryAiRecords.isNotEmpty()) {
                database.transactionCategoryAiDao().upsertAll(backup.categoryAiRecords)
            }
        }
        settingsStore.applyBackupSettings(backup.settings)
        DailyReminderScheduler.scheduleNext(appContext)
        BackupScheduler.scheduleNext(appContext)
        GmailAxisSyncManager.ensureScheduled(appContext)
        WidgetUpdater.updateAll(appContext)
    }

    private fun parseBackup(json: String): SpendWiseBackupFile {
        return runCatching { gson.fromJson(json, SpendWiseBackupFile::class.java) }
            .getOrNull()
            ?: error("This does not look like a SpendWise backup.")
    }

    private fun getDriveToken(accountEmail: String): String {
        return GoogleAuthUtil.getToken(appContext, accountEmail, DRIVE_OAUTH_SCOPE)
    }

    private fun findLatestDriveBackup(token: String): DriveFile? {
        val query = "name='$DRIVE_BACKUP_FILE_NAME' and trashed=false"
        val url = "https://www.googleapis.com/drive/v3/files" +
            "?spaces=appDataFolder" +
            "&q=${URLEncoder.encode(query, "UTF-8")}" +
            "&fields=files(id,name,modifiedTime,size)" +
            "&orderBy=modifiedTime desc" +
            "&pageSize=1"
        val response = executeDriveRequest(
            Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()
        )
        return gson.fromJson(response, DriveFilesResponse::class.java).files.firstOrNull()
    }

    private fun createDriveBackup(token: String, json: String) {
        val metadataJson = gson.toJson(
            mapOf(
                "name" to DRIVE_BACKUP_FILE_NAME,
                "parents" to listOf("appDataFolder"),
                "mimeType" to BACKUP_MIME_TYPE
            )
        )
        val body = MultipartBody.Builder("SpendWiseBackup-${UUID.randomUUID()}")
            .setType(MultipartBody.FORM)
            .addFormDataPart(
                "metadata",
                null,
                metadataJson.toRequestBody("application/json; charset=utf-8".toMediaType())
            )
            .addFormDataPart(
                "file",
                DRIVE_BACKUP_FILE_NAME,
                json.toRequestBody(BACKUP_MIME_TYPE.toMediaType())
            )
            .build()
        executeDriveRequest(
            Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files?uploadType=multipart&fields=id")
                .addHeader("Authorization", "Bearer $token")
                .post(body)
                .build()
        )
    }

    private fun updateDriveBackup(token: String, fileId: String, json: String) {
        executeDriveRequest(
            Request.Builder()
                .url("https://www.googleapis.com/upload/drive/v3/files/$fileId?uploadType=media")
                .addHeader("Authorization", "Bearer $token")
                .patch(json.toRequestBody(BACKUP_MIME_TYPE.toMediaType()))
                .build()
        )
    }

    private fun downloadDriveBackup(token: String, fileId: String): String {
        return executeDriveRequest(
            Request.Builder()
                .url("https://www.googleapis.com/drive/v3/files/$fileId?alt=media")
                .addHeader("Authorization", "Bearer $token")
                .get()
                .build()
        )
    }

    private fun executeDriveRequest(request: Request): String {
        CLIENT.newCall(request).execute().use { response ->
            val body = response.body?.string().orEmpty()
            if (!response.isSuccessful) {
                val message = when (response.code) {
                    401, 403 -> "Drive permission failed. Enable Google Drive API for the project, then reconnect Drive backup."
                    404 -> "Drive backup file was not found."
                    else -> "Drive request failed: ${response.code} ${body.take(180)}"
                }
                error(message)
            }
            return body
        }
    }

    private fun SpendWiseBackupFile.toResult(location: String, message: String): BackupResult {
        return BackupResult(
            location = location,
            itemCount = itemCount(),
            transactionCount = transactions.size,
            message = message
        )
    }

    private fun SpendWiseBackupFile.itemCount(): Int {
        return transactions.size +
            smsReviewEvents.size +
            pendingSms.size +
            categoryAiRecords.size +
            settings.customCategories.size +
            settings.transactionRules.size +
            settings.budgetGoals.size
    }

    private fun BackupResult.toHistory(
        trigger: String,
        destination: String,
        status: String
    ): BackupHistoryEntry {
        return BackupHistoryEntry(
            trigger = trigger,
            destination = destination,
            status = status,
            itemCount = itemCount,
            transactionCount = transactionCount,
            message = message
        )
    }

    private fun Throwable.friendlyBackupMessage(): String {
        return when (this) {
            is UserRecoverableAuthException -> "Google needs permission again. Reconnect Drive backup."
            else -> message?.takeIf { it.isNotBlank() } ?: "Backup failed."
        }
    }

    companion object {
        private const val TAG = "SpendWiseBackup"
        private const val CURRENT_SCHEMA_VERSION = 1
        private const val DRIVE_BACKUP_FILE_NAME = "spendwise-backup.json"
        private const val BACKUP_MIME_TYPE = "application/json; charset=utf-8"
        private const val DRIVE_SCOPE_URI = "https://www.googleapis.com/auth/drive.appdata"
        private const val DRIVE_OAUTH_SCOPE = "oauth2:$DRIVE_SCOPE_URI"
        private val CLIENT = OkHttpClient()

        fun driveScope(): Scope = Scope(DRIVE_SCOPE_URI)

        fun buildDriveSignInClient(context: Context): GoogleSignInClient {
            val options = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestScopes(driveScope())
                .build()
            return GoogleSignIn.getClient(context.applicationContext, options)
        }
    }
}
