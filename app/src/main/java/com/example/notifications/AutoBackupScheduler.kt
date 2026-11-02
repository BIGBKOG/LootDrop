package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import com.example.preferences.AppPreferences

object AutoBackupScheduler {

    private const val REQUEST_CODE_AUTO_BACKUP = 4002
    const val ACTION_AUTO_BACKUP = "com.example.lootdrop.ACTION_AUTO_BACKUP"

    fun schedulePeriodicAutoBackup(context: Context) {
        val prefs = AppPreferences(context)
        if (!prefs.isAutoBackupEnabled || prefs.autoBackupFolderUri.isBlank()) {
            cancelPeriodicAutoBackup(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AutoBackupReceiver::class.java).apply {
            action = ACTION_AUTO_BACKUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_AUTO_BACKUP,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val days = prefs.autoBackupIntervalDays.coerceAtLeast(1)
        val intervalMillis = days.toLong() * AlarmManager.INTERVAL_DAY

        // Calculate time until next backup
        val elapsedSinceLast = System.currentTimeMillis() - prefs.lastAutoBackupTimestamp
        val delayMillis = if (prefs.lastAutoBackupTimestamp <= 0L || elapsedSinceLast >= intervalMillis) {
            60_000L // Run in 1 minute if never run or due
        } else {
            intervalMillis - elapsedSinceLast
        }

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + delayMillis,
                intervalMillis,
                pendingIntent
            )
        } catch (_: Exception) {
            // Graceful handling
        }
    }

    fun cancelPeriodicAutoBackup(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, AutoBackupReceiver::class.java).apply {
            action = ACTION_AUTO_BACKUP
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_AUTO_BACKUP,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun triggerImmediateAutoBackup(context: Context) {
        val intent = Intent(context, AutoBackupReceiver::class.java).apply {
            action = ACTION_AUTO_BACKUP
        }
        context.sendBroadcast(intent)
    }
}
