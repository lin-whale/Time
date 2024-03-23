import androidx.annotation.WorkerThread
import com.example.time.logic.dao.LifePieceDao
import com.example.time.logic.dao.TimePieceDao
import com.example.time.logic.model.LifePiece
import com.example.time.logic.model.TimePiece
import kotlinx.coroutines.flow.Flow

// Declares the DAO as a private property in the constructor. Pass in the DAO
// instead of the whole database, because you only need access to the DAO
class TimeRepository(private val lifePieceDao: LifePieceDao) {

    // Room executes all queries on a separate thread.
    // Observed Flow will notify the observer when the data has changed.

    var allLifePieces: List<LifePiece> = lifePieceDao.getAlphabetizedLifePieces()

    // By default Room runs suspend queries off the main thread, therefore, we don't need to
    // implement anything else to ensure we're not doing long running database work
    // off the main thread.
    @Suppress("RedundantSuspendModifier")
    @WorkerThread
    suspend fun insert(lifePiece: LifePiece) {
        lifePieceDao.insert(lifePiece)
    }
}