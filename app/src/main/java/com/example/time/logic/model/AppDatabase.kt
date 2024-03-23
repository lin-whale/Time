package com.example.time.logic.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.time.logic.dao.LifePieceDao
import com.example.time.logic.dao.TimePieceDao

@Database(version = 2, entities = [TimePiece::class, LifePiece::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timePieceDao(): TimePieceDao
    abstract fun lifePieceDao(): LifePieceDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "app_database")
                .addMigrations(MIGRATION_1_2)
                .build().apply {
                    instance = this
                }
        }

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS LifePiece " +
                            "(id INTEGER PRIMARY KEY autoincrement not null, lifePiece TEXT)")
            }
        }
    }

}