/**
 * Simple Modern Card - 简化版现代化卡片（无动画）
 * 支持深色模式
 */
package com.example.time.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
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
import com.example.time.ui.theme.ModernColors
import kotlin.math.roundToInt

@Composable
fun SimpleModernCard(
    timePiece: TimePiece,
    onClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
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
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                
                Spacer(modifier = Modifier.height(8.dp))
                
                // 时间范围
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "${convertTimeFormat(timePiece.fromTimePoint, "HH:mm")} - ${convertTimeFormat(timePiece.timePoint, "HH:mm")}",
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
                
                // 备注
                if (timePiece.lastTimeRecord.isNotEmpty()) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "💭 ${timePiece.lastTimeRecord}",
                        fontSize = 13.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}
