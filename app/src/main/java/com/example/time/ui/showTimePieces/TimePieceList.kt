/**
 * TimePieceList - 时间片段列表组件
 * 支持点击编辑功能，自动保持时间连续性
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.time.logic.model.TimePiece
import com.example.time.logic.utils.convertDurationFormat
import com.example.time.logic.utils.convertTimeFormat
import com.example.time.ui.TimeViewModel
import com.example.time.ui.timeRecord.TimePieceEditDialog

@Composable
fun TimePieceListColumn(timePieces: List<TimePiece>) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        for (timePiece in timePieces) {
            TimePieceCard(timePiece = timePiece)
        }
    }
}

/**
 * 时间片段列表
 * 编辑保存时自动调整相邻记录，保持时间连续不重叠
 * 
 * 时间线示例（列表是倒序的，最新的在前面）：
 * 索引0 (nextPiece): fromTimePoint=11:00, timePoint=12:00 （时间上最晚/最新）
 * 索引1 (piece):     fromTimePoint=10:00, timePoint=11:00 （当前编辑的记录）
 * 索引2 (prevPiece): fromTimePoint=09:00, timePoint=10:00 （时间上最早）
 * 
 * 连续性要求：
 * - prevPiece.timePoint == piece.fromTimePoint （前一条的结束 = 当前的开始）
 * - piece.timePoint == nextPiece.fromTimePoint （当前的结束 = 后一条的开始）
 */
@Composable
fun TimePieceList(
    timePieces: List<TimePiece>,
    viewModel: TimeViewModel? = null
) {
    var editingPiece by remember { mutableStateOf<TimePiece?>(null) }
    
    LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        items(timePieces) { timePiece ->
            TimePieceCard(
                timePiece = timePiece,
                modifier = Modifier.padding(horizontal = 8.dp),
                onClick = if (viewModel != null) {
                    { editingPiece = timePiece }
                } else null
            )
        }
    }
    
    // 编辑对话框
    if (viewModel != null && editingPiece != null) {
        val piece = editingPiece!!
        val pieceIndex = timePieces.indexOf(piece)
        
        // 列表是倒序的：index小=时间晚，index大=时间早
        // nextPiece: 时间上在当前记录之后（更晚/更新）的记录
        // prevPiece: 时间上在当前记录之前（更早）的记录
        val nextPiece = if (pieceIndex > 0) timePieces[pieceIndex - 1] else null
        val prevPiece = if (pieceIndex < timePieces.size - 1) timePieces[pieceIndex + 1] else null
        
        // 时间范围参数（传给对话框，但实际选择范围在对话框内部计算）
        val minTime = prevPiece?.timePoint ?: 0L
        val maxTime = nextPiece?.fromTimePoint ?: System.currentTimeMillis()
        
        TimePieceEditDialog(
            timePiece = piece,
            onSave = { updated ->
                // 保存时检查是否需要调整相邻记录
                // 
                // 重要：修改开始时间可能影响多条之前的记录，修改结束时间可能影响多条之后的记录
                // 需要遍历所有受影响的记录进行调整或删除
                // 
                // 示例：
                // 记录列表（时间顺序）：
                // A: 08:00 → 09:00
                // B: 09:00 → 09:30
                // C: 09:30 → 10:00
                // D: 10:00 → 11:00  ← 当前编辑的记录
                // E: 11:00 → 12:00
                //
                // 如果把 D 的开始时间从 10:00 改成 08:30：
                // - A 的结束时间改为 08:30（被部分覆盖）
                // - B 被完全覆盖，删除
                // - C 被完全覆盖，删除
                // - D 变成 08:30 → 11:00
                
                // 1. 处理开始时间提前的情况（影响之前的记录）
                // 列表是倒序的，pieceIndex 之后的元素是时间更早的记录
                if (updated.fromTimePoint < piece.fromTimePoint) {
                    // 遍历所有更早的记录
                    for (i in (pieceIndex + 1) until timePieces.size) {
                        val earlierPiece = timePieces[i]
                        
                        if (earlierPiece.timePoint <= updated.fromTimePoint) {
                            // 这条记录在新开始时间之前结束，不受影响，后面的更不会受影响
                            break
                        } else if (earlierPiece.fromTimePoint >= updated.fromTimePoint) {
                            // 这条记录被完全覆盖，删除
                            viewModel.deleteTimePiece(earlierPiece)
                        } else {
                            // 这条记录被部分覆盖，调整其结束时间
                            val adjusted = earlierPiece.copy(timePoint = updated.fromTimePoint)
                            viewModel.updateTimePiece(adjusted)
                            break  // 再往前的记录不会受影响了
                        }
                    }
                }
                
                // 2. 处理结束时间延后的情况（影响之后的记录）
                // 列表是倒序的，pieceIndex 之前的元素是时间更晚的记录
                if (updated.timePoint > piece.timePoint) {
                    // 遍历所有更晚的记录
                    for (i in (pieceIndex - 1) downTo 0) {
                        val laterPiece = timePieces[i]
                        
                        if (laterPiece.fromTimePoint >= updated.timePoint) {
                            // 这条记录在新结束时间之后开始，不受影响，后面的更不会受影响
                            break
                        } else if (laterPiece.timePoint <= updated.timePoint) {
                            // 这条记录被完全覆盖，删除
                            viewModel.deleteTimePiece(laterPiece)
                        } else {
                            // 这条记录被部分覆盖，调整其开始时间
                            val adjusted = laterPiece.copy(fromTimePoint = updated.timePoint)
                            viewModel.updateTimePiece(adjusted)
                            break  // 再往后的记录不会受影响了
                        }
                    }
                }
                
                // 3. 保存当前记录
                viewModel.updateTimePiece(updated)
                editingPiece = null
            },
            onDelete = { deleted ->
                // 删除时，让前一条记录的结束时间延长到后一条记录的开始时间
                // 这样就填补了删除产生的空白
                //
                // 删除前：
                // prevPiece: 09:00 → 10:00
                // deleted:   10:00 → 11:00  （要删除）
                // nextPiece: 11:00 → 12:00
                //
                // 删除后：
                // prevPiece: 09:00 → 11:00  （结束时间延长到 nextPiece.fromTimePoint）
                // nextPiece: 11:00 → 12:00  （不变）
                
                if (prevPiece != null && nextPiece != null) {
                    // 中间记录删除：前一条延长到后一条的开始
                    val adjustedPrev = prevPiece.copy(timePoint = nextPiece.fromTimePoint)
                    viewModel.updateTimePiece(adjustedPrev)
                } else if (prevPiece != null && nextPiece == null) {
                    // 删除的是最新一条：前一条延长到被删除记录的结束时间
                    val adjustedPrev = prevPiece.copy(timePoint = deleted.timePoint)
                    viewModel.updateTimePiece(adjustedPrev)
                } else if (prevPiece == null && nextPiece != null) {
                    // 删除的是最早一条：后一条的开始时间提前到被删除记录的开始时间
                    val adjustedNext = nextPiece.copy(fromTimePoint = deleted.fromTimePoint)
                    viewModel.updateTimePiece(adjustedNext)
                }
                // 如果 prevPiece == null && nextPiece == null，说明只有一条记录，直接删除即可
                
                viewModel.deleteTimePiece(deleted)
                editingPiece = null
            },
            onInsertBefore = { splitTime, originalPiece ->
                // 在当前记录中间插入一条新记录
                // 
                // 原始：piece 10:00 → 12:00
                // 插入点：11:00
                // 结果：
                //   newPiece: 10:00 → 11:00 （新插入的）
                //   piece:    11:00 → 12:00 （原记录开始时间改为 splitTime）
                
                val newPiece = TimePiece(
                    timePoint = splitTime,
                    fromTimePoint = originalPiece.fromTimePoint,
                    emotion = originalPiece.emotion,
                    lastTimeRecord = "",
                    mainEvent = originalPiece.mainEvent,
                    subEvent = originalPiece.subEvent
                )
                viewModel.insertTimePieceWithSplit(splitTime, originalPiece, newPiece)
                editingPiece = null
            },
            onCancel = { editingPiece = null },
            minTime = minTime,
            maxTime = maxTime
        )
    }
}

@Composable
fun TimePieceCard(
    timePiece: TimePiece,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = convertTimeFormat(timePiece.timePoint, "M/d HH:mm"),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.primary
                )
                Text(
                    text = convertDurationFormat(timePiece.timePoint - timePiece.fromTimePoint, "%d时%d分"),
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Row {
                    repeat(5) { index ->
                        Icon(
                            imageVector = Icons.Filled.Star,
                            contentDescription = null,
                            tint = if (index < timePiece.emotion) Color(0xFFFFD700) else Color.Gray.copy(alpha = 0.2f),
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = timePiece.mainEvent + if (timePiece.subEvent.isNotEmpty()) "：${timePiece.subEvent}" else "",
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
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
