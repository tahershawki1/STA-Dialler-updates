package com.example.stadialler.model

data class ApkRelease(
    val versionName: String,
    val versionCode: Int,
    val releaseTag: String,
    val channel: String = "Stable",
    val releaseDate: String,
    val apkFileName: String,
    val apkSizeBytes: Long,
    val sha256Checksum: String,
    val signatureFingerprint: String = "SHA256:4C:E3:B2:99:A1:08:72:4F:90:3A:D8:1F:B6:5E:20:CC",
    val isCurrentInstalled: Boolean = false,
    val downloadUrl: String,
    val changelog: List<String>,
    val isMandatory: Boolean = false
) {
    val formattedSize: String
        get() {
            val mb = apkSizeBytes.toDouble() / (1024.0 * 1024.0)
            return String.format(java.util.Locale.US, "%.1f MB", mb)
        }
}
