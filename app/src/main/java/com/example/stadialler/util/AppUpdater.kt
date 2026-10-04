package com.example.stadialler.util

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.core.content.FileProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.net.HttpURLConnection
import java.net.URL

data class RemoteUpdateInfo(
    val versionCode: Int,
    val versionName: String,
    val apkUrl: String,
    val releaseNotes: String,
    val isMandatory: Boolean,
    val publishedDate: String
)

object AppUpdater {

    private const val DEFAULT_MANIFEST_URL =
        "https://raw.githubusercontent.com/tahershawki1/STA-Dialler-updates/main/version.json"

    suspend fun checkForUpdate(
        manifestUrl: String = DEFAULT_MANIFEST_URL
    ): Result<RemoteUpdateInfo?> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$manifestUrl?t=${System.currentTimeMillis()}")
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 8000
                readTimeout = 8000
                requestMethod = "GET"
                useCaches = false
                setRequestProperty("Cache-Control", "no-cache")
                setRequestProperty("Accept", "application/json")
            }

            if (connection.responseCode !in 200..299) {
                return@withContext Result.failure(
                    Exception("Failed to fetch manifest: HTTP ${connection.responseCode}")
                )
            }

            val responseText = connection.inputStream.bufferedReader().use { it.readText() }
            val json = JSONObject(responseText)

            val versionCode = json.getInt("versionCode")
            val versionName = json.getString("versionName")
            val apkUrl = json.getString("apkUrl")
            val releaseNotes = json.optString("releaseNotes", "تحديث جديد لتطبيق الهاتف")
            val isMandatory = json.optBoolean("mandatory", false)
            val published = json.optString("publishedAt", "")

            Result.success(
                RemoteUpdateInfo(
                    versionCode = versionCode,
                    versionName = versionName,
                    apkUrl = apkUrl,
                    releaseNotes = releaseNotes,
                    isMandatory = isMandatory,
                    publishedDate = published
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun downloadApk(
        context: Context,
        apkUrl: String,
        onProgress: (Float) -> Unit
    ): Result<File> = withContext(Dispatchers.IO) {
        try {
            val destinationDir = File(context.cacheDir, "updates").apply {
                if (!exists()) mkdirs()
            }
            val destinationFile = File(destinationDir, "STA-Dialler-update.apk")
            if (destinationFile.exists()) {
                destinationFile.delete()
            }

            val url = URL(apkUrl)
            val connection = (url.openConnection() as HttpURLConnection).apply {
                connectTimeout = 15000
                readTimeout = 30000
                requestMethod = "GET"
                setRequestProperty("User-Agent", "STA-Dialler-AutoUpdater")
            }

            val contentLength = connection.contentLengthLong
            var totalBytesRead = 0L

            connection.inputStream.use { input: InputStream ->
                FileOutputStream(destinationFile).use { output: FileOutputStream ->
                    val buffer = ByteArray(8 * 1024)
                    var bytesRead: Int
                    while (input.read(buffer).also { bytesRead = it } != -1) {
                        output.write(buffer, 0, bytesRead)
                        totalBytesRead += bytesRead
                        if (contentLength > 0) {
                            val progress = (totalBytesRead.toFloat() / contentLength.toFloat()).coerceIn(0f, 1f)
                            onProgress(progress)
                        }
                    }
                    output.flush()
                }
            }

            onProgress(1.0f)
            Result.success(destinationFile)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun canRequestPackageInstalls(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.packageManager.canRequestPackageInstalls()
        } else {
            true
        }
    }

    fun openInstallPermissionSettings(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val intent = Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).apply {
                data = Uri.parse("package:${context.packageName}")
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
            context.startActivity(intent)
        }
    }

    fun installApk(context: Context, apkFile: File): Result<Unit> {
        return try {
            if (!apkFile.exists() || apkFile.length() == 0L) {
                return Result.failure(Exception("APK file is missing or empty"))
            }

            val authority = "${context.packageName}.fileprovider"
            val apkUri = FileProvider.getUriForFile(context, authority, apkFile)

            val installIntent = Intent(Intent.ACTION_VIEW).apply {
                setDataAndType(apkUri, "application/vnd.android.package-archive")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            context.startActivity(installIntent)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
