package com.example.widget

import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import com.example.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Android Home Screen Widget displaying the top 3 active free game drops ranked by rating & value.
 */
class TopDropsListWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        val pendingResult = runCatching { goAsync() }.getOrNull()
        updateAppWidgets(context, appWidgetManager, appWidgetIds) {
            runCatching { pendingResult?.finish() }
        }
    }

    override fun onReceive(context: Context, intent: Intent) {
        super.onReceive(context, intent)
        if (intent.action == WidgetUpdateHelper.ACTION_REFRESH_WIDGETS) {
            WidgetUpdateHelper.triggerManualSync(context)
        }
    }

    companion object {
        fun updateAppWidgets(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetIds: IntArray,
            onComplete: (() -> Unit)? = null
        ) {
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    for (widgetId in appWidgetIds) {
                        val views = RemoteViews(context.packageName, R.layout.widget_top_drops)
                        WidgetUpdateHelper.populateTopDropsList(context, views, widgetId)
                        appWidgetManager.updateAppWidget(widgetId, views)
                    }
                } finally {
                    onComplete?.invoke()
                }
            }
        }
    }
}
