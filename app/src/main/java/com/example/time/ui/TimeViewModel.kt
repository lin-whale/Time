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

class TimeViewModel(private val repository: TimeRepository) : ViewModel() {

    // Using LiveData and caching what alllifePieces returns has several benefits:
    // - We can put an observer on the data (instead of polling for changes) and only update the
    //   the UI when the data actually changes.
    // - Repository is completely separated from the UI through the ViewModel.
    val allLifePieces: LiveData<List<LifePiece>> = repository.allLifePieces
    val allTimePieces: LiveData<List<TimePiece>> = repository.allTimePieces
    val previousTimePiece: LiveData<List<TimePiece>> = repository.previousTimePiece

    /**
     * Launching a new coroutine to insert the data in a non-blocking way
     */
    fun insertLifePiece(lifePiece: LifePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertLifePiece(lifePiece)
    }

    fun deleteLifePiece(lifePiece: String) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteLifePiece(lifePiece)
    }

    fun insertTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertTimePiece(timePiece)
    }
    private val _timePieces = MutableLiveData<List<TimePiece>>()
    val timePieces: LiveData<List<TimePiece>> = _timePieces

    fun getTimePiecesBetween(startTime: Long, endTime: Long) {
        viewModelScope.launch {
            val timePiecesList = repository.getTimePiecesBetween(startTime, endTime)
            _timePieces.value = timePiecesList
        }
    }

    fun getTimePiecesByMainEvent(mainEvent: String){
        viewModelScope.launch {
            val timePiecesList = repository.getTimePiecesByMainEvent(mainEvent)
            _timePieces.value = timePiecesList
        }
    }
    
    // 新增：更新TimePiece的方法
    fun updateTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.updateTimePiece(timePiece)
    }
    
    // 新增：删除TimePiece的方法
    fun deleteTimePiece(timePiece: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.deleteTimePiece(timePiece)
    }
    
    // 新增：插入和删除的原子操作（用于时间段切割）
    fun insertAndDeleteTimePieces(toInsert: List<TimePiece>, toDelete: TimePiece) = viewModelScope.launch(Dispatchers.IO) {
        repository.insertAndDeleteTimePieces(toInsert, toDelete)
    }
}

class TimeViewModelFactory(private val repository: TimeRepository) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TimeViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return TimeViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}