/**
 * Simple Modern Card - 简化版现代化卡片（支持展开/折叠）
 * 支持深色模式
 */
package com.example.time.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.logic.utils.convertTimeFormatSmart
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.ui.theme.ModernColors
import kotlin.math.roundToInt
import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.foundation.gestures.detectTapGestures

@Composable
fun SimpleModernCard(
    timePiece: TimePiece,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    var showImagePreview by remember { mutableStateOf<Pair<Boolean, Int>>(false to 0) }
    val emotionColor = ModernColors.getEmotionColor(timePiece.emotion)
    
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 情绪色块
            Surface(
                modifier = Modifier.size(4.dp, 80.dp),
                shape = RoundedCornerShape(2.dp),
                color = emotionColor
            ) {}
            
            Spacer(modifier = Modifier.width(16.dp))
            
            // 主内容
            Column(modifier = Modifier.weight(1f)) {
                // 时间和事件名
                Text(
                    text = timePiece.mainEvent + if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = if (expanded) 100 else 2,
                    overflow = if (expanded) TextOverflow.Visible else TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 时间范围
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${convertTimeFormatSmart(timePiece.fromTimePoint, "M/d HH:mm")} - ${convertTimeFormatSmart(timePiece.timePoint, "M/d HH:mm")}",
                        fontSize = 14.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    
                    Spacer(modifier = Modifier.width(12.dp))
                    
                    // 星星评分
                    Row {
                        repeat(5) { index ->
                            Icon(
                                imageVector = Icons.Filled.Star,
                                contentDescription = null,
                                tint = if (index < timePiece.emotion) emotionColor else MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
                
                // 展开/折叠内容
                AnimatedVisibility(
                    visible = expanded,
                    enter = expandVertically(),
                    exit = shrinkVertically()
                ) {
                    Column {
                        // 时长
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "⏱ 时长：${convertDurationFormat(timePiece.timePoint - timePiece.fromTimePoint, "%d时%d分")}",
                            fontSize = 14.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        
                        // 备注（完整显示）
                        if (timePiece.lastTimeRecord.isNotEmpty()) {
                            Spacer(modifier = Modifier.height(6.dp))
                            Text(
                                text = "💭 ${timePiece.lastTimeRecord}",
                                fontSize = 13.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        
                        // 媒体附件（图片）
                        if (timePiece.hasMedia()) {
                            Spacer(modifier = Modifier.height(8.dp))
                            LazyRow(
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                items(timePiece.getMediaList().size) { index ->
                                    val path = timePiece.getMediaList()[index]
                                    val bitmap = remember(path) {
                                        try {
                                            BitmapFactory.decodeFile(path)
                                        } catch (e: Exception) {
                                            null
                                        }
                                    }
                                    bitmap?.let {
                                        Image(
                                            bitmap = it.asImageBitmap(),
                                            contentDescription = "媒体附件",
                                            modifier = Modifier
                                                .size(80.dp)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable {
                                                    showImagePreview = true to index
                                                },
                                            contentScale = ContentScale.Crop
                                        )
                                    } ?: Box(
                                        modifier = Modifier
                                            .size(80.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(MaterialTheme.colorScheme.surfaceVariant)
                                            .clickable {
                                                showImagePreview = true to index
                                            },
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Text("📷")
                                    }
                                }
                            }
                        }
                    }
                }
                
                // 备注（折叠时只显示一行）
                if (!expanded && timePiece.lastTimeRecord.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💭 ${timePiece.lastTimeRecord}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
                
                // 媒体数量提示（折叠时）
                if (!expanded && timePiece.hasMedia()) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "📷 ${timePiece.getMediaCount()}张",
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
            
            // 展开/折叠按钮
            IconButton(
                onClick = { expanded = !expanded },
                modifier = Modifier.size(40.dp)
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expanded) "收起" else "展开",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    }
    
    // 图片预览对话框
    if (showImagePreview.first && timePiece.hasMedia()) {
        ImageViewerDialog(
            imagePaths = timePiece.getMediaList(),
            initialIndex = showImagePreview.second,
            onDismiss = { showImagePreview = false to 0 }
        )
    }
}