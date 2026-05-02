package com.example.time

import TimeRepository
import android.app.Application
import android.util.Log
import com.example.time.logic.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LifePieceApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TimeRepository(database.timePieceDao(),database.lifePieceDao()) }
    
    override fun onCreate() {
        super.onCreate()
        
        // 设置全局异常处理器，防止应用崩溃
        Thread.setDefaultUncaughtExceptionHandler { thread, throwable ->
            Log.e("LifePieceApp", "Uncaught exception: ${throwable.message}", throwable)
            // 不重新抛出异常，让应用继续运行
        }
    }
}