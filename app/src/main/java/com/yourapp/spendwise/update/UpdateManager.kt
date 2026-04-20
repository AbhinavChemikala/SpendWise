package com.yourapp.spendwise.update

import android.content.Context
import android.content.Intent
import android.net.Uri
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import java.io.File
import java.util.concurrent.TimeUnit

object UpdateManager {

    private const val GITHUB_API =
        "https://api.github.com/repos/AbhinavChemikala/SpendWise/releases/latest"

    data class ReleaseInfo(
        val tagName: String,
        val versionNumber: Int,
        val downloadUrl: String,
        val releaseNotes: String
    )

    sealed class UpdateCheckResult {
        data class UpdateAvailable(val release: ReleaseInfo) : UpdateCheckResult()
        object UpToDate : UpdateCheckResult()
        data class Error(val message: String) : UpdateCheckResult()
    }

    sealed class DownloadResult {
        object Success : DownloadResult()
        data class Error(val message: String) : DownloadResult()
    }

    private val client = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .build()

    suspend fun checkForUpdate(currentVersionCode: Int): UpdateCheckResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder()
                    .url(GITHUB_API)
                    .header("Accept", "application/vnd.github.v3+json")
                    .build()

                val response = client.newCall(request).execute()
                if (!response.isSuccessful) {
                    return@withContext UpdateCheckResult.Error("Server error: ${response.code}")
                }

                val body = response.body?.string()
                    ?: return@withContext UpdateCheckResult.Error("Empty response from server")

                val json = JSONObject(body)
                val tagName = json.getString("tag_name")           // e.g. "v7"
                val versionNumber = tagName.removePrefix("v").toIntOrNull()
                    ?: return@withContext UpdateCheckResult.Error("Could not parse version from tag: $tagName")

                val releaseNotes = json.optString("body", "No release notes available.")

                val assets = json.getJSONArray("assets")
                if (assets.length() == 0) {
                    return@withContext UpdateCheckResult.Error("No APK attached to this release.")
                }

                // Find the APK asset (first .apk file)
                var apkUrl: String? = null
                for (i in 0 until assets.length()) {
                    val asset = assets.getJSONObject(i)
                    val name = asset.getString("name")
                    if (name.endsWith(".apk")) {
                        apkUrl = asset.getString("browser_download_url")
                        break
                    }
                }

                if (apkUrl == null) {
                    return@withContext UpdateCheckResult.Error("No APK file found in release assets.")
                }

                if (versionNumber > currentVersionCode) {
                    UpdateCheckResult.UpdateAvailable(
                        ReleaseInfo(tagName, versionNumber, apkUrl, releaseNotes)
                    )
                } else {
                    UpdateCheckResult.UpToDate
                }

            } catch (e: Exception) {
                UpdateCheckResult.Error(e.message ?: "Unknown error while checking for updates")
            }
        }
    }

    suspend fun downloadAndInstall(context: Context, apkUrl: String): DownloadResult {
        return withContext(Dispatchers.IO) {
            try {
                val request = Request.Builder().url(apkUrl).build()
                val response = client.newCall(request).execute()

                if (!response.isSuccessful) {
                    return@withContext DownloadResult.Error("Download failed: ${response.code}")
                }

                val apkFile = File(context.getExternalFilesDir(null), "spendwise_update.apk")
                response.body?.byteStream()?.use { input ->
                    apkFile.outputStream().use { output ->
                        input.copyTo(output)
                    }
                } ?: return@withContext DownloadResult.Error("Empty download body")

                val uri: Uri = FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    apkFile
                )

                val intent = Intent(Intent.ACTION_VIEW).apply {
                    setDataAndType(uri, "application/vnd.android.package-archive")
                    addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }
                context.startActivity(intent)

                DownloadResult.Success

            } catch (e: Exception) {
                DownloadResult.Error(e.message ?: "Unknown error during download")
            }
        }
    }
}
