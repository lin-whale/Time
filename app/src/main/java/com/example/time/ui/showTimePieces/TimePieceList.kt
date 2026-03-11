/**
 * TimePieceList - 时间片段列表组件
 * 
 * 改动说明：
 * - 美化卡片样式，增加圆角和阴影
 * - 优化信息布局，更易读
 * - 支持点击编辑功能（需配合 ViewModel）
 * - 使用新的情绪颜色方案
 * 
 * 开发原则：
 * - 所有数据仅在本地显示和处理
 */
package com.example.time.ui.showTimePieces

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.theme.EmotionColors
import com.example.time.ui.timeRecord.TimePieceEditDialog

/**
 * 时间片段列表（Column 布局，用于非滚动容器）
 */
@Composable
fun TimePieceListColumn(timePieces: List<TimePiece>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (timePiece in timePieces) {
            TimePieceCard(timePiece = timePiece)
        }
    }
}

/**
 * 时间片段列表（LazyColumn 布局，用于滚动显示）
 * 
 * @param timePieces 时间片段列表
 * @param onEdit 编辑回调（可选）
 * @param onDelete 删除回调（可选）
 */
@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    onEdit: ((TimePiece) -> Unit)? = null,
    onDelete: ((TimePiece) -> Unit)? = null
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(timePieces) { timePiece ->
            TimePieceCard(
                timePiece = timePiece,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = if (onEdit != null) { { onEdit(timePiece) } } else null
            )
        }
    }
}

/**
 * 可编辑的时间片段列表
 * 支持点击编辑和删除功能
 */
@Composable
fun TimePieceListEditable(
    timePieces: List<TimePiece>,
    onUpdate: (TimePiece) -> Unit,
    onDelete: (TimePiece) -> Unit,
    onInsertBefore: ((splitTime: Long, originalPiece: TimePiece) -> Unit)? = null
) {
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(timePieces) { timePiece ->
            TimePieceCard(
                timePiece = timePiece,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = { editingPiece = timePiece }
            )
        }
    }
    
    // 编辑对话框
    editingPiece?.let { piece ->
        // 计算时间限制
        val pieceIndex = timePieces.indexOf(piece)
        val minTime = if (pieceIndex > 0) timePieces[pieceIndex - 1].timePoint else 0L
        val maxTime = if (pieceIndex < timePieces.size - 1) 
            timePieces[pieceIndex + 1].fromTimePoint 
        else System.currentTimeMillis()
        
        TimePieceEditDialog(
            timePiece = piece,
            onSave = { updatedPiece ->
                onUpdate(updatedPiece)
                editingPiece = null
            },
            onDelete = { deletedPiece ->
                onDelete(deletedPiece)
                editingPiece = null
            },
            onInsertBefore = if (onInsertBefore != null) {
                { splitTime, originalPiece ->
                    onInsertBefore(splitTime, originalPiece)
                    editingPiece = null
                }
            } else null,
            onCancel = { editingPiece = null },
            minTime = minTime,
            maxTime = maxTime
        )
    }
}

/**
 * 时间片段卡片组件
 * 
 * @param timePiece 时间片段数据
 * @param modifier 修饰符
 * @param onClick 点击回调（用于编辑）
 */
@Composable
fun TimePieceCard(
    timePiece: TimePiece,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(
                if (onClick != null) Modifier.clickable(onClick = onClick)
                else Modifier
            ),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            // 第一行：时间信息
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 结束时间
                Text(
                    text = convertTimeFormat(timePiece.timePoint, "M/d HH:mm"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                
                // 时长
                Text(
                    text = convertDurationFormat(
                        timePiece.timePoint - timePiece.fromTimePoint,
                        "%d时%d分"
                    ),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                
                // 情绪星级（使用颜色）
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < timePiece.emotion)
                                EmotionColors.getColorForStar(timePiece.emotion)
                            else Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第二行：事件名称
            Text(
                text = timePiece.mainEvent + 
                       (if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else ""),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            
            // 第三行：体验记录（如果有）
            if (timePiece.lastTimeRecord.isNotEmpty()) {
                Spacer(modifier = Modifier.height(6.dp))
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
