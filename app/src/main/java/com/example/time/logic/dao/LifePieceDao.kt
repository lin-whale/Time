package com.example.time.logic.dao

import androidx.lifecycle.LiveData
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

    @Query("SELECT * FROM LifePiece ORDER BY lifePiece ASC")
    fun getAlphabetizedLifePieces(): LiveData<List<LifePiece>>

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(lifePiece: LifePiece): Long

    @Query("select * from LifePiece")
    fun loadAllLifePieces(): LiveData<List<LifePiece>>

    @Query("DELETE FROM LifePiece WHERE lifePiece = :lifePiece")
    fun deleteByLifePiece(lifePiece: String)

    @Delete
    fun deleteLifePiece(lifePiece: LifePiece)
}