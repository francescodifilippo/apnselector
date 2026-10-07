package com.example.apnselector.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.apnselector.util.WidgetUpdateHelper

class NetworkChangeReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "android.telephony.action.CARRIER_CONFIG_CHANGED") {
            WidgetUpdateHelper.updateAllWidgets(context)
        }
    }
}
