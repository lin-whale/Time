package com.example.time.ui.activity

import android.content.Intent
import android.os.Build
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import android.os.Bundle
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.showTimePieces.CustomDatePicker
import com.example.time.ui.showTimePieces.HowTimeGo
import com.example.time.ui.showTimePieces.TimeFeelingList
import com.example.time.ui.showTimePieces.TimeFeelingListByEvent
import com.example.time.ui.showTimePieces.TimePieceList
import com.example.tiptime.ui.theme.TimeTheme
import java.time.LocalDate
import java.time.ZoneId

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
            TimeTheme {
                if (mainEvent != null) {
                    Box(Modifier.safeDrawingPadding()) {
                        showEventFeelingPieces(
                            viewModel = lifePieceViewModel, 
                            mainEvent = mainEvent,
                            onBackPressed = { finish() }
                        )
                    }
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun showEventFeelingPieces(viewModel: TimeViewModel, mainEvent: String, onBackPressed: () -> Unit = {}){
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    // 默认获取最近1年的数据
    viewModel.getTimePiecesByMainEvent(mainEvent)
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            Text(
                text = mainEvent, 
                textAlign = TextAlign.Center, 
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onBackground
            )
            // 添加时间选择器
            EventDatePeriodPicker(viewModel = viewModel, mainEvent = mainEvent)
            HowTimeGo(timePieces = timePieces, onBackPressed = onBackPressed)
            TimeFeelingListByEvent(timePieceList = timePieces)
        }
    }
}

/**
 * 事件统计的时间选择器
 * 默认显示最近1年
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun EventDatePeriodPicker(viewModel: TimeViewModel, mainEvent: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    val prefs = remember { context.getSharedPreferences("time_app_settings", android.content.Context.MODE_PRIVATE) }
    
    // 默认显示最近1年
    val now = java.time.LocalDate.now()
    val dateFrom = remember { mutableStateOf(now.minusYears(1)) }
    val dateTo = remember { mutableStateOf(now) }

    val msTimeFrom = remember {
        mutableStateOf(
            dateFrom.value.atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
        )
    }
    val msTimeTo = remember {
        mutableStateOf(
            dateTo.value.atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
        )
    }
    
    // 初始加载
    LaunchedEffect(Unit) {
        viewModel.getTimePiecesByMainEventBetween(mainEvent, msTimeFrom.value, msTimeTo.value)
    }

    Column {
        Row(
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            com.example.time.ui.showTimePieces.CustomDatePicker(
                value = dateFrom.value,
                onValueChange = {
                    msTimeFrom.value =
                        it.atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    dateFrom.value = it
                    viewModel.getTimePiecesByMainEventBetween(mainEvent, msTimeFrom.value, msTimeTo.value)
                },
                modifier = Modifier.weight(1f)
            )
            androidx.compose.material3.Icon(
                imageVector = androidx.compose.material.icons.Icons.Default.ArrowForward,
                contentDescription = "Arrow",
                tint = MaterialTheme.colorScheme.onSurface,
                modifier = androidx.compose.ui.Modifier.padding(horizontal = 8.dp)
            )
            com.example.time.ui.showTimePieces.CustomDatePicker(
                value = dateTo.value,
                onValueChange = {
                    msTimeTo.value =
                        it.atStartOfDay(java.time.ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()
                    dateTo.value = it
                    viewModel.getTimePiecesByMainEventBetween(mainEvent, msTimeFrom.value, msTimeTo.value)
                },
                modifier = Modifier.weight(1f)
            )
        }
    }
}
