package com.example.apnselector.widget

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.widget.RemoteViews
import android.widget.RemoteViewsService
import com.example.apnselector.R
import com.example.apnselector.data.ApnInfo
import com.example.apnselector.data.ApnRepository

class ApnListRemoteViewsFactory(
    private val context: Context
) : RemoteViewsService.RemoteViewsFactory {

    private var apnList: List<ApnInfo> = emptyList()

    override fun onCreate() {}

    override fun onDataSetChanged() {
        apnList = ApnRepository.getApnList(context)
    }

    override fun onDestroy() {
        apnList = emptyList()
    }

    override fun getCount(): Int = apnList.size

    override fun getViewAt(position: Int): RemoteViews {
        val views = RemoteViews(context.packageName, R.layout.widget_apn_list_item)
        if (position >= apnList.size) return views

        val apn = apnList[position]
        views.setTextViewText(R.id.item_name, apn.name)
        views.setTextViewText(R.id.item_apn, apn.apn)

        val icon = if (apn.isActive) R.drawable.ic_check else R.drawable.ic_cross
        val nameColor = if (apn.isActive) R.color.apn_active else R.color.widget_text
        views.setImageViewResource(R.id.item_icon, icon)
        views.setInt(R.id.item_name, "setTextColor", context.getColor(nameColor))

        views.setOnClickFillInIntent(R.id.item_root, Intent(Settings.ACTION_APN_SETTINGS))
        return views
    }

    override fun getLoadingView(): RemoteViews? = null

    override fun getViewTypeCount(): Int = 1

    override fun getItemId(position: Int): Long =
        if (position < apnList.size) apnList[position].id.toLong() else position.toLong()

    override fun hasStableIds(): Boolean = true
}
