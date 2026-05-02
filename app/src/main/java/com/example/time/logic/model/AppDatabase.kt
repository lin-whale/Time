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

/**
 * 应用数据库
 * 
 * version 3: 添加 TimePiece.mediaPaths 字段支持媒体附件
 * version 2: 添加 LifePiece 表
 * version 1: 初始版本，TimePiece 表
 */
@Database(version = 3, entities = [TimePiece::class, LifePiece::class], exportSchema = false)
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
                    .addCallback(AppDatabaseCallback(scope))
                    .addMigrations(MIGRATION_1_2, MIGRATION_2_3)
                    .build()
                AppDatabase.INSTANCE = instance
                instance
            }
        }

        /**
         * 版本1→2迁移：添加 LifePiece 表
         */
        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL(
                    "CREATE TABLE IF NOT EXISTS LifePiece " +
                            "(id INTEGER PRIMARY KEY autoincrement not null, lifePiece TEXT)"
                )
            }
        }

        /**
         * 版本2→3迁移：添加 TimePiece.mediaPaths 字段
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                // 添加 mediaPaths 字段，默认值为空JSON数组
                database.execSQL(
                    "ALTER TABLE TimePiece ADD COLUMN mediaPaths TEXT NOT NULL DEFAULT '[]'"
                )
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

        suspend fun populateDatabase(timePieceDao: TimePieceDao, lifePieceDao: LifePieceDao) {
            val timePiece = TimePiece(
                timePoint = System.currentTimeMillis(), 
                fromTimePoint = System.currentTimeMillis(),
                emotion = 3, 
                lastTimeRecord = " ",
                mainEvent = "开始记录生命体验吧~", 
                subEvent = ""
            )
            timePieceDao.insert(timePiece)
        }
    }
}