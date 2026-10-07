package com.example.apnselector.widget

import android.content.Intent
import android.widget.RemoteViewsService

class ApnListRemoteViewsService : RemoteViewsService() {

    override fun onGetViewFactory(intent: Intent): RemoteViewsFactory {
        return ApnListRemoteViewsFactory(applicationContext)
    }
}
