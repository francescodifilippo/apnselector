package com.example.apnselector.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.RemoteViews
import com.example.apnselector.R
import com.example.apnselector.data.ApnRepository

class ApnSingleWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (appWidgetId in appWidgetIds) {
            updateWidget(context, appWidgetManager, appWidgetId)
        }
    }

    override fun onDeleted(context: Context, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            WidgetPrefsHelper.deleteWidgetConfig(context, appWidgetId)
        }
    }

    companion object {
        fun updateWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val configuredApnId = WidgetPrefsHelper.getApnId(context, appWidgetId)
            val apnName = WidgetPrefsHelper.getApnName(context, appWidgetId)
            val bgColor = WidgetPrefsHelper.getColor(context, appWidgetId)

            if (configuredApnId == -1) return

            val apnList = ApnRepository.getApnList(context)
            val isActive = apnList.any { it.id == configuredApnId && it.isActive }

            val views = RemoteViews(context.packageName, R.layout.widget_apn_single)
            views.setInt(R.id.widget_single_root, "setBackgroundColor", bgColor)
            views.setImageViewResource(
                R.id.single_icon,
                if (isActive) R.drawable.ic_check_white else R.drawable.ic_cross_white
            )
            views.setTextViewText(R.id.single_apn_name, apnName)

            val pendingIntent = PendingIntent.getActivity(
                context,
                appWidgetId,
                Intent(Settings.ACTION_APN_SETTINGS),
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_single_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
