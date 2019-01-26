package com.naman14.timberx

import android.app.Application
import com.naman14.timberx.network.DataHandler

class TimberXApp: Application() {

    override fun onCreate() {
        super.onCreate()
        DataHandler.initCache(this)
    }
}