package com.example.ui

import android.content.Context
import android.widget.Toast
import com.example.BuildConfig
import com.example.data.model.AppUpdateInfo
import com.example.data.model.UpdateDownloadState
import com.example.data.remote.GithubReleaseClient
import com.example.i18n.AppStringsRegistry
import com.example.notifications.NotificationHelper
import com.example.preferences.AppPreferences
import com.example.util.AppUpdateInstaller
import java.io.File

internal class AppUpdateDelegate(
    private val appContext: Context,
    private val preferences: AppPreferences,
    private val githubReleaseClient: GithubReleaseClient,
    private val notificationHelper: NotificationHelper
) {
    var pendingInstallAfterPermissionGrant: Boolean = false

    suspend fun downloadAndInstallUpdate(
        updateInfo: AppUpdateInfo,
        installContext: Context,
        onState: (UpdateDownloadState) -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val targetUrl = updateInfo.downloadUrl ?: updateInfo.releaseUrl
        if (targetUrl.isBlank()) {
            onState(UpdateDownloadState.Error(s.system.updateNoDownloadUrl))
            return
        }

        pendingInstallAfterPermissionGrant = false
        onState(UpdateDownloadState.Downloading(progress = 0f, bytesDownloaded = 0L, totalBytes = 0L))

        val result = AppUpdateInstaller.downloadApk(
            context = appContext,
            downloadUrl = targetUrl,
            versionTag = updateInfo.latestVersion,
            onProgress = { progress, downloaded, total ->
                onState(UpdateDownloadState.Downloading(progress, downloaded, total))
            }
        )

        if (result.isSuccess) {
            val apkFile = result.getOrThrow()
            onState(UpdateDownloadState.ReadyToInstall(apkFile, updateInfo.latestVersion))
            triggerInstallWithAutoResume(installContext, apkFile, updateInfo.latestVersion)
        } else {
            onState(UpdateDownloadState.Error(result.exceptionOrNull()?.localizedMessage ?: s.system.updateDownloadFailed))
        }
    }

    fun triggerInstallWithAutoResume(context: Context, apkFile: File, versionTag: String = "") {
        val needsUnknownSourcesGrant = !context.packageManager.canRequestPackageInstalls()
        val launched = AppUpdateInstaller.installApk(context, apkFile)
        if (!launched && needsUnknownSourcesGrant) {
            pendingInstallAfterPermissionGrant = true
            preferences.pendingUpdateApkPath = apkFile.absolutePath
            preferences.pendingUpdateVersionTag = versionTag
        } else {
            pendingInstallAfterPermissionGrant = false
            preferences.clearPendingUpdateInstall()
        }
    }

    fun resumePendingUpdateInstallIfPermitted(
        context: Context,
        currentState: UpdateDownloadState,
        onStateRestored: (UpdateDownloadState) -> Unit = {}
    ) {
        val savedPath = preferences.pendingUpdateApkPath
        val savedTag = preferences.pendingUpdateVersionTag
        if (!pendingInstallAfterPermissionGrant && savedPath.isBlank()) return

        val candidateFile: File?
        when (currentState) {
            is UpdateDownloadState.ReadyToInstall -> {
                candidateFile = currentState.apkFile
            }
            else -> {
                val restoredFile = if (savedPath.isNotBlank()) File(savedPath) else null
                if (restoredFile != null && AppUpdateInstaller.hasValidApkFile(restoredFile)) {
                    candidateFile = restoredFile
                    onStateRestored(UpdateDownloadState.ReadyToInstall(restoredFile, savedTag))
                } else {
                    pendingInstallAfterPermissionGrant = false
                    preferences.clearPendingUpdateInstall()
                    return
                }
            }
        }

        val canInstall = context.packageManager.canRequestPackageInstalls()
        if (canInstall && AppUpdateInstaller.hasValidApkFile(candidateFile)) {
            pendingInstallAfterPermissionGrant = false
            preferences.clearPendingUpdateInstall()
            AppUpdateInstaller.installApk(context, candidateFile)
        } else if (!AppUpdateInstaller.hasValidApkFile(candidateFile)) {
            pendingInstallAfterPermissionGrant = false
            preferences.clearPendingUpdateInstall()
        } else {
            pendingInstallAfterPermissionGrant = true
        }
    }

    suspend fun checkGithubReleases(
        isManual: Boolean,
        onChecking: (Boolean) -> Unit,
        onUpdateInfo: (AppUpdateInfo?) -> Unit,
        onStatus: (String?) -> Unit,
        onReopenMessage: (String) -> Unit
    ) {
        if (!isManual && !preferences.isAutoCheckGithubUpdates) return
        val now = System.currentTimeMillis()
        if (!isManual && preferences.lastGithubCheckTime > 0L && (now - preferences.lastGithubCheckTime < 5 * 60 * 1000L)) {
            return
        }

        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        onChecking(true)
        if (isManual) onStatus(s.system.updateCheckingGithub)

        val result = githubReleaseClient.checkLatestRelease(currentVersionName = BuildConfig.VERSION_NAME)
        preferences.lastGithubCheckTime = System.currentTimeMillis()
        onChecking(false)

        if (result.isSuccess) {
            val info = result.getOrNull()
            onUpdateInfo(info)
            if (info != null) {
                preferences.cachedLatestGithubVersion = info.latestVersion
                if (info.hasUpdate) {
                    onStatus(String.format(s.system.updateNewReleaseFormat, info.latestVersion))
                    if (preferences.lastDismissedUpdateTag != info.latestVersion) {
                        onReopenMessage(String.format(s.system.updateNewReleaseBannerFormat, info.latestVersion))
                        notificationHelper.sendUpdateNotification(info)
                    }
                } else if (isManual) {
                    onStatus(String.format(s.system.updateUpToDateFormat, BuildConfig.VERSION_NAME))
                }
            }
        } else if (isManual) {
            onStatus(String.format(s.system.updateCheckErrorFormat, result.exceptionOrNull()?.localizedMessage ?: s.system.updateNetworkIssue))
        }
    }
}

internal fun showShortToast(context: Context, message: String) {
    runCatching { Toast.makeText(context, message, Toast.LENGTH_SHORT).show() }
}

internal fun showLongToast(context: Context, message: String) {
    runCatching { Toast.makeText(context, message, Toast.LENGTH_LONG).show() }
}
