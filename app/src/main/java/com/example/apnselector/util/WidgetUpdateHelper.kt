package com.example.apnselector.util

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import com.example.apnselector.widget.ApnListWidgetProvider
import com.example.apnselector.widget.ApnSingleWidgetProvider

object WidgetUpdateHelper {

    fun updateAllWidgets(context: Context) {
        val appWidgetManager = AppWidgetManager.getInstance(context)
        updateListWidgets(context, appWidgetManager)
        updateSingleWidgets(context, appWidgetManager)
    }

    private fun updateListWidgets(context: Context, appWidgetManager: AppWidgetManager) {
        val component = ComponentName(context, ApnListWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(component)
        for (id in widgetIds) {
            ApnListWidgetProvider.updateWidget(context, appWidgetManager, id)
        }
    }

    private fun updateSingleWidgets(context: Context, appWidgetManager: AppWidgetManager) {
        val component = ComponentName(context, ApnSingleWidgetProvider::class.java)
        val widgetIds = appWidgetManager.getAppWidgetIds(component)
        for (id in widgetIds) {
            ApnSingleWidgetProvider.updateWidget(context, appWidgetManager, id)
        }
    }
}
