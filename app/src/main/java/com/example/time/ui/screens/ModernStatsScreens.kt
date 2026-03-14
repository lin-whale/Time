package com.example.time.ui.screens

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.ui.TimeViewModel
import com.example.time.ui.theme.EmotionColors
import java.time.Duration
import kotlin.math.roundToInt

/**
 * 现代化时间统计界面
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernTimeStatsScreen(
    viewModel: TimeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    val statsData = remember(timePieces) {
        calculateTimeStats(timePieces)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("时间统计") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                TimeOverviewCard(statsData.totalMinutes, statsData.eventCount)
            }
            
            item {
                Text(
                    text = "事件时长排行",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(statsData.eventStats) { eventStat ->
                SimpleEventCard(eventStat, statsData.totalMinutes)
            }
        }
    }
}

/**
 * 现代化心情统计界面
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ModernFeelingStatsScreen(
    viewModel: TimeViewModel,
    onBackClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val timePieces by viewModel.timePieces.observeAsState(listOf())
    val feelingData = remember(timePieces) {
        calculateFeelingStats(timePieces)
    }
    
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("心情统计") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, "返回")
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                FeelingOverviewCard(feelingData.totalCount, feelingData.averageEmotion)
            }
            
            item {
                Text(
                    text = "心情分布",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold
                )
            }
            
            items(feelingData.emotionStats) { emotionStat ->
                SimpleEmotionCard(emotionStat, feelingData.totalCount)
            }
        }
    }
}

@Composable
fun TimeOverviewCard(totalMinutes: Int, eventCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("总时长: ${totalMinutes / 60}h ${totalMinutes % 60}m", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("事件数: $eventCount", fontSize = 16.sp)
        }
    }
}

@Composable
fun FeelingOverviewCard(totalCount: Int, averageEmotion: Float) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("记录数: $totalCount", fontSize = 18.sp)
            Spacer(modifier = Modifier.height(8.dp))
            Text("平均心情: ${String.format("%.1f", averageEmotion)}星", fontSize = 16.sp)
        }
    }
}

@Composable
fun SimpleEventCard(eventStat: EventTimeStat, totalMinutes: Int) {
    val percentage = if (totalMinutes > 0) {
        (eventStat.minutes.toFloat() / totalMinutes * 100).roundToInt()
    } else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(eventStat.eventName, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(4.dp))
            Text("${eventStat.minutes / 60}h ${eventStat.minutes % 60}m ($percentage%)")
        }
    }
}

@Composable
fun SimpleEmotionCard(emotionStat: EmotionStat, totalCount: Int) {
    val percentage = if (totalCount > 0) {
        (emotionStat.count.toFloat() / totalCount * 100).roundToInt()
    } else 0
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            repeat(emotionStat.emotion) {
                Icon(
                    Icons.Default.Star,
                    contentDescription = null,
                    tint = EmotionColors.getEmotionColor(emotionStat.emotion),
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text("${emotionStat.count}次 ($percentage%)")
        }
    }
}

// 数据类
data class TimeStatsData(
    val totalMinutes: Int,
    val eventCount: Int,
    val eventStats: List<EventTimeStat>
)

data class EventTimeStat(
    val eventName: String,
    val minutes: Int,
    val count: Int
)

data class FeelingStatsData(
    val totalCount: Int,
    val averageEmotion: Float,
    val emotionStats: List<EmotionStat>
)

data class EmotionStat(
    val emotion: Int,
    val count: Int
)

// 计算函数
@RequiresApi(Build.VERSION_CODES.O)
fun calculateTimeStats(timePieces: List<TimePiece>): TimeStatsData {
    if (timePieces.isEmpty()) {
        return TimeStatsData(0, 0, emptyList())
    }
    
    val eventMap = mutableMapOf<String, MutableList<Int>>()
    var totalMinutes = 0
    
    timePieces.forEach { piece ->
        val minutes = try {
            Duration.between(piece.startTime, piece.endTime).toMinutes().toInt()
        } catch (e: Exception) {
            0
        }
        
        if (minutes > 0) {
            totalMinutes += minutes
            eventMap.getOrPut(piece.event) { mutableListOf() }.add(minutes)
        }
    }
    
    val eventStats = eventMap.map { (event, minutes) ->
        EventTimeStat(event, minutes.sum(), minutes.size)
    }.sortedByDescending { it.minutes }
    
    return TimeStatsData(totalMinutes, eventStats.size, eventStats)
}

fun calculateFeelingStats(timePieces: List<TimePiece>): FeelingStatsData {
    if (timePieces.isEmpty()) {
        return FeelingStatsData(0, 0f, emptyList())
    }
    
    val emotionMap = mutableMapOf<Int, Int>()
    var totalEmotion = 0
    
    timePieces.forEach { piece ->
        val emotion = piece.emotion
        emotionMap[emotion] = emotionMap.getOrDefault(emotion, 0) + 1
        totalEmotion += emotion
    }
    
    val emotionStats = emotionMap.map { (emotion, count) ->
        EmotionStat(emotion, count)
    }.sortedByDescending { it.count }
    
    val averageEmotion = totalEmotion.toFloat() / timePieces.size
    
    return FeelingStatsData(timePieces.size, averageEmotion, emotionStats)
}
