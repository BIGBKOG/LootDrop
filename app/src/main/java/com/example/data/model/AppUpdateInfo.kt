package com.example.data.model

/**
 * Represents metadata about the latest release from the GitHub repository.
 */
data class AppUpdateInfo(
    val hasUpdate: Boolean = false,
    val latestVersion: String = "",
    val currentVersion: String = "",
    val releaseTitle: String = "",
    val releaseNotes: String = "",
    val releaseUrl: String = "",
    val downloadUrl: String? = null,
    val publishedAt: String? = null,
    val isApkAvailable: Boolean = false,
    val checkedTimestamp: Long = System.currentTimeMillis()
)
