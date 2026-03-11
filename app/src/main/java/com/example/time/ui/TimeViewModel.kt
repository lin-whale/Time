/**
 * TimeViewModel - 时间记录视图模型
 * 
 * 改动说明：
 * - 新增 updateTimePiece: 更新已有的时间片段
 * - 新增 deleteTimePiece: 删除时间片段
 * - 新增 insertTimePieceWithSplit: 在现有片段中插入新记录并自动切割时间
 * - 新增 getAllTimePiecesOnce: 一次性获取所有记录（用于数据导出）
 * 
 * 开发原则：
 * - 所有数据操作在本地进行，不涉及网络传输
 * - 使用协程确保数据库操作不阻塞主线程
 */
package com.example.time.ui

import TimeRepository
import androidx.annotation.WorkerThread
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class TimeViewModel(private val repository: TimeRepository) : ViewModel() {

    // ========== 现有功能 ==========
    
    // 使用 LiveData 观察数据变化，自动更新 UI
    val allLifePieces: LiveData<List<LifePiece>> = repository.allLifePieces
    val allTimePieces: LiveData<List<TimePiece>> = repository.allTimePieces
    val previousTimePiece: LiveData<List<TimePiece>> = repository.previousTimePiece

    /**
     * 插入新的生活片段标签
     */
    fun insertLifePiece(lifePiece: LifePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertLifePiece(lifePiece)
    }

    /**
     * 删除生活片段标签
     */
    fun deleteLifePiece(lifePiece: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteLifePiece(lifePiece)
    }

    /**
     * 插入新的时间片段
     */
    fun insertTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTimePiece(timePiece)
    }
    
    // 用于时间范围查询的结果
    private val _timePieces = MutableLiveData<List<TimePiece>>()
    val timePieces: LiveData<List<TimePiece>> = _timePieces

    /**
     * 获取指定时间范围内的时间片段
     */
    fun getTimePiecesBetween(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val timePiecesList = repository.getTimePiecesBetween(startTime, endTime)
            _timePieces.value = timePiecesList
        }
    }

    /**
     * 根据主事件获取时间片段
     */
    fun getTimePiecesByMainEvent(mainEvent: String){
        viewModelScope.launch {
            val timePiecesList = repository.getTimePiecesByMainEvent(mainEvent)
            _timePieces.value = timePiecesList
        }
    }
    
    // ========== 新增编辑功能 ==========
    
    /**
     * 更新已有的时间片段
     * 用于编辑记录的开始时间、结束时间、事件名称、情绪等
     * 
     * @param timePiece 修改后的时间片段对象（必须包含有效的id）
     */
    fun updateTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTimePiece(timePiece)
    }
    
    /**
     * 删除指定的时间片段
     * 
     * @param timePiece 要删除的时间片段
     */
    fun deleteTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTimePiece(timePiece)
    }
    
    /**
     * 在现有时间片段中插入新记录，自动切割时间
     * 
     * 例如：原记录 10:00-12:00，在 11:00 处切割
     * 结果：新记录 10:00-11:00，原记录变为 11:00-12:00
     * 
     * @param splitTime 切割时间点
     * @param originalPiece 原始时间片段
     * @param newPiece 新插入的时间片段（timePoint 会被设为 splitTime，fromTimePoint 会被设为原记录的 fromTimePoint）
     */
    fun insertTimePieceWithSplit(
        splitTime: Long,
        originalPiece: TimePiece,
        newPiece: TimePiece
    ) = viewModelScope.launch(Dispatchers.IO) {
        // 1. 更新原记录的开始时间为切割点
        val updatedOriginal = originalPiece.copy(fromTimePoint = splitTime)
        repository.updateTimePiece(updatedOriginal)
        
        // 2. 插入新记录：开始时间为原记录的开始时间，结束时间为切割点
        val newPieceWithTime = newPiece.copy(
            fromTimePoint = originalPiece.fromTimePoint,
            timePoint = splitTime
        )
        repository.insertTimePiece(newPieceWithTime)
    }
    
    // ========== 数据导入导出支持 ==========
    
    // 导出结果
    private val _exportData = MutableLiveData<List<TimePiece>>()
    val exportData: LiveData<List<TimePiece>> = _exportData
    
    /**
     * 获取所有时间片段数据（用于导出）
     * 按时间顺序排列
     */
    fun getAllTimePiecesForExport() = viewModelScope.launch {
        val allPieces = repository.getAllTimePiecesOrdered()
        _exportData.value = allPieces
    }
    
    /**
     * 批量导入时间片段数据
     * 用于从备份文件恢复数据
     * 
     * @param pieces 要导入的时间片段列表
     * @param clearExisting 是否清除现有数据（默认 false，追加导入）
     */
    fun importTimePieces(pieces: List<TimePiece>, clearExisting: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (clearExisting) {
            repository.clearAllTimePieces()
        }
        pieces.forEach { piece ->
            // 重置 id 为 0，让数据库自动生成新 id
            repository.insertTimePiece(piece.copy(id = 0))
        }
    }
    
    /**
     * 批量导入生活片段标签
     */
    fun importLifePieces(pieces: List<LifePiece>, clearExisting: Boolean = false) = viewModelScope.launch(Dispatchers.IO) {
        if (clearExisting) {
            repository.clearAllLifePieces()
        }
        pieces.forEach { piece ->
            repository.insertLifePiece(piece.copy(id = 0))
        }
    }
}

/**
 * ViewModel 工厂类
 * 用于依赖注入 Repository
 */
class TimeViewModelFactory(private val repository: TimeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
