package com.example.time

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class TimeApplication : Application() {

    companion object {
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context
        const val TOKEN = "MSaQ8mvmbhGZouvZ"
    }

    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

}