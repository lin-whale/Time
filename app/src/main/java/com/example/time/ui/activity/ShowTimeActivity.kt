package com.example.time.ui.activity

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.showTimePieces.DatePeriodPicker
import com.example.time.ui.showTimePieces.WhereTimeFly

class ShowTimeActivity : ComponentActivity() {
    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            showTime(viewModel = lifePieceViewModel)
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun showTime(viewModel: TimeViewModel){
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    Column {
        DatePeriodPicker(viewModel = viewModel)
        WhereTimeFly(timePieces = timePieces)
    }
}
