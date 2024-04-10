package com.example.time.logic.dao

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.time.logic.model.TimePiece

@Dao
interface TimePieceDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(timePiece: TimePiece): Long

    @Query("SELECT * FROM TimePiece WHERE timePoint BETWEEN :startTime AND :endTime ORDER BY timePoint ASC")
    fun getTimePiecesBetween(startTime: Long, endTime: Long): List<TimePiece>

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

}