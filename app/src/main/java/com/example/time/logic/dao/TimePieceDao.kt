package com.example.time.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.time.logic.model.TimePiece

@Dao
interface TimePieceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(timePiece: TimePiece): Long

    @Query("SELECT * FROM TimePiece WHERE timePoint BETWEEN :startTime AND :endTime ORDER BY timePoint ASC")
    fun getTimePiecesBetween(startTime: Long, endTime: Long): List<TimePiece>

    @Query("SELECT * FROM TimePiece WHERE mainEvent = :mainEvent")
    fun getTimePiecesByMainEvent(mainEvent: String): List<TimePiece>

    @Update
    fun updateTimePiece(newTimePiece: TimePiece)

    @Delete
    fun deleteTimePiece(timePiece: TimePiece)

    @Query("SELECT * FROM TimePiece ORDER BY timePoint")
    fun getOrderedTimePiece(): List<TimePiece>

    @Query("SELECT * FROM TimePiece ORDER BY id DESC LIMIT 1")
    fun getLatestRow(): LiveData<List<TimePiece>>

    @Query("select * from TimePiece")
    fun loadAllTimePieces(): LiveData<List<TimePiece>>

    @Query("SELECT COUNT(*) FROM TimePiece")
    fun getCount(): Int
    @Query("delete from TimePiece where emotion = :emotion")
    fun deleteTimePieceByEmotion(emotion: Int): Int
    
    // 新增：批量插入TimePiece的事务方法
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insertAll(vararg timePieces: TimePiece)
    
    // 新增：在事务中插入和删除的方法
    @Transaction
    fun insertAndDelete(toInsert: List<TimePiece>, toDelete: TimePiece) {
        // 先删除原记录，避免重叠
        deleteTimePiece(toDelete)
        // 然后插入新记录
        toInsert.forEach { insert(it) }
    }

}