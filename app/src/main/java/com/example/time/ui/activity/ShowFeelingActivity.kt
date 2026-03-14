package com.example.time.ui.activity

import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.showTimePieces.DatePeriodPicker
import com.example.time.ui.showTimePieces.HowTimeGo
import com.example.time.ui.showTimePieces.TimeFeelingList
import com.example.tiptime.ui.theme.TimeTheme

class ShowFeelingActivity : ComponentActivity() {
    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            Box(Modifier.safeDrawingPadding()) {
                showFeelingPieces(viewModel = lifePieceViewModel)
            }
//            TimeTheme {
//                Box(Modifier.safeDrawingPadding()){
//                    Surface(
//                        modifier = Modifier.fillMaxSize(),
//                    ) {
//                        showFeelingPieces(viewModel = lifePieceViewModel)
//                    }
//                }
//            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun showFeelingPieces(viewModel: TimeViewModel){
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    Column {
        DatePeriodPicker(viewModel = viewModel)
        HowTimeGo(timePieces = timePieces)
        TimeFeelingList(timePieceList = timePieces)
    }
}
