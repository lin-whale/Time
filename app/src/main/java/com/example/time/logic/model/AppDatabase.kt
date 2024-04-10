package com.example.time.logic.model

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.time.logic.dao.LifePieceDao
import com.example.time.logic.dao.TimePieceDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

//@Database(version = 2, entities = [TimePiece::class, LifePiece::class], exportSchema = false)
//abstract class AppDatabase : RoomDatabase() {
//
//    abstract fun timePieceDao(): TimePieceDao
//    abstract fun lifePieceDao(): LifePieceDao
//
//    companion object {
//
//        private var instance: AppDatabase? = null
//
//        @Synchronized
//        fun getDatabase(context: Context): AppDatabase {
//            instance?.let {
//                return it
//            }
//            return Room.databaseBuilder(context.applicationContext,
//                AppDatabase::class.java, "app_database")
//                .addMigrations(MIGRATION_1_2)
//                .build().apply {
//                    instance = this
//                }
//        }
//
//        private val MIGRATION_1_2 = object : Migration(1, 2) {
//            override fun migrate(database: SupportSQLiteDatabase) {
//                database.execSQL(
//                    "CREATE TABLE IF NOT EXISTS LifePiece " +
//                            "(id INTEGER PRIMARY KEY autoincrement not null, lifePiece TEXT)")
//            }
//        }
//    }
//
//}

@Database(version = 2, entities = [TimePiece::class, LifePiece::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timePieceDao(): TimePieceDao
    abstract fun lifePieceDao(): LifePieceDao

    companion object {

        private var INSTANCE: AppDatabase? = null

        @Synchronized
        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return AppDatabase.INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database"
                )
                    .addCallback(AppDatabase.AppDatabaseCallback(scope))
                    .build()
                AppDatabase.INSTANCE = instance
                // return instance
                instance
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

    private class AppDatabaseCallback(
        private val scope: CoroutineScope
    ) : RoomDatabase.Callback() {

        override fun onCreate(db: SupportSQLiteDatabase) {
            super.onCreate(db)
            INSTANCE?.let { database ->
                scope.launch {
                    populateDatabase(database.timePieceDao(), database.lifePieceDao())
                }
            }
        }

        suspend fun populateDatabase(timePieceDao: TimePieceDao ,lifePieceDao: LifePieceDao) {
//            // Delete all content here.
//            wordDao.deleteAll()
            val timePiece = TimePiece(
                timePoint = System.currentTimeMillis(), fromTimePoint = System.currentTimeMillis(),
                emotion = 3, lastTimeRecord = " ",
                mainEvent = "开始记录生命体验吧~", subEvent = ""
            )
            timePieceDao.insert(timePiece)
            // TODO: Add your own words!
        }
    }
}