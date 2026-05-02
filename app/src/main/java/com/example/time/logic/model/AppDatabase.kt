package com.example.time.logic.model

import android.content.Context
import android.util.Log
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
 * version 3: 添加 TimePiece.mediaPaths 字段支持媒体附件（可选字段）
 * version 2: 添加 LifePiece 表
 * version 1: 初始版本，TimePiece 表
 */
@Database(version = 3, entities = [TimePiece::class, LifePiece::class], exportSchema = false)
abstract class AppDatabase : RoomDatabase() {

    abstract fun timePieceDao(): TimePieceDao
    abstract fun lifePieceDao(): LifePieceDao

    companion object {
        private const val TAG = "AppDatabase"
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
                try {
                    database.execSQL(
                        "CREATE TABLE IF NOT EXISTS LifePiece " +
                                "(id INTEGER PRIMARY KEY autoincrement not null, lifePiece TEXT)"
                    )
                    Log.d(TAG, "Migration 1->2 successful")
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 1->2 failed: ${e.message}")
                }
            }
        }

        /**
         * 版本2→3迁移：添加 TimePiece.mediaPaths 字段（可选）
         * 安全检查：先检查列是否存在，避免重复添加导致崩溃
         */
        private val MIGRATION_2_3 = object : Migration(2, 3) {
            override fun migrate(database: SupportSQLiteDatabase) {
                try {
                    // 检查 mediaPaths 列是否已存在
                    val cursor = database.query("PRAGMA table_info(TimePiece)")
                    var columnExists = false
                    while (cursor.moveToNext()) {
                        val columnNameIndex = cursor.getColumnIndex("name")
                        if (columnNameIndex >= 0) {
                            val columnName = cursor.getString(columnNameIndex)
                            if (columnName == "mediaPaths") {
                                columnExists = true
                                break
                            }
                        }
                    }
                    cursor.close()
                    
                    if (!columnExists) {
                        // 添加可空列，不强制 NOT NULL
                        database.execSQL(
                            "ALTER TABLE TimePiece ADD COLUMN mediaPaths TEXT"
                        )
                        Log.d(TAG, "Migration 2->3: mediaPaths column added")
                    } else {
                        Log.d(TAG, "Migration 2->3: mediaPaths column already exists, skipping")
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Migration 2->3 failed: ${e.message}")
                    // 不抛出异常，让迁移继续
                }
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