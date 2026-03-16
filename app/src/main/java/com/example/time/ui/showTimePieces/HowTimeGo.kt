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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.lerp
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.platform.LocalContext
import com.example.time.logic.model.TimePiece
import com.example.time.ui.theme.EmotionColors
import java.util.*
import kotlin.math.min

/**
 * 心情分布统计界面 - 现代化设计版本
 * 展示不同心情等级的时间分布
 */
@OptIn(ExperimentalMaterial3Api::class)
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
            .filter { it.endTime != null && it.emotion > 0 }
            .groupBy { it.emotion }
            .mapValues { (_, pieces) ->
                pieces.sumOf { piece ->
                    (piece.endTime?.time ?: 0L) - piece.startTime.time
                }
            }
            .filter { it.value > 0 }
            .toList()
            .sortedByDescending { it.second }
    }

    val totalDuration = feelingDurations.sumOf { it.second }
    
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
                    containerColor = Color.Transparent,
                    elevation = 0.dp
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                contentPadding = PaddingValues(vertical = 8.dp, bottom = 80.dp)
            ) {
                // 心情概览卡片
                item {
                    FeelingOverviewCard(
                        totalDuration = totalDuration,
                        itemCount = timePieces.count { it.emotion > 0 },
                        avgFeeling = if (feelingDurations.isNotEmpty()) {
                            val totalFeelingScore = timePieces
                                .filter { it.emotion > 0 && it.endTime != null }
                                .sumOf { it.emotion.toLong() * ((it.endTime?.time ?: 0L) - it.startTime.time) }
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
                        )
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
                            
                            // 饼图
                            Box(
                                modifier = Modifier
                                    .size(240.dp)
                                    .padding(16.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                ModernFeelingPieChart(
                                    data = feelingDurations,
                                    colors = feelingColors,
                                    totalDuration = totalDuration,
                                    progress = animatedProgress
                                )
                                
                                // 中心显示平均心情
                                val avgFeeling = if (feelingDurations.isNotEmpty()) {
                                    val totalFeelingScore = timePieces
                                        .filter { it.emotion > 0 && it.endTime != null }
                                        .sumOf { it.emotion.toLong() * ((it.endTime?.time ?: 0L) - it.startTime.time) }
                                    (totalFeelingScore.toFloat() / totalDuration).coerceIn(1f, 5f)
                                } else 3f
                                
                                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                    Text(
                                        text = feelingEmojis[(avgFeeling - 1).toInt().coerceIn(0, 4)],
                                        fontSize = 32.sp
                                    )
                                    Text(
                                        text = String.format("%.1f", avgFeeling),
                                        fontSize = 16.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = feelingColors[(avgFeeling - 1).toInt().coerceIn(0, 4)]
                                    )
                                }
                            }
                            
                            // 心情图例（修复使用FlowRow）
                            FlowRow(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(top = 16.dp),
                                horizontalArrangement = Arrangement.spacedBy(8.dp),
                                verticalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                feelingDurations.forEach { (feeling, duration) ->
                                    val percentage = if (totalDuration > 0) 
                                        (duration.toFloat() / totalDuration * 100).toInt() else 0
                                    FeelingLegendItem(
                                        emoji = feelingEmojis[feeling - 1],
                                        feeling = feeling,
                                        duration = duration,
                                        percentage = percentage,
                                        color = feelingColors[feeling - 1]
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
                    
                    FeelingDurationCard(
                        feeling = feeling,
                        emoji = feelingEmojis[feeling - 1],
                        label = feelingLabels[feeling - 1],
                        duration = duration,
                        percentage = percentage,
                        color = feelingColors[feeling - 1],
                        onClick = { onFeelingClick(feeling) }
                    )
                }
            }
        }
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
private fun FeelingLegendItem(
    emoji: String,
    feeling: Int,
    duration: Long,
    percentage: Int,
    color: Color
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .background(
                color.copy(alpha = 0.1f),
                RoundedCornerShape(8.dp)
            )
            .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
        Text(text = emoji, fontSize = 14.sp)
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = "$percentage%",
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
            color = color
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun FeelingDurationCard(
    feeling: Int,
    emoji: String,
    label: String,
    duration: Long,
    percentage: Float,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .shadow(
                elevation = 2.dp,
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
