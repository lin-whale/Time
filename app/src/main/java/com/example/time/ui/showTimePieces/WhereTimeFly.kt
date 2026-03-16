package com.example.time.ui.showTimePieces

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowRight
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.R
import com.example.time.data.TimePiece
import com.example.time.data.TimePieceType
import com.example.time.ui.theme.ChartColors
import com.example.time.viewmodel.TimePieceViewModel
import java.util.Date
import java.util.Calendar
import kotlin.math.min
import kotlin.math.max
import kotlin.math.abs
import kotlin.math.pow

/**
 * 时间分布统计界面 - 现代化设计版本
 * 使用优雅的卡片布局和微动画
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WhereTimeFly(
    timePieces: List<TimePiece>,
    onTypeClick: (TimePieceType) -> Unit,
    onBackPressed: () -> Unit
) {
    val context = LocalContext.current
    
    // 计算各类型的时间分布
    val typeDurations = remember(timePieces) {
        timePieces
            .filter { it.endTime != null && it.type != TimePieceType.UNKNOWN }
            .groupBy { it.type }
            .mapValues { (_, pieces) ->
                pieces.sumOf { piece ->
                    (piece.endTime?.time ?: 0L) - piece.startTime.time
                }
            }
            .filter { it.value > 0 }
            .toList()
            .sortedByDescending { it.second }
    }
    
    val totalDuration = typeDurations.sumOf { it.second }
    
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
                    containerColor = Color.Transparent,
                    elevation = 0.dp
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp, bottom = 80.dp)
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
                        )
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
                            
                            // 饼图
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ModernPieChart(
                                    data = typeDurations,
                                    colors = chartColors,
                                    totalDuration = totalDuration,
                                    progress = animatedProgress
                                )
                            }
                            
                            // 图例
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                typeDurations.forEachIndexed { index, (type, duration) ->
                                    val percentage = if (totalDuration > 0) 
                                        (duration.toFloat() / totalDuration * 100).toInt() else 0
                                    LegendItem(
                                        color = chartColors[index % chartColors.size],
                                        label = type.name ?: context.getString(type.nameResId),
                                        duration = duration,
                                        percentage = percentage
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
                    
                    TypeDurationCard(
                        type = type,
                        duration = duration,
                        percentage = percentage,
                        color = chartColors[colorIndex % chartColors.size],
                        onClick = { onTypeClick(type) },
                        animationDelay = colorIndex * 100
                    )
                }
            }
        }
    }
}

@Composable
private fun TimeOverviewCard(
    totalDuration: Long,
    itemCount: Int,
    typeCount: Int
) {
    val context = LocalContext.current
    
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
    data: List<Pair<TimePieceType, Long>>,
    colors: List<Color>,
    totalDuration: Long,
    progress: Float
) {
    var hoverIndex by remember { mutableStateOf(-1) }
    
    Canvas(
        modifier = Modifier.fillMaxSize()
    ) {
        var startAngle = -90f
        
        data.forEachIndexed { index, (_, duration) ->
            val sweep = if (totalDuration > 0) 
                (duration.toFloat() / totalDuration * 360f) else 0f
            
            // 应用动画进度
            val animatedSweep = sweep * progress
            
            val color = colors[index % colors.size]
            val isHovered = hoverIndex == index
            
            // 绘制扇形
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = animatedSweep,
                useCenter = true,
                size = size
            )
            
            startAngle += animatedSweep
        }
        
        // 中心空白圆（甜甜圈效果）
        drawCircle(
            color = android.graphics.Color.WHITE,
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
    percentage: Int
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(end = 8.dp)
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun TypeDurationCard(
    type: TimePieceType,
    duration: Long,
    percentage: Float,
    color: Color,
    onClick: () -> Unit,
    animationDelay: Int
) {
    val context = LocalContext.current
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
            .clickable(onClick = onClick)
            .shadow(
                elevation = if (isVisible) 2.dp else 0.dp,
                shape = RoundedCornerShape(16.dp)
            ),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
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
                text = type.name ?: context.getString(type.nameResId),
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
            
            // 时长和箭头
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
            
            Icon(
                imageVector = Icons.Default.KeyboardArrowRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.5f),
                modifier = Modifier.size(20.dp)
            )
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
