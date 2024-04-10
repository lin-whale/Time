import androidx.annotation.WorkerThread
import androidx.lifecycle.LiveData
import com.example.time.logic.dao.LifePieceDao
import com.example.time.logic.dao.TimePieceDao
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TimeRepository(private val timePieceDao: TimePieceDao, private val lifePieceDao: LifePieceDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    var allLifePieces: LiveData<List<LifePiece>> = lifePieceDao.loadAllLifePieces()
    var allTimePieces: LiveData<List<TimePiece>> = timePieceDao.loadAllTimePieces()
    var previousTimePiece: LiveData<List<TimePiece>> = timePieceDao.getLatestRow()

    suspend fun getTimePiecesBetween(startTime: Long, endTime: Long): List<TimePiece> {
        return withContext(Dispatchers.IO) {
            timePieceDao.getTimePiecesBetween(startTime, endTime)
        }
    }

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @WorkerThread
    fun insertLifePiece(lifePiece: LifePiece) {
        lifePieceDao.insert(lifePiece)
    }

    @WorkerThread
    fun insertTimePiece(timePiece: TimePiece) {
        timePieceDao.insert(timePiece)
    }
}