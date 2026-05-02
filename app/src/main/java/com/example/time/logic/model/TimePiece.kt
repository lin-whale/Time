package com.example.time.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.json.JSONArray

/**
 * 时间片段数据模型
 * 
 * @param id 主键（自动生成）
 * @param timePoint 结束时间（时间戳）
 * @param fromTimePoint 开始时间（时间戳）
 * @param emotion 心情等级 (1-5)
 * @param lastTimeRecord 体验记录文本
 * @param mainEvent 主事件名称
 * @param subEvent 子事件名称
 * @param mediaPaths 媒体附件路径列表（JSON字符串格式，可选）
 */
@Entity
data class TimePiece(
    @PrimaryKey(autoGenerate = true) var id: Long = 0,
    var timePoint: Long,
    var fromTimePoint: Long,
    var emotion: Int,
    var lastTimeRecord: String,
    var mainEvent: String,
    var subEvent: String,
    var mediaPaths: String? = null  // 可选字段，JSON数组格式存储媒体路径
) {
    /**
     * 获取媒体路径列表
     */
    fun getMediaList(): List<String> {
        return try {
            mediaPaths?.let {
                val jsonArray = JSONArray(it)
                (0 until jsonArray.length()).map { jsonArray.getString(it) }
            } ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 设置媒体路径列表
     */
    fun setMediaList(mediaList: List<String>) {
        mediaPaths = if (mediaList.isEmpty()) null else JSONArray(mediaList).toString()
    }
    
    /**
     * 添加单个媒体路径
     */
    fun addMedia(path: String) {
        val list = getMediaList().toMutableList()
        if (path !in list) {
            list.add(path)
            setMediaList(list)
        }
    }
    
    /**
     * 移除单个媒体路径
     */
    fun removeMedia(path: String) {
        val list = getMediaList().toMutableList()
        list.remove(path)
        setMediaList(list)
    }
    
    /**
     * 获取媒体数量
     */
    fun getMediaCount(): Int = getMediaList().size
    
    /**
     * 是否有媒体附件
     */
    fun hasMedia(): Boolean = getMediaList().isNotEmpty()
}