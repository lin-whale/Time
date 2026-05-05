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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.time.LifePieceApplication
import com.example.time.ui.TimeViewModel
import com.example.time.ui.TimeViewModelFactory
import com.example.time.ui.showTimePieces.DatePeriodPicker
import com.example.time.ui.showTimePieces.TimePieceList
import com.example.time.ui.showTimePieces.WhereTimeFly
import com.example.time.ui.timeRecord.TimeAPPMainLayout
import com.example.tiptime.ui.theme.TimeTheme
import java.time.LocalDate
import java.time.ZoneId

class ShowTimePiecesActivity : ComponentActivity() {
    private val lifePieceViewModel: TimeViewModel by viewModels {
        TimeViewModelFactory((application as LifePieceApplication).repository)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            TimeTheme {
                Box(Modifier.safeDrawingPadding()) {
                    showTimePieces(viewModel = lifePieceViewModel)
                }
            }
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun showTimePieces(viewModel: TimeViewModel){
    // 保存当前时间范围状态
    var currentTimeFrom by remember { mutableStateOf(LocalDate.now().atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()) }
    var currentTimeTo by remember { mutableStateOf(LocalDate.now().plusDays(1).atStartOfDay(ZoneId.of("Asia/Shanghai")).toInstant().toEpochMilli()) }
    
    // 刷新计数器，用于触发重新查询
    var refreshCounter by remember { mutableStateOf(0) }
    
    // 每当 refreshCounter 变化时重新查询
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    
    // 刷新函数
    val refresh: () -> Unit = {
        refreshCounter++
        viewModel.getTimePiecesBetween(currentTimeFrom, currentTimeTo)
    }
    
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column {
            DatePeriodPicker(
                viewModel = viewModel,
                onTimeRangeChanged = { from, to ->
                    currentTimeFrom = from
                    currentTimeTo = to
                }
            )
            // 传入 viewModel 和刷新回调，支持点击编辑
            TimePieceList(
                timePieces = timePieces,
                viewModel = viewModel,
                onRefresh = refresh
            )
        }
    }
}
