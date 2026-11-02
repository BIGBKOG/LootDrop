package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.util.BackupRestoreManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class AutoBackupReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                BackupRestoreManager.performScheduledAutoBackup(appContext)
                AutoBackupScheduler.schedulePeriodicAutoBackup(appContext)
            } catch (_: Exception) {
                // Handled gracefully inside performScheduledAutoBackup
            } finally {
                pendingResult.finish()
            }
        }
    }
}
