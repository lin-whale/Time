/**
 * Modern Statistics - 现代化统计组件
 * 
 * 包含：
 * - 今日总览卡片
 * - 情绪分布图
 * - 时间分布条形图
 * - 事件标签云
 */
package com.example.time.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.ui.theme.ModernColors
import com.example.time.ui.theme.ModernSizes
import kotlin.math.roundToInt

/**
 * 今日总览卡片
 */
@Composable
fun TodayOverviewCard(
    totalMinutes: Int,
    eventCount: Int,
    averageEmotion: Float,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ModernSizes.ElevationMedium.dp,
                shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
                spotColor = ModernColors.Primary.copy(alpha = 0.1f)
            ),
        shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 渐变背景装饰
            Canvas(modifier = Modifier.fillMaxSize()) {
                val path = Path().apply {
                    moveTo(0f, size.height * 0.3f)
                    cubicTo(
                        size.width * 0.3f, size.height * 0.1f,
                        size.width * 0.7f, size.height * 0.5f,
                        size.width, size.height * 0.3f
                    )
                    lineTo(size.width, size.height)
                    lineTo(0f, size.height)
                    close()
                }
                
                drawPath(
                    path = path,
                    brush = Brush.verticalGradient(
                        colors = listOf(
                            ModernColors.Primary.copy(alpha = 0.05f),
                            ModernColors.Primary.copy(alpha = 0.02f)
                        )
                    )
                )
            }
            
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                // 标题
                Text(
                    text = "📊 今日总览",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = ModernColors.TextPrimary
                )
                
                Spacer(modifier = Modifier.height(20.dp))
                
                // 统计数据
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    StatItem(
                        label = "总时长",
                        value = "${totalMinutes / 60}h ${totalMinutes % 60}m",
                        icon = Icons.Filled.AccessTime,
                        color = ModernColors.Info
                    )
                    
                    StatItem(
                        label = "事件数",
                        value = "$eventCount",
                        icon = Icons.Filled.CalendarMonth,
                        color = ModernColors.Success
                    )
                    
                    StatItem(
                        label = "平均心情",
                        value = String.format("%.1f", averageEmotion),
                        icon = Icons.Default.FavoriteBorder,
                        color = ModernColors.getEmotionColor(averageEmotion.roundToInt())
                    )
                }
            }
        }
    }
}

@Composable
private fun StatItem(
    label: String,
    value: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // 图标背景
        Surface(
            shape = CircleShape,
            color = color.copy(alpha = 0.15f),
            modifier = Modifier.size(56.dp)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Icon(
                    imageVector = icon,
                    contentDescription = label,
                    tint = color,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = ModernColors.TextPrimary
        )
        
        Text(
            text = label,
            fontSize = 12.sp,
            color = ModernColors.TextSecondary
        )
    }
}

/**
 * 情绪分布饼图
 */
@Composable
fun EmotionDistributionChart(
    data: Map<Int, Int>,  // emotion -> count
    modifier: Modifier = Modifier
) {
    val total = data.values.sum()
    if (total == 0) return
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ModernSizes.ElevationMedium.dp,
                shape = RoundedCornerShape(ModernSizes.CornerLarge.dp)
            ),
        shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "😊 情绪分布",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // 情绪列表
            data.toList()
                .sortedByDescending { it.second }
                .forEach { (emotion, count) ->
                    EmotionDistributionItem(
                        emotion = emotion,
                        count = count,
                        percentage = (count.toFloat() / total * 100).roundToInt()
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                }
        }
    }
}

@Composable
private fun EmotionDistributionItem(
    emotion: Int,
    count: Int,
    percentage: Int
) {
    val color = ModernColors.getEmotionColor(emotion)
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage.toFloat() / 100f,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "percentage"
    )
    
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 星星评分
        repeat(5) { index ->
            Icon(
                imageVector = Icons.Filled.Star,
                contentDescription = null,
                tint = if (index < emotion) color else ModernColors.TextTertiary.copy(alpha = 0.2f),
                modifier = Modifier.size(16.dp)
            )
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 进度条
        Column(modifier = Modifier.weight(1f)) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp))
                    .background(color.copy(alpha = 0.2f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedPercentage)
                        .height(8.dp)
                        .clip(RoundedCornerShape(4.dp))
                        .background(color)
                )
            }
        }
        
        Spacer(modifier = Modifier.width(12.dp))
        
        // 百分比
        Text(
            text = "$percentage%",
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
            modifier = Modifier.width(45.dp)
        )
        
        // 次数
        Text(
            text = "${count}次",
            fontSize = 12.sp,
            color = ModernColors.TextSecondary,
            modifier = Modifier.width(40.dp)
        )
    }
}

/**
 * 事件时长排行
 */
@Composable
fun EventDurationRanking(
    events: List<Pair<String, Int>>,  // event name -> duration in minutes
    modifier: Modifier = Modifier
) {
    if (events.isEmpty()) return
    
    val maxDuration = events.maxOf { it.second }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ModernSizes.ElevationMedium.dp,
                shape = RoundedCornerShape(ModernSizes.CornerLarge.dp)
            ),
        shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White)
    ) {
        Column(
            modifier = Modifier.padding(24.dp)
        ) {
            Text(
                text = "⏱️ 事件时长排行",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = ModernColors.TextPrimary
            )
            
            Spacer(modifier = Modifier.height(20.dp))
            
            events.take(5).forEachIndexed { index, (event, duration) ->
                EventRankingItem(
                    rank = index + 1,
                    eventName = event,
                    duration = duration,
                    maxDuration = maxDuration
                )
                
                if (index < events.size - 1 && index < 4) {
                    Spacer(modifier = Modifier.height(16.dp))
                }
            }
        }
    }
}

@Composable
private fun EventRankingItem(
    rank: Int,
    eventName: String,
    duration: Int,  // minutes
    maxDuration: Int
) {
    val percentage = duration.toFloat() / maxDuration
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = tween(1000, easing = FastOutSlowInEasing),
        label = "percentage"
    )
    
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700)  // 金色
        2 -> Color(0xFFC0C0C0)  // 银色
        3 -> Color(0xFFCD7F32)  // 铜色
        else -> ModernColors.TextSecondary
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排名徽章
            Surface(
                shape = CircleShape,
                color = rankColor.copy(alpha = 0.2f),
                modifier = Modifier.size(32.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = "$rank",
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = rankColor
                    )
                }
            }
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 事件名称
            Text(
                text = eventName,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
                color = ModernColors.TextPrimary,
                modifier = Modifier.weight(1f)
            )
            
            // 时长
            Text(
                text = "${duration / 60}h ${duration % 60}m",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = ModernColors.Primary
            )
        }
        
        Spacer(modifier = Modifier.height(8.dp))
        
        // 进度条
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(6.dp)
                .clip(RoundedCornerShape(3.dp))
                .background(ModernColors.SurfaceVariant)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth(animatedPercentage)
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(
                        Brush.horizontalGradient(
                            colors = listOf(
                                ModernColors.Primary,
                                ModernColors.PrimaryLight
                            )
                        )
                    )
            )
        }
    }
}
