package com.MyCarApp

import android.app.Application
import android.content.Context

class MyCarApp : Application() {
    companion object {
        lateinit var appContext: Context
            private set
    }

    override fun onCreate() {
        super.onCreate()
        appContext = applicationContext
    }
}
