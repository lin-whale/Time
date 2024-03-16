package com.example.time.logic.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.time.logic.dao.TimePieceDao

@Database(version = 1, entities = [TimePiece::class])
abstract class AppDatabase : RoomDatabase() {

    abstract fun timePieceDao(): TimePieceDao

    companion object {

        private var instance: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context): AppDatabase {
            instance?.let {
                return it
            }
            return Room.databaseBuilder(context.applicationContext,
                AppDatabase::class.java, "app_database")
                .build().apply {
                    instance = this
                }
        }
//            fun getDatabase(context: Context): AppDatabase {
//                return instance ?: synchronized(this) {
//                    val newInstance = Room.databaseBuilder(
//                        context.applicationContext,
//                        AppDatabase::class.java, "app_database"
//                    ).build()
//                    instance = newInstance
//                    newInstance
//                }
//            }
    }

}