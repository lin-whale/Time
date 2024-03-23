package com.example.time

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import androidx.room.Room
import com.example.time.logic.model.AppDatabase

class TimeApplication : Application() {

    val db by lazy {
        Room.databaseBuilder(this, AppDatabase::class.java, "app_database")
            .build()
    }
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