/**
 * Modern TimePiece Card - 现代化时间记录卡片
 * 支持深色模式
 * 
 * 设计特点：
 * - 大圆角（24dp）
 * - 渐变色背景（根据情绪评分）
 * - 毛玻璃效果（半透明）
 * - 流畅的动画
 * - 清晰的信息层次
 * - 支持显示媒体附件
 */
package com.example.time.ui.components

import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.logic.utils.convertTimeFormatSmart
import com.example.time.ui.theme.ModernColors
import com.example.time.ui.theme.ModernSizes
import kotlin.math.roundToInt

@Composable
fun ModernTimePieceCard(
    timePiece: TimePiece,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
    
    // 获取媒体列表
    val mediaList = timePiece.getMediaList()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .shadow(
                elevation = ModernSizes.ElevationMedium.dp,
                shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
                spotColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
            )
            .then(
                if (onClick != null) {
                    Modifier.clickable { onClick() }
                } else {
                    Modifier
                }
            ),
        shape = RoundedCornerShape(ModernSizes.CornerLarge.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
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
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        
                        // 子事件（如果有）
                        if (timePiece.subEvent.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = timePiece.subEvent,
                                fontSize = 14.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 时长标签
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                    ) {
                        Text(
                            text = durationText,
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.primary,
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
                            tint = MaterialTheme.colorScheme.outline,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "${convertTimeFormatSmart(timePiece.fromTimePoint, "M/d HH:mm")} - ${convertTimeFormatSmart(timePiece.timePoint, "M/d HH:mm")}",
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
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
                                    MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
                
                // 媒体附件显示
                if (mediaList.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    LazyRow(
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(mediaList.take(4)) { path ->
                            MediaThumbnail(path = path)
                        }
                        
                        // 如果超过4张，显示数量提示
                        if (mediaList.size > 4) {
                            item {
                                Box(
                                    modifier = Modifier
                                        .size(60.dp)
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(MaterialTheme.colorScheme.surfaceVariant),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "+${mediaList.size - 4}",
                                        fontSize = 14.sp,
                                        fontWeight = FontWeight.Medium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
                
                // 底部：体验记录（如果有）
                if (timePiece.lastTimeRecord.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Divider(
                        color = MaterialTheme.colorScheme.outlineVariant,
                        thickness = 1.dp
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    Text(
                        text = timePiece.lastTimeRecord,
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis,
                        lineHeight = 18.sp
                    )
                }
            }
        }
    }
}

/**
 * 媒体缩略图组件
 */
@Composable
private fun MediaThumbnail(
    path: String,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val bitmap = remember(path) {
        try {
            if (path.startsWith("/")) {
                BitmapFactory.decodeFile(path)
            } else {
                val inputStream = context.contentResolver.openInputStream(Uri.parse(path))
                BitmapFactory.decodeStream(inputStream).also {
                    inputStream?.close()
                }
            }
        } catch (e: Exception) {
            null
        }
    }
    
    Box(
        modifier = modifier
            .size(60.dp)
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
    ) {
        bitmap?.let {
            Image(
                bitmap = it.asImageBitmap(),
                contentDescription = "附件图片",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } ?: Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text("📷", fontSize = 20.sp)
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
    val mediaList = timePiece.getMediaList()
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(enabled = onClick != null) { onClick?.invoke() },
        shape = RoundedCornerShape(ModernSizes.CornerMedium.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
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
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(2.dp))
                
                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "${convertTimeFormatSmart(timePiece.fromTimePoint, "M/d HH:mm")} - ${convertTimeFormatSmart(timePiece.timePoint, "M/d HH:mm")}",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    // 媒体数量提示
                    if (mediaList.isNotEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "📷${mediaList.size}",
                            fontSize = 12.sp,
                            color = emotionColor
                        )
                    }
                }
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