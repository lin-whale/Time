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
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.R
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.activity.ShowEventFeelingActivity
import com.example.time.ui.theme.ChartColors
import kotlinx.coroutines.launch
import java.util.*
import kotlin.math.*

/**
 * 时间分布统计界面 - 现代化设计版本
 * 使用优雅的卡片布局和微动画
 * 支持饼图点击交互、列表展开查看时间片详情
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun WhereTimeFly(
    timePieces: List<TimePiece>,
    onEventClick: (String) -> Unit = {},
    onBackPressed: () -> Unit = {}
) {
    val context = LocalContext.current
    
    // 计算各类型的时间分布
    val typeDurations = remember(timePieces) {
        timePieces
            .filter { it.timePoint > 0 && it.mainEvent.isNotEmpty() }
            .groupBy { it.mainEvent }
            .mapValues { (_, pieces) ->
                pieces.sumOf { piece ->
                    piece.timePoint - piece.fromTimePoint
                }
            }
            .filter { it.value > 0 }
            .toList()
            .sortedByDescending { it.second }
    }
    
    // 按类型分组的时间片
    val timePiecesByType = remember(timePieces) {
        timePieces
            .filter { it.timePoint > 0 && it.mainEvent.isNotEmpty() }
            .groupBy { it.mainEvent }
    }
    
    val totalDuration = typeDurations.sumOf { it.second }
    
    // 选中状态
    var selectedTypeIndex by remember { mutableStateOf(-1) }
    // 展开状态
    val expandedStates = remember { mutableStateListOf<Boolean>().apply { repeat(100) { add(false) } } }
    
    // 列表滚动状态
    val listState = rememberLazyListState()
    
    // 动画控制器
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
    
    // 解析颜色
    val chartColors = remember {
        listOf(
            Color(android.graphics.Color.parseColor("#FF6B6B")), // 珊瑚红
            Color(android.graphics.Color.parseColor("#4ECDC4")), // 青绿
            Color(android.graphics.Color.parseColor("#45B7D1")), // 天蓝
            Color(android.graphics.Color.parseColor("#96CEB4")), // 薄荷绿
            Color(android.graphics.Color.parseColor("#FFEAA7")), // 柠檬黄
            Color(android.graphics.Color.parseColor("#DDA0DD")), // 梅红
            Color(android.graphics.Color.parseColor("#98D8C8")), // 水绿
            Color(android.graphics.Color.parseColor("#F7DC6F")), // 金黄
            Color(android.graphics.Color.parseColor("#BB8FCE")), // 浅紫
            Color(android.graphics.Color.parseColor("#85C1E9"))  // 浅蓝
        )
    }
    
    // 饼图点击处理：使用 CoroutineScope
    val coroutineScope = rememberCoroutineScope()
    
    fun onPieSliceClick(index: Int) {
        selectedTypeIndex = if (selectedTypeIndex == index) -1 else index
        if (selectedTypeIndex >= 0) {
            // 滚动到对应的列表项（+2 是因为有概览卡片和饼图两个item）
            coroutineScope.launch {
                listState.animateScrollToItem(index + 2)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "时间分布",
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
        if (typeDurations.isEmpty()) {
            // 空状态
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "📊",
                        fontSize = 64.sp,
                        modifier = Modifier.padding(bottom = 16.dp)
                    )
                    Text(
                        text = "暂无统计数据",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "记录一些时间片后就能看到分布啦",
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
                // 总时间概览卡片
                item {
                    TimeOverviewCard(
                        totalDuration = totalDuration,
                        itemCount = timePieces.size,
                        typeCount = typeDurations.size
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
                                text = "时间分布",
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
                                InteractivePieChart(
                                    data = typeDurations,
                                    colors = chartColors,
                                    totalDuration = totalDuration,
                                    progress = animatedProgress,
                                    selectedIndex = selectedTypeIndex,
                                    onSliceClick = { index -> onPieSliceClick(index) }
                                )
                                
                                // 中心显示选中类型信息
                                if (selectedTypeIndex >= 0 && selectedTypeIndex < typeDurations.size) {
                                    val (type, duration) = typeDurations[selectedTypeIndex]
                                    val percentage = if (totalDuration > 0) 
                                        (duration.toFloat() / totalDuration * 100).toInt() else 0
                                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                        Text(
                                            text = type,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = chartColors[selectedTypeIndex % chartColors.size],
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "$percentage%",
                                            fontSize = 16.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = chartColors[selectedTypeIndex % chartColors.size]
                                        )
                                    }
                                }
                            }
                            
                            // 图例（点击可选中）
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                typeDurations.forEachIndexed { index, (type, duration) ->
                                    val percentage = if (totalDuration > 0) 
                                        (duration.toDouble() / totalDuration * 100).toInt() else 0
                                    LegendItem(
                                        color = chartColors[index % chartColors.size],
                                        label = type,
                                        duration = duration,
                                        percentage = percentage,
                                        isSelected = selectedTypeIndex == index,
                                        onClick = { onPieSliceClick(index) }
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 详细列表
                item {
                    Text(
                        text = "类型详情",
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp, horizontal = 4.dp)
                    )
                }
                
                // 类型卡片列表
                items(typeDurations) { (type, duration) ->
                    val percentage = if (totalDuration > 0) 
                        duration.toFloat() / totalDuration else 0f
                    val colorIndex = typeDurations.indexOf(Pair(type, duration))
                    val isSelected = selectedTypeIndex == colorIndex
                    
                    ExpandableTypeCard(
                        type = type,
                        duration = duration,
                        percentage = percentage,
                        color = chartColors[colorIndex % chartColors.size],
                        timePieces = timePiecesByType[type] ?: emptyList(),
                        isExpanded = expandedStates[colorIndex],
                        isSelected = isSelected,
                        onToggleExpand = { expandedStates[colorIndex] = !expandedStates[colorIndex] },
                        onViewStats = {
                            // 跳转到统计详情页面
                            val intent = Intent(context, ShowEventFeelingActivity::class.java)
                            intent.putExtra("mainEvent", type)
                            context.startActivity(intent)
                        },
                        animationDelay = colorIndex * 100
                    )
                }
            }
        }
    }
}

/**
 * 可交互的饼图组件
 */
@Composable
private fun InteractivePieChart(
    data: List<Pair<String, Long>>,
    colors: List<Color>,
    totalDuration: Long,
    progress: Float,
    selectedIndex: Int,
    onSliceClick: (Int) -> Unit
) {
    // 记录每个扇形的角度范围
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
                    // 计算点击位置对应的角度
                    val center = Offset(size.width / 2f, size.height / 2f)
                    val dx = offset.x - center.x
                    val dy = offset.y - center.y
                    
                    // 检查是否在圆环内（非中心区域）
                    val distance = sqrt(dx * dx + dy * dy)
                    val minDim = min(size.width, size.height)
                    val outerRadius = minDim / 2f
                    val innerRadius = outerRadius / 4f
                    
                    if (distance >= innerRadius && distance <= outerRadius) {
                        // 计算角度（从顶部开始，顺时针）
                        var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                        angle = (angle + 90 + 360) % 360 // 转换为从顶部开始的角度
                        
                        // 找到对应的扇形
                        sliceAngles.forEachIndexed { index, (start, end) ->
                            val normalizedStart = (start + 90 + 360) % 360
                            val normalizedEnd = (end + 90 + 360) % 360
                            
                            val isInSlice = if (normalizedStart <= normalizedEnd) {
                                angle in normalizedStart..normalizedEnd
                            } else {
                                angle >= normalizedStart || angle <= normalizedEnd
                            }
                            
                            // 简化判断：直接使用原始角度
                            val simpleStart = start + 90
                            val simpleEnd = end + 90
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
        val minDim = min(size.width, size.height)
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            
            val animatedSweep = sweep * progress
            val color = colors[index % colors.size]
            val isSelected = selectedIndex == index
            
            // 选中的扇形稍微扩大
            val radius = if (isSelected) minDim / 2f + 8.dp.toPx() else minDim / 2f
            
            // 绘制扇形
            drawArc(
                color = if (isSelected) color.copy(alpha = 1f) else color,
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                size = Size(radius, radius),
                topLeft = Offset(center.x - radius / 2f, center.y - radius / 2f)
            )
            
            startAngle += animatedSweep
        }
        
        // 中心空白圆（甜甜圈效果）
        drawCircle(
            color = Color.White,
            radius = minDim / 4f,
            center = center
        )
    }
}

@Composable
private fun TimeOverviewCard(
    totalDuration: Long,
    itemCount: Int,
    typeCount: Int
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
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            OverviewStat(
                value = formatDurationShort(totalDuration),
                label = "总时长"
            )
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
            
            OverviewStat(
                value = "$itemCount",
                label = "时间片"
            )
            
            Box(
                modifier = Modifier
                    .height(40.dp)
                    .width(1.dp)
                    .background(MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.2f))
            )
            
            OverviewStat(
                value = "$typeCount",
                label = "类型数"
            )
        }
    }
}

@Composable
private fun OverviewStat(
    value: String,
    label: String
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun ModernPieChart(
    data: List<Pair<String, Long>>,
    colors: List<Color>,
    totalDuration: Long,
    progress: Float
) {
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            
            val animatedSweep = sweep * progress
            val color = colors[index % colors.size]
            
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
private fun LegendItem(
    color: Color,
    label: String,
    duration: Long,
    percentage: Int,
    isSelected: Boolean = false,
    onClick: () -> Unit = {}
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .background(
                if (isSelected) color.copy(alpha = 0.2f) else Color.Transparent
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(10.dp)
                .background(color, CircleShape)
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = label,
            fontSize = 12.sp,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        if (percentage > 0) {
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = "$percentage%",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = color
            )
        }
    }
}

/**
 * 可展开的类型卡片
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExpandableTypeCard(
    type: String,
    duration: Long,
    percentage: Float,
    color: Color,
    timePieces: List<TimePiece>,
    isExpanded: Boolean,
    isSelected: Boolean = false,
    onToggleExpand: () -> Unit,
    onViewStats: () -> Unit,
    animationDelay: Int
) {
    var isVisible by remember { mutableStateOf(false) }
    
    LaunchedEffect(Unit) {
        kotlinx.coroutines.delay(animationDelay.toLong())
        isVisible = true
    }
    
    val animatedProgress by animateFloatAsState(
        targetValue = if (isVisible) 1f else 0f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "cardAnimation"
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(
                elevation = if (isVisible) 2.dp else 0.dp,
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
                // 颜色标识
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .background(color, CircleShape)
                )
                
                Spacer(modifier = Modifier.width(16.dp))
                
                // 类型名称
                Text(
                    text = type,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.weight(1f)
                )
                
                // 进度条
                Box(
                    modifier = Modifier
                        .weight(2f)
                        .height(8.dp)
                        .background(
                            MaterialTheme.colorScheme.surfaceVariant,
                            RoundedCornerShape(4.dp)
                        )
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(animatedProgress * percentage)
                            .background(color, RoundedCornerShape(4.dp))
                    )
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
                Divider(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                    thickness = 1.dp
                )
                
                Column(
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    timePieces.forEach { piece ->
                        TimePieceItem(
                            timePiece = piece,
                            color = color,
                            onClick = { /* 可以添加点击查看详情逻辑 */ }
                        )
                    }
                    
                    // 查看统计按钮
                    Spacer(modifier = Modifier.height(4.dp))
                    OutlinedButton(
                        onClick = onViewStats,
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.KeyboardArrowRight,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("查看「$type」完整统计")
                    }
                }
            }
        }
    }
}

/**
 * 时间片项
 */
@Composable
private fun TimePieceItem(
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
                    text = if (timePiece.subEvent.isNotEmpty()) timePiece.subEvent else timePiece.mainEvent,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    maxLines = 2
                )
            }
            
            // 心情评分
            Row {
                repeat(5) { index ->
                    Text(
                        text = if (index < timePiece.emotion) "⭐" else "☆",
                        fontSize = 10.sp,
                        color = if (index < timePiece.emotion) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.3f)
                    )
                }
            }
        }
    }
}

// 辅助函数
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
