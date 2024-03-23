package com.example.time.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import com.example.time.logic.model.TimePiece

@Dao
interface TimePieceDao {

    @Insert
    fun insertTimePiece(timePiece: TimePiece): Long

    @Update
    fun updateTimePiece(newTimePiece: TimePiece)

    @Delete
    fun deleteTimePiece(timePiece: TimePiece)

    @Query("SELECT * FROM TimePiece ORDER BY timePoint")
    fun getOrderedTimePiece(): List<TimePiece>

    @Query("SELECT * FROM TimePiece ORDER BY id DESC LIMIT 1")
    fun getLatestRow(): List<TimePiece>

    @Query("select * from TimePiece")
    fun loadAllTimePieces(): List<TimePiece>

    @Query("SELECT COUNT(*) FROM TimePiece")
    fun getCount(): Int
    @Query("delete from TimePiece where emotion = :emotion")
    fun deleteTimePieceByEmotion(emotion: Int): Int

}