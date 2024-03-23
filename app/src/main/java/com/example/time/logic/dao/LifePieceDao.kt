package com.example.time.logic.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import kotlinx.coroutines.flow.Flow

@Dao
interface LifePieceDao {

    @Query("SELECT * FROM LifePiece ORDER BY lifePiece")
    fun getAlphabetizedLifePieces(): List<LifePiece>

    @Insert
    fun insert(lifePiece: LifePiece): Long
    // suspend

    @Query("select * from LifePiece")
    fun loadAllLifePieces(): List<LifePiece>

//    @Query("DELETE FROM life_piece_table")
//    suspend fun deleteAll() : Int

}