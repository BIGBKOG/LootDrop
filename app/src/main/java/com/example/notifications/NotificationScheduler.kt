package com.example.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.SystemClock
import com.example.preferences.AppPreferences

object NotificationScheduler {

    private const val REQUEST_CODE_SYNC = 4001
    const val ACTION_PERIODIC_SYNC = "com.example.lootdrop.ACTION_PERIODIC_SYNC"

    fun schedulePeriodicSync(context: Context) {
        val prefs = AppPreferences(context)
        val helper = NotificationHelper(context)
        val intervalHours = prefs.backgroundSyncIntervalHours

        // If interval is 0, user chose manual only
        if (intervalHours <= 0 || !helper.isNotificationsEnabled) {
            cancelPeriodicSync(context)
            return
        }

        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationSyncReceiver::class.java).apply {
            action = ACTION_PERIODIC_SYNC
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SYNC,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val intervalMillis = intervalHours.toLong() * AlarmManager.INTERVAL_HOUR

        try {
            alarmManager.setInexactRepeating(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + intervalMillis,
                intervalMillis,
                pendingIntent
            )
        } catch (_: Exception) {
            // Graceful handling
        }
    }

    fun cancelPeriodicSync(context: Context) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as? AlarmManager ?: return
        val intent = Intent(context, NotificationSyncReceiver::class.java).apply {
            action = ACTION_PERIODIC_SYNC
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            REQUEST_CODE_SYNC,
            intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        )
        if (pendingIntent != null) {
            alarmManager.cancel(pendingIntent)
            pendingIntent.cancel()
        }
    }

    fun triggerImmediateCheck(context: Context) {
        val intent = Intent(context, NotificationSyncReceiver::class.java).apply {
            action = ACTION_PERIODIC_SYNC
        }
        context.sendBroadcast(intent)
    }
}
