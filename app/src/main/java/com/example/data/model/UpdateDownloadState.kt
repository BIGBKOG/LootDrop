package com.example.data.model

import java.io.File

/**
 * State representing in-app APK download and installation progress.
 */
sealed interface UpdateDownloadState {
    object Idle : UpdateDownloadState

    data class Downloading(
        val progress: Float, // 0.0f to 1.0f
        val bytesDownloaded: Long,
        val totalBytes: Long,
        val speedBytesPerSec: Long = 0L
    ) : UpdateDownloadState

    data class ReadyToInstall(
        val apkFile: File,
        val versionTag: String
    ) : UpdateDownloadState

    data class Error(val message: String) : UpdateDownloadState
}
