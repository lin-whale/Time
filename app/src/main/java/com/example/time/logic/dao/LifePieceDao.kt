/**
 * LifePieceDao - 生活片段标签数据访问对象
 * 
 * 改动说明：
 * - 新增 deleteAll: 清空所有标签数据
 * 
 * 开发原则：
 * - 所有数据存储在本地 Room 数据库
 * - 不涉及任何网络传输
 */
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

    /**
     * 获取所有生活片段标签，按字母顺序排列
     */
    @Query("SELECT * FROM LifePiece ORDER BY lifePiece ASC")
    fun getAlphabetizedLifePieces(): LiveData<List<LifePiece>>

    /**
     * 插入新的生活片段标签
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(lifePiece: LifePiece): Long

    /**
     * 获取所有生活片段标签
     */
    @Query("select * from LifePiece")
    fun loadAllLifePieces(): LiveData<List<LifePiece>>

    /**
     * 根据标签名称删除
     */
    @Query("DELETE FROM LifePiece WHERE lifePiece = :lifePiece")
    fun deleteByLifePiece(lifePiece: String)

    /**
     * 删除指定的生活片段标签
     */
    @Delete
    fun deleteLifePiece(lifePiece: LifePiece)
    
    /**
     * 清空所有生活片段标签
     * 警告：此操作不可撤销！仅用于数据导入前的清理
     */
    @Query("DELETE FROM LifePiece")
    fun deleteAll()
}
