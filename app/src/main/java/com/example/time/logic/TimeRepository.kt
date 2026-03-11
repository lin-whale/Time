/**
 * TimeRepository - 数据仓库层
 * 
 * 改动说明：
 * - 新增 updateTimePiece: 更新时间片段
 * - 新增 deleteTimePiece: 删除时间片段
 * - 新增 getAllTimePiecesOrdered: 按时间顺序获取所有记录
 * - 新增 clearAllTimePieces/clearAllLifePieces: 清空数据（用于导入前清理）
 * 
 * 开发原则：
 * - Repository 层隔离数据源和业务逻辑
 * - 所有数据库操作在 IO 线程执行
 * - 数据仅存储在本地 Room 数据库，不上传网络
 */

import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.time.logic.dao.LifePieceDao
import com.example.time.logic.dao.TimePieceDao
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

/**
 * 数据仓库类
 * 提供统一的数据访问接口，封装 DAO 层操作
 * 
 * @param timePieceDao 时间片段数据访问对象
 * @param lifePieceDao 生活片段数据访问对象
 */
class TimeRepository(
    private val timePieceDao: TimePieceDao, 
    private val lifePieceDao: LifePieceDao
) {

    // ========== LiveData 观察数据（自动更新） ==========
    
    /** 所有生活片段标签 */
    var allLifePieces: LiveData<List<LifePiece>> = lifePieceDao.loadAllLifePieces()
    
    /** 所有时间片段记录 */
    var allTimePieces: LiveData<List<TimePiece>> = timePieceDao.loadAllTimePieces()
    
    /** 最新的一条时间记录（用于显示上一事件） */
    var previousTimePiece: LiveData<List<TimePiece>> = timePieceDao.getLatestRow()

    // ========== 查询方法 ==========
    
    /**
     * 获取指定时间范围内的时间片段
     * 
     * @param startTime 开始时间戳（毫秒）
     * @param endTime 结束时间戳（毫秒）
     * @return 符合条件的时间片段列表，按时间升序排列
     */
    suspend fun getTimePiecesBetween(startTime: Long, endTime: Long): List<TimePiece> {
        return withContext(Dispatchers.IO) {
            timePieceDao.getTimePiecesBetween(startTime, endTime)
        }
    }

    /**
     * 根据主事件名称获取所有相关时间片段
     * 
     * @param mainEvent 主事件名称
     * @return 该事件的所有时间记录
     */
    suspend fun getTimePiecesByMainEvent(mainEvent: String): List<TimePiece> {
        return withContext(Dispatchers.IO) {
            timePieceDao.getTimePiecesByMainEvent(mainEvent)
        }
    }
    
    /**
     * 获取所有时间片段，按时间顺序排列
     * 用于数据导出功能
     * 
     * @return 所有时间片段，按 timePoint 升序排列
     */
    suspend fun getAllTimePiecesOrdered(): List<TimePiece> {
        return withContext(Dispatchers.IO) {
            timePieceDao.getOrderedTimePiece()
        }
    }

    // ========== 插入方法 ==========
    
    /**
     * 插入新的生活片段标签
     */
    @WorkerThread
    fun insertLifePiece(lifePiece: LifePiece) {
        lifePieceDao.insert(lifePiece)
    }

    /**
     * 插入新的时间片段记录
     */
    @WorkerThread
    fun insertTimePiece(timePiece: TimePiece) {
        timePieceDao.insert(timePiece)
    }
    
    // ========== 更新方法 ==========
    
    /**
     * 更新已有的时间片段
     * 
     * @param timePiece 修改后的时间片段（必须包含有效的 id）
     */
    @WorkerThread
    fun updateTimePiece(timePiece: TimePiece) {
        timePieceDao.updateTimePiece(timePiece)
    }

    // ========== 删除方法 ==========
    
    /**
     * 删除生活片段标签
     */
    @WorkerThread
    fun deleteLifePiece(lifePiece: String) {
        lifePieceDao.deleteByLifePiece(lifePiece)
    }
    
    /**
     * 删除指定的时间片段
     */
    @WorkerThread
    fun deleteTimePiece(timePiece: TimePiece) {
        timePieceDao.deleteTimePiece(timePiece)
    }
    
    // ========== 清空方法（用于数据导入） ==========
    
    /**
     * 清空所有时间片段数据
     * 警告：此操作不可撤销！
     */
    @WorkerThread
    fun clearAllTimePieces() {
        timePieceDao.deleteAll()
    }
    
    /**
     * 清空所有生活片段标签
     * 警告：此操作不可撤销！
     */
    @WorkerThread
    fun clearAllLifePieces() {
        lifePieceDao.deleteAll()
    }
}
