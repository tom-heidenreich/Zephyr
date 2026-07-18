package com.tomheidenreich.zephyr.runtime

import android.app.Application
import com.tomheidenreich.zephyr.runtime.di.AppGraph

class ZephyrApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AppGraph.initialize(this)
    }
}