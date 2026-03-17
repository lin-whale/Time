package com.example.time.ui.showTimePieces

import android.content.Intent
import android.util.Log
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.time.R
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.activity.ShowEventFeelingActivity
import com.example.time.ui.theme.EmotionColors
import java.util.*
import kotlin.math.*

/**
 * 心情分布统计界面 - 现代化设计版本
 * 展示不同心情等级的时间分布
 * 支持饼图点击交互、列表展开查看时间片详情
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun HowTimeGo(
    timePieces: List<TimePiece>,
    onFeelingClick: (Int) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // 计算各心情等级的时间分布
    val feelingDurations = remember(timePieces) {
        timePieces
            .filter { it.timePoint > 0 && it.emotion > 0 }
            .groupBy { it.emotion }
            .mapValues { (_, pieces) ->
                pieces.sumOf { piece ->
                    piece.timePoint - piece.fromTimePoint
                }
            }
            .filter { it.value > 0 }
            .toList()
            .sortedByDescending { it.second }
    }
    
    // 按心情分组的时间片
    val timePiecesByFeeling = remember(timePieces) {
        timePieces
            .filter { it.timePoint > 0 && it.emotion > 0 }
            .groupBy { it.emotion }
    }

    val totalDuration = feelingDurations.sumOf { it.second }
    
    // 选中状态
    var selectedFeelingIndex by remember { mutableStateOf(-1) }
    // 展开状态
    val expandedStates = remember { mutableStateListOf<Boolean>().apply { repeat(10) { add(false) } } }
    
    // 列表滚动状态
    val listState = rememberLazyListState()
    
    // 动画状态
    var animationProgress by remember { mutableStateOf(0f) }
    val animatedProgress by animateFloatAsState(
        targetValue = animationProgress,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "chartAnimation"
    )

    LaunchedEffect(Unit) {
        animationProgress = 1f
    }

    // 心情等级对应的颜色（从差到好）
    val feelingColors = remember {
        listOf(
            Color(android.graphics.Color.parseColor("#FF6B6B")), // 😞 很差 - 红色
            Color(android.graphics.Color.parseColor("#FF9F43")), // 😕 较差 - 橙色
            Color(android.graphics.Color.parseColor("#FECA57")), // 😐 一般 - 黄色
            Color(android.graphics.Color.parseColor("#1DD1A1")), // 😊 较好 - 青绿
            Color(android.graphics.Color.parseColor("#10AC84"))  // 😄 很好 - 翠绿
        )
    }

    // 心情等级对应的表情
    val feelingEmojis = listOf("😞", "😕", "😐", "😊", "😄")
    val feelingLabels = listOf("很差", "较差", "一般", "较好", "很好")
    
    // 饼图点击处理
    fun onPieSliceClick(index: Int) {
        selectedFeelingIndex = if (selectedFeelingIndex == index) -1 else index
        if (selectedFeelingIndex >= 0) {
            kotlinx.coroutines.GlobalScope.launch {
                listState.animateScrollToItem(index + 2)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "心情分布",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackPressed) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = Color.Transparent
                )
            )
        }
    ) { padding ->
        if (feelingDurations.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "🎭",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "暂无心情数据",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "给时间片添加心情后就能看到分布啦",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 16.dp),
                state = listState,
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(top = 8.dp, bottom = 80.dp)
            ) {
                // 心情概览卡片
                item {
                    FeelingOverviewCard(
                        totalDuration = totalDuration,
                        itemCount = timePieces.count { it.emotion > 0 },
                        avgFeeling = if (feelingDurations.isNotEmpty() && totalDuration > 0) {
                            val totalFeelingScore = timePieces
                                .filter { it.emotion > 0 && it.timePoint > 0 }
                                .sumOf { it.emotion.toLong() * (it.timePoint - it.fromTimePoint) }
                            (totalFeelingScore.toFloat() / totalDuration).coerceIn(1f, 5f)
                        } else 3f,
                        feelingEmojis = feelingEmojis
                    )
                }
                
                // 饼图区域
                item {
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .shadow(
                                elevation = 4.dp,
                                shape = RoundedCornerShape(24.dp),
                                ambientColor = Color.Black.copy(alpha = 0.1f),
                                spotColor = Color.Black.copy(alpha = 0.1f)
                            ),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surface
                        ),
                        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text(
                                text = "心情分布",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(bottom = 16.dp)
                            )
                            
                            // 饼图（支持点击交互）
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                InteractiveFeelingPieChart(
                                    data = feelingDurations,
                                    colors = feelingColors,
                                    totalDuration = totalDuration,
                                    progress = animatedProgress,
                                    selectedIndex = selectedFeelingIndex,
                                    onSliceClick = { index -> onPieSliceClick(index) }
                                )
                                
                                // 中心显示平均心情或选中心情
                                val displayFeeling = if (selectedFeelingIndex >= 0 && selectedFeelingIndex < feelingDurations.size) {
                                    feelingDurations[selectedFeelingIndex].first
                                } else {
                                    // 计算平均心情
                                    if (feelingDurations.isNotEmpty() && totalDuration > 0) {
                                        val totalFeelingScore = timePieces
                                            .filter { it.emotion > 0 && it.timePoint > 0 }
                                            .sumOf { it.emotion.toLong() * (it.timePoint - it.fromTimePoint) }
                                        (totalFeelingScore.toFloat() / totalDuration).coerceIn(1f, 5f).toInt()
                                    } else 3
                                }
                                
                                val emojiIndex = (displayFeeling - 1).coerceIn(0, 4)
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = feelingEmojis[emojiIndex],
                                        fontSize = 32.sp
                                    )
                                    if (selectedFeelingIndex >= 0 && selectedFeelingIndex < feelingDurations.size) {
                                        val (feeling, duration) = feelingDurations[selectedFeelingIndex]
                                        val percentage = if (totalDuration > 0) 
                                            (duration.toFloat() / totalDuration * 100).toInt() else 0
                                        Text(
                                            text = "$percentage%",
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = feelingColors[feelingColors.indices.find { feelingDurations[selectedFeelingIndex].first - 1 == it } ?: 2]
                                        )
                                    }
                                }
                            }
                            
                            // 心情图例（点击可选中）
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                feelingDurations.forEach { (feeling, duration) ->
                                    val percentage = if (totalDuration > 0) 
                                        (duration.toFloat() / totalDuration * 100).toInt() else 0
                                    val feelingIndex = feelingDurations.indexOf(Pair(feeling, duration))
                                    FeelingLegendItem(
                                        emoji = feelingEmojis[feeling - 1],
                                        feeling = feeling,
                                        duration = duration,
                                        percentage = percentage,
                                        color = feelingColors[feeling - 1],
                                        isSelected = selectedFeelingIndex == feelingIndex,
                                        onClick = { onPieSliceClick(feelingIndex) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 详细列表标题
                item {
                    Text(
                        text = "心情详情",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
                
                // 心情等级卡片列表
                items(feelingDurations) { (feeling, duration) ->
                    val percentage = if (totalDuration > 0) 
                        duration.toFloat() / totalDuration else 0f
                    val feelingIndex = feelingDurations.indexOf(Pair(feeling, duration))
                    val isSelected = selectedFeelingIndex == feelingIndex
                    
                    ExpandableFeelingCard(
                        feeling = feeling,
                        emoji = feelingEmojis[feeling - 1],
                        label = feelingLabels[feeling - 1],
                        duration = duration,
                        percentage = percentage,
                        color = feelingColors[feeling - 1],
                        timePieces = timePiecesByFeeling[feeling] ?: emptyList(),
                        isExpanded = expandedStates[feelingIndex],
                        isSelected = isSelected,
                        onToggleExpand = { expandedStates[feelingIndex] = !expandedStates[feelingIndex] },
                        onFeelingClick = { onFeelingClick(feeling) }
                    )
                }
            }
        }
    }
}

/**
 * 可交互的心情饼图组件
 */
@Composable
private fun InteractiveFeelingPieChart(
    data: List<Pair<Int, Long>>,
    colors: List<Color>,
    totalDuration: Long,
    progress: Float,
    selectedIndex: Int,
    onSliceClick: (Int) -> Unit
) {
    val sliceAngles = remember(data, totalDuration) {
        var startAngle = -90f
        data.map { (_, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            val result = startAngle to startAngle + sweep
            startAngle += sweep
            result
        }
    }
    
    Canvas(
        modifier = Modifier
            .fillMaxSize()
            .pointerInput(data, totalDuration) {
                detectTapGestures { offset ->
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    
                    val distance = sqrt(dx * dx + dy * dy)
                    val outerRadius = size.minDimension / 2f
                    val innerRadius = outerRadius / 4f
                    
                    if (distance >= innerRadius && distance <= outerRadius) {
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        angle = (angle + 90 + 360) % 360
                        
                        sliceAngles.forEachIndexed { index, (start, end) ->
                            val simpleAngle = angle - 90
                            if (simpleAngle in start..end || 
                                (start < -90 && simpleAngle in (start + 360)..(end + 360))) {
                                onSliceClick(index)
                                return@detectTapGestures
                            }
                        }
                    }
                }
            }
    ) {
        var startAngle = -90f
        
        data.forEachIndexed { index, (feeling, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            
            val animatedSweep = sweep * progress
            val color = colors[feeling - 1]
            val isSelected = selectedIndex == index
            
            val radius = if (isSelected) size.minDimension / 2f + 8.dp.toPx() else size.minDimension / 2f
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                size = Size(radius * 2, radius * 2),
                topLeft = Offset(center.x - radius, center.y - radius)
            )
            
            startAngle += animatedSweep
        }
        
        drawCircle(
            color = Color.White,
            radius = size.minDimension / 4f,
            center = center
        )
    }
}

@Composable
private fun FeelingOverviewCard(
    totalDuration: Long,
    itemCount: Int,
    avgFeeling: Float,
    feelingEmojis: List<String>
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(20.dp)
            ),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 平均心情
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = feelingEmojis[(avgFeeling - 1).toInt().coerceIn(0, 4)],
                    fontSize = 28.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "平均",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
            
            // 总时长
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = formatDurationShort(totalDuration),
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "总时长",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
            
            // 时间片数量
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$itemCount",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "心情片",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
private fun ModernFeelingPieChart(
    data: List<Pair<Int, Long>>,
    colors: List<Color>,
    totalDuration: Long,
    progress: Float
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        var startAngle = -90f
        
        data.forEach { (feeling, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            
            val animatedSweep = sweep * progress
            val color = colors[feeling - 1]
            
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                size = size
            )
            
            startAngle += animatedSweep
        }
        
        drawCircle(
            color = Color.White,
            radius = size.minDimension / 4f,
            center = center
        )
    }
}

@Composable
private fun FeelingLegendItem(
    emoji: String,
    feeling: Int,
    duration: Long,
    percentage: Int,
    color: Color,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$percentage%",
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = color
        )
    }
}

/**
 * 可展开的心情卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableFeelingCard(
    feeling: Int,
    emoji: String,
    label: String,
    duration: Long,
    percentage: Float,
    color: Color,
    timePieces: List<TimePiece>,
    isExpanded: Boolean,
    isSelected: Boolean = false,
    onToggleExpand: () -> Unit,
    onFeelingClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = 2.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) 
                color.copy(alpha = 0.1f) 
            else 
                MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column {
            // 主卡片内容
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onToggleExpand)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 表情符号（加大）
                Text(
                    text = emoji,
                    fontSize = 28.sp,
                    modifier = Modifier.padding(end = 12.dp)
                )
                
                // 心情标签
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = label,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    // 进度条
                    Box(
                        modifier = Modifier
                            .fillMaxWidth(0.6f)
                            .height(6.dp)
                            .background(
                                MaterialTheme.colorScheme.surfaceVariant,
                                RoundedCornerShape(3.dp)
                            )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(percentage)
                                .background(color, RoundedCornerShape(3.dp))
                        )
                    }
                }
                
                Spacer(modifier = Modifier.width(12.dp))
                
                // 时长
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatDurationShort(duration),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "${(percentage * 100).toInt()}%",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                
                Spacer(modifier = Modifier.width(8.dp))
                
                // 展开/收起图标
                Icon(
                    imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                    contentDescription = if (isExpanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                    modifier = Modifier.size(20.dp)
                )
            }
            
            // 展开后的时间片列表
            if (isExpanded && timePieces.isNotEmpty()) {
                HorizontalDivider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timePieces.forEach { piece ->
                        TimePieceItemFeeling(
                            timePiece = piece,
                            color = color,
                            onClick = { /* 可以添加点击查看详情逻辑 */ }
                        )
                    }
                    
                    // 查看详细统计
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onFeelingClick,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("查看「$label」相关事件")
                    }
                }
            }
        }
    }
}

/**
 * 时间片项（心情视图）
 */
@Composable
private fun TimePieceItemFeeling(
    timePiece: TimePiece,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(8.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧颜色条
            Box(
                modifier = Modifier
                    .width(3.dp)
                    .height(40.dp)
                    .background(color, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            Column(modifier = Modifier.weight(1f)) {
                // 时间
                Text(
                    text = "${convertTimeFormat(timePiece.timePoint, "M/d HH:mm")} · ${convertDurationFormat(timePiece.timePoint - timePiece.fromTimePoint, "%d时%d分")}",
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                // 事件描述
                Text(
                    text = timePiece.mainEvent + if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
            
            // 心情
            Text(
                text = listOf("😞", "😕", "😐", "😊", "😄").getOrElse(timePiece.emotion - 1) { "😐" },
                fontSize = 20.sp
            )
        }
    }
}

private fun formatDurationShort(durationMs: Long): String {
    val hours = durationMs / (1000 * 60 * 60)
    val minutes = (durationMs % (1000 * 60 * 60)) / (1000 * 60)
    
    return when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        minutes > 0 -> "${minutes}m"
        else -> "0m"
    }
}
