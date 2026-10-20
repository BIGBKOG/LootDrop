package com.example.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.core.app.NotificationManagerCompat
import com.example.data.local.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {

    companion object {
        const val ACTION_BOOKMARK = "com.example.lootdrop.ACTION_BOOKMARK"
        const val ACTION_MARK_READ = "com.example.lootdrop.ACTION_MARK_READ"
        const val EXTRA_GIVEAWAY_ID = "extra_giveaway_id"
        const val EXTRA_GIVEAWAY_TITLE = "extra_giveaway_title"
        const val EXTRA_HISTORY_ID = "extra_history_id"
    }

    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val giveawayId = intent.getIntExtra(EXTRA_GIVEAWAY_ID, -1)
        val giveawayTitle = intent.getStringExtra(EXTRA_GIVEAWAY_TITLE) ?: "Game"
        val historyId = intent.getLongExtra(EXTRA_HISTORY_ID, -1L)

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val db = AppDatabase.getInstance(context)

                when (intent.action) {
                    ACTION_BOOKMARK -> {
                        if (giveawayId != -1) {
                            db.giveawayDao().setBookmarked(giveawayId, true)
                            NotificationManagerCompat.from(context).cancel(giveawayId)
                            Handler(Looper.getMainLooper()).post {
                                Toast.makeText(
                                    context,
                                    "★ '$giveawayTitle' saved to your Bookmarks!",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                    ACTION_MARK_READ -> {
                        if (historyId != -1L) {
                            db.notificationHistoryDao().markAsRead(historyId)
                        }
                    }
                }
            } catch (_: Exception) {
                // Graceful fallback
            } finally {
                pendingResult.finish()
            }
        }
    }
}
