package com.example.time.ui.activity

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import android.text.Layout.Alignment
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.showTimePieces.DatePeriodPicker
import com.example.time.ui.showTimePieces.HowTimeGo
import com.example.time.ui.showTimePieces.TimeFeelingList
import com.example.time.ui.showTimePieces.TimeFeelingListByEvent
import com.example.time.ui.showTimePieces.TimePieceList
import com.example.tiptime.ui.theme.TimeTheme

class ShowEventFeelingActivity : ComponentActivity() {
    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val mainEvent = intent.getStringExtra("mainEvent")
        setContent {
            if (mainEvent != null) {
                Box(Modifier.safeDrawingPadding()) {
                    showEventFeelingPieces(viewModel = lifePieceViewModel, mainEvent = mainEvent)
                }
//                TimeTheme {
//                    Box(Modifier.safeDrawingPadding()){
//                        Surface(
//                            modifier = Modifier.fillMaxSize(),
//                        ) {
//                            showEventFeelingPieces(viewModel = lifePieceViewModel, mainEvent = mainEvent)
//                        }
//                    }
//                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun showEventFeelingPieces(viewModel: TimeViewModel, mainEvent: String){
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    viewModel.getTimePiecesByMainEvent(mainEvent)
    Column {
        Text(text = mainEvent, textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth())
        HowTimeGo(timePieces = timePieces)
        TimeFeelingListByEvent(timePieceList = timePieces)
    }
}
