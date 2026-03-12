/**
 * Modern TimePiece Card - 现代化时间记录卡片
 * 
 * 设计特点：
 * - 大圆角（24dp）
 * - 渐变色背景（根据情绪评分）
 * - 毛玻璃效果（半透明）
 * - 流畅的动画
 * - 清晰的信息层次
 */
package com.example.time.ui.components

import androidx.compose.animation.core.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.theme.ModernColors
import com.example.time.ui.theme.ModernSizes
import kotlin.math.roundToInt

@Composable
fun ModernTimePieceCard(
    timePiece: TimePiece,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // 动画状态
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )
    
    // 根据情绪评分选择渐变色
    val gradientBrush = when (timePiece.emotion) {
        5 -> ModernColors.GradientPurple
        4 -> ModernColors.GradientGreen
        3 -> ModernColors.GradientBlue
        2 -> ModernColors.GradientOrange
        else -> ModernColors.GradientPink
    }
    
    // 计算时长
    val durationMinutes = ((timePiece.timePoint - timePiece.fromTimePoint) / (1000 * 60)).toInt()
    val hours = durationMinutes / 60
    val minutes = durationMinutes % 60
    val durationText = when {
        hours > 0 && minutes > 0 -> "${hours}h ${minutes}m"
        hours > 0 -> "${hours}h"
        else -> "${minutes}m"
    }
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ModernSizes.ElevationMedium.dp,
                shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
                spotColor = ModernColors.Primary.copy(alpha = 0.1f)
            )
            .clickable(enabled = onClick != null) {
                isPressed = true
                onClick?.invoke()
            },
        shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color.White
        )
    ) {
        Box(
            modifier = Modifier.fillMaxWidth()
        ) {
            // 渐变色背景条（左侧装饰）
            Box(
                modifier = Modifier
                    .width(6.dp)
                    .fillMaxHeight()
                    .background(gradientBrush)
            )
            
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(start = 18.dp, end = 16.dp, top = 16.dp, bottom = 16.dp)
            ) {
                // 顶部：事件名称 + 时长标签
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        // 主事件
                        Text(
                            text = timePiece.mainEvent,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold,
                            color = ModernColors.TextPrimary,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 子事件（如果有）
                        if (timePiece.subEvent.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = timePiece.subEvent,
                                fontSize = 14.sp,
                                color = ModernColors.TextSecondary,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 时长标签
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = ModernColors.getEmotionColor(timePiece.emotion).copy(alpha = 0.15f)
                    ) {
                        Text(
                            text = durationText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = ModernColors.getEmotionColor(timePiece.emotion),
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))
                
                // 中部：时间范围
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // 时间范围
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = null,
                            tint = ModernColors.TextTertiary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${convertTimeFormat(timePiece.fromTimePoint, "HH:mm")} - ${convertTimeFormat(timePiece.timePoint, "HH:mm")}",
                            fontSize = 13.sp,
                            color = ModernColors.TextSecondary
                        )
                    }
                    
                    // 情绪评分（星星）
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                    ) {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < timePiece.emotion)
                                    ModernColors.getEmotionColor(timePiece.emotion)
                                else
                                    ModernColors.TextTertiary.copy(alpha = 0.2f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // 底部：体验记录（如果有）
                if (timePiece.lastTimeRecord.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Divider(
                        color = ModernColors.SurfaceVariant,
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = timePiece.lastTimeRecord,
                        fontSize = 13.sp,
                        color = ModernColors.TextSecondary,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
    
    LaunchedEffect(isPressed) {
        if (isPressed) {
            kotlinx.coroutines.delay(100)
            isPressed = false
        }
    }
}

/**
 * 紧凑版卡片（用于列表密集展示）
 */
@Composable
fun CompactTimePieceCard(
    timePiece: TimePiece,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val emotionColor = ModernColors.getEmotionColor(timePiece.emotion)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
        colors = CardDefaults.cardColors(
            containerColor = emotionColor.copy(alpha = 0.08f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧颜色指示器
            Box(
                modifier = Modifier
                    .size(4.dp, 32.dp)
                    .background(emotionColor, RoundedCornerShape(2.dp))
            )
            
            Spacer(modifier = Modifier.width(12.dp))
            
            // 内容
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = timePiece.mainEvent,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    color = ModernColors.TextPrimary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Text(
                    text = "${convertTimeFormat(timePiece.fromTimePoint, "HH:mm")} - ${convertTimeFormat(timePiece.timePoint, "HH:mm")}",
                    fontSize = 12.sp,
                    color = ModernColors.TextSecondary
                )
            }
            
            // 情绪指示器
            Surface(
                shape = CircleShape,
                color = emotionColor.copy(alpha = 0.2f)
            ) {
                Text(
                    text = timePiece.emotion.toString(),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = emotionColor,
                    modifier = Modifier.padding(8.dp)
                )
            }
        }
    }
}
