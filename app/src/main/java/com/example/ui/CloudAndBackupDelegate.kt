package com.example.ui

import android.content.Context
import android.net.Uri
import com.example.data.local.AppDatabase
import com.example.i18n.AppStringsRegistry
import com.example.preferences.AppPreferences
import com.example.util.BackupRestoreManager
import com.example.util.CloudBackupManager

internal object CloudAndBackupDelegate {

    suspend fun exportLocalBackup(
        context: Context,
        uri: Uri,
        database: AppDatabase,
        preferences: AppPreferences,
        onStatus: (String) -> Unit,
        onUpdated: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val result = BackupRestoreManager.exportBackup(context, uri, database, preferences)
        result.onSuccess { summary ->
            onStatus(String.format(s.system.backupExportedStatusFormat, summary.totalGamesCount, summary.fileSizeFormatted))
            showLongToast(context, String.format(s.system.backupExportedToastFormat, summary.totalGamesCount))
            onUpdated()
        }.onFailure { e ->
            val msg = String.format(s.system.backupExportFailedFormat, e.message ?: "")
            onStatus(msg)
            showLongToast(context, msg)
        }
    }

    suspend fun restoreLocalBackup(
        context: Context,
        uri: Uri,
        database: AppDatabase,
        preferences: AppPreferences,
        onStatus: (String) -> Unit,
        onRestored: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val result = BackupRestoreManager.restoreBackup(context, uri, database, preferences)
        result.onSuccess { summary ->
            onStatus("🎉 ${summary.message}")
            showLongToast(context, summary.message)
            onRestored()
        }.onFailure { e ->
            val msg = String.format(s.system.backupRestoreFailedFormat, e.message ?: "")
            onStatus(msg)
            showLongToast(context, msg)
        }
    }

    suspend fun performManualAutoBackup(
        context: Context,
        folderUri: Uri,
        database: AppDatabase,
        preferences: AppPreferences,
        onSyncPrefs: (Long, String) -> Unit,
        onUpdated: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val result = BackupRestoreManager.exportToFolderUri(
            context, folderUri, database, preferences, "lootdrop_backup_latest.json"
        )
        onSyncPrefs(preferences.lastAutoBackupTimestamp, preferences.lastAutoBackupStatus)
        result.onSuccess { summary ->
            val folderName = preferences.autoBackupFolderDisplayName.ifBlank { "Cloud" }
            showLongToast(context, String.format(s.system.backupAutoUpdatedFormat, folderName, summary.totalGamesCount))
            onUpdated()
        }.onFailure { e ->
            val errorMsg = String.format(s.system.backupAutoFailedFormat, e.message ?: "")
            onSyncPrefs(preferences.lastAutoBackupTimestamp, "❌ $errorMsg")
            showLongToast(context, errorMsg)
        }
    }

    suspend fun uploadToCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences,
        isAutomated: Boolean,
        onSyncState: () -> Unit,
        onUpdated: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val result = CloudBackupManager.uploadBackupToCloud(context, database, preferences)
        onSyncState()
        result.onSuccess { statusMsg ->
            if (!isAutomated) showLongToast(context, statusMsg)
            onUpdated()
        }.onFailure { e ->
            if (!isAutomated) showLongToast(context, String.format(s.system.cloudBackupFailedFormat, e.message ?: ""))
        }
    }

    suspend fun restoreFromCloud(
        context: Context,
        database: AppDatabase,
        preferences: AppPreferences,
        onSyncState: () -> Unit,
        onRestored: () -> Unit
    ) {
        val s = AppStringsRegistry.forLanguage(preferences.appLanguage)
        val result = CloudBackupManager.restoreBackupFromCloud(context, database, preferences)
        onSyncState()
        result.onSuccess { summary ->
            showLongToast(context, summary.message)
            onRestored()
        }.onFailure { e ->
            showLongToast(context, String.format(s.system.cloudRestoreFailedFormat, e.message ?: ""))
        }
    }
}
