package com.example.apnselector.widget

import android.content.Context
import android.content.SharedPreferences

object WidgetPrefsHelper {

    private const val PREFS_NAME = "apn_single_widget_prefs"
    private const val KEY_APN_ID = "apn_id_"
    private const val KEY_APN_NAME = "apn_name_"
    private const val KEY_COLOR = "color_"
    private const val DEFAULT_COLOR = 0xFF1976D2.toInt()

    fun saveWidgetConfig(context: Context, widgetId: Int, apnId: Int, apnName: String, color: Int) {
        context.prefs().edit().apply {
            putInt(KEY_APN_ID + widgetId, apnId)
            putString(KEY_APN_NAME + widgetId, apnName)
            putInt(KEY_COLOR + widgetId, color)
            apply()
        }
    }

    fun getApnId(context: Context, widgetId: Int): Int =
        context.prefs().getInt(KEY_APN_ID + widgetId, -1)

    fun getApnName(context: Context, widgetId: Int): String =
        context.prefs().getString(KEY_APN_NAME + widgetId, "") ?: ""

    fun getColor(context: Context, widgetId: Int): Int =
        context.prefs().getInt(KEY_COLOR + widgetId, DEFAULT_COLOR)

    fun deleteWidgetConfig(context: Context, widgetId: Int) {
        context.prefs().edit().apply {
            remove(KEY_APN_ID + widgetId)
            remove(KEY_APN_NAME + widgetId)
            remove(KEY_COLOR + widgetId)
            apply()
        }
    }

    private fun Context.prefs(): SharedPreferences =
        getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
}
