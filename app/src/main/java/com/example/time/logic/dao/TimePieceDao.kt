/**
 * TimePieceDao - 时间片段数据访问对象
 * 
 * 改动说明：
 * - 新增 deleteAll: 清空所有时间片段数据
 * - 优化注释，说明各方法用途
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
import com.example.time.logic.model.TimePiece

@Dao
interface TimePieceDao {

    /**
     * 插入新的时间片段
     * 如果存在相同主键则忽略
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    fun insert(timePiece: TimePiece): Long

    /**
     * 获取指定时间范围内的时间片段
     * 按时间升序排列
     */
    @Query("SELECT * FROM TimePiece WHERE timePoint BETWEEN :startTime AND :endTime ORDER BY timePoint ASC")
    fun getTimePiecesBetween(startTime: Long, endTime: Long): List<TimePiece>

    /**
     * 根据主事件名称获取所有相关记录
     */
    @Query("SELECT * FROM TimePiece WHERE mainEvent = :mainEvent")
    fun getTimePiecesByMainEvent(mainEvent: String): List<TimePiece>

    /**
     * 更新已有的时间片段
     */
    @Update
    fun updateTimePiece(newTimePiece: TimePiece)

    /**
     * 删除指定的时间片段
     */
    @Delete
    fun deleteTimePiece(timePiece: TimePiece)

    /**
     * 获取所有时间片段，按时间顺序排列
     * 用于数据导出和统计
     */
    @Query("SELECT * FROM TimePiece ORDER BY timePoint")
    fun getOrderedTimePiece(): List<TimePiece>

    /**
     * 获取最新的一条记录
     * 返回 List 是因为可能为空
     */
    @Query("SELECT * FROM TimePiece ORDER BY id DESC LIMIT 1")
    fun getLatestRow(): LiveData<List<TimePiece>>

    /**
     * 获取所有时间片段（LiveData 自动更新）
     */
    @Query("select * from TimePiece")
    fun loadAllTimePieces(): LiveData<List<TimePiece>>

    /**
     * 获取时间片段总数
     */
    @Query("SELECT COUNT(*) FROM TimePiece")
    fun getCount(): Int
    
    /**
     * 根据情绪评分删除记录
     */
    @Query("delete from TimePiece where emotion = :emotion")
    fun deleteTimePieceByEmotion(emotion: Int): Int
    
    /**
     * 清空所有时间片段数据
     * 警告：此操作不可撤销！仅用于数据导入前的清理
     */
    @Query("DELETE FROM TimePiece")
    fun deleteAll()
}
