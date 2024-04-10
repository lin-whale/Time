package com.example.time

import TimeRepository
import android.app.Application
import com.example.time.logic.model.AppDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

class LifePieceApplication : Application() {
    val applicationScope = CoroutineScope(SupervisorJob())

    // Using by lazy so the database and the repository are only created when they're needed
    // rather than when the application starts
    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }
    val repository by lazy { TimeRepository(database.timePieceDao(),database.lifePieceDao()) }
}