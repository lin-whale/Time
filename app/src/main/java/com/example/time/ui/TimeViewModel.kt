import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import com.example.time.logic.model.LifePiece
import kotlinx.coroutines.launch

class LifePieceViewModel(private val repository: TimeRepository) : ViewModel() {

    // Using LiveData and caching what alllifePieces returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
//    val allLifePieces: LiveData<List<LifePiece>> = repository.allLifePieces.asLiveData()

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insert(lifePiece: LifePiece) = viewModelScope.launch {
        repository.insert(lifePiece)
    }
}

class LifePieceViewModelFactory(private val repository: TimeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(LifePieceViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return LifePieceViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}