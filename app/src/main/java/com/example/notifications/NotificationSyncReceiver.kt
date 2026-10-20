package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import com.example.data.local.AppDatabase
import com.example.data.model.toDomain
import com.example.data.repository.GiveawaysRepository
import com.example.preferences.AppPreferences
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

class NotificationSyncReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val appContext = context.applicationContext

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val prefs = AppPreferences(appContext)
                val helper = NotificationHelper(appContext)
                if (!helper.isNotificationsEnabled) return@launch

                // Network check
                if (!isNetworkAvailable(appContext, prefs.backgroundSyncWifiOnly)) {
                    return@launch
                }

                if (helper.isQuietHoursActive()) {
                    return@launch
                }

                val db = AppDatabase.getInstance(appContext)
                val repository = GiveawaysRepository(db.giveawayDao(), helper)
                repository.refreshGiveaways()

                // Check for Ending Soon giveaways (ends today or in next 24h)
                if (prefs.isNotifyEndingSoon) {
                    val bookmarkedItems = db.giveawayDao().getBookmarkedGiveawaysSync().map { it.toDomain() }
                    for (item in bookmarkedItems) {
                        if (isEndingWithin24Hours(item.endDate)) {
                            helper.sendEndingSoonNotification(item)
                        }
                    }
                }

                // Reschedule next sync alarm
                NotificationScheduler.schedulePeriodicSync(appContext)
            } catch (_: Exception) {
                // Network failure or background timeout gracefully handled
            } finally {
                pendingResult.finish()
            }
        }
    }

    private fun isNetworkAvailable(context: Context, wifiOnly: Boolean): Boolean {
        val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as? ConnectivityManager ?: return false
        val activeNetwork = cm.activeNetwork ?: return false
        val caps = cm.getNetworkCapabilities(activeNetwork) ?: return false

        val hasInternet = caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        if (!hasInternet) return false

        if (wifiOnly) {
            return caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) ||
                    caps.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)
        }
        return true
    }

    private fun isEndingWithin24Hours(endDateStr: String?): Boolean {
        if (endDateStr.isNullOrBlank() || endDateStr.equals("N/A", ignoreCase = true)) return false
        return try {
            val datePart = endDateStr.trim().split(" ").firstOrNull() ?: return false
            val endLocalDate = LocalDate.parse(datePart, DateTimeFormatter.ISO_LOCAL_DATE)
            val today = LocalDate.now()
            val daysUntil = ChronoUnit.DAYS.between(today, endLocalDate)
            daysUntil in 0..1
        } catch (_: Exception) {
            false
        }
    }
}
